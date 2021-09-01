package com.virjar.ratel.runtime.ipc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.XposedModuleLoader;

public class IPCControlHandler {

    private static int retryTimes = 0;

    public static void initRatelManagerIPCClient() {
        if (!XposedModuleLoader.isRatelManagerInstalled()) {
            return;
        }
        if (XposedModuleLoader.getRatelManagerVersionCode() < 4) {
            return;
        }
        //从1.0.3开始才支持ipc control

        Intent intent = new Intent();
        intent.setAction("com.virjar.ratel.ipc.register");
        intent.setPackage("com.virjar.ratel.manager");
        boolean isBindSuccess = RatelRuntime.getOriginContext().bindService(intent, new ClientHandlerServiceConnection(), Context.BIND_AUTO_CREATE);
        if (!isBindSuccess) {
            retryTimes++;
            Log.w(Constants.TAG, "connect to ratel manager server failed");
            Intent ratelLaunchIntent = RatelRuntime.getOriginContext().getPackageManager().getLaunchIntentForPackage(Constants.ratelManagerPackage);
            // notice this is an asynchronized call,we can not wait for ratel manager process started
            try {
                RatelRuntime.getOriginContext().startActivity(ratelLaunchIntent);
            } catch (Exception e) {
                //ignore
            }
//            if (retryTimes % 3 == 1) {
//
//            }
//            if (XposedModuleLoader.getRatelManagerVersionCode() >= 6 && retryTimes <= 1) {
//                ClientHandlerServiceConnection.addOnManagerIPCListener(iRatelManagerClientRegister -> {
//                    try {
//                        iRatelManagerClientRegister.addDelayDeamonTask(RatelRuntime.getAppPackageName(), 5000);
//                        ProcessUtil.killMe();
//                    } catch (RemoteException e) {
//                        Log.i(Constants.TAG, "addDelayDeamonTask error", e);
//                    }
//
//                });
//            }

            if (retryTimes < 15) {
                new Handler(Looper.getMainLooper()).postDelayed(IPCControlHandler::initRatelManagerIPCClient, 1000);
            } else {
                Log.e(Constants.TAG, "can not connect to ratel manager with too many times");
            }
        }
    }
}
