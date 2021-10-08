package mirror.android.os;

import mirror.RefClass;
import mirror.RefInt;

public class Parcel {
    public static java.lang.Class Class = RefClass.load(Parcel.class, "android.os.Parcel");

    public static RefInt EX_TRANSACTION_FAILED;//-129
    /**
     * Android9-10
     */
    public static RefInt EX_HAS_REPLY_HEADER;//-128

    /**
     * Android11
     */
    public static RefInt EX_HAS_NOTED_APPOPS_REPLY_HEADER;//-127 // special; see below
    public static RefInt EX_HAS_STRICTMODE_REPLY_HEADER;//-128;  // special; see below

}
