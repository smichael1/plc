package atst.giss.abplc;

/**
 * Interface of the Allen-Bradley PLC Connection used by GIS Equipment
 * controller atst.giss.
 *  
 * @author David Morris (NSO)
 * @author Alastair Borrowman (OSL)
 *
 */
public interface IABPlcioConnectionGis extends IABPlcioConnection {
        
        // Helper gets for GISS
        public String getGisPlcTime();
        //public Boolean getGicIOStatus();
        public Boolean shutdownGicIOStatus();
        public Boolean getisSimulatedStatus();
        public Boolean getGisConnectionStatus();
        //status of monitored subsytem, if any status within a subsystem is interlocked
        // the interlock status will be set to false (BAD), set to true (GOOD) when not interlocked
        // 1Hz within GissHardwareController.getEventTable()
        /**
         * Returns the Interlock Status of Global Interlock Controller
         * <p>
         * Read posted atst.giss.ghc.cStatus GIC_Interlock_Status. If a tag status is 
         * BAD GIC_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of GIC tags.
         */ 
        public Boolean getGICInterlockStatus();
        /**
         * Returns the Interlock Status of Enclosure
         * <p>
         * Read posted atst.giss.ghc.cStatus ENC_Interlock_Status. If a tag status is 
         * BAD ENC_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of ENC tags.
         */ 
        public Boolean getENCInterlockStatus();
        /**
         * Returns the Interlock Status of Coude Rotator
         * <p>
         * Read posted atst.giss.ghc.cStatus ROT_Interlock_Status. If a tag status is 
         * BAD ROT_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of ROT tags.
         */ 
        public Boolean getROTInterlockStatus();
        /**
         * Returns the Interlock Status of OSS
         * <p>
         * Read posted atst.giss.ghc.cStatus OSS_Interlock_Status. If a tag status is 
         * BAD OSS_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of OSS tags.
         */ 
        public Boolean getOSSInterlockStatus();
        /**
         * Returns the Interlock Status of Facility
         * <p>
         * Read posted atst.giss.ghc.cStatus FAC_Interlock_Status. If a tag status is 
         * BAD FAC_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of FAC tags.
         */ 
        public Boolean getFACInterlockStatus();
        /**
         * Returns the Interlock Status of Instrument
         * <p>
         * Read posted atst.giss.ghc.cStatus INST_Interlock_Status. If a tag status is 
         * BAD INST_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of INST tags.
         */ 
        public Boolean getINSTInterlockStatus();
        /**
         * Returns the Interlock Status of Telescope
         * <p>
         * Read posted atst.giss.ghc.cStatus TEL_Interlock_Status. If a tag status is 
         * BAD TEL_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of TEL tags.
         */ 
        public Boolean getTELInterlockStatus();
        /**
         * Returns the Interlock Status of Facility Thermal System
         * <p>
         * Read posted atst.giss.ghc.cStatus FTS_Interlock_Status. If a tag status is 
         * BAD FTS_Interlock_Status will be set to false, if GOOD set to true
         * 
         * @return Boolean status of FTS tags.
         */ 
        public Boolean getFTSInterlockStatus();
        //public Boolean getGicIOStatus();

} // end interface IABPlcioConnectionGis
