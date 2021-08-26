package android.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.IContentProvider;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.*;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.util.AndroidRuntimeException;
import com.android.internal.app.IVoiceInteractor;
import com.android.internal.content.ReferrerIntent;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

final class RemoteServiceException extends AndroidRuntimeException {
    public RemoteServiceException(String msg) {
        super(msg);
    }
}

/**
 * This manages the execution of the main thread in an
 * application process, scheduling and executing activities,
 * broadcasts, and other operations on it as the activity
 * manager requests.
 *
 * {@hide}
 */
public final class ActivityThread {
    /** @hide */
    public static final String TAG = "ActivityThread";
    private static final android.graphics.Bitmap.Config THUMBNAIL_FORMAT = Bitmap.Config.RGB_565;
    static final boolean localLOGV = false;
    static final boolean DEBUG_MESSAGES = false;
    /** @hide */
    public static final boolean DEBUG_BROADCAST = false;
    private static final boolean DEBUG_RESULTS = false;
    private static final boolean DEBUG_BACKUP = false;
    public static final boolean DEBUG_CONFIGURATION = false;
    private static final boolean DEBUG_SERVICE = false;
    private static final boolean DEBUG_MEMORY_TRIM = false;
    private static final boolean DEBUG_PROVIDER = false;
    private static final boolean DEBUG_ORDER = false;
    private static final long MIN_TIME_BETWEEN_GCS = 5*1000;
    private static final int SQLITE_MEM_RELEASED_EVENT_LOG_TAG = 75003;
    private static final int LOG_AM_ON_PAUSE_CALLED = 30021;
    private static final int LOG_AM_ON_RESUME_CALLED = 30022;
    private static final int LOG_AM_ON_STOP_CALLED = 30049;

    /** Type for IActivityManager.serviceDoneExecuting: anonymous operation */
    public static final int SERVICE_DONE_EXECUTING_ANON = 0;
    /** Type for IActivityManager.serviceDoneExecuting: done with an onStart call */
    public static final int SERVICE_DONE_EXECUTING_START = 1;
    /** Type for IActivityManager.serviceDoneExecuting: done stopping (destroying) service */
    public static final int SERVICE_DONE_EXECUTING_STOP = 2;

    // Details for pausing activity.
    private static final int USER_LEAVING = 1;
    private static final int DONT_REPORT = 2;

    // Whether to invoke an activity callback after delivering new configuration.
    private static final boolean REPORT_TO_ACTIVITY = true;

    /**
     * Denotes an invalid sequence number corresponding to a process state change.
     */
    public static final long INVALID_PROC_STATE_SEQ = -1;

    public static ActivityThread currentActivityThread() {
        throw new UnsupportedOperationException("STUB");
    }

    public static boolean isSystem() {
        throw new UnsupportedOperationException("STUB");
    }

    public static String currentOpPackageName() {
        throw new UnsupportedOperationException("STUB");
    }

    public static String currentPackageName() {
        throw new UnsupportedOperationException("STUB");
    }

    public static String currentProcessName() {
        throw new UnsupportedOperationException("STUB");
    }

    public static Application currentApplication() {
        throw new UnsupportedOperationException("STUB");
    }

