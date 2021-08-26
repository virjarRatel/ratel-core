/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/accessibilityservice/IAccessibilityServiceClient.aidl
 */
package android.accessibilityservice;

/**
 * Top-level interface to an accessibility service component.
 *
 * @hide
 */
public interface IAccessibilityServiceClient extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.accessibilityservice.IAccessibilityServiceClient {
        private static final java.lang.String DESCRIPTOR = "android.accessibilityservice.IAccessibilityServiceClient";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.accessibilityservice.IAccessibilityServiceClient interface,
         * generating a proxy if needed.
         */
        public static android.accessibilityservice.IAccessibilityServiceClient asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_init = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_onAccessibilityEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_onInterrupt = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_onGesture = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_clearAccessibilityCache = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_onKeyEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_onMagnificationChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_onSoftKeyboardShowModeChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_onPerformGestureResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_onFingerprintCapturingGesturesChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_onFingerprintGesture = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_onAccessibilityButtonClicked = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_onAccessibilityButtonAvailabilityChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    }

    public void init(android.accessibilityservice.IAccessibilityServiceConnection connection, int connectionId, android.os.IBinder windowToken) throws android.os.RemoteException;

    public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event, boolean serviceWantsEvent) throws android.os.RemoteException;

    public void onInterrupt() throws android.os.RemoteException;

    public void onGesture(int gesture) throws android.os.RemoteException;

    public void clearAccessibilityCache() throws android.os.RemoteException;

    public void onKeyEvent(android.view.KeyEvent event, int sequence) throws android.os.RemoteException;

    public void onMagnificationChanged(android.graphics.Region region, float scale, float centerX, float centerY) throws android.os.RemoteException;

    public void onSoftKeyboardShowModeChanged(int showMode) throws android.os.RemoteException;

    public void onPerformGestureResult(int sequence, boolean completedSuccessfully) throws android.os.RemoteException;

    public void onFingerprintCapturingGesturesChanged(boolean capturing) throws android.os.RemoteException;

    public void onFingerprintGesture(int gesture) throws android.os.RemoteException;

    public void onAccessibilityButtonClicked() throws android.os.RemoteException;

    public void onAccessibilityButtonAvailabilityChanged(boolean available) throws android.os.RemoteException;
}
