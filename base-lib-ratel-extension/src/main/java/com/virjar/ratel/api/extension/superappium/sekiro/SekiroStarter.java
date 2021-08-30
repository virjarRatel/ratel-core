package com.virjar.ratel.api.extension.superappium.sekiro;

import android.util.Log;

import com.virjar.ratel.api.extension.superappium.PageTriggerManager;
import com.virjar.ratel.api.extension.superappium.SuperAppium;
import com.virjar.sekiro.api.SekiroClient;

import java.util.UUID;

/**
 * @deprecated 已过期，开源版本的sekiro不再维护，迁移到商业版。同时商业版有一定免费配额，开源版sekiro服务器不保证长久稳定性
 */
@Deprecated
public class SekiroStarter {
    public static String sekiroGroup = "ratel-appium";
    private static final String dumpTopActivity = "dumpActivity";
    private static final String dumpTopFragment = "dumpFragment";
    private static final String screenShot = "screenShot";
    private static final String executeJsOnWebView = "ExecuteJsOnWebView";
    private static final String fileExplore = "fileExplore";

    private static boolean isStarted = false;


    private static SekiroClient defaultSekiroClient = null;

    public static void startService(String host, int port, String token) {
        if (isStarted) {
            return;
        }
        Log.i(SuperAppium.TAG, "start a supperAppium client: " + token);
        PageTriggerManager.getTopFragment("insureComponentStarted");
        defaultSekiroClient = SekiroClient.start(host, port, token, sekiroGroup)
                .registerHandler(dumpTopActivity, new DumpTopActivityHandler())
                .registerHandler(dumpTopFragment, new DumpTopFragmentHandler())
                .registerHandler(screenShot, new ScreenShotHandler())
                .registerHandler(executeJsOnWebView, new ExecuteJsOnWebViewHandler())
                .registerHandler(fileExplore, new FileExploreHandler())
        ;

        isStarted = true;
    }

    public static void startService(String host, int port) {
        startService(host, port, UUID.randomUUID().toString());
    }

    public static SekiroClient getDefaultSekiroClient() {
        return defaultSekiroClient;
    }
}
