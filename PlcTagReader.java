package atst.giss.abplc;

import atst.base.hardware.connections.ConnectionException;
import atst.base.hardware.interfaces.IInterruptHandler;
import atst.base.tabs.util.PostingTAB;
import atst.cs.services.Alarm;
import atst.cs.services.Log;
import atst.cs.util.Cache;
import atst.cs.util.Misc;
import atst.giss.abplc.ABPlcioExceptionPLCIO.PlcioErrorCode;
import atst.giss.abplc.IPlcioCall.PlcioMethodName;

/**
 * This class is used to read a specific PLC tag at a specified interval.
 * 
 * @author Alastair Borrowman (OSL)
 */
public class PlcTagReader implements Runnable {
	
	private static final String LOG_CAT_PREFIX = "PLCTAG_READER_";
	private static final int MAX_RUNS_CH_NOT_CONNECTED = 30;
	
	private final ABPlcioChannel readCh;
	private final PlcTag readTag;
	private final String readTagName;
	private final long readIntervalMs;
	private final String logCat;
	private final IInterruptHandler<ABPlcioExceptionPLCIO> plcioErrorIH;
	private final IInterruptHandler<Boolean> readTagIH;
	private Thread readThread;
	String readThreadName;
	long readThreadId;
	private volatile boolean readThreadRunning;
	private volatile boolean tagRead;
	private volatile boolean done;
	
	/**
	 * Construct a PlcTagReader.
	 * 
	 * @param ch The open channel to the PLC to be used to read the PLC tag.
	 * @param tag The PLC tag to be read on each run of the thread.
	 * @param interval The read interval in seconds at which the thread should run.
	 * @param logCatSuffix The suffix to be added to the PlcTagReader's log category prefix to
	 * create the log category string used in all log messages.
	 * @param plcioErrorIHandler The interrupt handler to be called if a PLCIO error occurs
	 * when reading the tag.
	 * @param readTagIHandler The interrupt handler to be called following read of the tag - null
	 * if no interrupt is to be called.
	 */
	protected PlcTagReader(ABPlcioChannel ch, PlcTag tag, double interval, String logCatSuffix,
			IInterruptHandler<ABPlcioExceptionPLCIO> plcioErrorIHandler,
			IInterruptHandler<Boolean> readTagIHandler) {
		readCh = ch;
		readTag = tag;
		readTagName = readTag.getName();
		// convert interval in seconds to milliseconds
		readIntervalMs = (long) (1000 * interval);
		logCat =  LOG_CAT_PREFIX + logCatSuffix;
		plcioErrorIH = plcioErrorIHandler;
		readTagIH = readTagIHandler;
		readThreadRunning = false;
		readThreadName = null;
		readThreadId = 0;
		tagRead = false;
		done = false;
	} // end Constructor
	
