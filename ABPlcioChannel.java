package atst.giss.abplc;

import atst.base.hardware.connections.ConnectionException;
import atst.base.hardware.connections.channels.Channel;
import atst.cs.util.Misc;
import atst.cs.data.HealthStatus;
import atst.cs.services.*;

import static atst.giss.OcsToGisIcdConstants.*;
import atst.giss.abplc.IPlcioCall.PlcioMethodName;

/**
 * This is the GISS channel class used by the ABPlcioConnection class to read
 * and write PLC tags to the GIS PLC. Each tag has its own channel. The channel
 * has a <i>name</i> that equates to the tag's name.
 * <p>
 * On creation the channel uses atst.cs.util.Misc.getSharedObject() to obtain
 * a reference to the ABPlcioMaster through the IABPlcioMaster interface. When
 * requested to open, read, write and close a channel to the PLC it is done by
 * calling the PLC access method of the ABPlcioMaster.
 * 
 * @author Alastair Borrowman (OSL)
 * @author David Morris (NSO)
 *
 */
public class ABPlcioChannel extends Channel {
	
    /*
     * Private class constants
     */
    /** Class name of the Master. */
    private static final String MASTER_CLASS_NAME = "atst.giss.abplc.ABPlcioMaster";
    /** Log category of ABPlcioChannel. */
    private static final String LOG_CAT = "ABPLCIO_CHANNEL";

	/**
	 * Get the CSF log categories in use by this class.
	 * 
	 * @return Log catetories used by this class.
	 */
	public static String[] getLogCatsUsed() {
		return new String[] {LOG_CAT};
	}
	
	/*
	 *  Private instance variables
	 */
        /** The reference to the singleton Allen-Bradley PLCIO Master that provides
        * all access to the PLC through JNI calls to the PLCIO C library. */	 
	private IABPlcioMaster master;
	
	/** The PLC hostname is supplied as parameter to open(). While a channel is
	 * opened the plcHostname will contain the PLC hostname of the channel, when
	 * closed it will be null. */
	private String plcHostname;
	
	/** The connection number in use by this channel. A connection number of -1
	 * signifies this channel does not currently have an open connection to a PLC. */
	private int connectionNumber;
	
	/** The channel's tag name is set at construction and is immutable. It is
	 * used to identify this channel's communications with PLCIO. A connection
	 * number cannot be used for this purpose as a connection number is not
	 * allocated until a connection is opened. E.g if a failure occurs in a call
	 * to PLCIO without an open connection the channel's tag name is used to
	 * communicate error information between JNI PLCIO C code and Java. */
	private final String channelTagName;

	private long callTime;
	private long waitTime;
	private long opTime;
	
