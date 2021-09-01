package com.virjar.ratel.manager.repo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.manager.model.RatelApp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import external.org.apache.commons.lang3.NumberUtils;

public class RatelAppRepo {
    private static Map<String, RatelApp> ratelAppMap = new HashMap<>();
    private static final List<RatelAppListener> sListeners = new CopyOnWriteArrayList<>();

    static {
        init();
    }

    public interface RatelAppListener {
        void onRatelAppReload(String packageName);
    }

    private static void init() {
        List<RatelApp> ratelApps = SQLite.select().from(RatelApp.class).queryList();
        for (RatelApp ratelApp : ratelApps) {
            ratelAppMap.put(ratelApp.getPackageName(), ratelApp);
        }
    }

    public static void scanInstalled(Context context) {
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
        Set<String> loadedApps = new HashSet<>();
        for (PackageInfo info : installedPackages) {
            RatelApp ratelApp = reloadSingleRatelApp(context, info.packageName);
            if (ratelApp != null) {
                loadedApps.add(ratelApp.getPackageName());
            }
        }

        //remove uninstalled

        Set<String> set = new HashSet<>(ratelAppMap.keySet());

        set.removeAll(loadedApps);

        for (String needRemove : set) {
            removeRatelApp(needRemove);
        }
    }

    public static RatelApp reloadSingleRatelApp(Context context, String packageName) {

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            removeRatelApp(packageName);
            return null;
        }

        RatelApp ratelApp = findByPackage(packageInfo.packageName);

        String apkPathDir = packageInfo.applicationInfo.sourceDir;
        try (ZipFile apkZip = new ZipFile(apkPathDir)) {
            ZipEntry entry = apkZip.getEntry("assets/ratel_serialNo.txt");
            if (entry == null) {
                //normal apk
                return null;
            }


//            InputStream serialInputStream = apkZip.getInputStream(entry);
//            Properties properties = new Properties();
//            properties.load(serialInputStream);
//            serialInputStream.close();

            if (ratelApp == null) {
                ratelApp = new RatelApp();
            }

            ratelApp.setPackageName(packageInfo.applicationInfo.packageName);
            ratelApp.setVersionName(packageInfo.versionName);
            ratelApp.setVersionCode(packageInfo.versionCode);
            ratelApp.setAppName(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());


            entry = apkZip.getEntry("assets/ratelConfig.properties");
            Properties properties = new Properties();
            if (entry != null) {
                //很早期的ratel没有这个文件
                InputStream inputStream = apkZip.getInputStream(entry);
                properties.load(inputStream);
                inputStream.close();
            } else {
                Log.i(Constants.TAG, "很早期的ratel apk: " + packageName);
            }

            ratelApp.setEngineVersionName(properties.getProperty("ratel_engine_versionName"));
            ratelApp.setEngineVersionCode(NumberUtils.toInt(properties.getProperty("ratel_engine_versionCode"), 0));
            ratelApp.setBuildTimeStamp(NumberUtils.toLong(properties.getProperty("ratel_buildTimestamp"), 0));

            addRatelApp(ratelApp);
            return ratelApp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<RatelApp> installedApps() {
        return new ArrayList<>(ratelAppMap.values());
    }

    private static void removeRatelApp(String packageName) {
        RatelApp ratelApp = ratelAppMap.remove(packageName);
        if (ratelApp == null) {
            return;
        }
        ratelApp.delete();
        fireRatelAppReload(packageName);

    }

    public static RatelApp findByPackage(String packageName) {
        return ratelAppMap.get(packageName);
    }

    public static void addRatelApp(RatelApp ratelApp) {
        RatelApp ratelAppOld = ratelAppMap.get(ratelApp.getPackageName());
        if (ratelAppOld != null) {
            ratelApp.update();
        } else {
            ratelApp.save();
        }
        ratelAppMap.put(ratelApp.getPackageName(), ratelApp);
        fireRatelAppReload(ratelApp.getPackageName());
    }


    public static void addListener(RatelAppListener ratelAppListener) {
        if (!sListeners.contains(ratelAppListener)) {
            sListeners.add(ratelAppListener);
        }
    }

    public static void removeListener(RatelAppListener ratelAppListener) {
        sListeners.remove(ratelAppListener);
    }

    public static void fireRatelAppReload(String packageName) {
        for (RatelAppListener ratelAppListener : sListeners) {
            ratelAppListener.onRatelAppReload(packageName);
        }
    }
}
