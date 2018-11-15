package atst.giss.abplc;

import java.util.*;

import atst.cs.data.*;
import atst.cs.interfaces.*;
import atst.cs.services.*;
import atst.cs.util.*;
import atst.base.hardware.connections.ConnectionException;
import atst.base.hardware.connections.interfaces.IConnection;
import atst.base.hardware.interfaces.IInterruptHandler;

/**
 * This is the GISS abstract connection class from which all GISS controller
 * specific connection classes are derived.
 * <p>
 * This class contains the none controller specific connection behavior
 * required by all GISS controller connection's to communicate with the GIS
 * PLC.
 * 
 * @author Alastair Borrowman (OSL)
 * @author David Morris (NSO)
 *
 */
public abstract class ABPlcioConnection implements IConnection, IABPlcioConnection {
    /*
     * Private class constants
     */
    // Log categories of class ABPlcioConnection
    private static final String LOG_CAT = "ABPLCIO_CONNECTION";
    private static final String LOG_CAT_PLC_READ = LOG_CAT + "_PLC_READ";
    private static final String LOG_CAT_PLC_WRITE = LOG_CAT + "_PLC_WRITE";
    private static final String LOG_CAT_PLCIO = LOG_CAT + "_PLCIO";
    /** Array containing all log categories used in this class. */
    private static final String[] LOG_CATS_INUSE = {LOG_CAT, LOG_CAT_PLC_READ, LOG_CAT_PLC_WRITE, LOG_CAT_PLCIO};
    
    private static final String THREAD_NAME_PLCTAGREADER_PREFIX = "gissPlcTagReader_";
    
    /** The propertyDB attribute name containing connection's current simulation status. */
    private static final String PROPERTY_CONNECTION_SIM = "connection:isSimulated";
    /** The PLCIO address of the Virtual GIS PLC, prefixing with '*' (asterisk)
     * turns on PLCIO debug messages. */
    private static final String VIRTUAL_GIS_ADDRESS = "vgis";
    
    /** The PLCIO communication module to be used - 'cip' is the Common Industrial Protocol
     * module as used for communication with Allen-Bradley ControlLogix PLCs. */
    private static final String PLCIO_COMMS_MODULE = "cip";
    /** Prefixing the PLCIO communication module with '*' (asterisk) signals PLCIO to
     * switch on debug. */
    private static final String PLCIO_DEBUG_ON = "*";
    
    /*
     * Protected class constants
     */
    protected static final String THREAD_NAME_CONNECT_PREFIX = "gissPlcConnect_";
    protected static final String THREAD_NAME_PLCIOERRORRECOVER_PREFIX = "gissPlcioErrorRecover_";
    protected static final String PROPERTY_OPEN_CH_NAMES = "connection:openChNames";

    /*
     * Protected instance variables
     */
    /** String containing the GIS PLC address - value set in doConnect. */
    protected String gisAddress = null;
    
    // Structures used to hold information about tags and associated
    // items used by this connection's controller
    protected Map<String,PlcTag> tagMap = null;
    protected Map<String,ABPlcioChannel> tagChMap = null;
    protected Map<String,PlcTagReader> tagReaderMap = null;
    protected Map<String,ConnectReadTagThread> connectReadTagThreadMap = null;
    
    // Strings containing this connection's controller's tag
    // names of the common EMCS tag types
    protected String tagCStatusName = null;
    protected PlcTag tagCStatus = null;
    protected String tagCmdsName = null;
    protected PlcTag tagCmds = null;
        
    /*
     * Private instance variables
     */
    private boolean isSimulated;
    private boolean isInterlocked;
    private int cmdID;
    
    /*
     * Public methods
     */
    
    /**
     * Returns the current simulation status of the connection.
     * <p>
     * The connection simulation state is set by {@linkplain #connect(String[])}
     * from the Property DB attribute {@linkplain #PROPERTY_CONNECTION_SIM}. When
     * simulation is true the connection is connected to the VGIS simulator and
     * not the real GIS PLC.
     * 
     * @return true if this connection is not connected to real GIS PLC hardware.
     */
    @Override
    public boolean isSimulated() {
        return isSimulated;
    }

