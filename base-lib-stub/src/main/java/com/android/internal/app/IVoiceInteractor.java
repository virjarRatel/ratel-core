/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/com/android/internal/app/IVoiceInteractor.aidl
 */
package com.android.internal.app;

/**
 * IPC interface for an application to perform calls through a VoiceInteractor.
 */
public interface IVoiceInteractor extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements com.android.internal.app.IVoiceInteractor {
        private static final java.lang.String DESCRIPTOR = "com.android.internal.app.IVoiceInteractor";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an com.android.internal.app.IVoiceInteractor interface,
         * generating a proxy if needed.
         */
        public static com.android.internal.app.IVoiceInteractor asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        static final int TRANSACTION_startConfirmation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_startPickOption = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_startCompleteVoice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_startAbortVoice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_startCommand = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_supportsCommands = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    }

    public com.android.internal.app.IVoiceInteractorRequest startConfirmation(java.lang.String callingPackage, com.android.internal.app.IVoiceInteractorCallback callback, Object prompt, android.os.Bundle extras) throws android.os.RemoteException;

    public com.android.internal.app.IVoiceInteractorRequest startPickOption(java.lang.String callingPackage, com.android.internal.app.IVoiceInteractorCallback callback, Object prompt, Object[] options, android.os.Bundle extras) throws android.os.RemoteException;

    public com.android.internal.app.IVoiceInteractorRequest startCompleteVoice(java.lang.String callingPackage, com.android.internal.app.IVoiceInteractorCallback callback, Object prompt, android.os.Bundle extras) throws android.os.RemoteException;

    public com.android.internal.app.IVoiceInteractorRequest startAbortVoice(java.lang.String callingPackage, com.android.internal.app.IVoiceInteractorCallback callback, Object prompt, android.os.Bundle extras) throws android.os.RemoteException;

    public com.android.internal.app.IVoiceInteractorRequest startCommand(java.lang.String callingPackage, com.android.internal.app.IVoiceInteractorCallback callback, java.lang.String command, android.os.Bundle extras) throws android.os.RemoteException;

    public boolean[] supportsCommands(java.lang.String callingPackage, java.lang.String[] commands) throws android.os.RemoteException;
}
