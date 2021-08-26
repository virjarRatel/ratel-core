package com.virjar.ratel.manager.repo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.virjar.ratel.manager.model.RatelAppModulesRelation;
import com.virjar.ratel.manager.model.RatelAppModulesRelation_Table;
import com.virjar.ratel.manager.model.RatelModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class RatelModuleRepo {
    private static Map<String, RatelModule> ratelModuleMap = new HashMap<>();
    private static final List<RatelModuleListener> sListeners = new CopyOnWriteArrayList<>();

    static {
        init();
    }

    public interface RatelModuleListener {
        void onRatelModuleReload(String packageName);
    }

    private static void init() {
        List<RatelModule> ratelModules = SQLite.select().from(RatelModule.class).queryList();
        for (RatelModule ratelModule : ratelModules) {
            ratelModuleMap.put(ratelModule.getPackageName(), ratelModule);
        }
    }

    public static void scanInstalled(Context context) {
        List<PackageInfo> installedPackages = context.getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
        Set<String> loadedModules = new HashSet<>();
        for (PackageInfo info : installedPackages) {
            RatelModule ratelModule = reloadSingleModule(context, info.packageName);
            if (ratelModule != null) {
                loadedModules.add(ratelModule.getPackageName());
            }
        }

        //remove uninstalled

        Set<String> set = new HashSet<>(ratelModuleMap.keySet());

        set.removeAll(loadedModules);

        for (String needRemove : set) {
            removeModule(needRemove);
        }
    }

    private static int extractIntPart(String str) {
        int result = 0, length = str.length();
        for (int offset = 0; offset < length; offset++) {
            char c = str.charAt(offset);
            if ('0' <= c && c <= '9')
                result = result * 10 + (c - '0');
            else
                break;
        }
        return result;
    }

    public static RatelModule reloadSingleModule(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            removeModule(packageName);
            return null;
        }
        ApplicationInfo app = packageInfo.applicationInfo;
        RatelModule ratelModule = findByPackage(app.packageName);

        if (app.enabled && app.metaData != null && app.metaData.containsKey("xposedmodule")) {

            if (ratelModule == null) {
                ratelModule = new RatelModule();
            }

            ratelModule.setPackageName(app.packageName);
            ratelModule.setVersionName(packageInfo.versionName);
            ratelModule.setVersionCode(packageInfo.versionCode);

            Object minVersionRaw = app.metaData.get("xposedminversion");
            if (minVersionRaw instanceof Integer) {
                ratelModule.setMinVersion((Integer) minVersionRaw);
            } else if (minVersionRaw instanceof String) {
                ratelModule.setMinVersion(extractIntPart((String) minVersionRaw));
            } else {
                ratelModule.setMinVersion(0);
            }

            Object descriptionRaw = app.metaData.get("xposeddescription");
            String descriptionTmp = null;
            if (descriptionRaw instanceof String) {
                descriptionTmp = ((String) descriptionRaw).trim();
            } else if (descriptionRaw instanceof Integer) {
                try {
                    int resId = (Integer) descriptionRaw;
                    if (resId != 0)
                        descriptionTmp = context.getPackageManager().getResourcesForApplication(app).getString(resId).trim();
                } catch (Exception ignored) {
                }
            }
            ratelModule.setDescription(descriptionTmp != null ? descriptionTmp : "");

            ratelModule.setAppName(app.loadLabel(context.getPackageManager()).toString());


            Object forRatelApps = app.metaData.get("for_ratel_apps");
            if (forRatelApps instanceof String) {
                String[] ratelApps = forRatelApps.toString().split(",");
                Set<String> ratelAppSets = new HashSet<>();
                for (String str : ratelApps) {
                    ratelAppSets.add(str.trim());
                }
                ratelModule.setForAppPackage(ratelAppSets);
            }

            addModule(ratelModule);

            return ratelModule;
        } else {
            removeModule(app.packageName);
            return null;
        }
    }


    public static List<RatelModule> installedModules() {
        return new ArrayList<>(ratelModuleMap.values());
    }

    private static void addModule(RatelModule ratelModule) {
        RatelModule ratelModuleOld = ratelModuleMap.get(ratelModule.getPackageName());
        if (ratelModuleOld != null) {
            ratelModule.update();
        } else {
            ratelModule.save();
        }
        ratelModuleMap.put(ratelModule.getPackageName(), ratelModule);


        Set<String> forAppPackage = ratelModule.getForAppPackage();
        if (forAppPackage == null) {
            forAppPackage = Collections.emptySet();
        }

        //select all relations
        List<RatelAppModulesRelation> ratelAppModulesRelationList = SQLite.select().from(RatelAppModulesRelation.class)
                .where(RatelAppModulesRelation_Table.xposedModulePackageName.eq(ratelModule.getPackageName()))
                .queryList();

        Map<String, RatelAppModulesRelation> ratelAppModulesRelationSet = new HashMap<>();
        for (RatelAppModulesRelation ratelAppModulesRelation : ratelAppModulesRelationList) {
            ratelAppModulesRelationSet.put(ratelAppModulesRelation.getAppPackageName() + ";" + ratelAppModulesRelation.getXposedModulePackageName(), ratelAppModulesRelation);
        }

        for (String appPackage : forAppPackage) {
            RatelAppModulesRelation existedRelation = ratelAppModulesRelationSet.remove(appPackage + ";" + ratelModule.getPackageName());
            if (existedRelation == null) {
                existedRelation = new RatelAppModulesRelation();
                existedRelation.setEnable(true);
            }
            existedRelation.setAppPackageName(appPackage);
            existedRelation.setXposedModulePackageName(ratelModule.getPackageName());
            existedRelation.save();
        }


        //被删除的数据，需要一并删除
        for (RatelAppModulesRelation ratelAppModulesRelation : ratelAppModulesRelationSet.values()) {
            ratelAppModulesRelation.delete();
        }

        fireModuleReload(ratelModule.getPackageName());
    }

    private static void removeModule(String packageName) {
        RatelModule ratelModule = ratelModuleMap.remove(packageName);
        if (ratelModule == null) {
            return;
        }
        ratelModule.delete();
        fireModuleReload(packageName);
    }

    public static RatelModule findByPackage(String packageName) {
        return ratelModuleMap.get(packageName);
    }

    public static void addListener(RatelModuleListener ratelAppListener) {
        if (!sListeners.contains(ratelAppListener)) {
            sListeners.add(ratelAppListener);
        }
    }

    public static void removeListener(RatelModuleListener ratelAppListener) {
        sListeners.remove(ratelAppListener);
    }

    public static void fireModuleReload(String modulePackage) {
        for (RatelModuleListener ratelModuleListener : sListeners) {
            ratelModuleListener.onRatelModuleReload(modulePackage);
        }
    }

}