    /**
     * Called by CSF when controller receives <i>startup</i> command. Opens first
     * connection to GIS PLC using address passed as parameter if connection is
     * not in simulation.
     * <p>
     * Sequence of operations carried out by this method:
     * <ol>
     * <li> Read from propertyDB this connection's isSimulated status (.connection:isSimulated).
     * Only if isSimulated=false does the connection connect to the real GIS PLC,
     * otherwise the VGIS simulator is used.
     * <li> If log category ABPLCIO_CONNECTION_PLCIO is greater than 0 (zero)
     * signal to PLCIO to log debug messages in file /var/tmp/plcio.log.
     * <li> Obtain list of all PLC tags used by this connection's controller from
     * the propertyDB. Property attribute name declared in {@linkplain IPlcTag#PROPERTY_TAG_LIST}.
     * If no such property exists for controller then a <code>ConnectionException</code>
     * is thrown.
     * <li> A {@linkplain PlcTag#PlcTag(String)} object is created for each tag
     * contained in the tag list. Tag objects are destroyed in {@linkplain #disconnect()}.
     * <li> For each tag create a {@linkplain ABPlcioChannel#ABPlcioChannel(String)} object
     * and add to the <code>IChannel</code> <code>Set</code> returned by this method.
     * </ol>
     * 
     * @param addresses  The network address of the GIS PLC obtained by the CSF from the
     * controller's <code>.connection:address</code> propertyDB attribute.
     * 
     * @throws ConnectionException When any failure occurs from which the connection
     * cannot recover.
     */
    @Override
    public void connect(String[] addresses) throws ConnectionException {
        if (addresses == null)
            throw new ConnectionException("Error bad address: addresses == null");
        if (addresses.length == 0)
            throw new ConnectionException("Error bad address: addresses.length  == 0");
        if (addresses.length > 1)
            throw new ConnectionException("Error bad address: addreses.length > 1");
        String address = addresses[0];
        // create correct address for EMCS PLC
        if (Cache.lookup(PROPERTY_CONNECTION_SIM).getBoolean()) {
            isSimulated = true;
            gisAddress = VIRTUAL_GIS_ADDRESS;
            Log.note(LOG_CAT, "Connection property ."+PROPERTY_CONNECTION_SIM+"="+
                    Cache.lookup(PROPERTY_CONNECTION_SIM).getBoolean()+
                    " - connecting to VEMCS using address '"+gisAddress+"'");
        }
        else {
            isSimulated = false;
            // when not in simulation param 'address' contains the IP address of the
            // GIS PLC - need to add PLCIO communication module and if appropriate
            // switch on debug
            gisAddress = PLCIO_COMMS_MODULE + " " + address;
        }
        // is PLCIO debug to be switched on?
        if (Log.getDebugLevel(LOG_CAT_PLCIO) > 0) {
            gisAddress = PLCIO_DEBUG_ON + gisAddress;
            Log.debug(LOG_CAT_PLCIO, 1, "PLCIO debug switched on, PLCIO debug messages will be in file '/var/tmp/plcio.log'");
        }

        Log.debug(LOG_CAT, 2, "doConnect() called, connection is using address '" + gisAddress + "' to GIS PLC");
        
        // initialize the interlocked state and command ID
        isInterlocked = false;
        cmdID = 0;
        
        // create PlcTag objects for all PLC tags that this connection's
        // controller uses - the controller's properties contain a list of
        // all tags used by this controller
        //Log.note(Cache.lookup(IPlcTag.PROPERTY_TAG_LIST).getStringArray());
        String[] tagList = Cache.lookup(IPlcTag.PROPERTY_TAG_LIST).getStringArray();
        
        if ((tagList != null) && (tagList.length > 0)) {
            Log.debug(LOG_CAT, 2, "Controller's tagList contains the tag names: " + Misc.array2string(tagList));

            tagMap = new HashMap<String,PlcTag>(tagList.length);
            for (String tagName : tagList) {
                // only create tag objects that have a tag name property defined
                // in the property DB - a tag name property can be removed from
                // the property DB to temporarily disable use of the tag
                if (Cache.contains(IPlcTag.PROPERTY_TAG + ":" + tagName)) {
                    try {
                        tagMap.put(tagName, (new PlcTag(tagName)));
                        
                    } catch (ABPlcioExceptionBadPlcTagProperties ex) {
                        throw new ConnectionException("The Plc tag named '" + tagName + "' has bad metadata " +
                                "property in the propertyDB. Exception when creating PlcTag object: " +
                                Misc.getExceptionAsString(ex));
                    }
                }
                else {
                    Log.warn(LOG_CAT, "The use of tag '" + tagName + "' has been disabled as no property '." +
                            IPlcTag.PROPERTY_TAG + ":" + tagName + "' exists");
                }
            }
            Log.note("TagMap: "+tagMap.toString());
        }
        else {
            throw new ConnectionException("Controller's '." + IPlcTag.PROPERTY_TAG_LIST +
                    "' property is either null or contains no PLC tag names - " +
                    "this controller will be unable to communicate with the GIS.");        
        }
        
        // create HashMap to contain any PLC tag reader objects this controller's
        // connection requires
        tagReaderMap = new HashMap<String,PlcTagReader>();
        // create HashMap used to hold any Connection Threads attempting to establish
        // connection to EMCS PLC read tags
        connectReadTagThreadMap = new HashMap<String,ConnectReadTagThread>();
        
        // create connection's tag channels and store in tagChMap, also
        // stored in a Set<IChannel> as this must be returned by this method
        tagChMap = new HashMap<String,ABPlcioChannel>(tagList.length);
        
        // for each PLC tag create a channel object to read/write it to/from the EMCS
        // and set its Cached connectionStatus to false
        for (String tagName : tagMap.keySet()) {
            ABPlcioChannel ch = new ABPlcioChannel(tagName);
            tagChMap.put(tagName, ch);
            
            updateCacheConnectionStatus(tagName, false);
            
            // and initialize the common GIS tags if appropriate
            if (tagName.endsWith(IPlcTag.GIS_TAG_TYPE_CSTATUS)) {
                tagCStatusName = tagName;
                tagCStatus = tagMap.get(tagCStatusName);
            }
            //if (tagName.endsWith(IPlcTag.GIS_TAG_TYPE_CMDS)) {
            //    tagCmdsName = tagName;
            //    tagCmds = tagMap.get(tagName);
            //}
        }
        
    } // end doConnect()

    @Override
    public void disconnect() throws ConnectionException {
        for (ABPlcioChannel chan: tagChMap.values()) 
            doDisconnect(chan);
        
        // remove all (now closed) channels from the channel map
        tagChMap.clear();
        
        // if the channel map is now empty reset all instance variables
        // to initial values
        if (tagChMap.isEmpty()) {
            tagMap = null;
            tagChMap = null;
            tagReaderMap = null;
            connectReadTagThreadMap = null;
            tagCStatusName = null;
            tagCStatus = null;
            tagCmdsName = null;
            tagCmds = null;
            cmdID = 0;
            Log.debug(LOG_CAT, 2, "Final channel closed.");
        }
    }
      
    protected void doDisconnect(ABPlcioChannel tagCh) throws ConnectionException {
        
        Log.debug(LOG_CAT, 2, "doDisconnect() called for channel used by tag '" + tagCh.getTagName() + "'");
        
        // only attempt to close channels that are open
        if (tagCh.isConnected()) {  
            // if this is the CStatus tag and its read thread has been started, stop
            // the thread before closing the channel
            if (tagReaderMap.containsKey(tagCStatusName)) {
                if (tagCh.equals(tagChMap.get(tagCStatusName))) {
                    tagReaderMap.get(tagCStatusName).stop();
                    tagReaderMap.remove(tagCStatusName);
                    Log.debug(LOG_CAT, 3, "Prior to disconnect of channel '" + tagCh.getTagName() +
                            "' (ch#" + tagCh.getPlcioConnectionNumber() +
                            ") stopped read of tag '" + tagCStatusName + "'");
                }
            }
            
            // now close the channel
            tagCh.close();
            if (!tagCh.isConnected()) {
                updateCacheConnectionStatus(tagCh.getTagName(), false);
            }
        }
        else {
            // if channel is not connected ensure it's connection thread
            // is stopped if it is running
            if (connectReadTagThreadMap.containsKey(tagCh.getTagName())) {
                connectReadTagThreadMap.get(tagCh.getTagName()).stop();
                Log.debug(LOG_CAT, 2, "In doDisconnect() no channel for tag '" + tagCh.getTagName() +
                        "' is opened but its running connection thread has been stopped");
            }
        }
        

        
    } // end doDisconnect()
    
