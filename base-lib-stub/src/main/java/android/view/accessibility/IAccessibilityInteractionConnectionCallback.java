/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/view/accessibility/IAccessibilityInteractionConnectionCallback.aidl
 */
package android.view.accessibility;

/**
 * Callback for specifying the result for an asynchronous request made
 * via calling a method on IAccessibilityInteractionConnectionCallback.
 *
 * @hide
 */
public interface IAccessibilityInteractionConnectionCallback extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.view.accessibility.IAccessibilityInteractionConnectionCallback {
        private static final java.lang.String DESCRIPTOR = "android.view.accessibility.IAccessibilityInteractionConnectionCallback";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.view.accessibility.IAccessibilityInteractionConnectionCallback interface,
         * generating a proxy if needed.
         */
        public static android.view.accessibility.IAccessibilityInteractionConnectionCallback asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_setFindAccessibilityNodeInfoResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_setFindAccessibilityNodeInfosResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_setPerformAccessibilityActionResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    }

    /**
     * Sets the result of an async request that returns an {@link AccessibilityNodeInfo}.
     *
     * @param info         The result {@link AccessibilityNodeInfo}.
     * @param interactionId The interaction id to match the result with the request.
     */
    public void setFindAccessibilityNodeInfoResult(android.view.accessibility.AccessibilityNodeInfo info, int interactionId) throws android.os.RemoteException;

    /**
     * Sets the result of an async request that returns {@link AccessibilityNodeInfo}s.
     *
     * @param infos         The result {@link AccessibilityNodeInfo}s.
     * @param interactionId The interaction id to match the result with the request.
     */
    public void setFindAccessibilityNodeInfosResult(java.util.List<android.view.accessibility.AccessibilityNodeInfo> infos, int interactionId) throws android.os.RemoteException;

    /**
     * Sets the result of a request to perform an accessibility action.
     *
     * @param succeeded       the action was performed.
     * @param interactionId The interaction id to match the result with the request.
     */
    public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) throws android.os.RemoteException;
}
