package com.virjar.ratel.runtime.fixer;

import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;

public class ProviderUriFixer {
    public static void fixUri() {
        relocateWhenRegisterContentObserver();
        relocateWhenAcquireProvider();
        relocateWhenInstallProvider();
        // relocateWhenQuery();
        //暂时不需要
    }

    private static void relocateWhenQuery() {
        //at android.content.ContentProviderProxy.query(ContentProviderNative.java:402)
        RposedBridge.hookAllMethods(
                RposedHelpers.findClass("android.content.ContentProviderProxy", ClassLoader.getSystemClassLoader()),
                "query", new RC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        for (int i = 0; i < param.args.length; i++) {
                            if (param.args[i] instanceof Uri) {
                                Uri uri = (Uri) param.args[i];
                                String authority = uri.getAuthority();

                                if (RatelRuntime.declaredAuthorities.contains(authority)) {
                                    Uri newUri = new Uri.Builder()
                                            .scheme(uri.getScheme())
                                            .authority(uri.getAuthority() + "." + RatelRuntime.sufferKey)
                                            .fragment(uri.getFragment())
                                            .query(uri.getQuery())
                                            .path(uri.getPath()).build();
                                    if (RatelRuntime.isRatelDebugBuild) {
                                        Log.i(Constants.TAG, "relocate uri from: " + uri + " to :" + newUri);
                                        param.args[i] = newUri;
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

    private static void relocateWhenInstallProvider() {
        RposedBridge.hookAllMethods(RatelRuntime.mainThread.getClass(), "installProvider", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                for (Object o : param.args) {
                    if (o instanceof ProviderInfo) {
                        ProviderInfo providerInfo = (ProviderInfo) o;
                        if (providerInfo.authority.endsWith(RatelRuntime.sufferKey)) {
                            //有一个点号分割
                            providerInfo.authority = providerInfo.authority.substring(0, providerInfo.authority.indexOf(RatelRuntime.sufferKey) - 1);
                        }
                        break;

                    }
                }
            }
        });
    }

    private static void relocateWhenAcquireProvider() {

        //android.app.IActivityManager$Stub$Proxy.getContentProvider
        //public android.app.ContentProviderHolder getContentProvider(android.app.IApplicationThread caller, java.lang.String name, int userId, boolean stable) throws android.os.RemoteException
        Class<?> activityManagerProxyStubClass = RposedHelpers.findClass("android.app.IActivityManager$Stub$Proxy", ClassLoader.getSystemClassLoader());
        RposedBridge.hookAllMethods(activityManagerProxyStubClass, "getContentProvider", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String name = (String) param.args[1];
                if (RatelRuntime.declaredAuthorities.contains(name)) {
                    if (RatelRuntime.isRatelDebugBuild) {
                        String newName = name + "." + RatelRuntime.sufferKey;
                        Log.i(Constants.TAG, "relocate contentProvider from: " + name + " to :" + newName);
                        param.args[1] = newName;
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object contentProviderHolder = param.getResult();
                if (contentProviderHolder == null) {
                    return;
                }

                //TODO 是否需要安装两次ContentProvider
                ProviderInfo providerInfo = RposedHelpers.getObjectField(contentProviderHolder, "info");

                if (providerInfo.authority.endsWith(RatelRuntime.sufferKey)) {
                    //有一个点号分割
                    providerInfo.authority = providerInfo.authority.substring(0, providerInfo.authority.indexOf(RatelRuntime.sufferKey) - 1);
                }
                IContentProvider iContentProvider = RposedHelpers.getObjectField(contentProviderHolder, "provider");
                Log.i(Constants.TAG, "iContentProvider:" + iContentProvider + "   providerInfo.authority: " + providerInfo.authority);

            }
        });
    }

    private static void relocateWhenRegisterContentObserver() {
        //public final  void registerContentObserver(android.net.Uri uri, boolean notifyForDescendents, android.database.ContentObserver observer) { throw new RuntimeException("Stub!"); }
        //android.content.IContentService$Stub$Proxy.registerContentObserver
        Class<?> contentServiceProxyClass = RposedHelpers.findClass("android.content.IContentService$Stub$Proxy", ClassLoader.getSystemClassLoader());

        RposedBridge.hookAllMethods(contentServiceProxyClass, "registerContentObserver", new RC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Uri uri = (Uri) param.args[0];
                String authority = uri.getAuthority();


                if (RatelRuntime.declaredAuthorities.contains(authority)) {
                    Uri newUri = new Uri.Builder()
                            .scheme(uri.getScheme())
                            .authority(uri.getAuthority() + "." + RatelRuntime.sufferKey)
                            .fragment(uri.getFragment())
                            .query(uri.getQuery())
                            .path(uri.getPath()).build();
                    if (RatelRuntime.isRatelDebugBuild) {
                        Log.i(Constants.TAG, "relocate uri from: " + uri + " to :" + newUri);
                        param.args[0] = newUri;
                    }
                }
                Log.i(Constants.TAG, "registerContentObserver for uri:" + param.args[0]);
            }
        });
    }
}
