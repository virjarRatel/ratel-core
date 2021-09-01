package com.virjar.ratel.manager.repo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.manager.ManagerInitiazer;
import com.virjar.ratel.manager.model.RatelModule;
import com.virjar.ratel.manager.model.SchedulerTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SchedulerTaskRepo {
    private static Map<String, SchedulerTask> schedulerTaskMap = new ConcurrentHashMap<>();

    static {
        init();
    }

    public static Collection<SchedulerTask> taskSnapshot() {
        // we will copy value from iterator with ArrayList constructor
        return new ArrayList<>(schedulerTaskMap.values());
    }

    private static void init() {
        List<SchedulerTask> schedulerTasks = SQLite.select().from(SchedulerTask.class).queryList();
        for (SchedulerTask schedulerTask : schedulerTasks) {
            schedulerTaskMap.put(schedulerTask.getTaskId(), schedulerTask);
        }

        RatelModuleRepo.addListener(new RatelModuleRepo.RatelModuleListener() {
            @Override
            public void onRatelModuleReload(String packageName) {
                //remove
                //TODO 需要remove diff data
//                for (SchedulerTask schedulerTask : schedulerTaskMap.values()) {
//                    if (schedulerTask.getModuleApkPackage().equals(packageName)) {
//                        removeSchedulerTask(schedulerTask.getTaskId());
//                    }
//                }

                RatelModule ratelModule = RatelModuleRepo.findByPackage(packageName);
                if (ratelModule == null || ratelModule.getForAppPackage() == null || ratelModule.getForAppPackage().size() != 1) {
                } else {
                    PackageInfo packageInfo;
                    try {
                        packageInfo = ManagerInitiazer.getSContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        List<SchedulerTask> schedulerTasks = parseApk(new File(packageInfo.applicationInfo.sourceDir), packageInfo);
                        for (SchedulerTask schedulerTask : schedulerTasks) {
                            addSchedulerTask(schedulerTask);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void scanInstalled(Context context) {
        Map<String, SchedulerTask> loadedTasks = new HashMap<>();
        List<RatelModule> ratelModules = RatelModuleRepo.installedModules();
        for (RatelModule ratelModule : ratelModules) {
            if (ratelModule.getForAppPackage() == null || ratelModule.getForAppPackage().size() != 1) {
                continue;
            }

            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(ratelModule.getPackageName(), PackageManager.GET_META_DATA);
                File baseApkFile = new File(packageInfo.applicationInfo.sourceDir);
                List<SchedulerTask> schedulerTasks = parseApk(baseApkFile, packageInfo);
                for (SchedulerTask schedulerTask : schedulerTasks) {
                    loadedTasks.put(schedulerTask.getTaskId(), schedulerTask);
                }
            } catch (PackageManager.NameNotFoundException | IOException e) {
                //ignore
            } catch (Exception e) {
                Log.e(Constants.SCHEDULER_TAG, "error load scheduler task config");
            }
        }

        HashSet<String> needRemove = new HashSet<>(schedulerTaskMap.keySet());
        needRemove.removeAll(loadedTasks.keySet());

        for (SchedulerTask schedulerTask : loadedTasks.values()) {
            schedulerTaskMap.put(schedulerTask.getTaskId(), schedulerTask);
        }

        for (String taskId : needRemove) {
            removeSchedulerTask(taskId);
        }
    }


    public static void removeSchedulerTask(String taskId) {
        SchedulerTask schedulerTask = queryById(taskId);

        if (schedulerTask == null) {
            return;
        }
        schedulerTaskMap.remove(taskId);
        schedulerTask.delete();
    }

    public static SchedulerTask queryById(String taskId) {
        return schedulerTaskMap.get(taskId);
    }

    public static void addSchedulerTask(SchedulerTask schedulerTask) {
        schedulerTask.save();
        schedulerTaskMap.put(schedulerTask.getTaskId(), schedulerTask);
    }

    private static List<SchedulerTask> parseApk(File baseApkFile, PackageInfo packageInfo) throws IOException {
        try (ZipFile zipFile = new ZipFile(baseApkFile)) {
            ZipEntry entry = zipFile.getEntry(Constants.ratelSchedulerFile);
            if (entry == null) {
                return Collections.emptyList();
            }
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                if (inputStream == null) {
                    return Collections.emptyList();
                }

                List<String> lines = new LinkedList<>();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String buf;
                while ((buf = bufferedReader.readLine()) != null) {
                    buf = buf.trim();
                    //we support comment in the json config file
                    if (buf.startsWith("//")) {
                        continue;
                    }
                    lines.add(buf);
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (String str : lines) {
                    stringBuilder.append(str);
                }

                String string = stringBuilder.toString();

                List<SchedulerTask> ret = new ArrayList<>();
                if (string.startsWith("[")) {
                    JSONArray jsonArray = JSONArray.parseArray(string);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        ret.add(jsonArray.getJSONObject(i).toJavaObject(SchedulerTask.class));
                    }
                } else {
                    ret.add(JSON.parseObject(string).toJavaObject(SchedulerTask.class));
                }

                for (SchedulerTask schedulerTask : ret) {
                    schedulerTask.setModuleApkPackage(packageInfo.applicationInfo.packageName);
                    ensureTargetAppPackage(schedulerTask, packageInfo);
                    fillSchedulerMeta(schedulerTask);
                }

                return ret;
            }
        }
    }

    private static void fillSchedulerMeta(SchedulerTask schedulerTask) {
        schedulerTask.setTaskId(schedulerTask.getTargetAppPackage() + "#" + schedulerTask.getTaskImplementationClassName() + "#" + schedulerTask.getModuleApkPackage());
        SchedulerTask oldTask = queryById(schedulerTask.getTaskId());
        if (oldTask != null) {
            schedulerTask.setLastExecute(oldTask.getLastExecute());
            schedulerTask.setTaskStatus(oldTask.getTaskStatus());
            schedulerTask.setCodeUpdate(oldTask.getCodeUpdate());
        }
    }

    private static void ensureTargetAppPackage(SchedulerTask schedulerTask, PackageInfo packageInfo) {
        if (!TextUtils.isEmpty(schedulerTask.getTargetAppPackage())) {
            return;
        }
        Set<String> appPackage = RatelModuleRepo.findByPackage(packageInfo.packageName).getForAppPackage();
        if (appPackage.size() == 1) {
            schedulerTask.setTargetAppPackage(appPackage.iterator().next());
        }

    }

}