    // Documented in IABPlcioConnection
    @Override
    public String[] getLogCatsUsed() {
        String[] logCatsChannel = ABPlcioChannel.getLogCatsUsed();
        String[] logCatsPlcTag = PlcTag.getLogCatsUsed();
        String[] logCatsPlcioPcFormat = PlcioPcFormat.getLogCatsUsed();
        String[] logCats = new String[LOG_CATS_INUSE.length + logCatsChannel.length +
                                      logCatsPlcTag.length + logCatsPlcioPcFormat.length];
        System.arraycopy(LOG_CATS_INUSE, 0, logCats, 0, LOG_CATS_INUSE.length);
        System.arraycopy(logCatsChannel, 0, logCats, LOG_CATS_INUSE.length, logCatsChannel.length);
        System.arraycopy(logCatsPlcTag, 0, logCats, (LOG_CATS_INUSE.length+logCatsChannel.length) , logCatsPlcTag.length);
        System.arraycopy(logCatsPlcioPcFormat, 0, logCats, (LOG_CATS_INUSE.length+logCatsChannel.length+logCatsPlcTag.length), logCatsPlcioPcFormat.length);
        if (tagReaderMap != null) {
            // add all PlcTagReaders' log categories to the list of log categories
            // in use
            String[] tmpLogCats = new String[logCats.length + tagReaderMap.size()];
            System.arraycopy(logCats, 0, tmpLogCats, 0, logCats.length);
            int tmpLogCatIndex = logCats.length;
            for (PlcTagReader tagReader : tagReaderMap.values()) {
                tmpLogCats[tmpLogCatIndex] = tagReader.getLogCatInUse();
                tmpLogCatIndex++;
            }
            logCats = tmpLogCats;
        }
        return logCats;
    } // end getLogCatsUsed()
    
    // Documented in IABPlcioConnection
    @Override
    public boolean isConnected() {
        if (Cache.lookup(PROPERTY_OPEN_CH_NAMES).getStringArray().length > 0) {
            return true;
        }
        return false;
    } // end isConnected()
    
    // Documented in IABPlcioConnection
    @Override
    public void startPlcTagReaders()  {
        // the reading of EMCS tag containing the current hardware status (CStatus)
        // is begun on component startup - open tag's channel and start thread to
        // read tag at defined interval.
        if (tagCStatusName != null) {
            // ensure that a ConnectReadTagThread is not running for this tag
            if (connectReadTagThreadMap.containsKey(tagCStatusName)) {
                Log.warn(LOG_CAT, "startPlcTagReaders() connect thread already running for tag '"+
                        tagCStatusName+"' - not starting another ConnectReadTagThread for "+tagCStatusName);
                return;
            }
            // and ensure that a PlcTagReader is not already started
            if (tagReaderMap.containsKey(tagCStatusName)) {
                Log.warn(LOG_CAT, "startPlcTagReaders() PlcTagReader already started for tag '"+
                        tagCStatusName+" - not starting another PlcTagReader for "+tagCStatusName);
                return;
            }
            
            // the opening of channel to read the tag is done by a thread to enable
            // connection attempts to continue if connection cannot be made immediately.
            // ConnectReadTagThread passes to the PlcTagReader it starts the
            // InterruptHandler to be called when tag's connection is lost
            PlcioErrorInterruptHandler connectionLostIH = new PlcioErrorInterruptHandler(tagCStatusName);
            connectReadTagThreadMap.put(tagCStatusName, (new ConnectReadTagThread(tagCStatusName,
                    IPlcTag.GIS_TAG_TYPE_CSTATUS.toUpperCase(), connectionLostIH, null)));
            
            Misc.startDaemon(connectReadTagThreadMap.get(tagCStatusName), THREAD_NAME_CONNECT_PREFIX+tagCStatusName);
            
        }
        else {
            Log.warn(LOG_CAT, "Controller's properties do not contain tag name " +
                    "conforming to tag type '" + IPlcTag.GIS_TAG_TYPE_CSTATUS +
                    "' therefore this controller is unable to read hardware status.");
        }
    } // end startPlcTagReaders()
    
    // Documented in IABPlcioConnection
    @Override
    public final void stopPlcTagReaders() {
        // don't iterate over the tagReaderMap as its contents
        // are going to be altered in the for loop
        String [] readerTagNames =  tagReaderMap.keySet().toArray(new String[0]);
        
        for (String tagName : readerTagNames) {
            tagReaderMap.get(tagName).stop();
            tagReaderMap.remove(tagName);
            Log.debug(LOG_CAT, 2, "Stopped PlcTagReader thread for tag '" + tagName + "'");
        }
    } // end stopPlcTagReaders()
    
    // Documented in IABPlcioConnection
    @Override
    public final boolean isTagReadCStatus() {
        if (tagReaderMap.containsKey(tagCStatusName)) {
            return tagReaderMap.get(tagCStatusName).isTagRead();
        }
        return false;
    }
    
    // Documented in IABPlcioConnection
    @Override
    public void interlockLowered() {
        isInterlocked = false;
    } // end interlockLowered()
    
    // Documented in IABPlcioConnection
    @Override
    public void interlockRaised() {
        isInterlocked = true;
    } // end interlockLowered()
    
    /**
     * Return the current interlocked state of this connection.
     * <p>
     * The connection is interlocked when the connection's controller
     * is interlocked.
     * 
     * @return The connection's current interlocked state
     */
    protected boolean isInterlocked() {
        return isInterlocked;
    }
    
