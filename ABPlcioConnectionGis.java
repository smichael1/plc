package atst.giss.abplc;

import atst.base.hardware.connections.ConnectionException;
import atst.cs.util.*;
import static atst.giss.abplc.GisToGissIcdConstants.*;

/**
 * This is the GISS connection class used by GIS controller
 * <b>atst.giss</b> to communicate with the GIS PLC.
 * <p>
 * The controller has its connection object of this class created for it by the
 * CSF, as the controller defines this class as its <code>.connection:class</code>
 *
 * @author Alastair Borrowman (OSL)
 * @author David Morris (NSO)
 *
 */
public class ABPlcioConnectionGis extends ABPlcioConnection implements IABPlcioConnectionGis {

    /*
     *  Private class constants
     */
    /* Log category of the ABPlcioConnectionGis class */
    private static final String LOG_CAT = "ABPLCIO_CONNECTION_GIS";
    /** Array containing all log categories used in this class. */
    private static final String[] LOG_CATS_INUSE = {LOG_CAT};
    
    /*
     *  Private instance variables
     */
    private IPlcTag tagR_icdVersion = null;
    private IPlcTag tagR_CStatus = null;
    private IPlcTag tagW_Cmds = null;
    ABPlcioChannel tagW_CmdsCh = null;

    /**
     * Called by CSF when connection's component receives <i>startup</i> command.
     * This in turn calls super class' {@linkplain ABPlcioConnection#connect(String[])}.
     * <p>
     * Following return from super's <code>doConnect</code> this method carries out GIS
     * connection specific initialization required.
     * 
     * @param addresses The network address of the GIS PLC obtained by the CSF from the
     * component's <code>.connection:address</code> propertyDB attribute.
     * 
     */
    @Override
    public void connect(String[] addresses) throws ConnectionException {
        
        // call the ABPlcioConnection class' connect to do all but the GIS
        // specific connection duties
        super.connect(addresses);
        
        // initialize the GIS PlcTag objects
        tagR_CStatus = super.tagMap.get(TAG_R_GISCSTATUS);
        //tagW_Cmds = super.tagMap.get(TAG_W_GISCMDS);
        // and ABPlcioChannel object
        //tagW_CmdsCh = tagChMap.get(TAG_W_GISCMDS);
        
    } // end doConnect()
    
    @Override
    public void disconnect() throws ConnectionException {
        // call ABPlcioConnection class' doDisconnet() to do rest of duties
        super.disconnect();
        
        // reset all instance variables to initial state
        tagR_CStatus = null;
        tagW_Cmds = null;
        tagW_CmdsCh = null;

    } // end doDisconnect()

    /*
     * Override IABPlcioConnection methods
     */
    
    /**
     * Overrides {@linkplain IABPlcioConnection#getLogCatsUsed()} to add
     * GIS connection specific log categories.
     * <p>
     * Calls super getLogCatsUsed() and appends this class' log categories.
     * 
     * @return A string array containing all log categories used by the
     * connection.
     */
    @Override
    public String[] getLogCatsUsed() {
        String[] superLogCats = super.getLogCatsUsed();
        String[] logCats = new String[superLogCats.length + LOG_CATS_INUSE.length];
        System.arraycopy(superLogCats, 0, logCats, 0, superLogCats.length);
        System.arraycopy(LOG_CATS_INUSE, 0, logCats, superLogCats.length, LOG_CATS_INUSE.length);
        return logCats;
    } // end getLogCatsUsed()

    @Override
    public String getGisPlcTime() {
        StringBuilder gisPlcTime = new StringBuilder();
        
        // time year
        int timeYear = Cache.lookup(tagCStatus.getTagItemPropName(TAG_ITEM_TIME_YEAR)).getInteger();
        gisPlcTime.append(String.format("%04d/", timeYear));
        // time month
        int timeMonth = Cache.lookup(tagCStatus.getTagItemPropName(TAG_ITEM_TIME_MONTH)).getInteger();
        gisPlcTime.append(String.format("%02d/", timeMonth));
        // time day
        int timeDay = Cache.lookup(tagCStatus.getTagItemPropName(TAG_ITEM_TIME_DAY)).getInteger();
        gisPlcTime.append(String.format("%02d ", timeDay));
        // time hour
        int timeHour = Cache.lookup(tagCStatus.getTagItemPropName(TAG_ITEM_TIME_HOUR)).getInteger();
        gisPlcTime.append(String.format("%02d:", timeHour));
        // time minute
        int timeMin = Cache.lookup(tagCStatus.getTagItemPropName(TAG_ITEM_TIME_MIN)).getInteger();
        gisPlcTime.append(String.format("%02d:", timeMin));
        // time second
        int timeSec = Cache.lookup(tagCStatus.getTagItemPropName(TAG_ITEM_TIME_SEC)).getInteger();
        gisPlcTime.append(String.format("%02d.", timeSec));
        // time millisecond
        int timeMs = Cache.lookup(tagCStatus.getTagItemPropName(TAG_ITEM_TIME_MS)).getInteger();
        gisPlcTime.append(String.format("%03d", timeMs));
        
        return gisPlcTime.toString();
    }
    
