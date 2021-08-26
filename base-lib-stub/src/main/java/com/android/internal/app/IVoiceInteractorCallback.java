/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/com/android/internal/app/IVoiceInteractorCallback.aidl
 */
package com.android.internal.app;

/**
 * IPC interface for an application to receive callbacks from the voice system.
 */
public interface IVoiceInteractorCallback extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements com.android.internal.app.IVoiceInteractorCallback {
        private static final java.lang.String DESCRIPTOR = "com.android.internal.app.IVoiceInteractorCallback";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.android.internal.app.IVoiceInteractorCallback interface,
         * generating a proxy if needed.
         */
        public static com.android.internal.app.IVoiceInteractorCallback asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_deliverConfirmationResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_deliverPickOptionResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_deliverCompleteVoiceResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_deliverAbortVoiceResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_deliverCommandResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_deliverCancel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    }

    public void deliverConfirmationResult(com.android.internal.app.IVoiceInteractorRequest request, boolean confirmed, android.os.Bundle result) throws android.os.RemoteException;

    public void deliverPickOptionResult(com.android.internal.app.IVoiceInteractorRequest request, boolean finished, Object[] selections, android.os.Bundle result) throws android.os.RemoteException;

    public void deliverCompleteVoiceResult(com.android.internal.app.IVoiceInteractorRequest request, android.os.Bundle result) throws android.os.RemoteException;

    public void deliverAbortVoiceResult(com.android.internal.app.IVoiceInteractorRequest request, android.os.Bundle result) throws android.os.RemoteException;

    public void deliverCommandResult(com.android.internal.app.IVoiceInteractorRequest request, boolean finished, android.os.Bundle result) throws android.os.RemoteException;

    public void deliverCancel(com.android.internal.app.IVoiceInteractorRequest request) throws android.os.RemoteException;
}