    // Documented in IABPlcioConnection
    @Override
    public boolean isValidTagName(String tagName) {
        // does tagName contain a tag that this connection's controller uses?
        return tagMap.containsKey(tagName);
    }
    
    // Documented in IABPlcioConnection
    @Override
    public boolean isValidTagNameItemName(String tagName, String itemName) {
        // does tagName contain a tag that this connection's controller uses?
        if (tagMap.containsKey(tagName)) {
            // the tagName is valid, does this tag contain itemName?
            PlcTag tag = tagMap.get(tagName);
            return tag.isValidTagItem(itemName);
        }
        return false;
    }

    // Documented in IABPlcioConnection
    @Override
    public boolean isReadTag(String tagName) {
        PlcTag tag = tagMap.get(tagName);
        return (tag.getDirection() == IPlcTag.DIRECTION_READ);
    }
    
    // Documented in IABPlcioConnection
    @Override
    public boolean isWriteTag(String tagName) {
        PlcTag tag = tagMap.get(tagName);
        return (tag.getDirection() == IPlcTag.DIRECTION_WRITE);
    }
    
    // Documented in IABPlcioConnection
    @Override
    public void readTag(IAttributeTable tbl) throws ConnectionException {
        // read each tag named in table from the EMCS PLC
        for (String attName : tbl.getNames()) {
            String tagName = PlcTag.getTagNameFromAttributeName(attName);
            PlcTag tag = tagMap.get(tagName);
            ABPlcioChannel tagCh = tagChMap.get(tagName);
            
            // check if tag's channel is already open - if not open it
            boolean chClosedAtStart = (!tagCh.isConnected());
            if (chClosedAtStart) {
                tagCh.open(gisAddress);
                if (tagCh.isConnected()) {
                    updateCacheConnectionStatus(tagName, true);
                }
            }
            else {
                Log.warn(LOG_CAT_PLC_READ, "Using already opened channel to read tag " + tagName +
                        ". Channel details: " + tagCh.toString());
            }
            
            // read the tag
            try {
                if (Log.getDebugLevel(LOG_CAT_PLC_READ) >= 4) {
                    Log.debug(LOG_CAT_PLC_READ, 4, "Prior to tag read tag = " + tag.toString());
                }
                
                tagCh.read(tag);
                
                if (Log.getDebugLevel(LOG_CAT_PLC_READ) >= 3) {
                    if (Log.getDebugLevel(LOG_CAT_PLC_READ) == 3) {
                        Log.debug(LOG_CAT_PLC_READ, 3, "Tag '" + tagName + "' read from PLC at " +
                                tag.getValuesLastUpdateString() + " values: " + tag.tagValuesToString());
                    }
                    else {
                        Log.debug(LOG_CAT_PLC_READ, 4, "Following tag read tag = " + tag.toString());
                    }
                }
            }
            finally {
                if (chClosedAtStart) {
                    // the channel was opened specifically for this read
                    // so now we're done make sure it's closed
                    tagCh.close();
                    if (!tagCh.isConnected()) {
                        updateCacheConnectionStatus(tagName, false);
                    }
                }
            }
        } // end for loop
    } // end readTag()

    // Documented in IABPlcioConnection
    @Override
    public void writeTag(IAttributeTable tbl) throws ConnectionException {
        // write each tag data item named in Table to the Cache and
        // then write the tag to the EMCS PLC
        
        // the Table can only contain data items for the same
        // tag, therefore each attribute name in Table containing
        // the tag data items to write must contain the same tag name,
        // we choose to get this tag name from the first attribute
        // name in the Table 
        String attName = tbl.getNames()[0];
        String tagName = PlcTag.getTagNameFromAttributeName(attName);
        PlcTag tag = tagMap.get(tagName);
        
        // when tag is written to PLC all tag data item values will be written, data item values
        // not in this Table will be retrieved from the Cache - to ensure no stale values are
        // written set any tag data items not contained in this Table that have a propertyDB
        // default value to their default value
        IAttributeTable tagCacheValuesTable = tag.getCacheTagItemValues();
        if (tbl.size() != tagCacheValuesTable.size()) {
            // tbl does not contain all of tag's data items
            IAttributeTable defaultsTable = new AttributeTable();
            for (IAttribute att : tagCacheValuesTable) {
                if ((!tbl.contains(att.getName())) && !(Property.getDefault(att.getName()).isEmpty())) {
                    // add this data item's propertyDB default value to Table
                    defaultsTable.insert(Property.getDefault(att.getName()));
                }
            }
            if (Log.getDebugLevel(LOG_CAT_PLC_WRITE) >= 2) {
                Log.debug(LOG_CAT_PLC_WRITE, 2, "Prior to write of tag '" + tagName + "' the table: " +
                        tbl.toString() + " has had following tag data item defaults added: " + defaultsTable.toString());
            }
            tbl.merge(defaultsTable);
        }
        
        // check if tag's channel is already open - if not open it
        ABPlcioChannel tagCh = tagChMap.get(tagName);
        boolean chClosedAtStart = (!tagCh.isConnected());
        if (chClosedAtStart) {
            tagCh.open(gisAddress);
            if (tagCh.isConnected()) {
                updateCacheConnectionStatus(tagName, true);
            }
        }
        else {
            Log.warn(LOG_CAT_PLC_WRITE, "Using already opened channel to write tag " + tagName +
                    ". Channel details: " + tagCh.toString());
        }
        
        // write the tag
        try {
            if (Log.getDebugLevel(LOG_CAT_PLC_WRITE) >= 4) {
                Log.debug(LOG_CAT_PLC_WRITE, 4, "Prior to tag write tag = " + tag.toString());
            }
            
            // store attributes containing the tag's data items to
            // be written to the EMCS in the Cache and then signal tag to
            // update its member values from data items stored in Cache.
            Cache.storeAll(tbl);
            tag.setMemberValues();
            
            // now write the tag to the EMCS PLC
            //tagCh.write(tag);
            
            if (Log.getDebugLevel(LOG_CAT_PLC_WRITE) >= 3) {
                if (Log.getDebugLevel(LOG_CAT_PLC_WRITE) == 3) {
                    Log.debug(LOG_CAT_PLC_WRITE, 3, "Tag '" + tagName + "' written to PLC at " +
                            tag.getValuesLastUpdateString() + " values: " + tag.tagValuesToString());
                }
                else {
                    Log.debug(LOG_CAT_PLC_WRITE, 4, "Following tag write tag = " + tag.toString());
                }
            }
        }
        finally {
            if (chClosedAtStart) {
                // the channel was opened specifically for this write
                // so now we're done make sure it's closed
                tagCh.close();
                if (!tagCh.isConnected()) {
                    updateCacheConnectionStatus(tagName, false);
                }
            }
        }        

    } // end writeTag()
    
