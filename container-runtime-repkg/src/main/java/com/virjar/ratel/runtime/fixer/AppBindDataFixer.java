package com.virjar.ratel.runtime.fixer;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.ProviderInfo;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import mirror.android.ddm.DdmHandleAppName;

public class AppBindDataFixer {

    public static void fixAppBindData() {
        Object mBoundApplication = RposedHelpers.getObjectField(RatelRuntime.mainThread, "mBoundApplication");

        fixProcessName(mBoundApplication);

        fixLoadApk(mBoundApplication);

        fixApplicationInfo(RposedHelpers.getObjectField(mBoundApplication, "appInfo"));

        fixProviderInfo(mBoundApplication);
    }

    private static void fixProviderInfo(Object appBindData) {
        //providers
        List<ProviderInfo> providers = RposedHelpers.getObjectField(appBindData, "providers");
        for (ProviderInfo providerInfo : providers) {
            fixOneProviderInfo(providerInfo);
        }
    }

    public static void fixOneProviderInfo(ProviderInfo providerInfo) {
        if (providerInfo.authority.endsWith(RatelRuntime.sufferKey)) {
            //有一个点号分割
            providerInfo.authority = providerInfo.authority.substring(0, providerInfo.authority.indexOf(RatelRuntime.sufferKey) - 1);
        }
        fixObjectField(providerInfo, providerInfo.getClass());
    }

    private static void fixObjectField(Object object, Class clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isSynthetic()) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (field.getType() == String.class) {
                String fieldValue = RposedHelpers.getObjectField(object, field.getName());
                if (fieldValue == null) {
                    continue;
                }
                RposedHelpers.setObjectField(object, field.getName(), fieldValue.replace(RatelRuntime.nowPackageName, RatelRuntime.originPackageName));
            } else if (field.getType() == File.class) {
                File fieldValue = RposedHelpers.getObjectField(object, field.getName());
                if (fieldValue == null) {
                    continue;
                }
                if (fieldValue.getAbsolutePath().contains(RatelRuntime.nowPackageName)) {
                    File newFile = new File(fieldValue.getAbsolutePath().replace(RatelRuntime.nowPackageName, RatelRuntime.originPackageName));
                    RposedHelpers.setObjectField(object, field.getName(), newFile);
                }
            } else if (PackageItemInfo.class.isAssignableFrom(field.getType())) {
                Object objectField = RposedHelpers.getObjectField(object, field.getName());
                fixObjectField(objectField, objectField.getClass());
            }
        }
        Class superclass = clazz.getSuperclass();
        if (superclass != null && superclass.getClassLoader() == Application.class.getClassLoader()) {
            fixObjectField(object, superclass);
        }
    }

    public static void fixApplicationInfo(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            return;
        }

        fixObjectField(applicationInfo, applicationInfo.getClass());

        if (!TextUtils.isEmpty(RatelRuntime.originApplicationClassName)) {
            applicationInfo.className = RatelRuntime.originApplicationClassName;
        } else {
            //the className value can not be com.virjar.zelda.engine.ZeldaApplication
            applicationInfo.className = "android.app.Application";
        }

    }

    private static void fixLoadApk(Object appBindData) {
        Object loadApk = RposedHelpers.getObjectField(appBindData, "info");
        RatelRuntime.theLoadApk = loadApk;
        fixObjectField(loadApk, loadApk.getClass());
        fixActivityThreadLoadApkCache();
    }

    private static void fixActivityThreadLoadApkCache() {
        //这个比较重要，否则会重新在底层创建classLoader
        ArrayMap<String, WeakReference<Object>> mPackages = RposedHelpers.getObjectField(RatelRuntime.mainThread, "mPackages");
        WeakReference<Object> objectWeakReference = mPackages.get(RatelRuntime.nowPackageName);
        if (objectWeakReference != null) {
            mPackages.put(RatelRuntime.originPackageName, objectWeakReference);
        }

        ArrayMap<String, WeakReference<Object>> mResourcePackages = RposedHelpers.getObjectField(RatelRuntime.mainThread, "mResourcePackages");
        objectWeakReference = mResourcePackages.get(RatelRuntime.nowPackageName);
        if (objectWeakReference != null) {
            mPackages.put(RatelRuntime.originPackageName, objectWeakReference);
        }
    }


    private static void fixProcessName(Object appBindData) {
        RatelRuntime.processName = RposedHelpers.getObjectField(appBindData, "processName");
        RatelRuntime.processName = RatelRuntime.processName.replace(RatelRuntime.nowPackageName, RatelRuntime.originPackageName);
        RposedHelpers.setObjectField(appBindData, "processName", RatelRuntime.processName);
        DdmHandleAppName.setAppName.call(RatelRuntime.processName, 0);
    }

}
