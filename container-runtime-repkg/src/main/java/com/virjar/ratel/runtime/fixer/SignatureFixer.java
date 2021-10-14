package com.virjar.ratel.runtime.fixer;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.fixer.pm.PackageParserEx;
import com.virjar.ratel.utils.BuildCompat;


public class SignatureFixer {


    /**
     * 考虑使用hook方案过签名检测，原因是packageManager非常敏感，切很容易检测到代理痕迹。
     *
     * @return 返回是拦截成功。一般来说不应该失败。失败后暂时使用代理的方式替换服务
     * @see <a href="https://bbs.pediy.com/thread-250871.htm">检测ActivityManagerNative</a>
     */
    @SuppressLint("PrivateApi")
    public static void fixSignature() {
        // android.content.pm.IPackageManager.Stub.Proxy#getPackageInfo
        // android.webkit.IWebViewUpdateService$Stub.Proxy#waitForAndGetProvider
        Class<?> packageManagerProxyClass;
        Class<?> webViewUpdateServiceProxyClass = null;
        try {
            packageManagerProxyClass = Class.forName("android.content.pm.IPackageManager$Stub$Proxy");
            handleIPackageManager$Stub$Proxy(packageManagerProxyClass);
        } catch (ClassNotFoundException e) {
            Log.w(Constants.TAG, "can not found packageManagerProxyClass", e);
        }

        try {
            webViewUpdateServiceProxyClass = Class.forName("android.webkit.IWebViewUpdateService$Stub$Proxy");
            handleIWebViewUpdateService$Stub$Proxy(webViewUpdateServiceProxyClass);
        } catch (ClassNotFoundException e) {
            Log.w(Constants.TAG, "can not found webViewUpdateServiceProxyClass", e);
        }

    }

    private static void handleIWebViewUpdateService$Stub$Proxy(Class webViewUpdateServiceProxyClass) {
        RposedBridge.hookAllMethods(webViewUpdateServiceProxyClass, "waitForAndGetProvider", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object result = param.getResult();
                if (result == null) {
                    return;
                }
                PackageInfo packageInfo;
                if (result instanceof PackageInfo) {
                    packageInfo = (PackageInfo) result;
                } else {
                    //android.webkit.WebViewProviderResponse
                    packageInfo = RposedHelpers.getObjectField(result, "packageInfo");
                }
                if (packageInfo.signatures == null) {
                    return;
                }
//                if (packageInfo.signatures != null && packageInfo.packageName.equals(RatelRuntime.originPackageName)) {
//                    Signature[] fakeSignature = PackageParserEx.getFakeSignatureForOwner();
//                    if (fakeSignature != null) {
//                        packageInfo.signatures = fakeSignature;
//                    }
//                }

                Signature[] fakeSignature = PackageParserEx.getFakeSignature(packageInfo.packageName);
                if (fakeSignature != null) {
                    packageInfo.signatures = fakeSignature;
                }
            }
        });
    }

    private static void handleIPackageManager$Stub$Proxy(Class packageManagerProxyClass) {
        RC_MethodHook signatureReplaceHook = new RC_MethodHook() {

            private boolean hasGetSignature(PackageInfo packageInfo) {
                if (packageInfo.signatures != null) {
                    return true;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    return packageInfo.signingInfo != null && packageInfo.signingInfo.getApkContentsSigners() != null;
                }
                return false;
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                PackageInfo result = (PackageInfo) param.getResult();
                if (result == null) {
                    return;
                }

                if (!hasGetSignature(result)) {
                    return;
                }

                String packageName = result.packageName;
                try {
                    Signature[] fakeSignature = PackageParserEx.getFakeSignature(packageName);
                    if (fakeSignature == null) {
                        return;
                    }
                    result.signatures = fakeSignature;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && result.signingInfo != null) {
                        //高版本可以调用这个api拿到真是签名
                        //Signature[] apkContentsSigners = result.signingInfo.getApkContentsSigners();
                        Object mSigningDetails = RposedHelpers.getObjectField(result.signingInfo, "mSigningDetails");
                        if (mSigningDetails != null) {
                            Object signatures = RposedHelpers.getObjectField(mSigningDetails, "signatures");
                            if (signatures != null) {
                                RposedHelpers.setObjectField(mSigningDetails, "signatures", fakeSignature);
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    Log.e(Constants.TAG, "error", throwable);
                    System.exit(0);
                }
            }
        };


        RposedBridge.hookAllMethods(packageManagerProxyClass, "getPackageInfo", signatureReplaceHook);
        //华为8.0 有缓存，需要把packageManager再次hook。当然IPC也是需要拦截的
        RposedBridge.hookAllMethods(RatelRuntime.originContext.getPackageManager().getClass(), "getPackageInfo", signatureReplaceHook);

        if (RatelRuntime.isZeldaEngine()) {

            RC_MethodHook applicationInfoRelocate = new RC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String packageName = (String) param.args[0];
                    if (RatelRuntime.originPackageName.equals(packageName)) {
                        AppBindDataFixer.fixApplicationInfo((ApplicationInfo) param.getResult());
                    }
                }
            };

            //public android.content.pm.ApplicationInfo getApplicationInfo(java.lang.String packageName, int flags, int userId) throws android.os.RemoteException
            RposedBridge.hookAllMethods(packageManagerProxyClass, "getApplicationInfo", applicationInfoRelocate);
            RposedBridge.hookAllMethods(RatelRuntime.originContext.getPackageManager().getClass(), "getApplicationInfo", applicationInfoRelocate);

            RC_MethodHook resolveContentProviderHook = new RC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String authorities = (String) param.args[0];
                    if (RatelRuntime.declaredAuthorities.contains(authorities)) {
                        param.args[0] = authorities + "." + RatelRuntime.sufferKey;
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ProviderInfo providerInfo = (ProviderInfo) param.getResult();
                    if (providerInfo == null) {
                        return;
                    }
                    AppBindDataFixer.fixOneProviderInfo(providerInfo);
                }
            };

            //android.content.pm.IPackageManager.Stub.Proxy#resolveContentProvider
            RposedBridge.hookAllMethods(packageManagerProxyClass, "resolveContentProvider", resolveContentProviderHook);
            RposedBridge.hookAllMethods(RatelRuntime.originContext.getPackageManager().getClass(), "resolveContentProvider", resolveContentProviderHook);
        }
    }

}
