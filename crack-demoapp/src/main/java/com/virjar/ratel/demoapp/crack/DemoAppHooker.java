package com.virjar.ratel.demoapp.crack;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.virjar.ratel.api.RatalStartUpCallback;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.extension.superappium.PageTriggerManager;
import com.virjar.ratel.api.extension.superappium.ViewImage;
import com.virjar.ratel.api.extension.superappium.WebViewHelper;
import com.virjar.ratel.api.extension.superappium.sekiro.SekiroStarter;
import com.virjar.ratel.api.rposed.IRposedHookLoadPackage;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage;
import com.virjar.sekiro.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.lang3.StringUtils;

/**
 * Created by virjar on 2018/10/6.
 */

public class DemoAppHooker implements IRposedHookLoadPackage {
    private static final String tag = "DEMO_HOOK";

    @Override
    public void handleLoadPackage(final RC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Log.i(tag, "hook start");

//        ApplicationInfo applicationInfo = new ApplicationInfo(RatelToolKit.sContext.getApplicationInfo());
//        applicationInfo.packageName = "com.virjar.ratel.demoapp.crack";
//        applicationInfo.sourceDir = lpparam.modulePath;
//
//        RatelToolKit.sContext.createPackageContext()

//
//        RatelToolKit.ratelUnpack.enableUnPack(
//                new File(RatelToolKit.sContext.getFilesDir(), "dex_dump"), false
//        );

//        Class<?> dexFileClass = RposedHelpers.findClass("dalvik.system.DexFile", ClassLoader.getSystemClassLoader());
//        if (dexFileClass == null) {
//            Log.e(tag, "dexFileClass is null");
//        } else {
//            try {
//                RposedHelpers.callStaticMethod(dexFileClass, "openShowGetDexFileLog");
//            } catch (Throwable throwable) {
//                Log.i(tag, "error", throwable);
//            }
//        }

        switchUserBySdcardFile();
        try {
            hookBaiduHomework(lpparam);
        } catch (Throwable e) {
            Log.e(tag, "hook baidu homework error:", e);
        }
        Log.i(tag, "hook end");
    }

