package com.virjar.ratel.runtime.fixer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.virjar.ratel.api.inspect.ClassLoadMonitor;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.lang.reflect.Method;

import external.org.apache.commons.lang3.StringUtils;

public class BroadcastFixer {
    public static void fix() {

        //android.app.IActivityManager$Stub$Proxy.broadcastIntent
        Class<?> activityManagerStubProxyClasss = RposedHelpers.findClassIfExists("android.app.IActivityManager$Stub$Proxy", ClassLoader.getSystemClassLoader());
        if (activityManagerStubProxyClasss == null) {
            return;
        }
        // public int broadcastIntent(android.app.IApplicationThread caller, android.content.Intent intent, java.lang.String resolvedType, android.content.IIntentReceiver resultTo, int resultCode,
        // java.lang.String resultData, android.os.Bundle map, java.lang.String[] requiredPermissions, int appOp, android.os.Bundle options, boolean serialized, boolean sticky, int userId) throws android.os.RemoteException
        RposedBridge.hookAllMethods(activityManagerStubProxyClasss, "broadcastIntent", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                Intent intent = (Intent) param.args[1];
                String resolvedType = (String) param.args[2];
                int resultCode = (int) param.args[4];
                String resultData = (String) param.args[5];
                Bundle map = (Bundle) param.args[6];
                String[] requiredPermissions = (String[]) param.args[7];
                int appOp = (int) param.args[8];
                Bundle options = (Bundle) param.args[9];
                boolean serialized = (boolean) param.args[10];
                boolean sticky = (boolean) param.args[11];
                int userId = (int) param.args[12];


                if ("android.intent.action.APPLICATION_MESSAGE_UPDATE".equals(intent.getAction())) {
                    //禁用这个广播
                    param.setResult(null);
//                    String updateApplicationComponentName = intent.getStringExtra("android.intent.extra.update_application_component_name");
//                    if (updateApplicationComponentName != null) {
//                        updateApplicationComponentName = updateApplicationComponentName.replace(RatelRuntime.originPackageName, RatelRuntime.nowPackageName);
//                        intent.putExtra("android.intent.extra.update_application_component_name", updateApplicationComponentName);
//                    }
//                    intent.putExtra("android.intent.extra.update_application_message_text", RatelRuntime.sufferKey);
                }


                Log.i("weijia", "broadcastIntent  intent:" + intent);
                Log.i("weijia", "broadcastIntent  resolvedType:" + resolvedType + " , resultCode:" + resultCode
                        + " resultData:" + resultData + " map:" + map
                        + " requiredPermissions:" + StringUtils.join(requiredPermissions) + " appOp:" + appOp + " options: " + options + " serialized:" + serialized + " sticky: " + sticky
                        + " userId:" + userId);

                Bundle extras = intent.getExtras();
                if (extras != null) {
                    extras.getString("test");
                    Log.i("weijia", "extras:" + extras);
                }
            }
        });

        //ctrip.business.splash.CtripSplashActivity
        ClassLoadMonitor.addClassLoadMonitor("ctrip.business.splash.CtripSplashActivity", new ClassLoadMonitor.OnClassLoader() {
            @Override
            public void onClassLoad(Class<?> clazz) {

                RC_MethodHook methodExecuteHook = new RC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.i("weijia", "call method: " + param.method, new Throwable());
                    }
                };

                for (Method method : clazz.getDeclaredMethods()) {
                    RposedBridge.hookMethod(method, methodExecuteHook);
                }
            }
        });
    }
}
