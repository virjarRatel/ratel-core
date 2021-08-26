/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/content/pm/IPackageManager.aidl
 */
package android.content.pm;

/**
 * See {@link PackageManager} for documentation on most of the APIs
 * here.
 * <p>
 * {@hide}
 */
public interface IPackageManager extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IPackageManager {
        private static final String DESCRIPTOR = "android.content.pm.IPackageManager";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.content.pm.IPackageManager interface,
         * generating a proxy if needed.
         */
        public static IPackageManager asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            throw new UnsupportedOperationException("STUB");
        }

    }

    public void checkPackageStartable(String packageName, int userId) throws android.os.RemoteException;

    public boolean isPackageAvailable(String packageName, int userId) throws android.os.RemoteException;

    public android.content.pm.PackageInfo getPackageInfo(String packageName, int flags, int userId) throws android.os.RemoteException;

    public int getPackageUid(String packageName, int flags, int userId) throws android.os.RemoteException;

    public int[] getPackageGids(String packageName, int flags, int userId) throws android.os.RemoteException;

    public String[] currentToCanonicalPackageNames(String[] names) throws android.os.RemoteException;

    public String[] canonicalToCurrentPackageNames(String[] names) throws android.os.RemoteException;

    public android.content.pm.PermissionInfo getPermissionInfo(String name, int flags) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryPermissionsByGroup(String group, int flags) throws android.os.RemoteException;

    public android.content.pm.PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice getAllPermissionGroups(int flags) throws android.os.RemoteException;

    public android.content.pm.ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ActivityInfo getActivityInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;

    public boolean activitySupportsIntent(android.content.ComponentName className, android.content.Intent intent, String resolvedType) throws android.os.RemoteException;

    public android.content.pm.ActivityInfo getReceiverInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ServiceInfo getServiceInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ProviderInfo getProviderInfo(android.content.ComponentName className, int flags, int userId) throws android.os.RemoteException;

    public int checkPermission(String permName, String pkgName, int userId) throws android.os.RemoteException;

    public int checkUidPermission(String permName, int uid) throws android.os.RemoteException;

    public boolean addPermission(android.content.pm.PermissionInfo info) throws android.os.RemoteException;

    public void removePermission(String name) throws android.os.RemoteException;

    public void grantRuntimePermission(String packageName, String permissionName, int userId) throws android.os.RemoteException;

    public void revokeRuntimePermission(String packageName, String permissionName, int userId) throws android.os.RemoteException;

    public void resetRuntimePermissions() throws android.os.RemoteException;

    public int getPermissionFlags(String permissionName, String packageName, int userId) throws android.os.RemoteException;

    public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, int userId) throws android.os.RemoteException;

    public void updatePermissionFlagsForAllApps(int flagMask, int flagValues, int userId) throws android.os.RemoteException;

    public boolean shouldShowRequestPermissionRationale(String permissionName, String packageName, int userId) throws android.os.RemoteException;

    public boolean isProtectedBroadcast(String actionName) throws android.os.RemoteException;

    public int checkSignatures(String pkg1, String pkg2) throws android.os.RemoteException;

    public int checkUidSignatures(int uid1, int uid2) throws android.os.RemoteException;

    public java.util.List<String> getAllPackages() throws android.os.RemoteException;

    public String[] getPackagesForUid(int uid) throws android.os.RemoteException;

    public String getNameForUid(int uid) throws android.os.RemoteException;

    public int getUidForSharedUser(String sharedUserName) throws android.os.RemoteException;

    public int getFlagsForUid(int uid) throws android.os.RemoteException;

    public int getPrivateFlagsForUid(int uid) throws android.os.RemoteException;

    public boolean isUidPrivileged(int uid) throws android.os.RemoteException;

    public String[] getAppOpPermissionPackages(String permissionName) throws android.os.RemoteException;

    public android.content.pm.ResolveInfo resolveIntent(android.content.Intent intent, String resolvedType, int flags, int userId) throws android.os.RemoteException;

    public boolean canForwardTo(android.content.Intent intent, String resolvedType, int sourceUserId, int targetUserId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryIntentActivities(android.content.Intent intent, String resolvedType, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryIntentActivityOptions(android.content.ComponentName caller, android.content.Intent[] specifics, String[] specificTypes, android.content.Intent intent, String resolvedType, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryIntentReceivers(android.content.Intent intent, String resolvedType, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ResolveInfo resolveService(android.content.Intent intent, String resolvedType, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryIntentServices(android.content.Intent intent, String resolvedType, int flags, int userId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryIntentContentProviders(android.content.Intent intent, String resolvedType, int flags, int userId) throws android.os.RemoteException;

    /**
     * This implements getInstalledPackages via a "last returned row"
     * mechanism that is not exposed in the API. This is to get around the IPC
     * limit that kicks in when flags are included that bloat up the data
     * returned.
     */
    public android.content.pm.ParceledListSlice getInstalledPackages(int flags, int userId) throws android.os.RemoteException;

    /**
     * This implements getPackagesHoldingPermissions via a "last returned row"
     * mechanism that is not exposed in the API. This is to get around the IPC
     * limit that kicks in when flags are included that bloat up the data
     * returned.
     */
    public android.content.pm.ParceledListSlice getPackagesHoldingPermissions(String[] permissions, int flags, int userId) throws android.os.RemoteException;

    /**
     * This implements getInstalledApplications via a "last returned row"
     * mechanism that is not exposed in the API. This is to get around the IPC
     * limit that kicks in when flags are included that bloat up the data
     * returned.
     */
    public android.content.pm.ParceledListSlice getInstalledApplications(int flags, int userId) throws android.os.RemoteException;

    /**
     * Retrieve all applications that are marked as persistent.
     *
     * @return A List&lt;applicationInfo> containing one entry for each persistent
     * application.
     */
    public android.content.pm.ParceledListSlice getPersistentApplications(int flags) throws android.os.RemoteException;

    public android.content.pm.ProviderInfo resolveContentProvider(String name, int flags, int userId) throws android.os.RemoteException;

    /**
     * Retrieve sync information for all content providers.
     *
     * @param outNames Filled in with a list of the root names of the content
     *                 providers that can sync.
     * @param outInfo  Filled in with a list of the ProviderInfo for each
     *                 name in 'outNames'.
     */
    public void querySyncProviders(java.util.List<String> outNames, java.util.List<android.content.pm.ProviderInfo> outInfo) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryContentProviders(String processName, int uid, int flags) throws android.os.RemoteException;

    public android.content.pm.InstrumentationInfo getInstrumentationInfo(android.content.ComponentName className, int flags) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice queryInstrumentation(String targetPackage, int flags) throws android.os.RemoteException;

//    /**
//     * @deprecated Use PackageInstaller instead
//     */
//    public void installPackageAsUser(java.lang.String originPath, android.content.pm.IPackageInstallObserver2 observer, int flags, java.lang.String installerPackageName, int userId) throws android.os.RemoteException;

    public void finishPackageInstall(int token, boolean didLaunch) throws android.os.RemoteException;

    public void setInstallerPackageName(String targetPackage, String installerPackageName) throws android.os.RemoteException;

//    /**
//     * @deprecated rawr, don't call AIDL methods directly!
//     */
//    public void deletePackageAsUser(java.lang.String packageName, android.content.pm.IPackageDeleteObserver observer, int userId, int flags) throws android.os.RemoteException;

//    /**
//     * Delete a package for a specific user.
//     *
//     * @param packageName The fully qualified name of the package to delete.
//     * @param observer    a callback to use to notify when the package deletion in finished.
//     * @param userId      the id of the user for whom to delete the package
//     * @param flags       - possible values: {@link #DONT_DELETE_DATA}
//     */
//    public void deletePackage(java.lang.String packageName, android.content.pm.IPackageDeleteObserver2 observer, int userId, int flags) throws android.os.RemoteException;

    public String getInstallerPackageName(String packageName) throws android.os.RemoteException;

    public void resetApplicationPreferences(int userId) throws android.os.RemoteException;

    public android.content.pm.ResolveInfo getLastChosenActivity(android.content.Intent intent, String resolvedType, int flags) throws android.os.RemoteException;

    public void setLastChosenActivity(android.content.Intent intent, String resolvedType, int flags, android.content.IntentFilter filter, int match, android.content.ComponentName activity) throws android.os.RemoteException;

    public void addPreferredActivity(android.content.IntentFilter filter, int match, android.content.ComponentName[] set, android.content.ComponentName activity, int userId) throws android.os.RemoteException;

    public void replacePreferredActivity(android.content.IntentFilter filter, int match, android.content.ComponentName[] set, android.content.ComponentName activity, int userId) throws android.os.RemoteException;

    public void clearPackagePreferredActivities(String packageName) throws android.os.RemoteException;

    public int getPreferredActivities(java.util.List<android.content.IntentFilter> outFilters, java.util.List<android.content.ComponentName> outActivities, String packageName) throws android.os.RemoteException;

    public void addPersistentPreferredActivity(android.content.IntentFilter filter, android.content.ComponentName activity, int userId) throws android.os.RemoteException;

    public void clearPackagePersistentPreferredActivities(String packageName, int userId) throws android.os.RemoteException;

    public void addCrossProfileIntentFilter(android.content.IntentFilter intentFilter, String ownerPackage, int sourceUserId, int targetUserId, int flags) throws android.os.RemoteException;

    public void clearCrossProfileIntentFilters(int sourceUserId, String ownerPackage) throws android.os.RemoteException;

    public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, int userId) throws android.os.RemoteException;

    public boolean isPackageSuspendedForUser(String packageName, int userId) throws android.os.RemoteException;

    /**
     * Backup/restore support - only the system uid may use these.
     */
    public byte[] getPreferredActivityBackup(int userId) throws android.os.RemoteException;

    public void restorePreferredActivities(byte[] backup, int userId) throws android.os.RemoteException;

    public byte[] getDefaultAppsBackup(int userId) throws android.os.RemoteException;

    public void restoreDefaultApps(byte[] backup, int userId) throws android.os.RemoteException;

    public byte[] getIntentFilterVerificationBackup(int userId) throws android.os.RemoteException;

    public void restoreIntentFilterVerification(byte[] backup, int userId) throws android.os.RemoteException;

    public byte[] getPermissionGrantBackup(int userId) throws android.os.RemoteException;

    public void restorePermissionGrants(byte[] backup, int userId) throws android.os.RemoteException;

    /**
     * Report the set of 'Home' activity candidates, plus (if any) which of them
     * is the current "always use this one" setting.
     */
    public android.content.ComponentName getHomeActivities(java.util.List<android.content.pm.ResolveInfo> outHomeCandidates) throws android.os.RemoteException;

    public void setHomeActivity(android.content.ComponentName className, int userId) throws android.os.RemoteException;

    /**
     * As per {@link android.content.pm.PackageManager#setComponentEnabledSetting}.
     */
    public void setComponentEnabledSetting(android.content.ComponentName componentName, int newState, int flags, int userId) throws android.os.RemoteException;

    public int getComponentEnabledSetting(android.content.ComponentName componentName, int userId) throws android.os.RemoteException;

    /**
     * As per {@link android.content.pm.PackageManager#setApplicationEnabledSetting}.
     */
    public void setApplicationEnabledSetting(String packageName, int newState, int flags, int userId, String callingPackage) throws android.os.RemoteException;

    public int getApplicationEnabledSetting(String packageName, int userId) throws android.os.RemoteException;

    /**
     * Logs process start information (including APK hash) to the security log.
     */
    public void logAppProcessStartIfNeeded(String processName, int uid, String seinfo, String apkFile, int pid) throws android.os.RemoteException;

//    /**
//     * As per {@link android.content.pm.PackageManager#flushPackageRestrictionsAsUser}.
//     */
    public void flushPackageRestrictionsAsUser(int userId) throws android.os.RemoteException;

    /**
     * Set whether the given package should be considered stopped, making
     * it not visible to implicit intents that filter out stopped packages.
     */
    public void setPackageStoppedState(String packageName, boolean stopped, int userId) throws android.os.RemoteException;

    /**
     * Free storage by deleting LRU sorted list of cache files across
     * all applications. If the currently available free storage
     * on the device is greater than or equal to the requested
     * free storage, no cache files are cleared. If the currently
     * available storage on the device is less than the requested
     * free storage, some or all of the cache files across
     * all applications are deleted (based on last accessed time)
     * to increase the free storage space on the device to
     * the requested value. There is no guarantee that clearing all
     * the cache files from all applications will clear up
     * enough storage to achieve the desired value.
     *
     * @param freeStorageSize The number of bytes of storage to be
     *                        freed by the system. Say if freeStorageSize is XX,
     *                        and the current free storage is YY,
     *                        if XX is less than YY, just return. if not free XX-YY number
     *                        of bytes if possible.
     * @param observer        call back used to notify when
     *                        the operation is completed
     */
    public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, android.content.pm.IPackageDataObserver observer) throws android.os.RemoteException;

    /**
     * Free storage by deleting LRU sorted list of cache files across
     * all applications. If the currently available free storage
     * on the device is greater than or equal to the requested
     * free storage, no cache files are cleared. If the currently
     * available storage on the device is less than the requested
     * free storage, some or all of the cache files across
     * all applications are deleted (based on last accessed time)
     * to increase the free storage space on the device to
     * the requested value. There is no guarantee that clearing all
     * the cache files from all applications will clear up
     * enough storage to achieve the desired value.
     *
     * @param freeStorageSize The number of bytes of storage to be
     *                        freed by the system. Say if freeStorageSize is XX,
     *                        and the current free storage is YY,
     *                        if XX is less than YY, just return. if not free XX-YY number
     *                        of bytes if possible.
     * @param pi              IntentSender call back used to
     *                        notify when the operation is completed.May be null
     *                        to indicate that no call back is desired.
     */
    public void freeStorage(String volumeUuid, long freeStorageSize, android.content.IntentSender pi) throws android.os.RemoteException;

    /**
     * Delete all the cache files in an applications cache directory
     *
     * @param packageName The package name of the application whose cache
     *                    files need to be deleted
     * @param observer    a callback used to notify when the deletion is finished.
     */
    public void deleteApplicationCacheFiles(String packageName, android.content.pm.IPackageDataObserver observer) throws android.os.RemoteException;

    /**
     * Delete all the cache files in an applications cache directory
     *
     * @param packageName The package name of the application whose cache
     *                    files need to be deleted
     * @param userId      the user to delete application cache for
     * @param observer    a callback used to notify when the deletion is finished.
     */
    public void deleteApplicationCacheFilesAsUser(String packageName, int userId, android.content.pm.IPackageDataObserver observer) throws android.os.RemoteException;

    /**
     * Clear the user data directory of an application.
     *
     * @param packageName The package name of the application whose cache
     *                    files need to be deleted
     * @param observer    a callback used to notify when the operation is completed.
     */
    public void clearApplicationUserData(String packageName, android.content.pm.IPackageDataObserver observer, int userId) throws android.os.RemoteException;

    /**
     * Clear the profile data of an application.
     *
     * @param packageName The package name of the application whose profile data
     *                    need to be deleted
     */
    public void clearApplicationProfileData(String packageName) throws android.os.RemoteException;

    /**
     * Get package statistics including the code, data and cache size for
     * an already installed package
     *
     * @param packageName The package name of the application
     * @param userHandle  Which user the size should be retrieved for
     * @param observer    a callback to use to notify when the asynchronous
     *                    retrieval of information is complete.
     */
    public void getPackageSizeInfo(String packageName, int userHandle, android.content.pm.IPackageStatsObserver observer) throws android.os.RemoteException;

    /**
     * Get a list of shared libraries that are available on the
     * system.
     */
    public String[] getSystemSharedLibraryNames() throws android.os.RemoteException;

    /**
     * Get a list of features that are available on the
     * system.
     */
    public android.content.pm.ParceledListSlice getSystemAvailableFeatures() throws android.os.RemoteException;

    public boolean hasSystemFeature(String name, int version) throws android.os.RemoteException;

    public void enterSafeMode() throws android.os.RemoteException;

    public boolean isSafeMode() throws android.os.RemoteException;

    public void systemReady() throws android.os.RemoteException;

    public boolean hasSystemUidErrors() throws android.os.RemoteException;

    /**
     * Ask the package manager to fstrim the disk if needed.
     */
    public void performFstrimIfNeeded() throws android.os.RemoteException;

    /**
     * Ask the package manager to update packages if needed.
     */
    public void updatePackagesIfNeeded() throws android.os.RemoteException;

    /**
     * Notify the package manager that a package is going to be used and why.
     * <p>
     * See PackageManager.NOTIFY_PACKAGE_USE_* for reasons.
     */
    public void notifyPackageUse(String packageName, int reason) throws android.os.RemoteException;

    /**
     * Ask the package manager to perform dex-opt (if needed) on the given
     * package if it already hasn't done so.
     * <p>
     * In most cases, apps are dexopted in advance and this function will
     * be a no-op.
     */
    public boolean performDexOptIfNeeded(String packageName) throws android.os.RemoteException;

    /**
     * Ask the package manager to perform a dex-opt for the given reason. The package
     * manager will map the reason to a compiler filter according to the current system
     * configuration.
     */
    public boolean performDexOpt(String packageName, boolean checkProfiles, int compileReason, boolean force) throws android.os.RemoteException;

    /**
     * Ask the package manager to perform a dex-opt with the given compiler filter.
     * <p>
     * Note: exposed only for the shell command to allow moving packages explicitly to a
     * definite state.
     */
    public boolean performDexOptMode(String packageName, boolean checkProfiles, String targetCompilerFilter, boolean force) throws android.os.RemoteException;

    /**
     * Ask the package manager to dump profiles associated with a package.
     */
    public void dumpProfiles(String packageName) throws android.os.RemoteException;

    public void forceDexOpt(String packageName) throws android.os.RemoteException;

    /**
     * Update status of external media on the package manager to scan and
     * install packages installed on the external media. Like say the
     * MountService uses this to call into the package manager to update
     * status of sdcard.
     */
    public void updateExternalMediaStatus(boolean mounted, boolean reportStatus) throws android.os.RemoteException;

//    public android.content.pm.PackageCleanItem nextPackageToClean(android.content.pm.PackageCleanItem lastPackage) throws android.os.RemoteException;

    public int getMoveStatus(int moveId) throws android.os.RemoteException;

    public void registerMoveCallback(android.content.pm.IPackageMoveObserver callback) throws android.os.RemoteException;

    public void unregisterMoveCallback(android.content.pm.IPackageMoveObserver callback) throws android.os.RemoteException;

    public int movePackage(String packageName, String volumeUuid) throws android.os.RemoteException;

    public int movePrimaryStorage(String volumeUuid) throws android.os.RemoteException;

    public boolean addPermissionAsync(android.content.pm.PermissionInfo info) throws android.os.RemoteException;

    public boolean setInstallLocation(int loc) throws android.os.RemoteException;

    public int getInstallLocation() throws android.os.RemoteException;

    public int installExistingPackageAsUser(String packageName, int userId) throws android.os.RemoteException;

    public void verifyPendingInstall(int id, int verificationCode) throws android.os.RemoteException;

    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) throws android.os.RemoteException;

    public void verifyIntentFilter(int id, int verificationCode, java.util.List<String> failedDomains) throws android.os.RemoteException;

    public int getIntentVerificationStatus(String packageName, int userId) throws android.os.RemoteException;

    public boolean updateIntentVerificationStatus(String packageName, int status, int userId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice getIntentFilterVerifications(String packageName) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice getAllIntentFilters(String packageName) throws android.os.RemoteException;

    public boolean setDefaultBrowserPackageName(String packageName, int userId) throws android.os.RemoteException;

    public String getDefaultBrowserPackageName(int userId) throws android.os.RemoteException;

//    public android.content.pm.VerifierDeviceIdentity getVerifierDeviceIdentity() throws android.os.RemoteException;

    public boolean isFirstBoot() throws android.os.RemoteException;

    public boolean isOnlyCoreApps() throws android.os.RemoteException;

    public boolean isUpgrade() throws android.os.RemoteException;

    public void setPermissionEnforced(String permission, boolean enforced) throws android.os.RemoteException;

    public boolean isPermissionEnforced(String permission) throws android.os.RemoteException;

    /**
     * Reflects current DeviceStorageMonitorService state
     */
    public boolean isStorageLow() throws android.os.RemoteException;

    public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, int userId) throws android.os.RemoteException;

    public boolean getApplicationHiddenSettingAsUser(String packageName, int userId) throws android.os.RemoteException;

    public android.content.pm.IPackageInstaller getPackageInstaller() throws android.os.RemoteException;

    public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall, int userId) throws android.os.RemoteException;

    public boolean getBlockUninstallForUser(String packageName, int userId) throws android.os.RemoteException;

    public KeySet getKeySetByAlias(String packageName, String alias) throws android.os.RemoteException;

    public KeySet getSigningKeySet(String packageName) throws android.os.RemoteException;

    public boolean isPackageSignedByKeySet(String packageName, KeySet ks) throws android.os.RemoteException;

    public boolean isPackageSignedByKeySetExactly(String packageName, KeySet ks) throws android.os.RemoteException;

    public void addOnPermissionsChangeListener(android.content.pm.IOnPermissionsChangeListener listener) throws android.os.RemoteException;

    public void removeOnPermissionsChangeListener(android.content.pm.IOnPermissionsChangeListener listener) throws android.os.RemoteException;

    public void grantDefaultPermissionsToEnabledCarrierApps(String[] packageNames, int userId) throws android.os.RemoteException;

    public boolean isPermissionRevokedByPolicy(String permission, String packageName, int userId) throws android.os.RemoteException;

    public String getPermissionControllerPackageName() throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice getEphemeralApplications(int userId) throws android.os.RemoteException;

    public byte[] getEphemeralApplicationCookie(String packageName, int userId) throws android.os.RemoteException;

    public boolean setEphemeralApplicationCookie(String packageName, byte[] cookie, int userId) throws android.os.RemoteException;

    public android.graphics.Bitmap getEphemeralApplicationIcon(String packageName, int userId) throws android.os.RemoteException;

    public boolean isEphemeralApplication(String packageName, int userId) throws android.os.RemoteException;

    public boolean setRequiredForSystemUser(String packageName, boolean systemUserApp) throws android.os.RemoteException;

    public String getServicesSystemSharedLibraryPackageName() throws android.os.RemoteException;

    public String getSharedSystemSharedLibraryPackageName() throws android.os.RemoteException;

    public boolean isPackageDeviceAdminOnAnyUser(String packageName) throws android.os.RemoteException;

    public java.util.List<String> getPreviousCodePaths(String packageName) throws android.os.RemoteException;
}
