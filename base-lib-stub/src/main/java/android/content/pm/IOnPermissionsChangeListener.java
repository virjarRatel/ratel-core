/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/content/pm/IOnPermissionsChangeListener.aidl
 */
package android.content.pm;

/**
 * Listener for changes in the permissions for installed packages.
 */
public interface IOnPermissionsChangeListener extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IOnPermissionsChangeListener {
        private static final String DESCRIPTOR = "android.content.pm.IOnPermissionsChangeListener";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.content.pm.IOnPermissionsChangeListener interface,
         * generating a proxy if needed.
         */
        public static IOnPermissionsChangeListener asInterface(android.os.IBinder obj) {
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

    public void onPermissionsChanged(int uid) throws android.os.RemoteException;
}
