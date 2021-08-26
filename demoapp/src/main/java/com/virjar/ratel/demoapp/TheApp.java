package com.virjar.ratel.demoapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

public class TheApp extends Application {
    private static final String LogTag = "demoAPPTag";
    private static AtomicInteger onCreateCallTimes = new AtomicInteger(0);


    public TheApp() {
        super();
        Log.i(LogTag, "appConstructor  ");
        // setNowUserId("undefined");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LogTag, "appOnCreate ");
        if (onCreateCallTimes.incrementAndGet() > 1) {
            Log.e("weijia", "application onCreate all times error");
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.i(LogTag, "appAttachBaseContext ");
    }


}
