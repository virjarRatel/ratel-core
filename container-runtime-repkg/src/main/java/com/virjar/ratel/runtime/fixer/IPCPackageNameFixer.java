package com.virjar.ratel.runtime.fixer;

import android.os.Parcel;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;


public class IPCPackageNameFixer {

    public static void fixIpcTransact() {
        //android.os.Parcel#nativeWriteString
        RposedHelpers.findAndHookMethod(Parcel.class, "nativeWriteString", long.class, String.class, new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String str = (String) param.args[1];
                if (str == null) {
                    return;
                }

                if (str.contains(RatelRuntime.sufferKey)) {
                    //需要被保留，不能被替换的场景 TODO 未来还需要优化
                    return;
                }
                if (RatelRuntime.declaredComponentClassNames.contains(str)) {
                    //此时不能替换，因为是className
                }
//                else if (str.contains(RatelRuntime.originPackageName)) {
                // //contains的方式太粗暴了，出现问题  android.os.BadParcelableException: ClassNotFoundException when unmarshalling: virjar.zelda.comtencentmm.zElDa.cwqklrCU.plugin.performance.elf.ElfCheckRequest
//                    param.args[1] = str.replace(RatelRuntime.originPackageName, RatelRuntime.nowPackageName);
//                    if (RatelRuntime.isRatelDebugBuild) {
//                        Log.i(Constants.TAG, "replace string from: " + str + "  to: " + param.args[1]);
//                    }
//
//                }
                else if (str.equals(RatelRuntime.originPackageName)) {
                    param.args[1] = RatelRuntime.nowPackageName;
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "replace string from: " + str + "  to: " + param.args[1]);
                    }
                } else if (str.startsWith(Constants.zeldaKeep)) {
                    param.args[1] = str.substring(Constants.zeldaKeep.length());
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "replace string from: " + str + "  to: " + param.args[1]);
                    }
                } else if (RatelRuntime.originProcess2NowProcess.containsKey(str)) {
                    param.args[1] = RatelRuntime.originProcess2NowProcess.get(str);
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "replace string from: " + str + "  to: " + param.args[1]);
                    }
                } else if (str.contains(RatelRuntime.originPackageName)) {
                    if (str.startsWith("/data/app/") || str.startsWith("/data/user/") || str.startsWith("/data/user_de/")) {
                        param.args[1] = str.replace(RatelRuntime.originPackageName, RatelRuntime.nowPackageName);
                    } else if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "the ipc call contains pkg feature string: " + param.args[1], new Throwable());
                    }
                }
            }
        });

        //android.os.Parcel.readString
        //android.os.Parcel#nativeReadString
        RC_MethodHook readStringReplacePackage = new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String result = (String) param.getResult();
                if (result == null) {
                    return;
                }
                if (result.equals(RatelRuntime.nowPackageName)) {
                    param.setResult(RatelRuntime.originPackageName);
                } else if (RatelRuntime.nowProcess2OriginProcess.containsKey(result)) {
                    param.setResult(RatelRuntime.nowProcess2OriginProcess.get(result));
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "ipc result replace string from: " + result + "  to: " + param.getResult());
                    }
                } else if (result.contains(RatelRuntime.nowPackageName)) {
                    if (result.startsWith("/data/app/") || result.startsWith("/data/user/") || result.startsWith("/data/user_de/")) {
                        param.setResult(result.replace(RatelRuntime.nowPackageName, RatelRuntime.originPackageName));
                    } else if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "the ipc call result pkg feature string: " + result);
                    }
                }

            }
        };
        // RposedHelpers.findAndHookMethod(Parcel.class, "readString", readStringReplacePackage);
        RposedHelpers.findAndHookMethod(Parcel.class, "nativeReadString", long.class, readStringReplacePackage);


    }


}
