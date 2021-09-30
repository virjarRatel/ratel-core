package com.virjar.ratel.runtime.fixer;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.ui.util.Constant;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.utils.BuildCompat;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.WeakHashMap;

import mirror.android.app.ActivityThread;

/**
 * 修复媒资管理器相关，涉及 ResourceManager 和 AssertManager
 */
public class AssertFixer {
    private static String sourceCanonicalPath;

    public static void beforeIORedirect() throws IOException {
        if (RatelRuntime.isKratosEngine()) {
            return;
        }
        String sourceAPKDir = RatelRuntime.originApplicationInfo.sourceDir;
        File file = RatelEnvironment.originApkDir();
        sourceCanonicalPath = new File(sourceAPKDir).getCanonicalPath();
        String destCanonicalPath = file.getCanonicalPath();
        // /data/app/com.tencent.mm/base.apk -> /data/data/zelda.comtentcentmm/xxx/file/zelda_resource/origin_apk.apk
        // and
        // /data/app/zelda.comtentcentmm/base.apk -> /data/data/zelda.comtentcentmm/xxx/file/zelda_resource/origin_apk.apk
        RatelNative.redirectFile(sourceCanonicalPath, destCanonicalPath);
        if (RatelRuntime.isZeldaEngine()) {
            RatelNative.redirectFile(sourceCanonicalPath.replace(RatelRuntime.nowPackageName, RatelRuntime.originPackageName), destCanonicalPath);
        }
    }

