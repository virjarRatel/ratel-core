/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/content/pm/IPackageMoveObserver.aidl
 */
package android.content.pm;

/**
 * Callback for moving package resources from the Package Manager.
 *
 */
public interface IPackageMoveObserver extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IPackageMoveObserver {
        private static final String DESCRIPTOR = "android.content.pm.IPackageMoveObserver";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.content.pm.IPackageMoveObserver interface,
         * generating a proxy if needed.
         */
        public static IPackageMoveObserver asInterface(android.os.IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            throw new UnsupportedOperationException("STUB");
        }

    }

    public void onCreated(int moveId, android.os.Bundle extras) throws android.os.RemoteException;

    public void onStatusChanged(int moveId, int status, long estMillis) throws android.os.RemoteException;
}