	@Override
	public void run() {
		// signal that readThread is now running
		readThreadRunning = true;

		long startTime = 0;
		long delay = 0;
		readThread = Thread.currentThread();
		readThreadName = readThread.getName();
		readThreadId = readThread.getId();

		// setup for getting property that defines whether the periodic reading of
		// the tag is currently enabled
		String propertyEnabled = IPlcTag.PROPERTY_TAG + ":" + readTagName + ":"+ PostingTAB.ENABLED;
		boolean enabled = true;
		boolean enabledAlways = true;
		if (Cache.lookup(propertyEnabled) != null) {
			enabledAlways = false;
			enabled = Cache.lookup(propertyEnabled).getBoolean();
		}
		
		if (Log.getDebugLevel(logCat) >= 2) {
			Log.debug(logCat, 2, "PlcTagReader thread (name=" + readThreadName + ", id=" + readThreadId + ") started, tag '" +
					readTagName + "', channel '" + readCh.getTagName() + "' (ch# " + readCh.getPlcioConnectionNumber() +
					"), enabled = " + enabled + ", interval = " + (readIntervalMs / 1000) + "s (" + readIntervalMs +
					"ms), given InterruptHandler = " + ((readTagIH == null) ? "no" : "YES"));
			Log.debug(logCat, 4, "PlcTagReader thread (name=" + readThreadName + ", id=" + readThreadId + ") tag member values on start:\n" +
					Misc.array2string(readTag.getMemberValues()));
		}
		
		if (!enabled) {
			Log.warn(logCat, "PlcTagReader thread (name=" + readThreadName + ", id=" + readThreadId + ") started for tag '" +
				readTagName + "' but tag's property '." + propertyEnabled + "' is currently set to disable " +
				"- to enable set property to 'true' using a 'set' command.");
		}
		
		// create the PLCIO call object that describes the PLCIO call used to read the tag from
		// the PLC - passed to ABPlcioChannel on each iteration of the while loop to read the tag
		IPlcioCall plcioCall = new PlcioCall(PlcioMethodName.PLC_READ, readCh.getPlcioConnectionNumber(), readTag);
		long split01Time = 0;
		long tagReadTime = 0;
		int runsChNotConnected = 0;
		while (!done) {
			startTime = System.currentTimeMillis();

			// if this tag has an :enabled property check whether value has changed
			if (!enabledAlways) {
				if (Cache.lookup(propertyEnabled).getBoolean() != enabled) {
					enabled = Cache.lookup(propertyEnabled).getBoolean();
					String actionStr;
					if (enabled) {
						actionStr = "enabled";
					}
					else {
						actionStr = "disabled";
					}
					Log.note(logCat, "Reading of tag '" + readTagName + "' by PlcTagReader thread (id=" + readThread.getId() +
							") now " + actionStr +" using channel '" + readCh.getTagName() + "' (ch# " +
							readCh.getPlcioConnectionNumber() +	") interval = " +
							(readIntervalMs / 1000) + "s (" + readIntervalMs + "ms)");
				}
			}
			split01Time = System.currentTimeMillis();
			tagReadTime = 0;
			
			// only read the tag if we are enabled
			if (enabled) {
				if (readCh.isConnected()) {
					runsChNotConnected = 0;
					try {
						readCh.plcioCall(plcioCall);
						tagRead = true;
						tagReadTime = System.currentTimeMillis() - split01Time;
						if (readTagIH != null) {
							// signal interrupt handler we've read the tag
							readTagIH.interrupt(true);
						}
					}
			        catch (ABPlcioExceptionPLCIO ex) {
						tagRead = false;
						if (readTagIH != null) {
							readTagIH.interrupt(false);
						}
						
						// is this a PLCIO timeout?
						if (ex.getPlcioErrorCode() == PlcioErrorCode.TIMEOUT) {
						    Alarm.raise("GISS-ALARM", "PlcTagReader received PLCIO timeout when reading tag "+readTagName+
                                        " using channel "+readCh+" (timeout="+readTag.getPlcioTimeoutMs()+
                                        "ms). Continuing...");
						}
						else {
    			        	// a PLCIO error has occurred - inform connection by calling interrupt handler
    						// and passing it the exception thrown by readCh.plcioCall(plcioCall)
    						if (Log.getDebugLevel(logCat) >= 2) {
    							Log.debug(logCat, 2, "PlcTagReader: PLCIO error when reading tag "+readTagName+
    									" using channel "+readCh+", calling connection's PlcioErrorInterruptHandler "+
    									" with exception returned");
    						}
    						plcioErrorIH.interrupt(ex);
						}
			        }
					catch (ConnectionException ex) {
						tagRead = false;
						if (readTagIH != null) {
							readTagIH.interrupt(false);
						}
						Log.warn(logCat, "PlcTagReader: Unable to read tag "+readTagName+
								" using channel "+readCh+". Exception: "+ex.getMessage());
					}
				}
				else {
					// can't read the tag as its channel is not currently connected
					runsChNotConnected++;
					tagRead = false;
					if (readTagIH != null) {
						// signal interrupt handler we've NOT read the tag
						readTagIH.interrupt(false);
					}
					Log.warn(logCat, "PlcTagReader (" + readThread.getId() + ") unable to read tag "+readTagName+
							" using channel "+readCh+". This is thread run #"+runsChNotConnected+
							" with disconnected channel.");
					if (runsChNotConnected >= MAX_RUNS_CH_NOT_CONNECTED) {
						Log.warn(logCat, "PlcTagReader (" + readThread.getId() + ") max number of runs ("+
								MAX_RUNS_CH_NOT_CONNECTED+") with disconnected channel surpassed - thread is exiting.");
						// signal that readThread is no longer running
						readThreadRunning = false;
						return;
					}
				}
			}
			else {
				tagRead = false;
				if (readTagIH != null) {
					// signal interrupt handler we've NOT read the tag
					readTagIH.interrupt(false);
				}
			}

			// calculate the time to delay based upon time taken to complete PLC read
			// and this thread's interval
			delay = (readIntervalMs - (System.currentTimeMillis() - startTime));
			if (delay < 20) {
				delay = 20;
				if (Log.getDebugLevel(logCat) < 4) {
					Log.debug(logCat, 1, "PlcTagReader thread (" + readThreadName + ", id=" + readThreadId + ") for tag '" +
							readTagName + "' thread execution time = " + (System.currentTimeMillis() - startTime) +
							"ms forcing delay of " + delay + "ms (requested readIntervalMs=" + readIntervalMs + ")" +
							", tagReadTime=" + tagReadTime + " PlcioCall waitTime=" + readCh.getWaitTime() +
							" PlcioCall opTime=" + readCh.getOpTime());
				}
			}
			Misc.pause(delay);

            if (Log.getDebugLevel(logCat) >= 4) {
                Log.debug(logCat, 4, "PlcTagReader thread (" + readThreadName + ", id=" + readThreadId + ") tag '" +
                        readTagName + "' channel '" + readCh.getTagName() + "' (#" + readCh.getPlcioConnectionNumber() +
                        ") tag read="+String.valueOf(tagRead) + " (thread execution time = " +
                        (System.currentTimeMillis() - startTime) + "ms calculated delay = " + delay +
                        "ms (requested readIntervalMs=" + readIntervalMs + ")" + ", tagReadTime=" + tagReadTime +
                        " PlcioCall waitTime=" + readCh.getWaitTime() + " PlcioCall opTime=" + readCh.getOpTime() + ")");
            }
		} // end while
		
		if (Log.getDebugLevel(logCat) >= 2) {
			Log.debug(logCat, 2, "PlcTagReader thread (name=" + readThreadName + ", id=" + readThreadId + ") stopped, tag '" +
					readTagName + "' channel '" + readCh.getTagName() + "' (ch# " + readCh.getPlcioConnectionNumber() + ")");
			Log.debug(logCat, 4, "PlcTagReader thread (name=" + readThreadName + ", id=" + readThreadId + ") tag member values on stop:\n" +
					Misc.array2string(readTag.getMemberValues()));
		}
		
		// signal that readThread is no longer running
		readThreadRunning = false;
	} // end method run()
	