    public static IPackageManager getPackageManager() {
        throw new UnsupportedOperationException("STUB");
    }

    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo,
                                          int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public final LoadedApk getPackageInfo(String packageName, CompatibilityInfo compatInfo,
                                          int flags, int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public final LoadedApk getPackageInfo(ApplicationInfo ai, CompatibilityInfo compatInfo,
                                          int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai,
                                                 CompatibilityInfo compatInfo) {
        throw new UnsupportedOperationException("STUB");
    }

    public final LoadedApk peekPackageInfo(String packageName, boolean includeCode) {
        throw new UnsupportedOperationException("STUB");
    }

    public ApplicationThread getApplicationThread()
    {
        throw new UnsupportedOperationException("STUB");
    }

    public Instrumentation getInstrumentation()
    {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean isProfiling() {
        throw new UnsupportedOperationException("STUB");
    }

    public String getProfileFilePath() {
        throw new UnsupportedOperationException("STUB");
    }

    public Looper getLooper() {
        throw new UnsupportedOperationException("STUB");
    }

    public Application getApplication() {
        throw new UnsupportedOperationException("STUB");
    }

    public String getProcessName() {
        throw new UnsupportedOperationException("STUB");
    }

    public ContextImpl getSystemContext() {
        throw new UnsupportedOperationException("STUB");
    }

    public ContextImpl getSystemUiContext() {
        throw new UnsupportedOperationException("STUB");
    }

    public void installSystemApplicationInfo(ApplicationInfo info, ClassLoader classLoader) {
        throw new UnsupportedOperationException("STUB");
    }

    public static void dumpMemInfoTable(PrintWriter pw, Debug.MemoryInfo memInfo, boolean checkin,
                                        boolean dumpFullInfo, boolean dumpDalvik, boolean dumpSummaryOnly,
                                        int pid, String processName,
                                        long nativeMax, long nativeAllocated, long nativeFree,
                                        long dalvikMax, long dalvikAllocated, long dalvikFree) {
        throw new UnsupportedOperationException("STUB");
    }

    public void registerOnActivityPausedListener(Activity activity,
                                                 OnActivityPausedListener listener) {
        throw new UnsupportedOperationException("STUB");
    }

    public void unregisterOnActivityPausedListener(Activity activity,
                                                   OnActivityPausedListener listener) {
        throw new UnsupportedOperationException("STUB");
    }

    public final ActivityInfo resolveActivityInfo(Intent intent) {
        throw new UnsupportedOperationException("STUB");
    }

//    public final Activity startActivityNow(Activity parent, String id,
//                                           Intent intent, ActivityInfo activityInfo, IBinder token, Bundle state,
//                                           Activity.NonConfigurationInstances lastNonConfigurationInstances) {
//        throw new UnsupportedOperationException("STUB");
//    }

    public final Activity getActivity(IBinder token) {
        throw new UnsupportedOperationException("STUB");
    }

    public final void sendActivityResult(
            IBinder token, String id, int requestCode,
            int resultCode, Intent data) {
        throw new UnsupportedOperationException("STUB");
    }

    public void handleRequestAssistContextExtras(RequestAssistContextExtras cmd) {
        throw new UnsupportedOperationException("STUB");
    }

    public void handleTranslucentConversionComplete(IBinder token, boolean drawComplete) {
        throw new UnsupportedOperationException("STUB");
    }

    public void onNewActivityOptions(IBinder token, ActivityOptions options) {
        throw new UnsupportedOperationException("STUB");
    }

    public void handleCancelVisibleBehind(IBinder token) {
        throw new UnsupportedOperationException("STUB");
    }

    public void handleOnBackgroundVisibleBehindChanged(IBinder token, boolean visible) {
        throw new UnsupportedOperationException("STUB");
    }

    public void handleInstallProvider(ProviderInfo info) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Return the Intent that's currently being handled by a
     * BroadcastReceiver on this thread, or null if none.
     * @hide
     */
    public static Intent getIntentBeingBroadcast() {
        throw new UnsupportedOperationException("STUB");
    }

    public final ActivityClientRecord performResumeActivity(IBinder token,
                                                            boolean clearHide, String reason) {
        throw new UnsupportedOperationException("STUB");
    }

    public final ActivityClientRecord performDestroyActivity(IBinder token, boolean finishing) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @param preserveWindow Whether the activity should try to reuse the window it created,
     *                        including the decor view after the relaunch.
     */
    public final void requestRelaunchActivity(IBinder token,
                                              List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents,
                                              int configChanges, boolean notResumed, Configuration config,
                                              Configuration overrideConfig, boolean fromServer, boolean preserveWindow) {
        throw new UnsupportedOperationException("STUB");
    }

    public final void applyConfigurationToResources(Configuration config) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Public entrypoint to stop profiling. This is required to end profiling when the app crashes,
     * so that profiler data won't be lost.
     *
     * @hide
     */
    public void stopProfiling() {
        throw new UnsupportedOperationException("STUB");
    }

    public final IContentProvider acquireProvider(
            Context c, String auth, int userId, boolean stable) {
        throw new UnsupportedOperationException("STUB");
    }

    public final IContentProvider acquireExistingProvider(
            Context c, String auth, int userId, boolean stable) {
        throw new UnsupportedOperationException("STUB");
    }

    public final boolean releaseProvider(IContentProvider provider, boolean stable) {
        throw new UnsupportedOperationException("STUB");
    }

    public static ActivityThread systemMain() {
        throw new UnsupportedOperationException("STUB");
    }

    public final void installSystemProviders(List<ProviderInfo> providers) {
        throw new UnsupportedOperationException("STUB");
    }

    public int getIntCoreSetting(String key, int defaultValue) {
        throw new UnsupportedOperationException("STUB");
    }

    public static void main(String[] args) {
        throw new UnsupportedOperationException("STUB");
    }

    private class ApplicationThread extends IApplicationThread.Stub {

        @Override
        public void schedulePauseActivity(IBinder token, boolean finished, boolean userLeaving, int configChanges, boolean dontReport) throws RemoteException {

        }

        @Override
        public void scheduleStopActivity(IBinder token, boolean showWindow, int configChanges) throws RemoteException {

        }

        @Override
        public void scheduleWindowVisibility(IBinder token, boolean showWindow) throws RemoteException {

        }

        @Override
        public void scheduleResumeActivity(IBinder token, int procState, boolean isForward, Bundle resumeArgs) throws RemoteException {

        }

        @Override
        public void scheduleSendResult(IBinder token, List<ResultInfo> results) throws RemoteException {

        }

        @Override
        public void scheduleLaunchActivity(Intent intent, IBinder token, int ident, ActivityInfo info, Configuration curConfig, Configuration overrideConfig, CompatibilityInfo compatInfo, String referrer, IVoiceInteractor voiceInteractor, int procState, Bundle state, Object persistentState, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, boolean notResumed, boolean isForward, ProfilerInfo profilerInfo) throws RemoteException {

        }

        @Override
        public void scheduleNewIntent(List<ReferrerIntent> intent, IBinder token, boolean andPause) throws RemoteException {

        }

        @Override
        public void scheduleDestroyActivity(IBinder token, boolean finished, int configChanges) throws RemoteException {

        }

        @Override
        public void scheduleReceiver(Intent intent, ActivityInfo info, CompatibilityInfo compatInfo, int resultCode, String data, Bundle extras, boolean sync, int sendingUser, int processState) throws RemoteException {

        }

        @Override
        public void scheduleCreateService(IBinder token, ServiceInfo info, CompatibilityInfo compatInfo, int processState) throws RemoteException {

        }

        @Override
        public void scheduleStopService(IBinder token) throws RemoteException {

        }

        @Override
        public void bindApplication(String packageName, ApplicationInfo info, List<ProviderInfo> providers, ComponentName testName, ProfilerInfo profilerInfo, Bundle testArguments, IInstrumentationWatcher testWatcher, IUiAutomationConnection uiAutomationConnection, int debugMode, boolean enableBinderTracking, boolean trackAllocation, boolean restrictedBackupMode, boolean persistent, Configuration config, CompatibilityInfo compatInfo, Map services, Bundle coreSettings, String buildSerial) throws RemoteException {

        }

        @Override
        public void scheduleExit() throws RemoteException {

        }

        @Override
        public void scheduleConfigurationChanged(Configuration config) throws RemoteException {

        }

        @Override
        public void scheduleServiceArgs(IBinder token, ParceledListSlice args) throws RemoteException {

        }

        @Override
        public void updateTimeZone() throws RemoteException {

        }

        @Override
        public void processInBackground() throws RemoteException {

        }

        @Override
        public void scheduleBindService(IBinder token, Intent intent, boolean rebind, int processState) throws RemoteException {

        }

        @Override
        public void scheduleUnbindService(IBinder token, Intent intent) throws RemoteException {

        }

        @Override
        public void dumpService(ParcelFileDescriptor fd, IBinder servicetoken, String[] args) throws RemoteException {

        }

        @Override
        public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser, int processState) throws RemoteException {

        }

        @Override
        public void scheduleLowMemory() throws RemoteException {

        }

        @Override
        public void scheduleActivityConfigurationChanged(IBinder token, Configuration overrideConfig) throws RemoteException {

        }

        @Override
        public void scheduleActivityMovedToDisplay(IBinder token, int displayId, Configuration overrideConfig) throws RemoteException {

        }

        @Override
        public void scheduleRelaunchActivity(IBinder token, List<ResultInfo> pendingResults, List<ReferrerIntent> pendingNewIntents, int configChanges, boolean notResumed, Configuration config, Configuration overrideConfig, boolean preserveWindow) throws RemoteException {

        }

        @Override
        public void scheduleSleeping(IBinder token, boolean sleeping) throws RemoteException {

        }

        @Override
        public void profilerControl(boolean start, ProfilerInfo profilerInfo, int profileType) throws RemoteException {

        }

        @Override
        public void setSchedulingGroup(int group) throws RemoteException {

        }

        @Override
        public void scheduleCreateBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo, int backupMode) throws RemoteException {

        }

        @Override
        public void scheduleDestroyBackupAgent(ApplicationInfo app, CompatibilityInfo compatInfo) throws RemoteException {

        }

        @Override
        public void scheduleOnNewActivityOptions(IBinder token, Bundle options) throws RemoteException {

        }

        @Override
        public void scheduleSuicide() throws RemoteException {

        }

        @Override
        public void dispatchPackageBroadcast(int cmd, String[] packages) throws RemoteException {

        }

        @Override
        public void scheduleCrash(String msg) throws RemoteException {

        }

        @Override
        public void dumpHeap(boolean managed, String path, ParcelFileDescriptor fd) throws RemoteException {

        }

        @Override
        public void dumpActivity(ParcelFileDescriptor fd, IBinder servicetoken, String prefix, String[] args) throws RemoteException {

        }

        @Override
        public void clearDnsCache() throws RemoteException {

        }

        @Override
        public void setHttpProxy(String proxy, String port, String exclList, Uri pacFileUrl) throws RemoteException {

        }

        @Override
        public void setCoreSettings(Bundle coreSettings) throws RemoteException {

        }

        @Override
        public void updatePackageCompatibilityInfo(String pkg, CompatibilityInfo info) throws RemoteException {

        }

        @Override
        public void scheduleTrimMemory(int level) throws RemoteException {

        }

        @Override
        public void dumpMemInfo(ParcelFileDescriptor fd, Debug.MemoryInfo mem, boolean checkin, boolean dumpInfo, boolean dumpDalvik, boolean dumpSummaryOnly, boolean dumpUnreachable, String[] args) throws RemoteException {

        }

        @Override
        public void dumpGfxInfo(ParcelFileDescriptor fd, String[] args) throws RemoteException {

        }

        @Override
        public void dumpProvider(ParcelFileDescriptor fd, IBinder servicetoken, String[] args) throws RemoteException {

        }

        @Override
        public void dumpDbInfo(ParcelFileDescriptor fd, String[] args) throws RemoteException {

        }

        @Override
        public void unstableProviderDied(IBinder provider) throws RemoteException {

        }

        @Override
        public void requestAssistContextExtras(IBinder activityToken, IBinder requestToken, int requestType, int sessionId, int flags) throws RemoteException {

        }

        @Override
        public void scheduleTranslucentConversionComplete(IBinder token, boolean timeout) throws RemoteException {

        }

        @Override
        public void setProcessState(int state) throws RemoteException {

        }

        @Override
        public void scheduleInstallProvider(ProviderInfo provider) throws RemoteException {

        }

        @Override
        public void updateTimePrefs(int timeFormatPreference) throws RemoteException {

        }

        @Override
        public void scheduleCancelVisibleBehind(IBinder token) throws RemoteException {

        }

        @Override
        public void scheduleBackgroundVisibleBehindChanged(IBinder token, boolean enabled) throws RemoteException {

        }

        @Override
        public void scheduleEnterAnimationComplete(IBinder token) throws RemoteException {

        }

        @Override
        public void notifyCleartextNetwork(byte[] firstPacket) throws RemoteException {

        }

        @Override
        public void startBinderTracking() throws RemoteException {

        }

        @Override
        public void stopBinderTrackingAndDump(ParcelFileDescriptor fd) throws RemoteException {

        }

        @Override
        public void scheduleMultiWindowModeChanged(IBinder token, boolean isInMultiWindowMode, Configuration newConfig) throws RemoteException {

        }

        @Override
        public void schedulePictureInPictureModeChanged(IBinder token, boolean isInPictureInPictureMode, Configuration newConfig) throws RemoteException {

        }

        @Override
        public void scheduleLocalVoiceInteractionStarted(IBinder token, IVoiceInteractor voiceInteractor) throws RemoteException {

        }

        @Override
        public void handleTrustStorageUpdate() throws RemoteException {

        }

        @Override
        public void attachAgent(String path) throws RemoteException {

        }

        @Override
        public void scheduleApplicationInfoChanged(ApplicationInfo ai) throws RemoteException {

        }

        @Override
        public void setNetworkBlockSeq(long procStateSeq) throws RemoteException {

        }

        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public void shellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, Object shellCallback, ResultReceiver resultReceiver) throws RemoteException {

        }
    }

    static final class RequestAssistContextExtras {
    }
    static final class ActivityClientRecord {
    }
}
