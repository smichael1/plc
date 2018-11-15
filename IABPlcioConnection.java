package atst.giss.abplc;

import atst.cs.interfaces.IAttributeTable;
import atst.base.hardware.connections.ConnectionException;

/**
 * The common interface of all Allen-Bradley PLC Connections used by the GISS to
 * communicate with the GIS PLC.
 * 
 * @author Alastair Borrowman (OSL)
 * @author David Morris(NSO)
 *
 */
public interface IABPlcioConnection {
	
	/**
	 * Get the list of CSF log categories used by the connection.
	 * 
	 * @return A string array containing all log categories used by the connection.
	 */
	public String[] getLogCatsUsed();
	
	/**
	 * Returns true if-and-only-if one or more connections to the GIS PLC are
	 * currently open.
	 * <p>
	 * During the controller's doStartup() the controller calls {@linkplain #startPlcTagReaders}
	 * to signal connection to open channel to GIS PLC to read its CStatus tag.
	 * In controller's doShutdown() the controller calls {@linkplain #stopPlcTagReaders}
	 * which will close the channel used to read the CStatus tag. Therefore while
	 * controller is running this method should always return true unless there
	 * has been a problem connecting to the GIS PLC.
	 * 
	 * @return true if one or more connections to the GIS PLC are open, otherwise
	 * false.
	 */
	public boolean isConnected();
	
	/**
	 * Signal the connection to start its {@linkplain PlcTagReader}(s).
	 * <p>
	 * Provided to be called when connection's controller receives doStartup().
	 */
	public void startPlcTagReaders();
	
	/**
	 * Signal the connection to stop its {@linkplain PlcTagReader}s.
	 * <p>
	 * Provided to be called when connection's controller receives doShutdown(); to
	 * ensure threads started to periodically read PLC tags stop communicating with
	 * hardware when shutdown is demanded.
	 */
	public void stopPlcTagReaders();
	
	/**
	 * Has the CStatus tag been read from the GIS PLC?
	 * 
	 * @return true if the CStatus has been read, false otherwise.
	 */
	public boolean isTagReadCStatus();
	
	/**
	 * Notify the connection that the connection's controller's interlock
	 * has been raised.
	 */
	public void interlockLowered();
	
	/**
	 * Notify the connection that the connection's controller's interlock
	 * has been raised.
	 */
	public void interlockRaised();
	
	/**
	 * Checks a tag name is a valid tag for this connection's controller.
	 * 
	 * @param tagName	The tag name to check.
	 * 
	 * @return <b>true</b> if the tag name is valid for this connection's controller,
	 * other <b>false</b>.
	 */
	public boolean isValidTagName(String tagName);

	/**
	 * Checks a tag name is a valid tag for this connection's controller
	 * and that the item name is a valid item name contained in the tag.
	 * 
	 * @param tagName	The tag name to check.
	 * @param itemName	The item name to check.
	 * 
	 * @return <b>true</b> if the tag name is valid for this connection's controller
	 * and that the item name is a valid item name for the tag.<br>
	 * <b>false</b> if either tag name is not valid or if the item name is not
	 * contained in the tag.
	 */
	public boolean isValidTagNameItemName(String tagName, String itemName);
	
	/**
	 * Test whether the named tag is an GIS PLC read tag.
	 * 
	 * @param tagName	The name of tag to check.
	 * 
	 * @return <b>true</b> if named tag is a read tag, otherwise <b>false</b>.
	 */
	public boolean isReadTag(String tagName);
	
	/**
	 * Test whether the named tag is an GIS PLC write tag.
	 * 
	 * @param tagName	The name of tag to check.
	 * 
	 * @return <b>true</b> if named tag is a write tag, otherwise <b>false</b>.
	 */
	public boolean isWriteTag(String tagName);

	/**
	 * Read GIS PLC tag[s] that are named in attributes contained in the table.
	 * 
	 * @param table Contains attribute[s] naming the tag[s] to read.
	 * @throws ConnectionException if there is a problem communicating with the hardware
	 */
	public void readTag(IAttributeTable table) throws ConnectionException;
	
	/**
	 * Write GIS PLC tag item[s] that are named in attributes contained in the 
	 * Table.
	 * 
	 * @param table Contains attribute[s] naming the tag item[s] to write.
	 * @throws ConnectionException if there is a problem communicating with the hardware
	 */
	public void writeTag(IAttributeTable table) throws ConnectionException;
	
	/**
	 * Explicitly open a connection to the GIS PLC - used for <b>testing</b> and <b>maintenance</b>/<b>engineering</b>
	 * only.
	 * <p>
	 * This method is not the method used during normal operations for opening connections
	 * to the GIS PLC, it is only used by controllers when a configuration is submitted
	 * containing the attribute <code>.plcConnection:open</code>.
	 * 
	 * The param <code>name</code> will be used as the opened connection's channel name.
	 * {@linkplain ABPlcioConnection} names the channels it opens using the PLC tag name
	 * that will be transferred using the connection, therefore to open a connection that
	 * will subsequently be used to read or write a tag call this method with <code>name</code>
	 * equal to a valid PLC tag name of the connection's controller.
	 * 
	 * @param name The name the connection will be identified by.
	 * @throws ConnectionException if there is a problem communicating with the hardware
	 */
	public void openPlcConnection(String name) throws ConnectionException;
	
	/**
	 * Explicitly close a connection to the GIS PLC - used for <b>testing</b> and <b>maintenance</b>/<b>engineering</b>
	 * only.
	 * <p>
	 * This method is not the method used during normal operations for closing connections
	 * to the GIS PLC, it is only used by controllers when a configuration is submitted
	 * containing the attribute <code>.plcConnection:close</code>.
	 * 
	 * @param name	The name of the connection to be closed.
	 * @throws ConnectionException if there is a problem communicating with the hardware
	 */
	public void closePlcConnection(String name) throws ConnectionException;
	
	/**
	 * Get the next <i>Command ID</i>.
	 * <p>
	 * GIS PLC write tags contain the item <i>ID</i>. The current command
	 * being acted upon by the GIS is returned to the GISS in the corresponding
	 * status read tag. The GISS produces the command ID in a sequence
	 * starting at <code>1</code> (one) and progressing by increments of
	 * <code>1</code> until the value <code>10000</code> is reached, at
	 * which point the ID is reset to value <code>0</code> and the sequence
	 * restarted.
	 * 
	 * @return The next Command ID to be used.
	 */
	public int getNextCmdID();

}
