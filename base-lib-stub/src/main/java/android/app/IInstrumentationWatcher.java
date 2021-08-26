/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/app/IInstrumentationWatcher.aidl
 */
package android.app;

/**
 * @hide
 */
public interface IInstrumentationWatcher extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.app.IInstrumentationWatcher {
        private static final java.lang.String DESCRIPTOR = "android.app.IInstrumentationWatcher";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.app.IInstrumentationWatcher interface,
         * generating a proxy if needed.
         */
        public static android.app.IInstrumentationWatcher asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_instrumentationStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_instrumentationFinished = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    }

    public void instrumentationStatus(android.content.ComponentName name, int resultCode, android.os.Bundle results) throws android.os.RemoteException;

    public void instrumentationFinished(android.content.ComponentName name, int resultCode, android.os.Bundle results) throws android.os.RemoteException;
}
