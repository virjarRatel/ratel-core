/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/os/ICancellationSignal.aidl
 */
package android.os;

/**
 * @hide
 */
public interface ICancellationSignal extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.os.ICancellationSignal {
        private static final java.lang.String DESCRIPTOR = "android.os.ICancellationSignal";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.os.ICancellationSignal interface,
         * generating a proxy if needed.
         */
        public static android.os.ICancellationSignal asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_cancel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void cancel() throws android.os.RemoteException;
}