	/**
	 * Construct a Channel using the given tag name. The
	 * ABPlcioChannel's tag name is immutable.
	 * 
	 * @param tagName The name of the tag to be communicated using
	 * this channel and identify this channel's communication
	 * with the PLC.
	 */
	protected ABPlcioChannel(String tagName) {

		// get reference to ABPlcioMaster
		try {
			master = (IABPlcioMaster) Misc.getSharedObject(MASTER_CLASS_NAME);
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		
		// set this channel's tag name to given name
                Log.note("CURRENT TAGNAME: "+tagName);
		channelTagName = tagName;

		// set plcHostname and connectionNumber to invalid
		plcHostname = null;
		connectionNumber = -1;
		
		callTime = 0;
		waitTime = 0;
	} // end Constructor
	
	/**
	 * Get the call time of last call to {@linkplain #doOpen(String)}, {@linkplain #doClose()},
	 * {@linkplain #read(IPlcTag)}, {@linkplain #write(IPlcTag)} or {@linkplain #plcioCall(IPlcioCall)}
	 * method.
	 * 
	 * @return The value returned by call to {@linkplain System#currentTimeMillis()}
	 * when method is called and begins processing.
	 */
	protected long getCallTime() {
		return callTime;
	}

	/**
	 * Get the time waited to access the synchronized <code>realPlcAccess</code>
	 * method of {@linkplain ABPlcioMaster} by the last call to this class' {@linkplain #doOpen(String)},
	 * {@linkplain #doClose()}, {@linkplain #read(IPlcTag)}, {@linkplain #write(IPlcTag)}
	 * or {@linkplain #plcioCall(IPlcioCall)} method.
	 * 
	 * @return The time difference in ms between {@linkplain #getCallTime()} and
	 * time <code>realPlcAccess</code> method begins processing this channel's
	 * PLCIO call.
	 */
	protected long getWaitTime() {
		return waitTime;
	}
	
	/**
	 * Get the time taken by the synchronized <code>realPlcAccess</code> method
	 * of {@linkplain ABPlcioMaster} to call and return from PLCIO JNI method by
	 * the last call to this class' {@linkplain #doOpen(String)}, {@linkplain #doClose()},
	 * {@linkplain #read(IPlcTag)}, {@linkplain #write(IPlcTag)} or
	 * {@linkplain #plcioCall(IPlcioCall)} method.
	 * 
	 * @return The time difference in ms between {@linkplain #getWaitTime()} and
	 * time <code>realPlcAccess</code> method completing call to PLCIO JNI method.
	 */
	protected long getOpTime() {
		return opTime;
	}
	
	/**
	 * Get the tag name that is communicated using this channel and
	 * used to identify this channel when communicating
	 * with PLCIO JNI. The channel's tag name is set on channel object's
	 * construction and is immutable.
	 * 
	 * @return The tag name of this channel.
	 */
	protected String getTagName() {
		return channelTagName;
	} // end getName()
	
	/**
	 * Get the PLCIO JNI connection number of the PLC connection being
	 * used by this channel. A connection number of -1 signals that this
	 * channel does not have an open PLC connection.
	 * @return
	 * 	The connection number being used by this channel, -1 signals no
	 * connection is opened to the PLC.
	 */
	protected int getPlcioConnectionNumber() {
		return connectionNumber;
	} // end getPlcioConnectionNumber()
	
	/**
	 * Get the current connected status of this channel.
	 * 
	 * @return <b>true</b> if channel is connected to PLC, otherwise <b>false</b>.
	 */
	protected boolean isConnected() {
	    return (connectionNumber >= 0);
	} // end isConnected()
	
	protected void doOpen(String address) throws ConnectionException {
		PlcioCall plcioCall = new PlcioCall(PlcioMethodName.PLC_OPEN, address, channelTagName, connectionNumber);
		callTime = System.currentTimeMillis();
		plcioCall.setCallTime(callTime);

		try {
			master.plcAccess(plcioCall);
		} // end try
		catch (Exception ex) {
			// due to the ABPlcioMaster running in container's namespace and not controller's namespace
			// the catching of specific exceptions, e.g. by using:
			//	catch (ABPlcioExceptionPLCIO ex)
			// does not work. Therefore here we check against exception class name and
			// then explicitly throw the more specific exceptions if applicable
			if (ex.getClass().getName().equals(ABPlcioExceptionPLCIO.class.getName())) {
				String msg = "Call to plcOpen() to open channel '"+channelTagName+
						"' to PLC '" + address + "' failed with PLCIO error message: "+
						ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionPLCIO("ABPlcioChannel: "+msg, ex);
			}
			else if (ex.getClass().getName().equals(ABPlcioExceptionJNI.class.getName())) {
				String msg = "Call to plcOpen() to open channel '"+channelTagName+
						"' to PLC '"+address+"' failed with JNI error message: "+
						ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionJNI("ABPlcioChannel: "+msg, ex);
			}
			else {
				String msg = "Call to plcOpen() to open channel '"+channelTagName+
						"' to PLC '" + address + "' failed with error message: "+
					ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ConnectionException("ABPlcioChannel: "+msg, ex);
			}
		} // end catch
		
		waitTime = plcioCall.getWaitTime();
		opTime = plcioCall.getOpTime();
		connectionNumber = plcioCall.getParamConnectionNumber();
		plcHostname = address;
		
		String msg = "Connection channel opened to PLC '"+plcHostname+"' for tag name '"+
				channelTagName+"' returned connection ch#" + connectionNumber;
		if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isBad()) {
			Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.good(msg));
		}
		Log.debug(LOG_CAT, 2, msg);
		