    // Documented in IABPlcioConnection
    @Override
    public void openPlcConnection(String name) throws ConnectionException {
        // is param 'name' a valid tagName of this connection's controller?
        if (isValidTagName(name)) {
            // is the channel for this tag already open?
            if (tagChMap.get(name).isConnected()) {
                Log.note(LOG_CAT, "PLC connection channel named '" + name + "' is already open and in use for tag named '" +
                        name + "' - no NEW connection opened.");
            }
            else {
                ABPlcioChannel tagCh = tagChMap.get(name);
                Log.debug(LOG_CAT, 2, "Opening connection to PLC address '" + gisAddress +
                        "' with channel name '" + name +
                        "' - this is a valid tag name and opened connection will be used for its transfer.");
                try {
                    tagCh.open(gisAddress);
                }
                catch (ABPlcioExceptionPLCIO ex) {
                    throw (ex);
                }
                if (tagCh.isConnected()) {
                    updateCacheConnectionStatus(name, true);
                }
            }
        }
        else {
            // is a channel using this name already open?
            boolean chOpen = false;
            String[] openChNames = tagChMap.keySet().toArray(new String[0]);
            
            if (openChNames != null) {
                for (String chName : openChNames) {
                    if (chName.equals(name)) {
                        // ensure it is not already connected
                        if (tagChMap.get(name).isConnected()) {
                            chOpen = true;
                            Log.note(LOG_CAT, "PLC connection channel named '" + name +
                                    "' is already open - no NEW connection opened.");
                        }
                    }
                }
            }
            
            if (!chOpen) {
                // create a new channel for the new connection
                ABPlcioChannel ch = new ABPlcioChannel(name);
                Log.debug(LOG_CAT, 2, "Opening connection to PLC address '" + gisAddress +
                        "' with channel name '" + name + "'.");
                try {
                    ch.open(gisAddress);
                }
                catch (ABPlcioExceptionPLCIO ex) {
                    throw (ex);
                }
                if (ch.isConnected()) {
                    // this is not a tag's channel so add to the tagChMap
                    tagChMap.put(name, ch);
                    updateCacheConnectionStatus(name, true);
                }
            }
        }
        
    } // end openPlcConnection()
    
    // Documented in IABPlcioConnection
    @Override
    public void closePlcConnection(String name) throws ConnectionException {
        // is param 'name' a valid tagName of this connection's controller?
        if (isValidTagName(name)) {
            // is the channel for this tag already closed?
            if (!tagChMap.get(name).isConnected()) {
                Log.note(LOG_CAT, "PLC connection channel named '" + name + "' is already closed for tag named '" +
                        name + "' - NO connection closed.");
            }
            else {
                ABPlcioChannel tagCh = tagChMap.get(name);
                Log.debug(LOG_CAT, 2, "Closing connection to PLC with channel name '" + name +
                        "' - this is a valid tag name and closing connection will mean a new " +
                        " connection will be opened prior to further data transfers.");
                try {
                    tagCh.close();
                }
                catch (ABPlcioExceptionPLCIO ex) {
                    throw (ex);
                }
                if (!tagCh.isConnected()) {
                    updateCacheConnectionStatus(name, false);
                }
            }
        }
        else {
            // is a channel using this name open?
            boolean chOpen = false;
            String[] openChNames = tagChMap.keySet().toArray(new String[0]);

            if (openChNames != null) {
                for (String chName : openChNames) {
                    if (chName.equals(name)) {
                        // ensure it is connected
                        if (tagChMap.get(name).isConnected()) {
                            chOpen = true;
                        }
                    }
                }
            }
            
            if (chOpen) {
                // close the channel
                ABPlcioChannel ch = tagChMap.get(name);
                Log.debug(LOG_CAT, 2, "Closing connection to PLC with channel name '" + name + "'");
                try {
                    ch.close();
                }
                catch (ABPlcioExceptionPLCIO ex) {
                    throw (ex);
                }
                if (!ch.isConnected()) {
                    // this is not a tag's channel so remove from the tagChMap
                    tagChMap.remove(name);
                    updateCacheConnectionStatus(name, false);
                }
            }
            else {
                boolean openChannels = false;
                if (openChNames != null) {
                    if (openChNames.length > 0) {
                        openChannels = true;
                    }
                }
                Log.warn(LOG_CAT, "PLC connection channel named '" + name +
                        "' is NOT open - no connection closed. "+
                        (openChannels ? ("Currently open channel names = "+Misc.array2string(openChNames))
                                : "This connectin currently has no open channels."));                
            }
        }
        
    } // end closePlcConnection()
    
    // Documented in IABPlcioConnection
    public synchronized int getNextCmdID() {
        if (cmdID > 10000) {
            cmdID = 0;
        }
        
        cmdID++;
        
        return cmdID;
    } // end getNextCmdID()
    