    /*
     * Private methods
     */
    
    private boolean isPlcCommFault() {
        return Cache.lookup(tagR_CStatus.getTagItemPropName(TAG_ITEM_COMM_FAULT)).getBoolean();
    } // end isPlcCommFault()
    
    private boolean isPlcSystemFault() {
        return Cache.lookup(tagR_CStatus.getTagItemPropName(TAG_ITEM_PLC_SYSTEM_FAULT)).getBoolean();
    } // end isPlcSystemFault()
    
    private int getPlcErrMajor() {
        return Cache.lookup(tagR_CStatus.getTagItemPropName(TAG_ITEM_PLC_ERR_CODE_MAJOR)).getInteger();
    } // end getPlcErrMajor()
    /*
    * Public methods for determining the Interlock Status of specific subsystems based on underlying tags
    */

//    @Override
//    public Boolean getGicIOStatus() {
//        //Log.note("INSIDE getGicIOStatus ABPlcioConnectionGis");
//        return Cache.lookup(tagR_CStatus.getTagItemPropName(TAG_ITEM_GICIOSTAT)).getBoolean();
//    }
    
    @Override
    public Boolean shutdownGicIOStatus() {
        Boolean shutdownGicIOStatus = false;
        
        return shutdownGicIOStatus;
    }
    
    @Override
    public Boolean getGisConnectionStatus() {
        Boolean gicConnectionStatus;
        gicConnectionStatus = Cache.lookup(tagR_CStatus.getTagItemPropName(CONNECTIONSTATUSGIC)).getBoolean() == true;
        return gicConnectionStatus;
    }
        