		return;
	} // end doOpen()

	protected void doClose() throws ConnectionException {
		// only try to close the channel if it is open
		if (!isConnected()) {
			waitTime = 0;
			opTime = 0;
			Log.warn(LOG_CAT, "doClose() channel to PLC '"+plcHostname+"' using tag name '"+
					channelTagName+"' is NOT OPEN - no action taken");
			return;
		}
		
		PlcioCall plcioCall = new PlcioCall(PlcioMethodName.PLC_CLOSE, connectionNumber);
		callTime = System.currentTimeMillis();
		plcioCall.setCallTime(callTime);

		try {
			master.plcAccess(plcioCall);
		} // end try
		catch (Exception ex) {
			// due to the ABPlcioMaster running in container's namespace and not controller's namespace
			// the catching of specific exceptions, e.g. by using:
			//	catch (ABPlcioExceptionPLCIO ex)
			// does not work. Therefore here we check against exception class name and
			// then explicitly throw the more specific exceptions if applicable
			if (ex.getClass().getName().equals(ABPlcioExceptionPLCIO.class.getName())) {
				String msg = "Call to plcClose() to close channel '"+channelTagName+
						"' failed with PLCIO error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionPLCIO("ABPlcioChannel: "+msg, ex);
			}
			else if (ex.getClass().getName().equals(ABPlcioExceptionJNI.class.getName())) {
				String msg = "Call to plcClose() to close channel '"+channelTagName+
						"' failed with JNI error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionJNI("ABPlcioChannel: "+msg, ex);
			}
			else {
				String msg = "Call to plcClose() to close channel '"+channelTagName+
						"' failed with error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ConnectionException("ABPlcioChannel: "+msg, ex);
			}
		} // end catch
		finally {
			// set connectionNumber to invalid
			connectionNumber = -1;
		} // end finally

		waitTime = plcioCall.getWaitTime();
		opTime = plcioCall.getOpTime();
		
		String msg = "Connection channel closed to PLC '"+plcHostname+"' using tag name '"+
				channelTagName+"' connection ch#" + connectionNumber;
		if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isBad()) {
			Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.good(msg));
		}
		Log.debug(LOG_CAT, 2, msg);

        return;
	} // end doClose()
	
	protected void read(IPlcTag tag) throws ConnectionException {
		PlcioCall plcioCall = new PlcioCall(PlcioMethodName.PLC_READ, connectionNumber, tag);
		callTime = System.currentTimeMillis();
		plcioCall.setCallTime(callTime);

		try {
			master.plcAccess(plcioCall);
		} // end try
		catch (Exception ex) {
			// due to the ABPlcioMaster running in container's namespace and not controller's namespace
			// the catching of specific exceptions, e.g. by using:
			//	catch (ABPlcioExceptionPLCIO ex)
			// does not work. Therefore here we check against exception class name and
			// then explicitly throw the more specific exceptions if applicable
			if (ex.getClass().getName().equals(ABPlcioExceptionPLCIO.class.getName())) {
				String msg = "Call to plcRead() to read tag '"+tag.getName()+"' using channel '"
						+channelTagName+"' failed with PLCIO error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionPLCIO("ABPlcioChannel: "+msg, ex);
			}
			else if (ex.getClass().getName().equals(ABPlcioExceptionJNI.class.getName())) {
				String msg = "Call to plcRead() to read tag '"+tag.getName()+"' using channel '"+
						channelTagName+"' failed with JNI error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionJNI("ABPlcioChannel: "+msg, ex);				
			}
			else {
				String msg = "Call to plcRead() to read tag '"+tag.getName()+"' using channel '"
						+channelTagName+"' failed with error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ConnectionException("ABPlcioChannel: "+msg, ex);
			}
		} // end catch
		
		waitTime = plcioCall.getWaitTime();
		opTime = plcioCall.getOpTime();

		String msg = "Connection channel read from PLC '"+plcHostname+"' using tag name '"+
				channelTagName+"' connection ch#"+connectionNumber;
		if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isBad()) {
			Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.good(msg));
		}
		Log.debug(LOG_CAT, 4, msg);

		return;
	} // end read()

	protected void write(IPlcTag tag) throws ConnectionException {
		PlcioCall plcioCall = new PlcioCall(PlcioMethodName.PLC_WRITE, connectionNumber, tag);
		callTime = System.currentTimeMillis();
		plcioCall.setCallTime(callTime);

		try {
			master.plcAccess(plcioCall);
		} // end try
		catch (Exception ex) {
			// due to the ABPlcioMaster running in container's namespace and not controller's namespace
			// the catching of specific exceptions, e.g. by using:
			//	catch (ABPlcioExceptionPLCIO ex)
			// does not work. Therefore here we check against exception class name and
			// then explicitly throw the more specific exceptions if applicable
			if (ex.getClass().getName().equals(ABPlcioExceptionPLCIO.class.getName())) {
				String msg = "Call to plcWrite() to write tag '"+tag.getName()+"' using channel '"+
						channelTagName+"' failed with PLCIO error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionPLCIO("ABPlcioChannel: "+msg, ex);
			}
			else if (ex.getClass().getName().equals(ABPlcioExceptionJNI.class.getName())) {
				String msg = "Call to plcWrite() to write tag '"+tag.getName()+"' using channel '"+
						channelTagName+"' failed with JNI error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionJNI("ABPlcioChannel: "+msg, ex);
			}
			else {
				String msg = "Call to plcWrite() to write tag '"+tag.getName()+"' using channel '"+
						channelTagName+"' failed with error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ConnectionException("ABPlcioChannel: "+msg, ex);
			}
		} // end catch
		
		waitTime = plcioCall.getWaitTime();
		opTime = plcioCall.getOpTime();

		String msg = "Connection channel written to PLC '"+plcHostname+"' using tag name '"+
				channelTagName+"' connection ch#" + connectionNumber;
		if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isBad()) {
			Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.good(msg));
		}		
		Log.debug(LOG_CAT, 4, msg);

		return;
	} // end write()

	protected void plcioCall(IPlcioCall plcioCall) throws ConnectionException {
		callTime = System.currentTimeMillis();
		plcioCall.setCallTime(callTime);
		
		try {
			master.plcAccess(plcioCall);
		} // end try
		catch (Exception ex) {
			// due to the ABPlcioMaster running in container's namespace and not controller's namespace
			// the catching of specific exceptions, e.g. by using:
			//	catch (ABPlcioExceptionPLCIO ex)
			// does not work. Therefore here we check against exception class name and
			// then explicitly throw the more specific exceptions if applicable
			if (ex.getClass().getName().equals(ABPlcioExceptionPLCIO.class.getName())) {
				String msg = "Call described by PlcioCall: "+plcioCall.toString()+"' using channel '"+
						channelTagName+"' failed with PLCIO error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionPLCIO("ABPlcioChannel.plcioCall(): "+msg, ex);
			}
			else if (ex.getClass().getName().equals(ABPlcioExceptionJNI.class.getName())) {
				String msg = "Call described by PlcioCall: "+plcioCall.toString()+"' using channel '"+
						channelTagName+"' failed with JNI error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ABPlcioExceptionJNI("ABPlcioChannel.plcioCall(): "+msg, ex);
			}
			else {
				String msg = "Call described by PlcioCall: "+plcioCall.toString()+"' using channel '"+
						channelTagName+"' failed with error message: "+ex.getMessage();
				if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isGood()) {
					Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.bad(msg));
				}
				throw new ConnectionException("ABPlcioChannel.plcioCall(): "+msg, ex);
			}
		} // end catch
		
		waitTime = plcioCall.getWaitTime();
		opTime = plcioCall.getOpTime();

		String msg = "Connection channel successfully called PLCIO "+plcioCall.getMethodName()+
				" on PLC '"+plcHostname+"' using tag name '"+channelTagName+
				"' connection ch#"+connectionNumber;
		if (Health.get(HEALTH_CAT_GIS_CHANNEL_ERR).isBad()) {
			Health.set(HEALTH_CAT_GIS_CHANNEL_ERR, HealthStatus.good(msg));
		}
		Log.debug(LOG_CAT, 4, msg);

		return;
	} // end plcioCall()
	
	@Override
	public String toString() {
		String connectionStr = null;
		if (connectionNumber >= 0) {
			connectionStr = "connected to '" + plcHostname + "' (ch#" + connectionNumber + ")";
		}
		else {
			connectionStr = "connection is closed";
		}
		return "ABPlcioChannel '" + channelTagName + "' " + connectionStr;
	} // end toString()
	
} // end class ABPlcioChannel