	/**
	 * Return the CSF log category in use by this PlcTagReader.
	 * 
	 * @return The log category in use.
	 */
	public String getLogCatInUse() {
		return logCat;
	} // end getLogCatInUse()
	
	/**
	 * Return whether the tag has been read.
	 * 
	 * @return true if the tag has been read. false if the tag has
	 * not been read.
	 */
	public boolean isTagRead() {
		return tagRead;
	} // end getTagRead()

	public void stop() {
		if (!done) {
			done = true;
			if (readThread != null)
				readThread.interrupt();
			// don't exit this method until thread is no longer running
			// but only wait for limited period
            int waitTimeTotal = 0;
            int waitTime = Math.round(readIntervalMs / 2);
            if (waitTime < 100) waitTime = 100;
			while (readThreadRunning && (waitTimeTotal < (10 * (waitTime * 2)))) {
				Misc.pause(waitTime); 
				waitTimeTotal += waitTime;
			}
			readThread = null;
			
			if (readThreadRunning) {
				Log.warn(logCat, "PlcTagReader thread ("+readThreadName+", id="+
						readThreadId+") has been requested to stop but unable to "+
						"verify thread is no longer running (waited "+waitTimeTotal+
						"ms) , this may be due to previous uncaught exception terminating the thread.");
			}
			else {
				Log.debug(logCat, 2, "PlcTagReader thread ("+readThreadName+", id="+
						readThreadId+") stopped");
			}
		}
		else if (Log.getDebugLevel(logCat) >= 2) {
			Log.debug(logCat, 2, "PlcTagReader thread already stopped for tag '"+readTagName+"'");
		}
	} // end stop()

} // end class PlcTagReader