    protected void updateCacheConnectionStatus(String name, Boolean newState) {
        IAttribute openChNamesAtt = Cache.lookup(PROPERTY_OPEN_CH_NAMES);
        
        if (Log.getDebugLevel(LOG_CAT) >= 4) {
            String openChNames;
            if (openChNamesAtt == null) {
                openChNames = "Cached attribute and property not currently present";
            }
            else {
                openChNames = Misc.array2string(openChNamesAtt.getStringArray());
            }
            Log.debug(LOG_CAT, 4, "updateCacheConnectionStatus() at start Cache '." + PROPERTY_OPEN_CH_NAMES +
                    "' = " + openChNames);
        }
        
        // is param 'name' a valid tagName of this connection's controller?
        if (isValidTagName(name)) {
            Cache.store(new Attribute(
                    IPlcTag.PROPERTY_TAG + ":" + name + IPlcTag.PROPERTY_CONNECTION_STATUS,
                    newState));
        }
        
        if (newState == true) {
            // add the 'name' to list of connected channels
            String openChNames;
            if ((openChNamesAtt != null) && (!openChNamesAtt.isEmpty())){
                openChNames = Misc.array2string(Cache.lookup(PROPERTY_OPEN_CH_NAMES).getStringArray());
                openChNames += "," + name+" ("+tagChMap.get(name).getPlcioConnectionNumber()+")";
                Cache.store(new Attribute(PROPERTY_OPEN_CH_NAMES, Misc.string2array(openChNames, ",")));
            }
            else {
                openChNames = name+" ("+tagChMap.get(name).getPlcioConnectionNumber()+")";
                Cache.store(new Attribute(PROPERTY_OPEN_CH_NAMES, openChNames));
            }
        }
        else if (openChNamesAtt != null) {
            // remove 'name' from list of connected channels
            String[] openChNamesArray = Cache.lookup(PROPERTY_OPEN_CH_NAMES).getStringArray();
            StringBuilder openChNames = new StringBuilder();
            Boolean foundName = false;
            
            for (String chName : openChNamesArray) {
                if (chName.startsWith(name+" (")) {
                    foundName = true;
                }
                else {
                    openChNames.append(chName + ",");
                }
            }
            
            if (foundName) {
                // remove trailing ',' (comma)
                if (openChNames.length() > 1) {
                    if (openChNames.charAt(openChNames.length()-1) == ',') {
                        openChNames.deleteCharAt(openChNames.length()-1);
                    }
                }
                if (openChNames.length() > 0) {
                    Cache.store(new Attribute(PROPERTY_OPEN_CH_NAMES,
                            Misc.string2array(openChNames.toString(), ",")));
                }
                else {
                    // have removed last open channel name so remove attribute
                    // from Cache so that on next open channel request attribute
                    // is created afresh
                    Cache.remove(PROPERTY_OPEN_CH_NAMES);
                }
            }
        }
        
        if (Log.getDebugLevel(LOG_CAT) >= 2) {
            String openChNames;
            openChNamesAtt = Cache.lookup(PROPERTY_OPEN_CH_NAMES);
            if (openChNamesAtt == null) {
                openChNames = "Cached attribute and property not currently present";
            }
            else {
                openChNames = Misc.array2string(openChNamesAtt.getStringArray());
            }
            Log.debug(LOG_CAT, 2, "Cache '." + PROPERTY_OPEN_CH_NAMES +    "' now = " +
                    ((openChNames.length() > 0) ? openChNames : "<no open channels>"));
        }
    } // end updateCacheConnectionStatus()
    
    /*
     * Protected inner-classes
     */
    
    protected class ConnectReadTagThread implements Runnable {
        private final String tagName;
        private final ABPlcioChannel tagCh;
        private final long connectionAttemptIntervalMs;
        private final String logCatSuffix;
        private final IInterruptHandler<ABPlcioExceptionPLCIO> connectionLostIH;
        private final IInterruptHandler<Boolean> readTagIH;
        private Thread connectThread;
        String connectThreadName;
        long connectThreadId;
        private volatile boolean connectThreadRunning;
        private volatile boolean done;

        /**
         * Construct a ConnectReadTagThread.
         * 
         * @param tName    The GIS PLC tag name that this thread is to connect
         * to the GIS to read.
         * @param logCS    The Log category suffix used to create Log category
         * used by the PlcTagReader.
         * @param connectionLostIHandler The interrupt handler passed to the
         * {@linkplain PlcTagReader} and called if connection being used to
         * read the tag is lost.
         * @param readTagIHandler The interrupt handler passed to the
         * {@linkplain PlcTagReader} and called following every read of the
         * tag from the GIS - may be null if no callback is required on each
         * read of the tag.
         */
        public ConnectReadTagThread(String tName, String logCS,
                IInterruptHandler<ABPlcioExceptionPLCIO> connectionLostIHandler,
                IInterruptHandler<Boolean> readTagIHandler) {
            tagName = tName;
            tagCh = tagChMap.get(tagName);
            // read in interval at which connection attempts are to be made
            // and convert from seconds to milliseconds
            connectionAttemptIntervalMs = (long) (1000 * Cache.lookup(IPlcTag.PROPERTY_TAG + ":" +
                    tagName + IPlcTag.PROPERTY_RECONNECT_INTERVAL).getDouble());
            logCatSuffix = logCS;
            connectionLostIH = connectionLostIHandler;
            readTagIH = readTagIHandler;
            connectThreadRunning = false;
            connectThreadName = null;
            connectThreadId = 0;
            done = false;
        }
        
