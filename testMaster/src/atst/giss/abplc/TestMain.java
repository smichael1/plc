package atst.giss.abplc;

public class TestMain {

    public static void main(String[] args) {
        System.out.println("testing");
        try {

            // create the class ABPlcioMaster
            IABPlcioMaster master = new ABPlcioMaster();

            // setup for a simple command (open)

            PlcioCall plcioCallOpen1 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_OPEN, "cip 192.168.1.20",
                    "Scott_R_Conn", 0);
            PlcioCall plcioCallOpen2 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_OPEN, "cip 192.168.1.20",
                    "Scott_D_Conn", 0);


            TagItem tagItem1 = new TagItem("myRealValue", IPlcTag.PropTypes.REAL.getTypeString(), 0,
                    PlcioPcFormat.TYPE_R, 0, 0);

            TagItem tagItem2 = new TagItem("myDecimalValue", IPlcTag.PropTypes.INTEGER.getTypeString(), 0,
                    PlcioPcFormat.TYPE_J, 0, 0);

            TagItem tagItem3 = new TagItem("myBooleanValue", IPlcTag.PropTypes.BOOLEAN.getTypeString(), 0,
                    PlcioPcFormat.TYPE_C, 0, 0);

            TagItem tagItem4 = new TagItem("myRealValue", IPlcTag.PropTypes.REAL.getTypeString(), 0,
                    PlcioPcFormat.TYPE_R, 0, 0);


            TagItem[] tagItems1 = {tagItem1};
            TagItem[] tagItems2 = {tagItem2};
            TagItem[] tagItems3 = {tagItem3};
            TagItem[] tagItems4 = {tagItem4};


            PlcTag plcTag1 = new PlcTag("Scott_R", IPlcTag.DIRECTION_READ, ""+ PlcioPcFormat.TYPE_R,
                    10000, 1, 4, tagItems1);
            PlcTag plcTag2 = new PlcTag("Scott_D", IPlcTag.DIRECTION_READ, ""+ PlcioPcFormat.TYPE_J,
                    10000, 1, 4, tagItems2);
            PlcTag plcTag3 = new PlcTag("Scott_B", IPlcTag.DIRECTION_READ, ""+ PlcioPcFormat.TYPE_C,
                    10000, 1, 1, tagItems3);

            PlcTag plcTag4 = new PlcTag("Scott_R", IPlcTag.DIRECTION_WRITE, ""+ PlcioPcFormat.TYPE_R,
                    10000, 1, 4, tagItems4);
            String[] newValues = {"5678.0"};
            plcTag4.setMemberValues(newValues);


            PlcioCall plcioCallRead1 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_READ, "cip 192.168.1.20",
                    "Scott_R_Conn", 0, plcTag1);

            PlcioCall plcioCallRead2 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_READ, "cip 192.168.1.20",
                    "Scott_D_Conn", 1, plcTag2);

            PlcioCall plcioCallRead3 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_READ, "cip 192.168.1.20",
                    "Scott_D_Conn", 1, plcTag3);

            PlcioCall plcioCallWrite1 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_WRITE, "cip 192.168.1.20",
                    "Scott_R_Conn", 0, plcTag4);


            PlcioCall plcioCallClose1 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_CLOSE, "cip 192.168.1.20",
                    "Scott_R_Conn", 0);
            PlcioCall plcioCallClose2 = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_CLOSE, "cip 192.168.1.20",
                    "Scott_D_Conn", 1);



            // now lets try to read the compound tag

            TagItem tagItemB = new TagItem("myBooleanValue", IPlcTag.PropTypes.BOOLEAN.getTypeString(), 0,
                    PlcioPcFormat.TYPE_J, 0, 0);

            TagItem tagItemS = new TagItem("myDecimalValue", IPlcTag.PropTypes.INTEGER.getTypeString(), 1,
                    PlcioPcFormat.TYPE_J, 0, 0);

            TagItem tagItemR = new TagItem("myRealValue", IPlcTag.PropTypes.REAL.getTypeString(), 2,
                    PlcioPcFormat.TYPE_R, 0, 0);

            TagItem[] tagItemsU = {tagItemB, tagItemS, tagItemR};

            PlcTag plcTagU = new PlcTag("Scott_U", IPlcTag.DIRECTION_READ,
                    ""+ PlcioPcFormat.TYPE_J + PlcioPcFormat.TYPE_J + PlcioPcFormat.TYPE_R,
                    10000, 3, 12, tagItemsU);

            PlcioCall plcioCallReadU = new PlcioCall(IPlcioCall.PlcioMethodName.PLC_READ, "cip 192.168.1.20",
                    "Scott_R_Conn", 0, plcTagU);



            master.plcAccess(plcioCallOpen1);
            master.plcAccess(plcioCallOpen2);
            //master.plcAccess(plcioCallRead1);
            //master.plcAccess(plcioCallRead2);
            //master.plcAccess(plcioCallRead3);
            master.plcAccess(plcioCallReadU);

            master.plcAccess(plcioCallWrite1);


            master.plcAccess(plcioCallClose1);
            master.plcAccess(plcioCallClose2);

            //String readValue1 = plcTag1.getMemberValue("myRealValue");
            //String readValue2 = plcTag2.getMemberValue("myDecimalValue");
            //String readValue3 = plcTag3.getMemberValue("myBooleanValue");

            //System.out.println("readValue1 read by client: " + readValue1);
            //System.out.println("readValue2 read by client: " + readValue2);
            //System.out.println("readValue3 read by client: " + readValue3);


            String readValueU1 = plcTagU.getMemberValue("myRealValue");
            String readValueU2 = plcTagU.getMemberValue("myDecimalValue");
            String readValueU3 = plcTagU.getMemberValue("myBooleanValue");

            System.out.println("readValueU1 read by client: " + readValueU1);
            System.out.println("readValueU2 read by client: " + readValueU2);
            System.out.println("readValueU3 read by client: " + readValueU3);






        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("testing complete");


    }
}
