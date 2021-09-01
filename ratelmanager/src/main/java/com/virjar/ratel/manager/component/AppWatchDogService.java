package com.virjar.ratel.manager.component;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.manager.AppDaemonTaskManager;
import com.virjar.ratel.manager.BuildConfig;
import com.virjar.ratel.manager.ManagerInitiazer;
import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.bridge.ClientInfo;
import com.virjar.ratel.manager.bridge.FingerData;
import com.virjar.ratel.manager.bridge.IRatelManagerClientRegister;
import com.virjar.ratel.manager.bridge.IRatelRemoteControlHandler;
import com.virjar.ratel.manager.model.FakeSignature;
import com.virjar.ratel.manager.model.FakeSignature_Table;
import com.virjar.ratel.manager.scheduler.TaskSchedulerService;
import com.virjar.ratel.manager.ui.DefaultSharedPreferenceHolder;
import com.virjar.ratel.manager.ui.WelcomeActivity;
import com.virjar.ratel.manager.util.NavUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class AppWatchDogService extends Service {
    public static boolean hasServiceStarted = false;
    private static ConcurrentHashMap<Integer, RemoteHandlerHolder> remoteHandlers = new ConcurrentHashMap<>();

    private class RemoteHandlerHolder implements IBinder.DeathRecipient {
        private IBinder iBinder;
        @Getter
        private IRatelRemoteControlHandler iRatelRemoteControlHandler;
        @Getter
        private ClientInfo clientInfo;

        RemoteHandlerHolder(IBinder iBinder, IRatelRemoteControlHandler iRatelRemoteControlHandler) {
            this.iBinder = iBinder;
            this.iRatelRemoteControlHandler = iRatelRemoteControlHandler;
        }

        void setClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
        }

        @Override
        public void binderDied() {
            if (clientInfo != null) {
                //第一次可能失败，但是clientInfo本身不包含IPC
                Log.w(RatelManagerApp.TAG, "the client: " + clientInfo.getProcessName() + " has died");
                remoteHandlers.remove(clientInfo.getPid());

                if (clientInfo.getProcessName().equals(clientInfo.getPackageName())) {
                    //这里只操作主进程
                    Set<RemoteControlHandlerStatusChangeEvent> remoteControlHandlerStatusChangeEventSets = AppWatchDogService.remoteControlHandlerStatusChangeEvents.get(clientInfo.getPackageName());
                    if (remoteControlHandlerStatusChangeEventSets != null) {
                        for (RemoteControlHandlerStatusChangeEvent remoteControlHandlerStatusChangeEvent : remoteControlHandlerStatusChangeEventSets) {
                            remoteControlHandlerStatusChangeEvent.onRemoteHandlerStatusChange(clientInfo.getPackageName(), null);
                        }
                    }

                }

                if (AppDaemonTaskManager.needWatch(clientInfo.getPackageName())) {
                    NavUtil.startApp(AppWatchDogService.this, clientInfo.getPackageName());
                }
            }
            iBinder.unlinkToDeath(this, 0);
        }
    }


    private IRatelManagerClientRegister.Stub binder = new IRatelManagerClientRegister.Stub() {
        @Override
        public void registerRemoteControlHandler(IRatelRemoteControlHandler ratelRemoteControlHandler) throws RemoteException {

            final IBinder iBinder = ratelRemoteControlHandler.asBinder();

            RemoteHandlerHolder deathRecipient = new RemoteHandlerHolder(iBinder, ratelRemoteControlHandler);
            try {
                iBinder.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            ClientInfo clientInfo = ratelRemoteControlHandler.getClientInfo();
            deathRecipient.setClientInfo(clientInfo);

            Log.i(RatelManagerApp.TAG, "register a remote control handler from: " + clientInfo.getProcessName());
            remoteHandlers.put(clientInfo.getPid(), deathRecipient);
            Set<RemoteControlHandlerStatusChangeEvent> remoteControlHandlerStatusChangeEventSets = AppWatchDogService.remoteControlHandlerStatusChangeEvents.get(clientInfo.getPackageName());
            if (remoteControlHandlerStatusChangeEventSets != null) {
                for (RemoteControlHandlerStatusChangeEvent remoteControlHandlerStatusChangeEvent : remoteControlHandlerStatusChangeEventSets) {
                    remoteControlHandlerStatusChangeEvent.onRemoteHandlerStatusChange(clientInfo.getPackageName(), ratelRemoteControlHandler);
                }
            }
        }

        @Override
        public Map queryProperties(String mPackage) throws RemoteException {
            Map<String, String> ret = new HashMap<>();

            DefaultSharedPreferenceHolder defaultSharedPreferenceHolder = DefaultSharedPreferenceHolder.getInstance(AppWatchDogService.this);
            String phoneIdentifier = defaultSharedPreferenceHolder.getPreferences().getString("phone_identifier", "");
            if (phoneIdentifier != null) {
                ret.put("phone_identifier", phoneIdentifier);
            }
            return ret;
        }

        @Override
        public FingerData getFingerData() throws RemoteException {
            return FingerDataLoader.getFingerData();
        }

        @Override
        public void saveMSignature(String mPackage, String mSignature) throws RemoteException {
            FakeSignature fakeSignature = SQLite.select().from(FakeSignature.class)
                    .where(FakeSignature_Table.packageName.eq(mPackage)).querySingle();
            if (fakeSignature == null) {
                fakeSignature = new FakeSignature();
            }
            fakeSignature.setPackageName(mPackage);
            fakeSignature.setSignature(mSignature);

            fakeSignature.save();
        }

        @Override
        public void addDelayDeamonTask(String mPackage, long delay) throws RemoteException {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent ratelLaunchIntent = ManagerInitiazer.getSContext().getPackageManager().getLaunchIntentForPackage(Constants.ratelManagerPackage);
                    // notice this is an asynchronized call,we can not wait for ratel manager process started
                    ManagerInitiazer.getSContext().startActivity(ratelLaunchIntent);
                }
            }, delay);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        startService();
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    private void startService() {
        if (hasServiceStarted) {
            return;
        }
        Notification.Builder builder = new Notification.Builder(this);
        Intent nfIntent = new Intent(this, WelcomeActivity.class);
        // 设置PendingIntent
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, FLAG_UPDATE_CURRENT))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("RatelManager")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Ratel Scheduler Task Service")
                .setWhen(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(BuildConfig.APPLICATION_ID);
        }

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(110, notification);

        Timer timer = new Timer("app-watch-task");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    doAppDaemon();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }, 2 * 1000, 20 * 1000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    TaskSchedulerService.schedule();
                } catch (Throwable throwable) {
                    Log.i(Constants.SCHEDULER_TAG, "do schedule task failed", throwable);
                }
            }
        }, 5 * 1000, 60 * 1000);

        hasServiceStarted = true;
    }

    private Set<String> getRunningAppProcess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            List<AndroidAppProcess> runningAppProcesses = AndroidProcesses.getRunningAppProcesses();
            Set<String> ret = new HashSet<>();
            for (AndroidAppProcess androidAppProcess : runningAppProcesses) {
                if (androidAppProcess.name.contains(":")) {
                    //skip sub process,we only watch main process
                    continue;
                }
                ret.add(androidAppProcess.getPackageName());
            }
            return ret;
        }
        Set<String> ret = new HashSet<>();
        for (RemoteHandlerHolder iRatelRemoteControlHandler : remoteHandlers.values()) {
            if (iRatelRemoteControlHandler == null) {
                //这里可能为null
                continue;
            }
            try {
                //TODO 这里可能卡死，如果客户端进程假死，那么可能直接导致这里没有响应，这个问题之前遇到过
                ClientInfo clientInfo = iRatelRemoteControlHandler.iRatelRemoteControlHandler.getClientInfo();
                if (clientInfo.getProcessName().contains(":")) {
                    continue;
                }
                ret.add(clientInfo.getPackageName());
            } catch (RemoteException e) {
                Log.e(RatelManagerApp.TAG, "call remote controller handler failed", e);
            }
        }
        return ret;

    }

    private static boolean isAppForeground(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList =
                activityManager.getRunningAppProcesses();
        if (runningAppProcessInfoList == null) {
            Log.d(RatelManagerApp.TAG, "runningAppProcessInfoList is null!");
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfoList) {
            if (processInfo.processName.equals(context.getPackageName())
                    && (processInfo.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                return true;
            }
        }
        return false;
    }

    private void doAppDaemon() {
        Log.i(RatelManagerApp.TAG, "run app daemon task..");
        Set<String> watchAppTask = AppDaemonTaskManager.getWatchAppTask();
        Set<String> runningAppProcess = getRunningAppProcess();
        List<String> needStart = new ArrayList<>();
        for (String watchApp : watchAppTask) {
            if (runningAppProcess.contains(watchApp)) {
                Log.i("ratel_manager", "app: " + watchApp + " is running");
                continue;
            }
            needStart.add(watchApp);
        }

        if (needStart.size() == 0) {
            return;
        }

        //now consider if we has running on foreground
//        if (!isAppForeground(this)) {
//            Log.i(RatelManagerApp.TAG, "need start brown ratelManager into foreground ");
//            RatelAccessibilityService.requestStartRatelManager();
//        }

        for (String targetPackage : needStart) {
            try {
                Log.i(RatelManagerApp.TAG, "start app: " + targetPackage);
                NavUtil.startApp(this, targetPackage);
            } catch (Exception e) {
                Log.e(RatelManagerApp.TAG, "can not start app");
                e.printStackTrace();
            }
        }
    }

    public static IRatelRemoteControlHandler queryRemoteHandler(String packageName) {
        for (RemoteHandlerHolder iRatelRemoteControlHandler : remoteHandlers.values()) {
            if (iRatelRemoteControlHandler == null) {
                continue;
            }
            if (iRatelRemoteControlHandler.getClientInfo().getProcessName().equals(packageName)) {
                return iRatelRemoteControlHandler.iRatelRemoteControlHandler;
            }
        }
        return null;
    }

    public interface RemoteControlHandlerStatusChangeEvent {
        void onRemoteHandlerStatusChange(String appPackage, IRatelRemoteControlHandler iRatelRemoteControlHandler);
    }

    private static Map<String, Set<RemoteControlHandlerStatusChangeEvent>> remoteControlHandlerStatusChangeEvents = new ConcurrentHashMap<>();

    public static void addRemoteControlHandlerStatusListener(String appPackageName, RemoteControlHandlerStatusChangeEvent remoteControlHandlerStatusChangeEvent) {
        Set<RemoteControlHandlerStatusChangeEvent> remoteControlHandlerStatusChangeEventSet = AppWatchDogService.remoteControlHandlerStatusChangeEvents.get(appPackageName);
        if (remoteControlHandlerStatusChangeEventSet == null) {
            remoteControlHandlerStatusChangeEventSet = new HashSet<>();
            remoteControlHandlerStatusChangeEvents.put(appPackageName, remoteControlHandlerStatusChangeEventSet);
        }

        remoteControlHandlerStatusChangeEventSet.add(remoteControlHandlerStatusChangeEvent);

        IRatelRemoteControlHandler iRatelRemoteControlHandler = queryRemoteHandler(appPackageName);
        remoteControlHandlerStatusChangeEvent.onRemoteHandlerStatusChange(appPackageName, iRatelRemoteControlHandler);
    }

    public static void removeRemoteControlHandlerListener(String appPackageName, RemoteControlHandlerStatusChangeEvent remoteControlHandlerStatusChangeEvent) {
        //remoteControlHandlerStatusChangeEvents.remove(appPackageName);
        Set<RemoteControlHandlerStatusChangeEvent> remoteControlHandlerStatusChangeEventSet = AppWatchDogService.remoteControlHandlerStatusChangeEvents.get(appPackageName);
        if (remoteControlHandlerStatusChangeEventSet == null) {
            return;
        }
        remoteControlHandlerStatusChangeEventSet.remove(remoteControlHandlerStatusChangeEvent);
    }
}
