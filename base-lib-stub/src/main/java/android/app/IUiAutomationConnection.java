/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/app/IUiAutomationConnection.aidl
 */
package android.app;

/**
 * This interface contains privileged operations a shell program can perform
 * on behalf of an instrumentation that it runs. These operations require
 * special permissions which the shell user has but the instrumentation does
 * not. Running privileged operations by the shell user on behalf of an
 * instrumentation is needed for running UiTestCases.
 * <p>
 * {@hide}
 */
public interface IUiAutomationConnection extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.app.IUiAutomationConnection {
        private static final java.lang.String DESCRIPTOR = "android.app.IUiAutomationConnection";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.app.IUiAutomationConnection interface,
         * generating a proxy if needed.
         */
        public static android.app.IUiAutomationConnection asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_connect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_injectInputEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_setRotation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_takeScreenshot = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_clearWindowContentFrameStats = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_getWindowContentFrameStats = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_clearWindowAnimationFrameStats = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_getWindowAnimationFrameStats = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_executeShellCommand = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_grantRuntimePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_revokeRuntimePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_shutdown = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    }

    public void connect(android.accessibilityservice.IAccessibilityServiceClient client, int flags) throws android.os.RemoteException;

    public void disconnect() throws android.os.RemoteException;

    public boolean injectInputEvent(android.view.InputEvent event, boolean sync) throws android.os.RemoteException;

    public boolean setRotation(int rotation) throws android.os.RemoteException;

    public android.graphics.Bitmap takeScreenshot(int width, int height) throws android.os.RemoteException;

    public boolean clearWindowContentFrameStats(int windowId) throws android.os.RemoteException;

    public Object getWindowContentFrameStats(int windowId) throws android.os.RemoteException;

    public void clearWindowAnimationFrameStats() throws android.os.RemoteException;

    public Object getWindowAnimationFrameStats() throws android.os.RemoteException;

    public void executeShellCommand(java.lang.String command, android.os.ParcelFileDescriptor fd) throws android.os.RemoteException;

    public void grantRuntimePermission(java.lang.String packageName, java.lang.String permission, int userId) throws android.os.RemoteException;

    public void revokeRuntimePermission(java.lang.String packageName, java.lang.String permission, int userId) throws android.os.RemoteException;
// Called from the system process.

    public void shutdown() throws android.os.RemoteException;
}
