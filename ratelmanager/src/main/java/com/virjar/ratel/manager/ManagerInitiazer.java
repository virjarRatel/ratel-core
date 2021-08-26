package com.virjar.ratel.manager;

import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.virjar.ratel.manager.engine.RatelEngineLoader;
import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.repo.RatelModuleRepo;
import com.virjar.ratel.manager.repo.SchedulerTaskRepo;
import com.virjar.ratel.manager.ui.DefaultSharedPreferenceHolder;

import lombok.Getter;

public class ManagerInitiazer {
    private static boolean init = false;

    @Getter
    private static Context sContext = null;

    public static void init(Context context) {
        if (context == null) {
            return;
        }
        sContext = context;
        if (init) {
            return;
        }
        init = true;
        DefaultSharedPreferenceHolder.getInstance(context);

        FlowManager.init(context);
        RatelModuleRepo.scanInstalled(context);
        RatelAppRepo.scanInstalled(context);
        SchedulerTaskRepo.scanInstalled(context);

        SdcardConfigSerializer sdcardConfigSerializer = new SdcardConfigSerializer();
        RatelModuleRepo.addListener(sdcardConfigSerializer);
        RatelAppRepo.addListener(sdcardConfigSerializer);


        //这里fire一次就够
        RatelModuleRepo.fireModuleReload(null);
        // RatelAppRepo.fireRatelAppReload(null);

        AppDaemonTaskManager.init(context);


        RatelEngineLoader.init(context);
    }

    public static void refreshRepository(Context context) {
        RatelModuleRepo.scanInstalled(context);
        RatelAppRepo.scanInstalled(context);

        RatelModuleRepo.fireModuleReload(null);
        RatelAppRepo.fireRatelAppReload(null);
    }
}
