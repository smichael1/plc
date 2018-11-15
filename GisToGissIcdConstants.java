package atst.giss.abplc;

/**
 * The constants for items needed between the GIS and GISS.
 * <p>
 * This class shall have no instances and its constants are imported for use
 * by other GISS classes using:<br>
 * <code>import static atst.giss.abplc.GistoGissIcsConstants</code>
 *
 * @author David Morris (NSO)
 * 
 */
public class GisToGissIcdConstants {
   
    /**
     * Name of read tag containing GIS controller's current status data.
     */
    public static final String TAG_R_GISCSTATUS = "GIC_to_OCS";
    //public static final String TAG_W_GISCMDS = "W_GIC_to_OCSCmds";
    /**
     * List of interlock names from OCS to GIS ICD for use in the abplc.
     */
    //Global Interlock Signals
    public static final String GICINTERLOCK = "GIC_Interlock_Status";
    public static final String GICESTOP = "EStop_OK";
    public static final String ENCAZREADY = "EncAz_Ready";
    public static final String ENCALTREADY = "EncAlt_Ready";
    public static final String TELALTREADY = "TelAlt_Ready";
    public static final String TELAZREADY = "TelAz_Ready";
    public static final String ROTREADY = "Rot_Ready";
    public static final String GICRUNMODE = "GIC_Run";
    public static final String ENCRUNMODE = "EncLIC_Run";
    public static final String FACRUNMODE = "FacLIC_Run";
    public static final String INSTRUNMODE = "InstLIC_Run";
    public static final String OSSRUNMODE = "OSSLIC_Run";
    public static final String ROTRUNMODE = "Rot_Run";
    public static final String TELRUNMODE = "TelLIC_Run";
    public static final String FTSRUNMODE = "FTSLIC_Run";
    public static final String GICIOSTAT = "GicIO_Status";
    public static final String ENCIOSTAT = "EncIO_Status";
    public static final String FACIOSTAT = "FacIO_Status";
    public static final String INSTIOSTAT = "InstIO_Status";
    public static final String OSSIOSTAT = "OSSIO_Status";
    public static final String ROTIOSTAT = "RotIO_Status";
    public static final String TELIOSTAT = "TelIO_Status";
    public static final String FTSIOSTAT = "FTSIO_Status";
    public static final String ENCALTTRAP = "EncAlt_TrappedKey";
    public static final String ENCAZTRAP = "EncAz_TrappedKey";
    public static final String TELALTTRAP = "TelAlt_TrappedKey";
    public static final String TELAZTRAP = "TelAz_TrappedKey";
    public static final String ROTTRAP = "Rot_TrappedKey";
    //Enclosure Interlock Signals
    public static final String ENCINTERLOCK = "ENC_Interlock_Status";
    public static final String ENCESTOP = "EncEStop_OK";
    public static final String ENCAZCWOTENSION = "EncAzCableOverTension_OK";
    public static final String ENCALTCWOTENSION = "EncAltCableOverTension_OK";
    public static final String ENCAZMSTOP = "EncAz_MotionStopped";
    public static final String ENCAZSPEEDLIM = "EncAz_SafeSpeedLimit";
    public static final String ENCAZNEGLIM = "EncAz_NegFinalLimit";
    public static final String ENCAZPOSLIM = "EncAz_PosFinalLimit";
    public static final String ENCALTMSTOP = "EncAlt_MotionStopped";
    public static final String ENCELENEGLIM = "EncElevation_NegFinalLimit";
    public static final String ENCELEPOSLIM = "EncElevation_PosFinalLimit";
    public static final String ENCAPCLOSE = "EncAperture_Closed";
    public static final String ENCBCRANE = "EncBridgeCrane_Stowed";
    public static final String ENCJCRANE = "EncJibCrane_Stowed";
    public static final String TEOAPLAT = "EncTEOAPlatform_Stowed";
    public static final String ENCAZALIGNLIFT = "EncAz_AlignLift";
    public static final String ENCTCRANE = "EncTransferBridge_Stowed";
    //FAC Interlock Signals
    public static final String FACINTERLOCK = "FAC_Interlock_Status";
    public static final String FACESTOPOK = "FacEStop_OK";
    public static final String FACFIREOK = "FacFireAlarm_OK";
    public static final String FACSEISMICOK = "FacSeismicAlarm_OK";
    public static final String FACIPHZONELOCK = "FacInnerPierHazardZone_Locked";
    public static final String FACMLHZONELOCK = "FacMezzLevelHazardZone_Locked";
    public static final String FACCLHZONELOCK = "FacCoudeLabHazardZone_Locked";
    public static final String FACULHZONELOCK = "FacUtilityLevelHazardZone_Locked";
    public static final String FACCATHZONELOCK = "FacCatwalkHazardZone_Locked";
    public static final String FACSRHZONELOCK = "FacServiceRingHazardZone_Locked";
    public static final String FACEFHZONELOCK = "FacEnclosureFloorHazardZone_Locked";
    public static final String FACTELHZONELOCK = "FacTelescopeHazardZone_Locked";
    public static final String FACUEHZONELOCK = "FacUpperEncHazardZone_Locked";
    public static final String FACBLIFT = "FacBoomLift_NotStowed";
    public static final String FACBLIFTPERM = "FacBoomLift_Permissive";
    public static final String FACCLCRANE = "FacCoudeLabCrane_Stowed";
    public static final String FACCLCRANEPERM = "FacCoudeLabCrane_Permissive";
    public static final String FACPFLIFT = "FacPFlowLift_Stowed";
    public static final String FACPFLIFTPERM = "FacPFlowLift_Permissive";
    public static final String FACPFRLATCH = "FacPFlowRoofLatch_Closed";
    //INST Interlock Signals
    public static final String INSTINTERLOCK = "INST_Interlock_Status";
    //OSS Interlock Signals
    public static final String OSSINTERLOCK = "OSS_Interlock_Status";
    //Coude Rotator Interlock Signals
    public static final String ROTINTERLOCK = "ROT_Interlock_Status";
    public static final String ROTESTOP = "RotEStop_OK";
    public static final String ROTMSTOP = "Rot_MotionStopped";
    public static final String ROTSAFELIMSPEED = "RotSafeLim_Speed";
    public static final String ROTAZCCWLIM = "Rot_CCWLimit";
    public static final String ROTAZCWLIM ="Rot_CWLimit";
    public static final String ROTOSPEED = "RotOver_Speed";
    //Telescope Interlock Signals
    public static final String TELINTERLOCK = "TEL_Interlock_Status";
    public static final String TELESTOP = "TelEStop_OK";
    public static final String M1AZMAINTPOS = "M1Az_MaintenancePos";
    public static final String M1ALTMAINTPOS = "M1Alt_MaintenancePos";
    public static final String TEOAAZMAINTPOS = "TEOAAz_MaintenancePos";
    public static final String TEOAALTMAINTPOS = "TEOAAlt_MaintenancePos";
    public static final String TELAZMSTOP = "TelAz_MotionStopped";
    public static final String TELAZSPEED = "TelAz_SafeSpeed";
    public static final String TELAZNEGLIM = "TelAz_NegFinalLimit";
    public static final String TELAZPOSLIM = "TelAz_PosFinalLimit";
    public static final String TELALTMSTOP = "TelAlt_MotionStopped";
    public static final String TELALTSPEED = "TelAlt_SafeSpeed";
    public static final String TELALTNEGLIM = "TelAlt_NegFinalLimit";
    public static final String TELALTPOSLIM = "TelAlt_PosFinalLimit";
    public static final String TELAZCWSTOP = "TelAz_CableWrapStopped";
    public static final String TELAZCWALIGN = "TelAz_CableWrapMisaligned";
    public static final String TELAZCWOTENSION = "TelAz_CableWrapOverTension";
    public static final String TELAZOSPEED = "TelAz_OverSpeed";
    public static final String TELALTOSPEED = "TelAlt_OverSpeed";
    public static final String OSSBRIDGE = "OSSBridge_Stowed";
    public static final String OSSBRIDGEPERM = "OSSBridge_Permissive";
    public static final String M5BRIDGE = "M5Bridge_Stowed";
    public static final String M5BRIDGEPERM = "M5Bridge_Permissive";
    public static final String TELDOORNOTCLOSE = "TelAccessDoors_NotClosed";
    public static final String M1CARTJACKS = "M1CartJacks_Stowed";
    public static final String M1COVER = "M1Cover_Closed";
    public static final String M1COVERPERM = "M1Cover_Permissive";
    //FTS Interlock Signals
    public static final String FTSINTERLOCK = "FTS_Interlock_Status";
    public static final String CONNECTIONSTATUSGIC = "GicIO_Status";
    public static final String PROPERTY_CONNECTION_SIM = "connection:isSimulated";
    /*
     * Name of the <i>cmd</i> data item of tag W_*Cmds tags.
     */
    public static final String TAG_ITEM_CMD = "cmd";

