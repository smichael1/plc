package atst.giss.abplc;

/*
 * class used to represent individual data items belonging
 * to this tag.
 *
 * @author Alastair Borrowman (OSL)
 * modified by Scott Michaels (TMT)
 *
 */
public class TagItem {
    private final String itemName;
    private String itemTypeString;
    private final int memberNum;
    private final char memberPlcioType;
    private final int bytePos;
    private final int bitPos;
    private final int bitMask;
    private final boolean isBoolean;

    // Constructor
    TagItem(String itemName, String itemTypeString, int memberNum, char memberPlcioType, int bytePos, int bitPos) {
        this.itemName = itemName;
        this.itemTypeString = itemTypeString;
        this.memberNum = memberNum;
        this.memberPlcioType = memberPlcioType;
        this.bytePos = bytePos;
        this.bitPos = bitPos;
        // does this item store a boolean value?
        if (itemTypeString.equals(IPlcTag.PropTypes.BOOLEAN.getTypeString())) {
            bitMask = (int) Math.pow(2, bitPos);
            isBoolean = true;
        }
        else {
            bitMask = -1;
            isBoolean = false;
        }
    } // end Constructor

    String getItemName() {
        return itemName;
    } // end getItemName()

    String getItemTypeString() {
        return itemTypeString;
    }

    int getMemberNum() {
        return memberNum;
    } // end getMemberNum()

    char getMemberPlcioType() {
        return memberPlcioType;
    } // end getMemberPlcioType()

    int getBytePos(){
        return bytePos;
    } // end getBytePos()

    int getBitPos() {
        return bitPos;
    } // end getBitPos()

    int getBitMask() {
        return bitMask;
    } // end getBitMask()


    boolean isBoolean() {
        return isBoolean;
    } // end isBoolean()

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("tag item name = '" + itemName +
                "', property type = " + itemTypeString +
                ", stored in PLCIO type '" + memberPlcioType + "' ");

        try {
            result.append("(" + PlcioPcFormat.getTypeString(memberPlcioType) +"), ");
        } catch(ABPlcioExceptionBadPlcTagProperties ex) {
            result.append("(not valid PLCIO type), ");
        }

        result.append("memberNum = " + memberNum + ", ");

        if (this.isBoolean) {
            result.append("bytePos:bitPos = " + bytePos + ":" + bitPos);
            result.append(" (bitMask = " + String.format("%#06x", bitMask) + ") raw = " + bitMask);
        }
        else {
            result.append("bytePos = " + bytePos);
        }

        return result.toString();
    } // end toString()


    private String tbd() {
        //getting values should be here?
        return null;
    }

}