        @Override
        public void run() {
            // signal that connectThread is now running
            connectThreadRunning = true;

            long originalStartTime = System.currentTimeMillis();
            long startTime = 0;
            long delay = 0;
            connectThread = Thread.currentThread();
            connectThreadName = connectThread.getName();
            connectThreadId = connectThread.getId();
            
            Log.note(LOG_CAT, "ConnectReadTagThread ("+connectThreadName+", id="+
                    connectThreadId+") started for tag '"+tagName+"', using channel '"+
                    tagCh.getTagName()+"', connection attempts will occur every "+
                    connectionAttemptIntervalMs+"ms");
            
            while (!done) {
                startTime = System.currentTimeMillis();
                try {
                    tagCh.open(gisAddress);
                }
                catch (ConnectionException ex) {
                    // query whether exception is of type ABPlcioExceptionPLCIO and whether
                    // exception message is for PLCIO error related to connecting to the PLC
                    if (ex instanceof ABPlcioExceptionPLCIO) {
                        ABPlcioExceptionPLCIO plcioEx = (ABPlcioExceptionPLCIO) ex;
                        if (plcioEx.getPlcioErrorCode() == ABPlcioExceptionPLCIO.PlcioErrorCode.CONNECT) {
                            // this is what will occur if we are unable to connect to the PLC so thread
                            // should continue trying...
                            Log.debug(LOG_CAT, 4, "ConnectReadTagThread ("+connectThreadName+", id="+
                                    connectThreadId+") for tag '"+tagName+"', using channel '"+
                                    tagCh.getTagName()+"' (ch#" + tagCh.getPlcioConnectionNumber()+
                                    ") still unable to connect to PLC address '"+gisAddress+
                                    "' - will continue trying...\n Exception message: "+ex.getMessage());
                        }
                        else {
                            // not a problem with connecting to the EMCS PLC - the
                            // PlcioErrorRecoverThread will alert the user
                            Log.warn(LOG_CAT, "ConnectReadTagThread ("+connectThreadName+", id="+
                                    connectThreadId+") for tag '"+tagName+" received PLCIO error "+
                                    plcioEx.getPlcioErrorCode().getErrorCode()+" thread is terminating");
                            Misc.startDaemon(new PlcioErrorRecoverThread(tagName, plcioEx),
                                    THREAD_NAME_PLCIOERRORRECOVER_PREFIX+tagName);
                            return;
                        }
                    }
                    else {
                        // this is not an exception the ECS can deal with - alert the user by terminating
                        Log.severe(LOG_CAT, "ConnectReadTagThread ("+connectThreadName+", id="+
                                connectThreadId+") for tag '"+tagName+
                                "' received exception it cannot recover from thread is terminating");
                        throw new RuntimeException(ex);
                    }                    
                }
                
                if (tagCh.isConnected()) {
                    // the channel has been successfully opened - we're done!
                    done = true;
                    
                    double channelReconnectTimeS = ((System.currentTimeMillis() - originalStartTime) / 1000.0);
                    
                    updateCacheConnectionStatus(tagName, true);
                    
                    Log.debug(LOG_CAT, 2, "ConnectReadTagThread ("+connectThreadName+", id="+
                            connectThreadId+") for tag '"+tagName+"', successfully opened channel '"
                            +tagCh.getTagName()+"' (ch#" + tagCh.getPlcioConnectionNumber()+
                            "). Time taken = "+channelReconnectTimeS+"s");
                    
                    // now create and start PLC tag reader to read the tag
                    // at required interval
                    double readInterval = Cache.lookup(IPlcTag.PROPERTY_TAG + ":" +
                            tagName + IPlcTag.PROPERTY_INTERVAL).getDouble();
                    tagReaderMap.put(tagName,
                            (new PlcTagReader(tagChMap.get(tagName), tagMap.get(tagName),
                                    readInterval, logCatSuffix, connectionLostIH, readTagIH)));
                    Misc.startDaemon(tagReaderMap.get(tagName), THREAD_NAME_PLCTAGREADER_PREFIX+tagName);
                    
                    Log.debug(LOG_CAT, 2, "ConnectReadTagThread ("+connectThreadName+", id="+
                            connectThreadId+") started read of tag '"+tagName+"' at interval = "+
                            readInterval + "s, using channel '" + tagChMap.get(tagName).getTagName()+
                            "' (ch#"+tagChMap.get(tagName).getPlcioConnectionNumber()+")");
                }
                else {
                    // calculate the time to delay based upon time taken to complete PLC open
                    // attempt and this thread's interval
                    delay = (connectionAttemptIntervalMs - (System.currentTimeMillis() - startTime));
                    Misc.pause(delay);
                }
                
            } // end while
            // remove this thread from the connect thread HashMap to ensure
            // it is no longer referenced
            connectReadTagThreadMap.remove(tagCh.getTagName());
            
            Log.debug(LOG_CAT, 2, "ConnectReadTagThread ("+connectThreadName+", id="+
                    connectThreadId+") for tag '"+tagName+"' completed with channel open status ="+
                    Boolean.toString(tagCh.isConnected()));
 
            // signal that connectThread is no longer running
            connectThreadRunning = false;
        } // end run
        
        public void stop() {
            if (!done) {
                done = true;
                if (connectThread != null)
                    connectThread.interrupt();
                // don't exit this method until thread is no longer running
                // but only wait for limited period
                int waitTimeTotal = 0;
                int waitTime = Math.round(connectionAttemptIntervalMs / 2);
                while (connectThreadRunning && (waitTimeTotal < (3 * connectionAttemptIntervalMs))) {
                    Misc.pause(waitTime); 
                    waitTimeTotal += waitTime;
                }
                connectThread = null;
                
                if (connectThreadRunning) {
                    Log.warn(LOG_CAT, "ConnectReadTagThread ("+connectThreadName+", id="+
                            connectThreadId+") has been requested to stop but unable to "+
                            "verify thread is no longer running, this may be due to "+
                            "previous uncaught exception terminating the thread.");
                }
                else {
                    Log.debug(LOG_CAT, 2, "ConnectReadTagThread ("+connectThreadName+", id="+
                            connectThreadId+") stopped");
                }

            }
            else {
                Log.note(LOG_CAT, "ConnectReadTagThread already stopped for tag '"+tagName+"'");
            }
        } // end method stop()

    } // end inner-class ConnectReadTagThread
    
    /**
     * The {@linkplain IInterruptHandler} class passed to the {@linkplain PlcTagReader}
     * constructor containing interrupt to be called if reader is unable to read its
     * tag due to a PLCIO error.
     * <p>
     * The {@linkplain PlcioErrorInterruptHandler#interrupt(ABPlcioExceptionPLCIO)}
     * method contained in this class (and called by PlcTagReader on PLCIO error,
     * e.g. if connection is lost to GIS PLC) creates and starts an
     * {@linkplain PlcioErrorRecoverThread} to do the actual work required when a PLCIO
     * error occurs. This ensures the PlcTagReader is not blocked while the error is
     * dealt with and can can respond to request to be stopped (as is done in
     * {@linkplain PlcioErrorRecoverThread#run()}).
     * 
     * @author Alastair Borrowman (OSL)
     *
     */
    protected class PlcioErrorInterruptHandler implements IInterruptHandler<ABPlcioExceptionPLCIO> {
        
        private final String tagName;
        
