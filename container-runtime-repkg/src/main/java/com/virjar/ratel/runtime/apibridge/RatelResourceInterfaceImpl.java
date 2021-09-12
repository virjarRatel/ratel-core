package com.virjar.ratel.runtime.apibridge;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;

import com.virjar.ratel.api.RatelResourceInterface;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.SDK_VERSION_CODES;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mirror.android.app.ActivityThread;

public class RatelResourceInterfaceImpl implements RatelResourceInterface {

    private static Map<String, Resources> caches = new ConcurrentHashMap<>();

    @Override
    public Resources createResource(String moduleApkPath, ClassLoader moduleClassLoader) {
        Resources resources = caches.get(moduleApkPath);
        if (resources != null) {
            return resources;
        }
        synchronized (RatelResourceInterfaceImpl.class) {
            resources = caches.get(moduleApkPath);
            if (resources != null) {
                return resources;
            }
            caches.put(moduleApkPath, createResourceNoCache(moduleApkPath, moduleClassLoader));
            return caches.get(moduleApkPath);
        }
    }

    private Resources createResourceNoCache(String moduleApkPath, ClassLoader moduleClassLoader) {
        if (Build.VERSION.SDK_INT >= SDK_VERSION_CODES.N) {
            return createResourceAfterN(ActivityThread.mResourcesManager.get(RatelRuntime.mainThread), moduleApkPath, moduleClassLoader);
        } else {
            //android version <= 6.0
            return createResourceBeoreN(ActivityThread.mResourcesManager.get(RatelRuntime.mainThread), moduleApkPath);
        }
    }

    @Override
    public Context createContext(String moduleApkPath, ClassLoader moduleClassLoader, Context baseContext) {
        Resources resource = createResource(moduleApkPath, moduleClassLoader);
        return new ContextWrapper(baseContext) {
            @Override
            public Resources getResources() {
                return resource;
            }

            @Override
            public AssetManager getAssets() {
                return resource.getAssets();
            }

            @Override
            public ClassLoader getClassLoader() {
                if (moduleClassLoader == null) {
                    return super.getClassLoader();
                }
                return moduleClassLoader;
            }
        };
    }

    private static Resources createResourceAfterN(Object resourceManager, String moduleApkPath, ClassLoader moduleClassLoader) {
        if (moduleClassLoader == null) {
            moduleClassLoader = RatelToolKit.hostClassLoader;
        }

        //android.content.res.ResourcesKey
        Class<?> resourceKeyClass = RposedHelpers.findClass("android.content.res.ResourcesKey", ClassLoader.getSystemClassLoader());

        //public ResourcesKey(@Nullable String resDir,
        //                        @Nullable String[] splitResDirs,
        //                        @Nullable String[] overlayDirs,
        //                        @Nullable String[] libDirs,
        //                        int displayId,
        //                        @Nullable Configuration overrideConfig,
        //                        @Nullable CompatibilityInfo compatInfo) {
        Object resourceKey = RposedHelpers.newInstance(resourceKeyClass,
                moduleApkPath, null, null, null,
                //displayId,
                Display.DEFAULT_DISPLAY,
                null, null
        );
        Object resourcesImpl = null;
        if(Build.VERSION.SDK_INT >= 30){
            Object mApkAssetsSupplier = RposedHelpers.callMethod(resourceManager, "createApkAssetsSupplierNotLocked", resourceKey);
            resourcesImpl = RposedHelpers.callMethod(resourceManager, "createResourcesImpl", resourceKey,mApkAssetsSupplier);
        }else{
            resourcesImpl = RposedHelpers.callMethod(resourceManager, "createResourcesImpl", resourceKey);
        }

        Resources resources = (Resources) RposedHelpers.newInstance(Resources.class, moduleClassLoader);
        RposedHelpers.callMethod(resources, "setImpl", resourcesImpl);
        return resources;
    }

    private static Resources createResourceBeoreN(Object resourceManager, String moduleApkPath) {
        AssetManager assets = (AssetManager) RposedHelpers.newInstance(AssetManager.class);
        RposedHelpers.callMethod(assets, "addAssetPath", moduleApkPath);

        Resources oldResource = RatelToolKit.sContext.getResources();

        Resources newResource;

        DisplayMetrics dm = oldResource.getDisplayMetrics();//(DisplayMetrics) RposedHelpers.callMethod(resourceManager, "getDisplayMetricsLocked", displayId);

        Configuration config = (Configuration) RposedHelpers.callMethod(resourceManager, "getConfiguration");


        Class oldResourceClass = oldResource.getClass();
        //the resource object is a instance of android.content.res.MiuiResource in the xiaomi MIUI ROM,
        //we can not create resource instance directly with android.content.res.Resource
        //so try with resource instance class constructor
        if (oldResourceClass.getName().equalsIgnoreCase(Resources.class.getName())) {
            //r = new Resources(assets, dm, config, compatInfo, token);
            newResource = (Resources) RposedHelpers.newInstance(Resources.class,
                    assets, dm, config,
                    RposedHelpers.getObjectField(oldResource, "mCompatibilityInfo"),
                    RposedHelpers.getObjectField(oldResource, "mToken")
            );
        } else if (RposedHelpers.findFieldIfExists(oldResourceClass, "mCompatibilityInfo") != null
                && RposedHelpers.findFieldIfExists(oldResourceClass, "mToken") != null
                && RposedHelpers.findConstructorExactIfExists(oldResourceClass,
                AssetManager.class, DisplayMetrics.class, Configuration.class,
                RposedHelpers.findClass("android.content.res.CompatibilityInfo", ClassLoader.getSystemClassLoader()),
                IBinder.class
        ) != null) {
            newResource = (Resources) RposedHelpers.newInstance(oldResource.getClass(),
                    assets, dm, config,
                    RposedHelpers.getObjectField(oldResource, "mCompatibilityInfo"),
                    RposedHelpers.getObjectField(oldResource, "mToken"));
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
            return null;
        }
        return newResource;
    }
}
