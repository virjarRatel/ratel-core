package com.virjar.ratel.manager.scheduler;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;
import android.util.LruCache;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.scheduler.RatelTask;
import com.virjar.ratel.manager.ManagerInitiazer;
import com.virjar.ratel.manager.bridge.IRatelRemoteControlHandler;
import com.virjar.ratel.manager.component.AppWatchDogService;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.model.RatelModule;
import com.virjar.ratel.manager.model.SchedulerTask;
import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.repo.RatelModuleRepo;
import com.virjar.ratel.manager.repo.SchedulerTaskRepo;
import com.virjar.ratel.manager.util.NavUtil;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.PathClassLoader;

public class TaskSchedulerService {
    public static void schedule() {
        Collection<SchedulerTask> schedulerTasks = SchedulerTaskRepo.taskSnapshot();
        for (SchedulerTask schedulerTask : schedulerTasks) {
            try {
                Log.i(Constants.SCHEDULER_TAG, "begin of scheduler task for :" + schedulerTask.getTaskId());
                handleOneTask(schedulerTask);
            } catch (Exception e) {
                Log.e(Constants.SCHEDULER_TAG, "handle task:" + schedulerTask.getTaskId() + " failed", e);
            }
        }
    }

    private static void handleOneTask(final SchedulerTask schedulerTask) {


        //首先确定任务是否需要执行
        if (!needSchedule(schedulerTask)) {
            Log.i(Constants.SCHEDULER_TAG, "task can not be schedule");
            return;
        }
        Log.i(Constants.SCHEDULER_TAG, "begin of scheduler task: " + schedulerTask.getTaskId());


        new Thread("scheduler-" + schedulerTask.getTaskImplementationClassName()) {
            @Override
            public void run() {
                if (schedulerTask.getTaskStatus() == SchedulerTask.TASK_STATUS_RUNNING
                        && AppWatchDogService.queryRemoteHandler(schedulerTask.getTargetAppPackage()) == null) {
                    Log.w(Constants.SCHEDULER_TAG, "app " + schedulerTask.getTargetAppPackage() + "not running ,but the task is now running,sleep to wait apk startup");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (AppWatchDogService.queryRemoteHandler(schedulerTask.getTargetAppPackage()) != null) {
                        //这个时候，可能是上一个任务正在开始执行，先去取消调度
                        Log.i(Constants.SCHEDULER_TAG, "app is running ,maybe another ratel task is running ");
                        return;
                    }
                }

                Log.i(Constants.SCHEDULER_TAG, "create ratelTask instance");
                //创建任务对象
                final RatelTask ratelTask = createRatelTask(schedulerTask);
                if (ratelTask == null) {
                    Log.e(Constants.SCHEDULER_TAG, "create ratel task failed for task: " + schedulerTask.getTaskId());
                    return;
                }
                try {
                    schedulerTask.setTaskStatus(SchedulerTask.TASK_STATUS_RUNNING);
                    schedulerTask.setLastExecute(new Date().getTime());
                    schedulerTask.update();
                    callRatelTaskSchedule(ratelTask, schedulerTask);
                } catch (Throwable throwable) {
                    Log.e(Constants.SCHEDULER_TAG, "ratelTask execute error", throwable);
                }
            }
        }.start();


    }

    private static void callRatelTaskSchedule(RatelTask ratelTask, SchedulerTask schedulerTask) {
        RatelApp ratelApp = RatelAppRepo.findByPackage(schedulerTask.getTargetAppPackage());
        if (ratelApp == null) {
            Log.e(Constants.SCHEDULER_TAG, "can not find ratel App:" + schedulerTask.getTargetAppPackage());
            return;
        }
        //cancel daemon for target app
        if (ratelApp.isDaemon()) {
            ratelApp.setDaemon(false);
            //取消app的自动守护
            ratelApp.update();
        }


        //kill target app
        IRatelRemoteControlHandler iRatelRemoteControlHandler = AppWatchDogService.queryRemoteHandler(ratelApp.getPackageName());
        if (iRatelRemoteControlHandler != null && schedulerTask.isRestartApp()) {
            makeSureAppStop(ratelApp, iRatelRemoteControlHandler);
        }

        Map<String, String> taskParams = ratelTask.loadTaskParams();
        if (taskParams == null) {
            taskParams = new HashMap<>();
        }
        taskParams.put("ratel.taskImplementationClassName", schedulerTask.getTaskImplementationClassName());
        taskParams.put("ratel.ratelTaskId", schedulerTask.getTaskId());
        taskParams.put("ratel.modulePackageName", schedulerTask.getModuleApkPackage());


        if (!schedulerTask.isRestartApp() && AppWatchDogService.queryRemoteHandler(ratelApp.getPackageName()) != null) {
            //            //任务不需要重启，并且apk当前正在运行
            IRatelRemoteControlHandler remoteControlHandler = AppWatchDogService.queryRemoteHandler(ratelApp.getPackageName());
            if (remoteControlHandler != null) {
                try {
                    remoteControlHandler.callSchedulerTask(taskParams);
                    return;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    makeSureAppStop(ratelApp, remoteControlHandler);
                }
            }
        }
        loadedParams.put(schedulerTask.getTargetAppPackage(), taskParams);

        //start up target app,and we will consume task param after apk startup again
        NavUtil.startApp(ManagerInitiazer.getSContext(), schedulerTask.getTargetAppPackage());


    }

