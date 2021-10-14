package com.virjar.ratel.manager;

import android.os.Environment;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.model.RatelModule;
import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.repo.RatelModuleRepo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SdcardConfigSerializer implements RatelModuleRepo.RatelModuleListener, RatelAppRepo.RatelAppListener {
    @Override
    public void onRatelModuleReload(String packageName) {
        saveModuleConfig();
    }

    @Override
    public void onRatelAppReload(String packageName) {
        saveModuleConfig();
    }

    private void saveModuleConfig() {
        File ratelConfigRoot = new File(Environment.getExternalStorageDirectory(), Constants.ratelSDCardRoot);
        File globalConfigFile = new File(ratelConfigRoot, Constants.modulePackageGlobal);
        File itemConfigFile = new File(ratelConfigRoot, Constants.modulePackageItem);
        if (!ratelConfigRoot.exists() && !ratelConfigRoot.mkdirs()) {
            Log.w(RatelManagerApp.TAG, "can not create directory: " + ratelConfigRoot.getAbsolutePath());
            return;
        }
        List<RatelModule> ratelModules = RatelModuleRepo.installedModules();
        Set<String> globalModuleConfigs = new HashSet<>();
        Set<String> itemModuleConfigs = new HashSet<>();
        for (RatelModule ratelModule : ratelModules) {
            if (!ratelModule.isEnable()) {
                continue;
            }
            if (ratelModule.getForAppPackage() == null || ratelModule.getForAppPackage().isEmpty()) {
                // a global module
                globalModuleConfigs.add(ratelModule.getPackageName());
            } else {
                // module for special target
                for (String targetAppPackage : ratelModule.getForAppPackage()) {
                    RatelApp ratelApp = RatelAppRepo.findByPackage(targetAppPackage);
                    if (ratelApp != null && !ratelApp.isEnabled()) {
                        continue;
                    }
                    //format(modulePackage:targetPackage)
                    itemModuleConfigs.add(ratelModule.getPackageName() + ":" + targetAppPackage);
                }
            }
        }

        // now save config into sdcard
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(globalConfigFile))) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (String str : globalModuleConfigs) {
                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(RatelManagerApp.TAG, "write config failed", e);
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(itemConfigFile))) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (String str : itemModuleConfigs) {
                bufferedWriter.write(str);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            Log.e(RatelManagerApp.TAG, "write config failed", e);
        }
    }


}
