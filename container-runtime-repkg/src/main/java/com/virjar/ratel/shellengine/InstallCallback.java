package com.virjar.ratel.shellengine;

public interface InstallCallback {
    void onInstallFailed(Throwable throwable);

    void onInstallSucced();
}