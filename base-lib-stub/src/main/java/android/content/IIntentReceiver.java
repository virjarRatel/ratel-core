/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/content/IIntentReceiver.aidl
 */
package android.content;

/**
 * System private API for dispatching intent broadcasts.  This is given to the
 * activity manager as part of registering for an intent broadcasts, and is
 * called when it receives intents.
 * <p>
 * {@hide}
 */
public interface IIntentReceiver extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.content.IIntentReceiver {
        private static final java.lang.String DESCRIPTOR = "android.content.IIntentReceiver";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        /**
         * Cast an IBinder object into an android.content.IIntentReceiver interface,
         * generating a proxy if needed.
         */
        public static android.content.IIntentReceiver asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_performReceive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void performReceive(android.content.Intent intent, int resultCode, java.lang.String data, android.os.Bundle extras, boolean ordered, boolean sticky, int sendingUser) throws android.os.RemoteException;
}
