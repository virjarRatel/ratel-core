/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.view.Display;
import android.view.DisplayAdjustments;

import java.io.*;

/**
 * Common implementation of Context API, which provides the base
 * context object for Activity and other application components.
 */
class ContextImpl extends Context {

    @Override
    public AssetManager getAssets() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Resources getResources() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public PackageManager getPackageManager() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public ContentResolver getContentResolver() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Looper getMainLooper() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Context getApplicationContext() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void setTheme(int resId) {
        throw new UnsupportedOperationException("STUB");
    }

    public int getThemeResId() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Resources.Theme getTheme() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String getPackageName() {
        throw new UnsupportedOperationException("STUB");
    }

    public String getBasePackageName() {
        throw new UnsupportedOperationException("STUB");
    }

    @SuppressLint("Override")
    public String getOpPackageName() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String getPackageResourcePath() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String getPackageCodePath() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        throw new UnsupportedOperationException("STUB");
    }

    public SharedPreferences getSharedPreferences(File file, int mode) {
        throw new UnsupportedOperationException("STUB");
    }

//    @TargetApi(24)
//    public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
//        throw new UnsupportedOperationException("STUB");
//    }
//
//    @TargetApi(24)
//    public boolean deleteSharedPreferences(String name) {
//        throw new UnsupportedOperationException("STUB");
//    }

    private File getPreferencesDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public FileInputStream openFileInput(String name)
        throws FileNotFoundException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean deleteFile(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getFilesDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getNoBackupFilesDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getExternalFilesDir(String type) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getObbDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File[] getObbDirs() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getCacheDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getCodeCacheDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getExternalCacheDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File[] getExternalCacheDirs() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File[] getExternalMediaDirs() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getFileStreamPath(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    public File getSharedPreferencesPath(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String[] fileList() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory,
                                               DatabaseErrorHandler errorHandler) {
        throw new UnsupportedOperationException("STUB");
    }

//    public boolean moveDatabaseFrom(Context sourceContext, String name) {
//        throw new UnsupportedOperationException("STUB");
//    }

    @Override
    public boolean deleteDatabase(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getDatabasePath(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String[] databaseList() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public Drawable getWallpaper() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public Drawable peekWallpaper() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public int getWallpaperDesiredMinimumWidth() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public int getWallpaperDesiredMinimumHeight() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void setWallpaper(Bitmap bitmap) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void setWallpaper(InputStream data) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void clearWallpaper() throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void startActivity(Intent intent) {
        throw new UnsupportedOperationException("STUB");
    }

    public void startActivityAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void startActivity(Intent intent, Bundle options) {
        throw new UnsupportedOperationException("STUB");
    }

    public void startActivityAsUser(Intent intent, Bundle options, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void startActivities(Intent[] intents) {
        throw new UnsupportedOperationException("STUB");
    }

    public void startActivitiesAsUser(Intent[] intents, Bundle options, UserHandle userHandle) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void startActivities(Intent[] intents, Bundle options) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void startIntentSender(IntentSender intent,
                                  Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
            throws IntentSender.SendIntentException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent,
                                  int flagsMask, int flagsValues, int extraFlags, Bundle options)
            throws IntentSender.SendIntentException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void sendBroadcast(Intent intent) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void sendBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendBroadcastMultiplePermissions(Intent intent, String[] receiverPermissions) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendBroadcast(Intent intent, String receiverPermission, Bundle options) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendBroadcast(Intent intent, String receiverPermission, int appOp) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void sendOrderedBroadcast(Intent intent,
                                     String receiverPermission, BroadcastReceiver resultReceiver,
                                     Handler scheduler, int initialCode, String initialData,
                                     Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendOrderedBroadcast(Intent intent,
                                     String receiverPermission, Bundle options, BroadcastReceiver resultReceiver,
                                     Handler scheduler, int initialCode, String initialData,
                                     Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendOrderedBroadcast(Intent intent,
                                     String receiverPermission, int appOp, BroadcastReceiver resultReceiver,
                                     Handler scheduler, int initialCode, String initialData,
                                     Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user,
                                    String receiverPermission) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user,
                                    String receiverPermission, int appOp) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user,
                                           String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler,
                                           int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user,
                                           String receiverPermission, int appOp, BroadcastReceiver resultReceiver,
                                           Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user,
                                           String receiverPermission, int appOp, Bundle options, BroadcastReceiver resultReceiver,
                                           Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void sendStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void sendStickyOrderedBroadcast(Intent intent,
                                           BroadcastReceiver resultReceiver,
                                           Handler scheduler, int initialCode, String initialData,
                                           Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void removeStickyBroadcast(Intent intent) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    @Deprecated
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user, Bundle options) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void sendStickyOrderedBroadcastAsUser(Intent intent,
                                                 UserHandle user, BroadcastReceiver resultReceiver,
                                                 Handler scheduler, int initialCode, String initialData,
                                                 Bundle initialExtras) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    @Deprecated
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        throw new UnsupportedOperationException("STUB");
    }

//    @Override
    public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, int i) {
        return null;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
                                   String broadcastPermission, Handler scheduler) {
        throw new UnsupportedOperationException("STUB");
    }

//    @Override
    public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, String s, Handler handler, int i) {
        return null;
    }

    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user,
                                         IntentFilter filter, String broadcastPermission, Handler scheduler) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public ComponentName startService(Intent service) {
        throw new UnsupportedOperationException("STUB");
    }

//    @Override
    public ComponentName startForegroundService(Intent intent) {
        return null;
    }

    @Override
    public boolean stopService(Intent service) {
        throw new UnsupportedOperationException("STUB");
    }

    public ComponentName startServiceAsUser(Intent service, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean stopServiceAsUser(Intent service, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn,
                               int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags,
                                     UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags,
                                     Handler handler, UserHandle user) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean startInstrumentation(ComponentName className,
                                        String profileFile, Bundle arguments) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Object getSystemService(String name) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String getSystemServiceName(Class<?> serviceClass) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        throw new UnsupportedOperationException("STUB");
    }

    public int checkPermission(String permission, int pid, int uid, IBinder callerToken) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int checkCallingPermission(String permission) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int checkSelfPermission(String permission) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void enforcePermission(
            String permission, int pid, int uid, String message) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void enforceCallingPermission(String permission, String message) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void enforceCallingOrSelfPermission(
            String permission, String message) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException("STUB");
    }

//    @Override
    public void revokeUriPermission(String s, Uri uri, int i) {

    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException("STUB");
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags, IBinder callerToken) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int checkUriPermission(Uri uri, String readPermission,
                                  String writePermission, int pid, int uid, int modeFlags) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void enforceUriPermission(
            Uri uri, int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void enforceCallingUriPermission(
            Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void enforceCallingOrSelfUriPermission(
            Uri uri, int modeFlags, String message) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void enforceUriPermission(
            Uri uri, String readPermission, String writePermission,
            int pid, int uid, int modeFlags, String message) {
        throw new UnsupportedOperationException("STUB");
    }

    public Context createApplicationContext(ApplicationInfo application, int flags)
            throws NameNotFoundException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Context createPackageContext(String packageName, int flags)
            throws NameNotFoundException {
        throw new UnsupportedOperationException("STUB");
    }

//    @Override
    public Context createContextForSplit(String s) throws NameNotFoundException {
        return null;
    }

    public Context createPackageContextAsUser(String packageName, int flags, UserHandle user)
            throws NameNotFoundException {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Context createDisplayContext(Display display) {
        throw new UnsupportedOperationException("STUB");
    }

//    @Override
//    public Context createDeviceProtectedStorageContext() {
//        throw new UnsupportedOperationException("STUB");
//    }

    public Context createCredentialProtectedStorageContext() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean isRestricted() {
        throw new UnsupportedOperationException("STUB");
    }

//    @Override
//    public boolean isDeviceProtectedStorage() {
//        throw new UnsupportedOperationException("STUB");
//    }

    public boolean isCredentialProtectedStorage() {
        throw new UnsupportedOperationException("STUB");
    }

    public Display getDisplay() {
        throw new UnsupportedOperationException("STUB");
    }

    public DisplayAdjustments getDisplayAdjustments(int displayId) {
        throw new UnsupportedOperationException("STUB");
    }

//    public File getDataDir() {
//        throw new UnsupportedOperationException("STUB");
//    }

    @Override
    public File getDir(String name, int mode) {
        throw new UnsupportedOperationException("STUB");
    }

    public int getUserId() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean moveSharedPreferencesFrom(Context context, String s) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean deleteSharedPreferences(String s) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public File getDataDir() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean moveDatabaseFrom(Context context, String s) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public Context createDeviceProtectedStorageContext() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean isDeviceProtectedStorage() {
        throw new UnsupportedOperationException("STUB");
    }
}
