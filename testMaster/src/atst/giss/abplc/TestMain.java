package atst.giss.abplc;

public class TestMain {

    public static void main(String[] args) {
        System.out.println("testing");
        try {

            // create the class ABPlcioMaster
            IABPlcioMaster master = new ABPlcioMaster();

            // setup for a simple command (open)

            PlcioCall plcioCallOpen = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_OPEN, "cip 192.168.1.20",
                    "Scott_R", 0);


            String[] itemNames = {"myRealValue"};
            String[] itemTypes = {IPlcTag.PropTypes.REAL.getTypeString()};

            PlcTag plcTag = new PlcTag("Scott_R", IPlcTag.DIRECTION_READ, ""+ PlcioPcFormat.TYPE_R,
                    10000, 1, 4, itemNames, itemTypes);

            PlcioCall plcioCallRead = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_READ, "cip 192.168.1.20",
                    "Scott_R", 0, plcTag);


            PlcioCall plcioCallClose = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_CLOSE, "cip 192.168.1.20",
                    "Scott_R", 0);

            master.plcAccess(plcioCallOpen);
            master.plcAccess(plcioCallRead);
            master.plcAccess(plcioCallClose);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("testing complete");


    }
}