        // Constructor
        public PlcioErrorInterruptHandler(String name) {
            tagName = name;
        } // end Constructor

        @Override
        public void interrupt(ABPlcioExceptionPLCIO plcioEx) {
            // start new thread to actually do work required so interrupt can return immediately
            Misc.startDaemon(new PlcioErrorRecoverThread(tagName, plcioEx), THREAD_NAME_PLCIOERRORRECOVER_PREFIX+tagName);
        } // end interrupt()
    } // end inner-class PlcioErrorInterruptHandler
    
    /**
     * Thread created and started when a call to access the GIS PLC throws a
     * {@linkplain ABPlcioExceptionPLCIO} exception.
     * <p>
     * For example, the {@linkplain PlcioErrorInterruptHandler#interrupt(ABPlcioExceptionPLCIO)} 
     * called by {@linkplain PlcTagReader} when the reader is unable to read its tag
     * due to an error being returned by PLCIO, creates and starts a thread of type to
     * recover from the error.
     * <p>
     * The thread's run() method interrogates the {@linkplain ABPlcioExceptionPLCIO} passed
     * to it at construction and takes the appropriate action based upon the PLCIO error
     * the exception describes. If this is an error caused by a loss of connection to the GIS
     * PLC it does the necessary clean-up required when a tag's connection is lost.
     * If the tag was being read by a {@linkplain PlcTagReader} then this method
     * calls {@linkplain ABPlcioConnection#startPlcTagReaders()}, which in turn calls
     * {@linkplain ConnectReadTagThread} to begin attempts to re-connect to GIS that
     * once successful will also start the re-start the tag's PlcTagReader.
     * 
     * @author Alastair Borrowman (OSL)
     *
     */
    protected class PlcioErrorRecoverThread implements Runnable {

        private final String tagName;
        private final ABPlcioExceptionPLCIO plcioEx;
        
        // Constructor
        public PlcioErrorRecoverThread(String name, ABPlcioExceptionPLCIO ex) {
            tagName = name;
            plcioEx = ex;
            
            if (Log.getDebugLevel(LOG_CAT) >= 3) {
                StackTraceElement[] ste = Thread.currentThread().getStackTrace();
                String[] stackTrace = new String[ste.length];
                for (int i = 0; i < ste.length; i++) {
                    stackTrace[i] = ste[i].toString() + "\n";
                }
                Log.debug(LOG_CAT, 3, "PlcioErrorRecoverThread() constructor tagName '"+tagName+
                        "' stackTrace: "+Misc.array2string(stackTrace));
            }
        } // end Constructor

        @Override
        public void run() {
            ABPlcioExceptionPLCIO.PlcioErrorCode plcioErrCode = plcioEx.getPlcioErrorCode();
            ABPlcioChannel tagCh = tagChMap.get(tagName);
            String threadName = Thread.currentThread().getName();
            Log.warn(LOG_CAT, "Connection notified of PLCIO Error accessing GIS PLC tag '"+
                    tagName+"' using channel: "+tagCh+". PLCIO error code="+plcioEx.getPlcioErrorCodeInt()+
                    ", msg='"+plcioEx.getPlcioErrorString()+"'");
            
            if (tagCh.isConnected()) {
                // update the connection status so all know channel to this tag
                // is no longer functioning
                updateCacheConnectionStatus(tagName, false);
                
                // close the channel - this will generate exception if unable to connect
                // to EMCS PLC but is required to do necessary clean-up in ECS
                try {
                    tagCh.close();
                } catch (ConnectionException ex) {
                    // query whether exception is of type ABPlcioExceptionPLCIO and whether
                    // exception message is for PLCIO error related to connecting to the PLC
                    if (ex instanceof ABPlcioExceptionPLCIO) {
                        ABPlcioExceptionPLCIO plcioEx = (ABPlcioExceptionPLCIO) ex;
                        if (plcioEx.getPlcioErrorCode() == ABPlcioExceptionPLCIO.PlcioErrorCode.CONNECT) {
                            // this is what will occur if we are unable to connect to the PLC
                            if (Log.getDebugLevel(LOG_CAT) >= 2 ) {
                                Log.debug(LOG_CAT, 2, "PlcioErrorRecoverThread ("+threadName+") exception received when closing channel "+
                                        tagCh+ " with no response from GIS: "+Misc.getExceptionAsString(ex));
                            }
                        }
                    }
                    else {
                        // this is not an exception the ECS can deal with - alert the user by terminating
                        Log.severe(LOG_CAT, "PlcioErrorRecoverThread ("+threadName+") for tag '"+
                                tagName+"' received exception it cannot recover from thread is terminating."+
                                " Exception: "+Misc.getExceptionAsString(ex));
                        throw new RuntimeException(ex);
                    }
                }
            }
            
            // if required stop the PlcTagReader that was reading this tag
            boolean restartTagReader = false;
            if (tagReaderMap.containsKey(tagName)) {
                tagReaderMap.get(tagName).stop();
                tagReaderMap.remove(tagName);
                restartTagReader = true;
                Log.debug(LOG_CAT, 2, "PlcioErrorRecoverThread ("+threadName+") stopped PlcTagReader for tag '"+tagName+
                        "' - will be restarted once connection is reopened to GIS PLC");
            }
            
            switch(plcioErrCode) {
            case CONNECT: // intentional full-through
            case TIMEOUT:
                // if required restart the PlcTagReader using same method as connection's controller
                // users in its doStartup(), this uses a ConnectReadTagThread to attempt to re-connect
                // to the EMCS PLC to read the tag, once successfully connected the thread starts the
                // PlcTagReader
                if (restartTagReader) startPlcTagReaders();
                break;
            case BAD_TAG_NAME: // intentional full-through
            case NOT_HANDLED:
                Log.note(LOG_CAT, "The GISS has disconnected the channel used by tag '"+tagName+
                        " following PLCIO error "+plcioEx.getPlcioErrorCodeInt()+
                        " ("+plcioErrCode.getErrorString()+") no more action is being taken.");
                break;
            } // end switch
            
        } // end run()
    } // end inner-class PlcioErrorRecoverThread
    
} // end class ABPlcioConnection
