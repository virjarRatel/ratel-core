package com.virjar.ratel.shellengine;

public class StatusCollectCallback implements InstallCallback {
    public Throwable throwable = null;
    public boolean success = false;

    @Override
    public void onInstallFailed(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void onInstallSucced() {
        success = true;
    }
}