    private static void hookBaiduHomework(final RC_LoadPackage.LoadPackageParam lpparam) {
        RposedHelpers.findAndHookMethod("com.baidu.homework.MyWrapperProxyApplication", lpparam.classLoader, "onCreate", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ClassLoader classLoader = param.thisObject.getClass().getClassLoader();

                Class<?> shuMeiClass = RposedHelpers.findClass("com.ishumei.O0000O000000oO.O000O0000OoO", classLoader);
                RposedHelpers.findAndHookMethod(shuMeiClass, "O000O00000o0O", String.class, boolean.class, new RC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            String shumeiInfo = Arrays.toString(param.args);
                            Log.i(tag, "Shumei info: " + shumeiInfo);
                            FileUtils.writeStringToFile(new File(RatelToolKit.whiteSdcardDirPath, "shumei.txt"), shumeiInfo, "utf-8");
                        } catch (Exception e) {
                            Log.e(tag, "Shumei info error: ", e);
                        }
                    }
                });
            }
        });
    }


    private static void switchUserBySdcardFile() {
        String nowUser = RatelToolKit.virtualEnv.nowUser();

        File file = new File(new File(RatelToolKit.whiteSdcardDirPath), "userId.txt");
        if (file.exists()) {
            try {
                String userIdFromSdcard = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                if (!StringUtils.equals(nowUser, userIdFromSdcard)) {
                    Log.i(tag, String.format("switch user %s to %s", nowUser, userIdFromSdcard));
                    RatelToolKit.virtualEnv.switchEnv(userIdFromSdcard);
                }
            } catch (IOException e) {
                Log.i(tag, "failed to read id ctr file", e);
            }

        } else {
            Log.i(tag, "multi user id control file not exist: " + file);
        }
    }

    private static void switchTo18788885597() {
        RatelToolKit.virtualEnv.switchEnv("18788885597");
    }


    private static void addFloatingButtonForActivity(final RC_LoadPackage.LoadPackageParam lpparam) {
        RposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                new Handler(Looper.getMainLooper())
                        .postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                createAndAttachFloatingButtonOnActivity((Activity) param.thisObject);
                            }
                        }, 1000);
            }

            private void createAndAttachFloatingButtonOnActivity(Activity activity) {
                Context context = RatelToolKit.ratelResourceInterface.createContext(lpparam.modulePath, DemoAppHooker.class.getClassLoader(), RatelToolKit.sContext);

                FrameLayout frameLayout = (FrameLayout) activity.getWindow().getDecorView();
                LayoutInflater.from(context).cloneInContext(context)
                        .inflate(R.layout.float_button, frameLayout);

            }
        });
    }


    private static void backup(final RC_LoadPackage.LoadPackageParam lpparam) {
        switchUserBySdcardFile();

        addFloatingButtonForActivity(lpparam);

        Log.i(tag, "xposed module entry :");
        Class<?> aClass = RposedHelpers.findClass("com.virjar.ratel.demoapp.MainActivity", lpparam.classLoader);
        Log.i(tag, "hook class for :" + aClass + " hash：" + aClass.hashCode());
        RposedHelpers.findAndHookMethod(aClass, "text", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.i(tag, "before hook");
                param.setResult("被hook后的文本");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.i(tag, "after hook");
            }
        });

        RatelToolKit.setOnRatelStartUpCallback(new RatalStartUpCallback() {
            @Override
            public void onRatelStartCompletedEvent() {
                File testWriteFile = new File(RatelToolKit.sContext.getFilesDir(), "testWriteFile.txt");
                try {
                    Log.i(tag, "test write file to :" + testWriteFile);
                    FileUtils.write(testWriteFile, "test for user: " + RatelToolKit.virtualEnv.nowUser() + "\n", StandardCharsets.UTF_8);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        //请注意，只有在release上面，才支持safe static method
        //staticMethodHookTest(lpparam);


        RatelToolKit.addOnRatelStartUpCallback(new RatalStartUpCallback() {
            @Override
            public void onRatelStartCompletedEvent() {
                Log.i(tag, "modify nowUserId:" + RatelToolKit.virtualEnv.nowUser());
                Class<?> theAppClass = RposedHelpers.findClass("com.virjar.ratel.demoapp.MainActivity", lpparam.classLoader);
                RposedHelpers.callStaticMethod(theAppClass, "setNowUserId", RatelToolKit.virtualEnv.nowUser());
                Log.i(tag, "modify nowUserId end");
            }
        });

        //com.virjar.ratel.demoapp.construtor_test.ParentClass.ParentClass
        RposedBridge.hookAllConstructors(RposedHelpers.findClass("com.virjar.ratel.demoapp.construtor_test.ParentClass", lpparam.classLoader), new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                Log.i(tag, "hook for ParentClass constructor!! ");
            }
        });

        //PageTriggerManager.getTopFragment("test");

        SekiroStarter.startService("sekiro.virjar.com", Constants.defaultNatServerPort);

        RposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject.getClass().getName().equals("com.virjar.ratel.demoapp.AlertDialogTest")) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(tag, "call killMe..");
                            RatelToolKit.processUtils.killMe();
                        }
                    });
                }
            }
        });


        PageTriggerManager.addHandler("com.virjar.ratel.demoapp.WebViewTestActivity", new PageTriggerManager.ActivityFocusHandler() {
            @Override
            public boolean handleActivity(Activity activity, ViewImage root) {
                WebView webView = root.findWebViewIfExist();
                if (webView == null) {
                    Log.i(tag, "无法定位登陆WebView...");
                    return false;
                }
                WebViewHelper.JsCallFuture jsCallFuture = new WebViewHelper(webView).typeByXpath("//input[@data-componentname='phoneNumber']", "18788885597");


                jsCallFuture.success()
                        .typeByXpath("//input[@data-componentname='password']", "258369")
                        .clickByXpath("//input[@type='button' and @value='登录']");

                jsCallFuture.failed().addOnJsCallFinishEvent(new WebViewHelper.OnJsCallFinishEvent() {
                    @Override
                    public void onJsCallFinished(String callResultId) {
                        if (triggerCount > 10) {
                            return;
                        }
                        triggerCount++;
                        Log.i(tag, "点击失败，等待400ms");
                        PageTriggerManager.trigger(400);

                    }
                });

                return true;
            }
        });

    }


    private static int triggerCount = 0;


    private static void staticMethodHookTest(RC_LoadPackage.LoadPackageParam lpparam) {
        //com.virjar.ratel.demoapp.TestStatic.checkFlag
        RposedHelpers.findAndHookMethod("com.virjar.ratel.demoapp.TestStatic", lpparam.classLoader,
                "checkFlag", new RC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.i(tag, "checkFlag");
                    }
                });
    }

}