    /*
     * Name of tag item containing identifier relating to command, used
     * in W_*Cmds tags and R_*CStatus tags.
     */
    public static final String TAG_ITEM_ID = "ID";
    /*
     * The R_gisCStatus tag item containing the current local control status of the GIS PLC.
     */
    public static final String TAG_ITEM_LOCAL_CONTROL = "localControl";
    /*
     * The R_gisCStatus tag item containing the communication fault status of the GIS PLC.
     */
    public static final String TAG_ITEM_COMM_FAULT = "commFault";
    /*
     * The R_gisCStatus tag item containing the status value of the GIS PLC.
     */
    public static final String TAG_ITEM_PLC_STATUS = "plcStatus";
    /*
     * The R_gisCStatus tag item containing the PLC system fault status of the GIS PLC.
     */
    public static final String TAG_ITEM_PLC_SYSTEM_FAULT = "plcSystemFault";
    /*
     * The R_gisCStatus tag item containing the current GIS PLC major error code.
     */
    public static final String TAG_ITEM_PLC_ERR_CODE_MAJOR = "plcErrCodeMajor";
    /*
     * The W_gisCmds tag item used to signal to GIS PLC that it should update
     * its clock time using supplied tag time items.
     */
    public static final String TAG_ITEM_DO_CLOCKUPDATE = "doClockUpdate";
    /**
     * The R_gisCStatus tag item relating to time year."
     */
    public static final String TAG_ITEM_TIME_YEAR = "timeYear";
    /**
     * The R_gisCStatus tag item relating to time month."
     */
    public static final String TAG_ITEM_TIME_MONTH = "timeMonth";
    /**
     * The R_gisCStatus tag item relating to time day."
     */
    public static final String TAG_ITEM_TIME_DAY = "timeDay";
    /**
     * The R_gisCStatus tag item relating to time hour."
     */
    public static final String TAG_ITEM_TIME_HOUR = "timeHour";
    /**
     * The R_gisCStatus tag item relating to time minute."
     */
    public static final String TAG_ITEM_TIME_MIN = "timeMin";
    /**
     * The R_gisCStatus tag item relating to time second."
     */
    public static final String TAG_ITEM_TIME_SEC = "timeSec";
    /**
     * The R_gisCStatus tag item relating to time millisecond."
     */
    public static final String TAG_ITEM_TIME_MS = "timeMs";
    
}