    public static void afterIORedirect() {
        if (RatelRuntime.isKratosEngine()) {
            return;
        }
        Object resourceManager = ActivityThread.mResourcesManager.get(RatelRuntime.mainThread);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fixResourceImplAfterN(resourceManager);
        } else {
            //android version <= 6.0
            fixResourceImplBeforeN(resourceManager);
        }

    }

    private static void fixResourceImplAfterN(Object resourceManager) {

        WeakHashMap mActivityResourceReferences = RposedHelpers.getObjectField(resourceManager, "mActivityResourceReferences");
        mActivityResourceReferences.clear();

        @SuppressWarnings("unchecked")
        ArrayMap<Object, Object> mResourceImpls = RposedHelpers.getObjectField(resourceManager, "mResourceImpls");

        if (BuildCompat.isPie()) {
            ArrayMap mCachedApkAssets = RposedHelpers.getObjectField(resourceManager, "mCachedApkAssets");
            mCachedApkAssets.clear();
        }

        ArrayList mResourceReferences = RposedHelpers.getObjectField(resourceManager, "mResourceReferences");
        mResourceReferences.clear();


        Object reuseResourceImpl = null;

        for (Object resourceKey : mResourceImpls.keySet()) {
            if (!sourceCanonicalPath.equalsIgnoreCase((String) RposedHelpers.getObjectField(resourceKey, "mResDir"))) {
                continue;
            }

            Log.i(Constants.TAG, "reload assetManager... " + RposedHelpers.getObjectField(resourceKey, "mResDir"));
            //我们需要在io重定向之后，重构assetManager对象，否则app使用重构前的apk文件维护zip索引信息，使用新的文件作为内容体。在asset关闭重开后会数据紊乱
            //否则使用Android自带刷新机制来刷新asset数据，这可能引发application重复加载。android认为apk文件改变，那么应该重新触发application加载
            //但是application的加载代码可能存在副作用，比如往静态空间写入flag等
            WeakReference resourcesImplWeakReference = (WeakReference) mResourceImpls.get(resourceKey);
            if (resourcesImplWeakReference == null || resourcesImplWeakReference.get() == null) {
                Log.w(Constants.TAG, "resource weak reference empty");
                continue;
            }
            Object resourceImpl = resourcesImplWeakReference.get();

// 不能close 在华为9上面导致了闪退
//            AssetManager oldAssets = RposedHelpers.getObjectField(resourceImpl, "mAssets");
//            oldAssets.close();

            Object newResourceImpl = null;
            if (Build.VERSION.SDK_INT >= 30) {
                Method method = RposedHelpers.findMethodExactIfExists(resourceManager.getClass(),
                        "createApkAssetsSupplierNotLocked", "android.content.res.ResourcesKey");
                if (method != null) {
                    try {
                        // 部分手机是通过这个方法创建对象的
                        Object mApkAssetsSupplier = RposedHelpers.callMethod(resourceManager, "createApkAssetsSupplierNotLocked", resourceKey);
                        newResourceImpl = RposedHelpers.callMethod(resourceManager, "createResourcesImpl", resourceKey, mApkAssetsSupplier);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "error", e);
                    }
                }
            }

            if (newResourceImpl == null) {
                newResourceImpl = RposedHelpers.callMethod(resourceManager, "createResourcesImpl", resourceKey);
            }
            //Object newResourceImpl = RposedHelpers.callMethod(resourceManager, "createResourcesImpl", resourceKey);
            if (newResourceImpl == null) {

                AssetManager assets = (AssetManager) RposedHelpers.callMethod(resourceManager, "createAssetManager", resourceKey);
                RposedHelpers.setObjectField(resourceImpl, "mAssets", assets);
                try {
                    RposedHelpers.callMethod(assets, "ensureStringBlocks");
                } catch (Throwable throwable) {
                    //ignore
                }
            } else {
                mResourceImpls.put(resourceKey, new WeakReference<>(newResourceImpl));
            }
            reuseResourceImpl = newResourceImpl;

        }

        if (reuseResourceImpl != null) {
            Object mBoundApplication = ActivityThread.mBoundApplication.get(RatelRuntime.mainThread);
            Object loadApk = RposedHelpers.getObjectField(mBoundApplication, "info");
            Resources resources = (Resources) RposedHelpers.getObjectField(loadApk, "mResources");
            RposedHelpers.setObjectField(resources, "mResourcesImpl", reuseResourceImpl);

            Context contextImpl = RatelRuntime.originContext;
            Context nextContext;
            while ((contextImpl instanceof ContextWrapper) &&
                    (nextContext = ((ContextWrapper) contextImpl).getBaseContext()) != null) {
                contextImpl = nextContext;
            }

            RposedHelpers.setObjectField(contextImpl, "mResources", resources);
        }
    }

    private static void fixResourceImplBeforeN(Object resourceManager) {
        @SuppressWarnings("unchecked")
        ArrayMap<Object, WeakReference<Resources>> mActiveResources = RposedHelpers.getObjectField(resourceManager, "mActiveResources");

        Resources reuseResource = null;
        for (Object resourceKey : mActiveResources.keySet()) {
            if (!sourceCanonicalPath.equalsIgnoreCase(RposedHelpers.getObjectField(resourceKey, "mResDir"))) {
                continue;
            }

            Log.i(Constants.TAG, "reload assetManager... " + RposedHelpers.getObjectField(resourceKey, "mResDir"));
            WeakReference<Resources> wr = mActiveResources.get(resourceKey);

            if (wr == null || wr.get() == null) {
                continue;
            }

            Resources oldResource = wr.get();
            oldResource.getAssets().close();

            AssetManager assets = (AssetManager) RposedHelpers.newInstance(AssetManager.class);

            String mResDir = RposedHelpers.getObjectField(resourceKey, "mResDir");
            if (mResDir != null) {
                RposedHelpers.callMethod(assets, "addAssetPath", mResDir);
            }

            int displayId = RposedHelpers.getIntField(resourceKey, "mDisplayId");

            DisplayMetrics dm = (DisplayMetrics) RposedHelpers.callMethod(resourceManager, "getDisplayMetricsLocked", displayId);

            Configuration config;
            boolean isDefaultDisplay = (displayId == Display.DEFAULT_DISPLAY);
            final boolean hasOverrideConfig = (boolean) RposedHelpers.callMethod(resourceKey, "hasOverrideConfiguration"); //； key.hasOverrideConfiguration();
            if (!isDefaultDisplay || hasOverrideConfig) {
                // config = new Configuration(getConfiguration());
                config = new Configuration((Configuration) RposedHelpers.callMethod(resourceManager, "getConfiguration"));
                if (!isDefaultDisplay) {
                    // applyNonDefaultDisplayMetricsToConfigurationLocked(dm, config);
                    RposedHelpers.callMethod(resourceManager, "applyNonDefaultDisplayMetricsToConfigurationLocked", dm, config);
                }
                if (hasOverrideConfig) {
                    //config.updateFrom(key.mOverrideConfiguration);
                    config.updateFrom((Configuration) RposedHelpers.getObjectField(resourceKey, "mOverrideConfiguration"));
                }
            } else {
                //config = getConfiguration();
                config = (Configuration) RposedHelpers.callMethod(resourceManager, "getConfiguration");
            }


            Resources newResource;
            Class oldResourceClass = oldResource.getClass();
            //the resource object is a instance of android.content.res.MiuiResource in the xiaomi MIUI ROM,
            //we can not create resource instance directly with android.content.res.Resource
            //so try with resource instance class constructor
//            if (oldResourceClass.getName().equalsIgnoreCase(Resources.class.getName())) {
//                //r = new Resources(assets, dm, config, compatInfo, token);
//                newResource = (Resources) RposedHelpers.newInstance(Resources.class,
//                        assets, dm, config,
//                        RposedHelpers.getObjectField(oldResource, "mCompatibilityInfo"),
//                        RposedHelpers.getObjectField(oldResource, "mToken")
//                );
//            } else
            if (RposedHelpers.findFieldIfExists(oldResourceClass, "mCompatibilityInfo") != null
                    && RposedHelpers.findFieldIfExists(oldResourceClass, "mToken") != null
                    && RposedHelpers.findConstructorExactIfExists(oldResourceClass,
                    AssetManager.class, DisplayMetrics.class, Configuration.class,
                    RposedHelpers.findClass("android.content.res.CompatibilityInfo", ClassLoader.getSystemClassLoader()),
                    IBinder.class
            ) != null) {
                // resolve token
                IBinder token = null;
                Object tokenWrapper = RposedHelpers.getObjectField(oldResource, "mToken");
                if (tokenWrapper != null) {
                    if (tokenWrapper instanceof IBinder) {
                        token = (IBinder) tokenWrapper;
                    } else if (tokenWrapper instanceof WeakReference) {
                        tokenWrapper = ((WeakReference) tokenWrapper).get();
                        if (tokenWrapper instanceof IBinder) {
                            token = (IBinder) tokenWrapper;
                        }
                    }
                }
                //android.content.res.NubiaResources(android.content.res.AssetManager,android.util.DisplayMetrics,android.content.res.Configuration,android.content.res.CompatibilityInfo,android.os.IBinder)
                newResource = (Resources) RposedHelpers.newInstance(oldResource.getClass(),
                        new Class[]{
                                AssetManager.class, DisplayMetrics.class, Configuration.class,
                                RposedHelpers.findClass("android.content.res.CompatibilityInfo", ClassLoader.getSystemClassLoader()),
                                IBinder.class},
                        assets, dm, config,
                        RposedHelpers.getObjectField(oldResource, "mCompatibilityInfo"),
                        token);
            } else if (RposedHelpers.findFieldIfExists(oldResourceClass, "mCompatibilityInfo") != null
                    && RposedHelpers.findConstructorExactIfExists(oldResourceClass,
                    AssetManager.class, DisplayMetrics.class, Configuration.class,
                    RposedHelpers.findClass("android.content.res.CompatibilityInfo", ClassLoader.getSystemClassLoader())
            ) != null) {
                newResource = (Resources) RposedHelpers.newInstance(oldResource.getClass(),
                        assets, dm, config,
                        RposedHelpers.getObjectField(oldResource, "mCompatibilityInfo"));
            } else if (RposedHelpers.findConstructorExactIfExists(oldResourceClass,
                    AssetManager.class, DisplayMetrics.class, Configuration.class
            ) != null) {
                newResource = (Resources) RposedHelpers.newInstance(oldResource.getClass(),
                        assets, dm, config);
            } else {
                Log.w(Constants.TAG, "can not recreate resource for resource class type: " + oldResourceClass.getName());
                fixAssetManagerPlan2();
                return;
            }
            reuseResource = newResource;
            mActiveResources.put(resourceKey, new WeakReference<>(newResource));
        }

        if (reuseResource != null) {
            Object mBoundApplication = ActivityThread.mBoundApplication.get(RatelRuntime.mainThread);
            Object loadApk = RposedHelpers.getObjectField(mBoundApplication, "info");
            RposedHelpers.setObjectField(loadApk, "mResources", reuseResource);

            Context contextImpl = RatelRuntime.originContext;
            Context nextContext;
            while ((contextImpl instanceof ContextWrapper) &&
                    (nextContext = ((ContextWrapper) contextImpl).getBaseContext()) != null) {
                contextImpl = nextContext;
            }
            RposedHelpers.setObjectField(contextImpl, "mResources", reuseResource);


            //android.app.Application#attach
            final Resources finalReuseResource = reuseResource;
            RposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new RC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Context contextImpl = (Context) param.args[0];
                    Context nextContext;
                    while ((contextImpl instanceof ContextWrapper) &&
                            (nextContext = ((ContextWrapper) contextImpl).getBaseContext()) != null) {
                        contextImpl = nextContext;
                    }
                    //TODO 这里有问题
                    if (contextImpl.getPackageName().equals(RatelRuntime.nowPackageName)) {
                        RposedHelpers.setObjectField(contextImpl, "mResources", finalReuseResource);
                    }
                }
            });
        }
    }

    private static void fixAssetManagerPlan2() {
        Object mBoundApplication = ActivityThread.mBoundApplication.get(RatelRuntime.mainThread);
        if (mBoundApplication != null) {
            Object loadApk = RposedHelpers.getObjectField(mBoundApplication, "info");
            Resources resources = RposedHelpers.getObjectField(loadApk, "mResources");
            final AssetManager assetManager = resources.getAssets();

            RposedHelpers.findAndHookMethod(AssetManager.class, "isUpToDate", new RC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.thisObject.equals(assetManager)) {
                        //prevent create new loadApk | application
                        param.setResult(true);
                    }
                }
            });
        } else {
            Log.w(Constants.TAG, "can not find mBoundApplication from ActivityThread!!");
        }

    }
}
