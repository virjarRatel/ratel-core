package de.robv.android.xposed;

import com.virjar.ratel.api.rposed.RC_MethodHook;

public class XC2RC_MethodHook extends RC_MethodHook {
    private XC_MethodHook delegate;

    XC2RC_MethodHook(XC_MethodHook delegate) {
        super(delegate.priority);
        this.delegate = delegate;
    }


    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        XC_MethodHook.MethodHookParam methodHookParam = new XC_MethodHook.MethodHookParam();
        methodHookParam.method = param.method;
        methodHookParam.thisObject = param.thisObject;
        methodHookParam.args = param.args;
        XposedHelpers.setObjectField(methodHookParam, "result", param.getResult());
        XposedHelpers.setObjectField(methodHookParam, "throwable", param.getThrowable());
        XposedHelpers.setBooleanField(methodHookParam, "returnEarly",
                XposedHelpers.getBooleanField(param, "returnEarly"));
        delegate.callBeforeHookedMethod(methodHookParam);
        param.args = methodHookParam.args;
        XposedHelpers.setObjectField(param, "result", methodHookParam.getResult());
        XposedHelpers.setObjectField(param, "throwable", methodHookParam.getThrowable());
        XposedHelpers.setBooleanField(param, "returnEarly", XposedHelpers.getBooleanField(methodHookParam, "returnEarly"));

    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        XC_MethodHook.MethodHookParam methodHookParam = new XC_MethodHook.MethodHookParam();
        methodHookParam.method = param.method;
        methodHookParam.thisObject = param.thisObject;
        methodHookParam.args = param.args;
        XposedHelpers.setObjectField(methodHookParam, "result", param.getResult());
        XposedHelpers.setObjectField(methodHookParam, "throwable", param.getThrowable());
        XposedHelpers.setBooleanField(methodHookParam, "returnEarly",
                XposedHelpers.getBooleanField(param, "returnEarly"));
        delegate.callAfterHookedMethod(methodHookParam);
        param.args = methodHookParam.args;
        XposedHelpers.setObjectField(param, "result", methodHookParam.getResult());
        XposedHelpers.setObjectField(param, "throwable", methodHookParam.getThrowable());
        XposedHelpers.setBooleanField(param, "returnEarly", XposedHelpers.getBooleanField(methodHookParam, "returnEarly"));

    }

    @Override
    public String toString() {
        return "XC2RC_MethodHook{" +
                "delegate=" + delegate +
                '}';
    }
}
