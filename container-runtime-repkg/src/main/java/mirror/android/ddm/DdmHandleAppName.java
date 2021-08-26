package mirror.android.ddm;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class DdmHandleAppName {
    public static java.lang.Class Class = RefClass.load(DdmHandleAppName.class, "android.ddm.DdmHandleAppName");
    @MethodParams({String.class, int.class})
    public static RefStaticMethod<Void> setAppName;
}