    @Override
    public Boolean getisSimulatedStatus() {
        Boolean isSimulatedStatus;
        isSimulatedStatus = Cache.lookup(PROPERTY_CONNECTION_SIM).getBoolean() == true;
        return isSimulatedStatus;
    }
    // get*InterlockStatus methods documented in IABPlcioConnectionGis
    @Override
    public Boolean getGICInterlockStatus(){
        Boolean gicInterlockStatus;
        if (Cache.lookup(tagR_CStatus.getTagItemPropName(GICESTOP)).getBoolean() == false){
            //Log.note("CACHE getGICInterlockStatus: " +Cache.lookup(tagR_CStatus.getTagItemPropName(GICESTOP)).getBoolean().toString());
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZREADY)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCALTREADY)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELALTREADY)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZREADY)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ROTREADY)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(GICRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(INSTRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(OSSRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ROTRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FTSRUNMODE)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(GICIOSTAT)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCALTTRAP)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZTRAP)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELALTTRAP)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZTRAP)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ROTTRAP)).getBoolean() == false){
            gicInterlockStatus = false;
        }
        else{
            gicInterlockStatus = true;
        }
       
        return gicInterlockStatus;
    }
    
    @Override
    public Boolean getENCInterlockStatus(){
        Boolean encInterlockStatus;
        if (Cache.lookup(tagR_CStatus.getTagItemPropName(ENCESTOP)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCIOSTAT)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZCWOTENSION)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCALTCWOTENSION)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZMSTOP)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZSPEEDLIM)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZNEGLIM)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZPOSLIM)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCALTMSTOP)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCELENEGLIM)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCELEPOSLIM)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAPCLOSE)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCBCRANE)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCJCRANE)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TEOAPLAT)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCAZALIGNLIFT)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ENCTCRANE)).getBoolean() == false){
            encInterlockStatus = false;
        }
        else {
            encInterlockStatus = true;
        }
        
        return encInterlockStatus;
    }
    
    @Override
    public Boolean getROTInterlockStatus(){
        Boolean rotInterlockStatus;
        if (Cache.lookup(tagR_CStatus.getTagItemPropName(ROTESTOP)).getBoolean() == false){
            rotInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(ROTIOSTAT)).getBoolean() == false){
            rotInterlockStatus = false;
        }
        else if (Cache.lookup(tagR_CStatus.getTagItemPropName(ROTMSTOP)).getBoolean() == false){
            rotInterlockStatus = false;
        }
        else if (Cache.lookup(tagR_CStatus.getTagItemPropName(ROTSAFELIMSPEED)).getBoolean() == false){
            rotInterlockStatus = false;
        }
        else if (Cache.lookup(tagR_CStatus.getTagItemPropName(ROTAZCCWLIM)).getBoolean() == false){
            rotInterlockStatus = false;
        }
        else if (Cache.lookup(tagR_CStatus.getTagItemPropName(ROTAZCWLIM)).getBoolean() == false){
            rotInterlockStatus = false;
        }
        else if (Cache.lookup(tagR_CStatus.getTagItemPropName(ROTOSPEED)).getBoolean() == false){
            rotInterlockStatus = false;
        }
        else {
            rotInterlockStatus = true;
        }
        
        return rotInterlockStatus;
    }
    
    @Override
    public Boolean getOSSInterlockStatus(){
        Boolean ossInterlockStatus;
        if(Cache.lookup(tagR_CStatus.getTagItemPropName(OSSIOSTAT)).getBoolean() == false){
            ossInterlockStatus = false;
        }
        else{
            ossInterlockStatus = true;
        }
        
        return ossInterlockStatus;
    }
    
    @Override
    public Boolean getFACInterlockStatus(){
        Boolean facInterlockStatus;
        if (Cache.lookup(tagR_CStatus.getTagItemPropName(FACESTOPOK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACIOSTAT)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACFIREOK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACSEISMICOK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACIPHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACMLHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACCLHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACULHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACCATHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACSRHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACEFHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACTELHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACUEHZONELOCK)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACBLIFT)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACBLIFTPERM)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACCLCRANE)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACCLCRANEPERM)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACPFLIFT)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACPFLIFTPERM)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(FACPFRLATCH)).getBoolean() == false){
            facInterlockStatus = false;
        }
        else{
            facInterlockStatus = true;
        }
        
        return facInterlockStatus;
    }
    
    @Override
    public Boolean getINSTInterlockStatus(){
        Boolean instInterlockStatus;
        if(Cache.lookup(tagR_CStatus.getTagItemPropName(INSTIOSTAT)).getBoolean() == false){
            instInterlockStatus = false;
        }
        else {
            instInterlockStatus = true;
        }
        
        return instInterlockStatus;
    }
    
    @Override
    public Boolean getTELInterlockStatus(){
        Boolean telInterlockStatus;
        if (Cache.lookup(tagR_CStatus.getTagItemPropName(TELESTOP)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELIOSTAT)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(M1AZMAINTPOS)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(M1ALTMAINTPOS)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TEOAAZMAINTPOS)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TEOAALTMAINTPOS)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZMSTOP)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZSPEED)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZNEGLIM)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZPOSLIM)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELALTMSTOP)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELALTSPEED)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELALTNEGLIM)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELALTPOSLIM)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZCWSTOP)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZCWALIGN)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZCWOTENSION)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELAZOSPEED)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELALTOSPEED)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(OSSBRIDGE)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(OSSBRIDGEPERM)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(M5BRIDGE)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(M5BRIDGEPERM)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(TELDOORNOTCLOSE)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(M1CARTJACKS)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(M1COVER)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else if(Cache.lookup(tagR_CStatus.getTagItemPropName(M1COVERPERM)).getBoolean() == false){
            telInterlockStatus = false;
        }
        else{
            telInterlockStatus = true;
        }
        
        return telInterlockStatus;
    }
    
    @Override
    public Boolean getFTSInterlockStatus(){
        Boolean ftsInterlockStatus;
        if(Cache.lookup(tagR_CStatus.getTagItemPropName(FTSIOSTAT)).getBoolean() == false){
            ftsInterlockStatus = false;
        }
        else {
            ftsInterlockStatus = true;
        }
        
        return ftsInterlockStatus;
    }

} // end class ABPlcioConnectionAux