    private static void makeSureAppStop(RatelApp ratelApp, IRatelRemoteControlHandler remoteControlHandler) {
        try {
            Log.i(Constants.SCHEDULER_TAG, "prepare to  kill target app:" + ratelApp.getPackageName());
            final Object lock = new Object();
            AppWatchDogService.addRemoteControlHandlerStatusListener(ratelApp.getPackageName(), new AppWatchDogService.RemoteControlHandlerStatusChangeEvent() {
                @Override
                public void onRemoteHandlerStatusChange(String appPackage, IRatelRemoteControlHandler iRatelRemoteControlHandler) {
                    AppWatchDogService.removeRemoteControlHandlerListener(appPackage, this);
                    if (iRatelRemoteControlHandler != null) {
                        return;
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });
            remoteControlHandler.killMe();

            synchronized (lock) {
                try {
                    //杀死等待20s
                    lock.wait(1000 * 20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> consumeSchedulerTask(String mPackage) {
        return loadedParams.remove(mPackage);
    }

    private static RatelTask createRatelTask(SchedulerTask schedulerTask) {
        //加载class
        String moduleApkPackage = schedulerTask.getModuleApkPackage();
        String taskImplementationClassName = schedulerTask.getTaskImplementationClassName();

        PackageInfo packageInfo;

        try {
            packageInfo = ManagerInitiazer.getSContext().getPackageManager().getPackageInfo(moduleApkPackage, PackageManager.GET_META_DATA);
            if (schedulerTask.getCodeUpdate() != null && packageInfo.lastUpdateTime > schedulerTask.getCodeUpdate()) {
                rateltaskCache.remove(schedulerTask.getTaskId());
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(Constants.SCHEDULER_TAG, "package not installed", e);
            return null;
        }

        //hint cache
        RatelTask ratelTask = rateltaskCache.get(schedulerTask.getTaskId());
        if (ratelTask != null) {
            return ratelTask;
        }

        try {
            ClassLoader taskClassLoader = new PathClassLoader(packageInfo.applicationInfo.sourceDir, packageInfo.applicationInfo.nativeLibraryDir, TaskSchedulerService.class.getClassLoader());
            @SuppressWarnings("unchecked") Class<? extends RatelTask> taskImplementationClass = (Class<? extends RatelTask>) taskClassLoader.loadClass(taskImplementationClassName);
            ratelTask = taskImplementationClass.newInstance();
            schedulerTask.setCodeUpdate(packageInfo.lastUpdateTime);
            rateltaskCache.put(schedulerTask.getTaskId(), ratelTask);
            return ratelTask;
        } catch (Throwable throwable) {
            Log.w(Constants.SCHEDULER_TAG, "create ratelTask failed", throwable);
            return null;
        }
    }


    private static Map<String, RatelTask> rateltaskCache = new ConcurrentHashMap<>();

    private static boolean needSchedule(SchedulerTask schedulerTask) {

        RatelApp ratelApp = RatelAppRepo.findByPackage(schedulerTask.getTargetAppPackage());
        if (ratelApp == null) {
            //对应apk没有被ratel感染
            Log.w(Constants.SCHEDULER_TAG, "the app:{" + schedulerTask.getTargetAppPackage() + "} not infected by ratel ");
            return false;
        }

        RatelModule ratelModule = RatelModuleRepo.findByPackage(schedulerTask.getModuleApkPackage());
        if (ratelModule == null) {
            Log.e(Constants.SCHEDULER_TAG, "the module:" + schedulerTask.getModuleApkPackage() + " not existed");
            return false;
        }
        if (!ratelModule.isEnable()) {
            Log.w(Constants.SCHEDULER_TAG, "the module:" + schedulerTask.getModuleApkPackage() + " disabled");
            return false;
        }

        if (ratelApp.getEngineVersionCode() <= 16) {
            //当前app的引擎版本过低，不支持任务调度
            Log.w(Constants.SCHEDULER_TAG, "please upgrade ratel engine version,now(" + ratelApp.getEngineVersionName() + ") version not support scheduler task");
            return false;
        }

        if (schedulerTask.getTaskStatus() == SchedulerTask.TASK_STATUS_RUNNING) {
            long lastExecute = 0;
            if (schedulerTask.getLastExecute() != null) {
                lastExecute = schedulerTask.getLastExecute();
            }

            int maxDuration = 0;
            if (schedulerTask.getMaxDuration() != null) {
                maxDuration = schedulerTask.getMaxDuration();
            }


            if (lastExecute + maxDuration * 1000L < new Date().getTime()) {
                return true;
            }

            // 任务正在执行
            return AppWatchDogService.queryRemoteHandler(schedulerTask.getTargetAppPackage()) == null;
        }

        if (schedulerTask.getLastExecute() == null) {
            return true;
        }

        String cronExpression = schedulerTask.getCronExpression();
        CronSequenceGenerator cronSequenceGenerator = cronGeneratorCache.get(cronExpression);
        if (cronSequenceGenerator == null) {
            Log.e(Constants.SCHEDULER_TAG, "CronExpression Error,remove task:" + schedulerTask.getTaskId());
            schedulerTask.delete();
            return false;
        }

        return cronSequenceGenerator.next(new Date(schedulerTask.getLastExecute())).before(new Date());
    }

    private static Map<String, Map<String, String>> loadedParams = new ConcurrentHashMap<>();

    private static LruCache<String, CronSequenceGenerator> cronGeneratorCache = new LruCache<String, CronSequenceGenerator>(64) {
        @Override
        protected CronSequenceGenerator create(String key) {
            try {
                return new CronSequenceGenerator(key);
            } catch (Exception e) {
                //print error if cron expression not illegal
                Log.e(Constants.SCHEDULER_TAG, "parse cron expression failed", e);
                return null;
            }
        }
    };
}
