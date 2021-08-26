/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Resources;

import java.io.File;
import java.util.List;

/**
 * Local state maintained about a currently loaded .apk.
 *
 */
public final class LoadedApk {
    /**
     * Create information about a new .apk
     * <p>
     * NOTE: This constructor is called with ActivityThread's lock held,
     * so MUST NOT call back out to the activity manager.
     */
    public LoadedApk(ActivityThread activityThread, ApplicationInfo aInfo,
                     CompatibilityInfo compatInfo, ClassLoader baseLoader,
                     boolean securityViolation, boolean includeCode, boolean registerPackage) {

        throw new UnsupportedOperationException("STUB");
    }

    public String getPackageName() {
        throw new UnsupportedOperationException("STUB");
    }

    public ApplicationInfo getApplicationInfo() {
        throw new UnsupportedOperationException("STUB");
    }

    public int getTargetSdkVersion() {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean isSecurityViolation() {
        throw new UnsupportedOperationException("STUB");
    }

    public CompatibilityInfo getCompatibilityInfo() {
        throw new UnsupportedOperationException("STUB");
    }

    public void setCompatibilityInfo(CompatibilityInfo compatInfo) {
        throw new UnsupportedOperationException("STUB");
    }

    public void updateApplicationInfo(ApplicationInfo aInfo, List<String> oldPaths) {
        throw new UnsupportedOperationException("STUB");
    }

    public static void makePaths(ActivityThread activityThread, ApplicationInfo aInfo,
                                 List<String> outZipPaths, List<String> outLibPaths) {
        throw new UnsupportedOperationException("STUB");
    }

    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("STUB");
    }

    public String getAppDir() {
        throw new UnsupportedOperationException("STUB");
    }

    public String getLibDir() {
        throw new UnsupportedOperationException("STUB");
    }

    public String getResDir() {
        throw new UnsupportedOperationException("STUB");
    }

    public String[] getSplitAppDirs() {
        throw new UnsupportedOperationException("STUB");
    }

    public String[] getSplitResDirs() {
        throw new UnsupportedOperationException("STUB");
    }

    public String[] getOverlayDirs() {
        throw new UnsupportedOperationException("STUB");
    }

    public String getDataDir() {
        throw new UnsupportedOperationException("STUB");
    }

    public File getDataDirFile() {
        throw new UnsupportedOperationException("STUB");
    }

    public File getDeviceProtectedDataDirFile() {
        throw new UnsupportedOperationException("STUB");
    }

    public File getCredentialProtectedDataDirFile() {
        throw new UnsupportedOperationException("STUB");
    }

    public AssetManager getAssets(ActivityThread mainThread) {
        throw new UnsupportedOperationException("STUB");
    }

    public Resources getResources(ActivityThread mainThread) {
        throw new UnsupportedOperationException("STUB");
    }

    public Application makeApplication(boolean forceDefaultAppClass,
                                       Instrumentation instrumentation) {
        throw new UnsupportedOperationException("STUB");
    }

    public void removeContextRegistrations(Context context,
                                           String who, String what) {
        throw new UnsupportedOperationException("STUB");
    }

//    public IIntentReceiver getReceiverDispatcher(BroadcastReceiver r,
//                                                 Context context, Handler handler,
//                                                 Instrumentation instrumentation, boolean registered) {
//        throw new UnsupportedOperationException("STUB");
//    }
//
//    public IIntentReceiver forgetReceiverDispatcher(Context context,
//                                                    BroadcastReceiver r) {
//        throw new UnsupportedOperationException("STUB");
//    }
//
//    public final IServiceConnection getServiceDispatcher(ServiceConnection c,
//                                                         Context context, Handler handler, int flags) {
//        throw new UnsupportedOperationException("STUB");
//    }
//
//    public final IServiceConnection forgetServiceDispatcher(Context context,
//                                                            ServiceConnection c) {
//        throw new UnsupportedOperationException("STUB");
//    }
}
