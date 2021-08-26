/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/accessibilityservice/IAccessibilityServiceConnection.aidl
 */
package android.accessibilityservice;

/**
 * Interface given to an AccessibilitySerivce to talk to the AccessibilityManagerService.
 *
 * @hide
 */
public interface IAccessibilityServiceConnection extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.accessibilityservice.IAccessibilityServiceConnection {
        private static final java.lang.String DESCRIPTOR = "android.accessibilityservice.IAccessibilityServiceConnection";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.accessibilityservice.IAccessibilityServiceConnection interface,
         * generating a proxy if needed.
         */
        public static android.accessibilityservice.IAccessibilityServiceConnection asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_setServiceInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_findAccessibilityNodeInfoByAccessibilityId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_findAccessibilityNodeInfosByText = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_findAccessibilityNodeInfosByViewId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_findFocus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_focusSearch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_performAccessibilityAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_getWindow = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_getWindows = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_getServiceInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_performGlobalAction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_disableSelf = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_setOnKeyEventResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
        static final int TRANSACTION_getMagnificationScale = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
        static final int TRANSACTION_getMagnificationCenterX = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
        static final int TRANSACTION_getMagnificationCenterY = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
        static final int TRANSACTION_getMagnificationRegion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
        static final int TRANSACTION_resetMagnification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
        static final int TRANSACTION_setMagnificationScaleAndCenter = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
        static final int TRANSACTION_setMagnificationCallbackEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
        static final int TRANSACTION_setSoftKeyboardShowMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
        static final int TRANSACTION_setSoftKeyboardCallbackEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
        static final int TRANSACTION_isAccessibilityButtonAvailable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
        static final int TRANSACTION_sendGesture = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
        static final int TRANSACTION_isFingerprintGestureDetectionAvailable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
    }

    public void setServiceInfo(android.accessibilityservice.AccessibilityServiceInfo info) throws android.os.RemoteException;

    public boolean findAccessibilityNodeInfoByAccessibilityId(int accessibilityWindowId, long accessibilityNodeId, int interactionId, android.view.accessibility.IAccessibilityInteractionConnectionCallback callback, int flags, long threadId, android.os.Bundle arguments) throws android.os.RemoteException;

    public boolean findAccessibilityNodeInfosByText(int accessibilityWindowId, long accessibilityNodeId, java.lang.String text, int interactionId, android.view.accessibility.IAccessibilityInteractionConnectionCallback callback, long threadId) throws android.os.RemoteException;

    public boolean findAccessibilityNodeInfosByViewId(int accessibilityWindowId, long accessibilityNodeId, java.lang.String viewId, int interactionId, android.view.accessibility.IAccessibilityInteractionConnectionCallback callback, long threadId) throws android.os.RemoteException;

    public boolean findFocus(int accessibilityWindowId, long accessibilityNodeId, int focusType, int interactionId, android.view.accessibility.IAccessibilityInteractionConnectionCallback callback, long threadId) throws android.os.RemoteException;

    public boolean focusSearch(int accessibilityWindowId, long accessibilityNodeId, int direction, int interactionId, android.view.accessibility.IAccessibilityInteractionConnectionCallback callback, long threadId) throws android.os.RemoteException;

    public boolean performAccessibilityAction(int accessibilityWindowId, long accessibilityNodeId, int action, android.os.Bundle arguments, int interactionId, android.view.accessibility.IAccessibilityInteractionConnectionCallback callback, long threadId) throws android.os.RemoteException;

    public Object getWindow(int windowId) throws android.os.RemoteException;

    public java.util.List<Object> getWindows() throws android.os.RemoteException;

    public android.accessibilityservice.AccessibilityServiceInfo getServiceInfo() throws android.os.RemoteException;

    public boolean performGlobalAction(int action) throws android.os.RemoteException;

    public void disableSelf() throws android.os.RemoteException;

    public void setOnKeyEventResult(boolean handled, int sequence) throws android.os.RemoteException;

    public float getMagnificationScale() throws android.os.RemoteException;

    public float getMagnificationCenterX() throws android.os.RemoteException;

    public float getMagnificationCenterY() throws android.os.RemoteException;

    public android.graphics.Region getMagnificationRegion() throws android.os.RemoteException;

    public boolean resetMagnification(boolean animate) throws android.os.RemoteException;

    public boolean setMagnificationScaleAndCenter(float scale, float centerX, float centerY, boolean animate) throws android.os.RemoteException;

    public void setMagnificationCallbackEnabled(boolean enabled) throws android.os.RemoteException;

    public boolean setSoftKeyboardShowMode(int showMode) throws android.os.RemoteException;

    public void setSoftKeyboardCallbackEnabled(boolean enabled) throws android.os.RemoteException;

    public boolean isAccessibilityButtonAvailable() throws android.os.RemoteException;

    public void sendGesture(int sequence, android.content.pm.ParceledListSlice gestureSteps) throws android.os.RemoteException;

    public boolean isFingerprintGestureDetectionAvailable() throws android.os.RemoteException;
}
