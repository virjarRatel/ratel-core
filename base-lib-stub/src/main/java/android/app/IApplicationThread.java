/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/app/IApplicationThread.aidl
 */
package android.app;
/**
 * System private API for communicating with the application.  This is given to
 * the activity manager by an application  when it starts up, for the activity
 * manager to tell the application about things it needs to do.
 *
 * {@hide}
 */
public interface IApplicationThread extends android.os.IInterface
{
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements android.app.IApplicationThread
    {
        /**
         * Cast an IBinder object into an android.app.IApplicationThread interface,
         * generating a proxy if needed.
         */
        public static android.app.IApplicationThread asInterface(android.os.IBinder obj)
        {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_schedulePauseActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_scheduleStopActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_scheduleWindowVisibility = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_scheduleResumeActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_scheduleSendResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_scheduleLaunchActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_scheduleNewIntent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_scheduleDestroyActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_scheduleReceiver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_scheduleCreateService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_scheduleStopService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_bindApplication = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_scheduleExit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
        static final int TRANSACTION_scheduleConfigurationChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
        static final int TRANSACTION_scheduleServiceArgs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
        static final int TRANSACTION_updateTimeZone = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
        static final int TRANSACTION_processInBackground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
        static final int TRANSACTION_scheduleBindService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
        static final int TRANSACTION_scheduleUnbindService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
        static final int TRANSACTION_dumpService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
        static final int TRANSACTION_scheduleRegisteredReceiver = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
        static final int TRANSACTION_scheduleLowMemory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
        static final int TRANSACTION_scheduleActivityConfigurationChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
        static final int TRANSACTION_scheduleActivityMovedToDisplay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
        static final int TRANSACTION_scheduleRelaunchActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
        static final int TRANSACTION_scheduleSleeping = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
        static final int TRANSACTION_profilerControl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
        static final int TRANSACTION_setSchedulingGroup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
        static final int TRANSACTION_scheduleCreateBackupAgent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
        static final int TRANSACTION_scheduleDestroyBackupAgent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
        static final int TRANSACTION_scheduleOnNewActivityOptions = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
        static final int TRANSACTION_scheduleSuicide = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
        static final int TRANSACTION_dispatchPackageBroadcast = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
        static final int TRANSACTION_scheduleCrash = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
        static final int TRANSACTION_dumpHeap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 34);
        static final int TRANSACTION_dumpActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 35);
        static final int TRANSACTION_clearDnsCache = (android.os.IBinder.FIRST_CALL_TRANSACTION + 36);
        static final int TRANSACTION_setHttpProxy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 37);
        static final int TRANSACTION_setCoreSettings = (android.os.IBinder.FIRST_CALL_TRANSACTION + 38);
        static final int TRANSACTION_updatePackageCompatibilityInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 39);
        static final int TRANSACTION_scheduleTrimMemory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 40);
        static final int TRANSACTION_dumpMemInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 41);
        static final int TRANSACTION_dumpGfxInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 42);
        static final int TRANSACTION_dumpProvider = (android.os.IBinder.FIRST_CALL_TRANSACTION + 43);
        static final int TRANSACTION_dumpDbInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 44);
        static final int TRANSACTION_unstableProviderDied = (android.os.IBinder.FIRST_CALL_TRANSACTION + 45);
        static final int TRANSACTION_requestAssistContextExtras = (android.os.IBinder.FIRST_CALL_TRANSACTION + 46);
        static final int TRANSACTION_scheduleTranslucentConversionComplete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 47);
        static final int TRANSACTION_setProcessState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 48);
        static final int TRANSACTION_scheduleInstallProvider = (android.os.IBinder.FIRST_CALL_TRANSACTION + 49);
        static final int TRANSACTION_updateTimePrefs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 50);
        static final int TRANSACTION_scheduleCancelVisibleBehind = (android.os.IBinder.FIRST_CALL_TRANSACTION + 51);
        static final int TRANSACTION_scheduleBackgroundVisibleBehindChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 52);
        static final int TRANSACTION_scheduleEnterAnimationComplete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 53);
        static final int TRANSACTION_notifyCleartextNetwork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 54);
        static final int TRANSACTION_startBinderTracking = (android.os.IBinder.FIRST_CALL_TRANSACTION + 55);
        static final int TRANSACTION_stopBinderTrackingAndDump = (android.os.IBinder.FIRST_CALL_TRANSACTION + 56);
        static final int TRANSACTION_scheduleMultiWindowModeChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 57);
        static final int TRANSACTION_schedulePictureInPictureModeChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 58);
        static final int TRANSACTION_scheduleLocalVoiceInteractionStarted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 59);
        static final int TRANSACTION_handleTrustStorageUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 60);
        static final int TRANSACTION_attachAgent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 61);
        static final int TRANSACTION_scheduleApplicationInfoChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 62);
        static final int TRANSACTION_setNetworkBlockSeq = (android.os.IBinder.FIRST_CALL_TRANSACTION + 63);
    }
    public void schedulePauseActivity(android.os.IBinder token, boolean finished, boolean userLeaving, int configChanges, boolean dontReport) throws android.os.RemoteException;
    public void scheduleStopActivity(android.os.IBinder token, boolean showWindow, int configChanges) throws android.os.RemoteException;
    public void scheduleWindowVisibility(android.os.IBinder token, boolean showWindow) throws android.os.RemoteException;
    public void scheduleResumeActivity(android.os.IBinder token, int procState, boolean isForward, android.os.Bundle resumeArgs) throws android.os.RemoteException;
    public void scheduleSendResult(android.os.IBinder token, java.util.List<android.app.ResultInfo> results) throws android.os.RemoteException;
    public void scheduleLaunchActivity(android.content.Intent intent, android.os.IBinder token, int ident, android.content.pm.ActivityInfo info, android.content.res.Configuration curConfig, android.content.res.Configuration overrideConfig, android.content.res.CompatibilityInfo compatInfo, java.lang.String referrer, com.android.internal.app.IVoiceInteractor voiceInteractor, int procState, android.os.Bundle state, Object persistentState, java.util.List<android.app.ResultInfo> pendingResults, java.util.List<com.android.internal.content.ReferrerIntent> pendingNewIntents, boolean notResumed, boolean isForward, android.app.ProfilerInfo profilerInfo) throws android.os.RemoteException;
    public void scheduleNewIntent(java.util.List<com.android.internal.content.ReferrerIntent> intent, android.os.IBinder token, boolean andPause) throws android.os.RemoteException;
    public void scheduleDestroyActivity(android.os.IBinder token, boolean finished, int configChanges) throws android.os.RemoteException;
    public void scheduleReceiver(android.content.Intent intent, android.content.pm.ActivityInfo info, android.content.res.CompatibilityInfo compatInfo, int resultCode, java.lang.String data, android.os.Bundle extras, boolean sync, int sendingUser, int processState) throws android.os.RemoteException;
    public void scheduleCreateService(android.os.IBinder token, android.content.pm.ServiceInfo info, android.content.res.CompatibilityInfo compatInfo, int processState) throws android.os.RemoteException;
    public void scheduleStopService(android.os.IBinder token) throws android.os.RemoteException;
    public void bindApplication(java.lang.String packageName, android.content.pm.ApplicationInfo info, java.util.List<android.content.pm.ProviderInfo> providers, android.content.ComponentName testName, android.app.ProfilerInfo profilerInfo, android.os.Bundle testArguments, android.app.IInstrumentationWatcher testWatcher, android.app.IUiAutomationConnection uiAutomationConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean restrictedBackupMode, boolean persistent, android.content.res.Configuration config, android.content.res.CompatibilityInfo compatInfo, java.util.Map services, android.os.Bundle coreSettings, java.lang.String buildSerial) throws android.os.RemoteException;
    public void scheduleExit() throws android.os.RemoteException;
    public void scheduleConfigurationChanged(android.content.res.Configuration config) throws android.os.RemoteException;
    public void scheduleServiceArgs(android.os.IBinder token, android.content.pm.ParceledListSlice args) throws android.os.RemoteException;
    public void updateTimeZone() throws android.os.RemoteException;
    public void processInBackground() throws android.os.RemoteException;
    public void scheduleBindService(android.os.IBinder token, android.content.Intent intent, boolean rebind, int processState) throws android.os.RemoteException;
    public void scheduleUnbindService(android.os.IBinder token, android.content.Intent intent) throws android.os.RemoteException;
    public void dumpService(android.os.ParcelFileDescriptor fd, android.os.IBinder servicetoken, java.lang.String[] args) throws android.os.RemoteException;
    public void scheduleRegisteredReceiver(android.content.IIntentReceiver receiver, android.content.Intent intent, int resultCode, java.lang.String data, android.os.Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws android.os.RemoteException;
    public void scheduleLowMemory() throws android.os.RemoteException;
    public void scheduleActivityConfigurationChanged(android.os.IBinder token, android.content.res.Configuration overrideConfig) throws android.os.RemoteException;
    public void scheduleActivityMovedToDisplay(android.os.IBinder token, int displayId, android.content.res.Configuration overrideConfig) throws android.os.RemoteException;
    public void scheduleRelaunchActivity(android.os.IBinder token, java.util.List<android.app.ResultInfo> pendingResults, java.util.List<com.android.internal.content.ReferrerIntent> pendingNewIntents, int configChanges, boolean notResumed, android.content.res.Configuration config, android.content.res.Configuration overrideConfig, boolean preserveWindow) throws android.os.RemoteException;
    public void scheduleSleeping(android.os.IBinder token, boolean sleeping) throws android.os.RemoteException;
    public void profilerControl(boolean start, android.app.ProfilerInfo profilerInfo, int profileType) throws android.os.RemoteException;
    public void setSchedulingGroup(int group) throws android.os.RemoteException;
    public void scheduleCreateBackupAgent(android.content.pm.ApplicationInfo app, android.content.res.CompatibilityInfo compatInfo, int backupMode) throws android.os.RemoteException;
    public void scheduleDestroyBackupAgent(android.content.pm.ApplicationInfo app, android.content.res.CompatibilityInfo compatInfo) throws android.os.RemoteException;
    public void scheduleOnNewActivityOptions(android.os.IBinder token, android.os.Bundle options) throws android.os.RemoteException;
    public void scheduleSuicide() throws android.os.RemoteException;
    public void dispatchPackageBroadcast(int cmd, java.lang.String[] packages) throws android.os.RemoteException;
    public void scheduleCrash(java.lang.String msg) throws android.os.RemoteException;
    public void dumpHeap(boolean managed, java.lang.String path, android.os.ParcelFileDescriptor fd) throws android.os.RemoteException;
    public void dumpActivity(android.os.ParcelFileDescriptor fd, android.os.IBinder servicetoken, java.lang.String prefix, java.lang.String[] args) throws android.os.RemoteException;
    public void clearDnsCache() throws android.os.RemoteException;
    public void setHttpProxy(java.lang.String proxy, java.lang.String port, java.lang.String exclList, android.net.Uri pacFileUrl) throws android.os.RemoteException;
    public void setCoreSettings(android.os.Bundle coreSettings) throws android.os.RemoteException;
    public void updatePackageCompatibilityInfo(java.lang.String pkg, android.content.res.CompatibilityInfo info) throws android.os.RemoteException;
    public void scheduleTrimMemory(int level) throws android.os.RemoteException;
    public void dumpMemInfo(android.os.ParcelFileDescriptor fd, android.os.Debug.MemoryInfo mem, boolean checkin, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, java.lang.String[] args) throws android.os.RemoteException;
    public void dumpGfxInfo(android.os.ParcelFileDescriptor fd, java.lang.String[] args) throws android.os.RemoteException;
    public void dumpProvider(android.os.ParcelFileDescriptor fd, android.os.IBinder servicetoken, java.lang.String[] args) throws android.os.RemoteException;
    public void dumpDbInfo(android.os.ParcelFileDescriptor fd, java.lang.String[] args) throws android.os.RemoteException;
    public void unstableProviderDied(android.os.IBinder provider) throws android.os.RemoteException;
    public void requestAssistContextExtras(android.os.IBinder activityToken, android.os.IBinder requestToken, int requestType, int sessionId, int flags) throws android.os.RemoteException;
    public void scheduleTranslucentConversionComplete(android.os.IBinder token, boolean timeout) throws android.os.RemoteException;
    public void setProcessState(int state) throws android.os.RemoteException;
    public void scheduleInstallProvider(android.content.pm.ProviderInfo provider) throws android.os.RemoteException;
    public void updateTimePrefs(int timeFormatPreference) throws android.os.RemoteException;
    public void scheduleCancelVisibleBehind(android.os.IBinder token) throws android.os.RemoteException;
    public void scheduleBackgroundVisibleBehindChanged(android.os.IBinder token, boolean enabled) throws android.os.RemoteException;
    public void scheduleEnterAnimationComplete(android.os.IBinder token) throws android.os.RemoteException;
    public void notifyCleartextNetwork(byte[] firstPacket) throws android.os.RemoteException;
    public void startBinderTracking() throws android.os.RemoteException;
    public void stopBinderTrackingAndDump(android.os.ParcelFileDescriptor fd) throws android.os.RemoteException;
    public void scheduleMultiWindowModeChanged(android.os.IBinder token, boolean isInMultiWindowMode, android.content.res.Configuration newConfig) throws android.os.RemoteException;
    public void schedulePictureInPictureModeChanged(android.os.IBinder token, boolean isInPictureInPictureMode, android.content.res.Configuration newConfig) throws android.os.RemoteException;
    public void scheduleLocalVoiceInteractionStarted(android.os.IBinder token, com.android.internal.app.IVoiceInteractor voiceInteractor) throws android.os.RemoteException;
    public void handleTrustStorageUpdate() throws android.os.RemoteException;
    public void attachAgent(java.lang.String path) throws android.os.RemoteException;
    public void scheduleApplicationInfoChanged(android.content.pm.ApplicationInfo ai) throws android.os.RemoteException;
    public void setNetworkBlockSeq(long procStateSeq) throws android.os.RemoteException;
}
