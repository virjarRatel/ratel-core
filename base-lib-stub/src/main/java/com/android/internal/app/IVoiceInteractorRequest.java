/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/com/android/internal/app/IVoiceInteractorRequest.aidl
 */
package com.android.internal.app;

/**
 * IPC interface identifying a request from an application calling through an IVoiceInteractor.
 */
public interface IVoiceInteractorRequest extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements com.android.internal.app.IVoiceInteractorRequest {
        private static final java.lang.String DESCRIPTOR = "com.android.internal.app.IVoiceInteractorRequest";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.android.internal.app.IVoiceInteractorRequest interface,
         * generating a proxy if needed.
         */
        public static com.android.internal.app.IVoiceInteractorRequest asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_cancel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void cancel() throws android.os.RemoteException;
}
