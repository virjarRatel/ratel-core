/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/content/pm/IPackageInstaller.aidl
 */
package android.content.pm;

/**
 * {@hide}
 */
public interface IPackageInstaller extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IPackageInstaller {
        private static final String DESCRIPTOR = "android.content.pm.IPackageInstaller";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.content.pm.IPackageInstaller interface,
         * generating a proxy if needed.
         */
        public static IPackageInstaller asInterface(android.os.IBinder obj) {
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

    public int createSession(Object params, String installerPackageName, int userId) throws android.os.RemoteException;

    public void updateSessionAppIcon(int sessionId, android.graphics.Bitmap appIcon) throws android.os.RemoteException;

    public void updateSessionAppLabel(int sessionId, String appLabel) throws android.os.RemoteException;

    public void abandonSession(int sessionId) throws android.os.RemoteException;

//    public android.content.pm.IPackageInstallerSession openSession(int sessionId) throws android.os.RemoteException;

    public Object getSessionInfo(int sessionId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice getAllSessions(int userId) throws android.os.RemoteException;

    public android.content.pm.ParceledListSlice getMySessions(String installerPackageName, int userId) throws android.os.RemoteException;

    public void registerCallback(android.content.pm.IPackageInstallerCallback callback, int userId) throws android.os.RemoteException;

    public void unregisterCallback(android.content.pm.IPackageInstallerCallback callback) throws android.os.RemoteException;

    public void uninstall(String packageName, String callerPackageName, int flags, android.content.IntentSender statusReceiver, int userId) throws android.os.RemoteException;

    public void setPermissionsResult(int sessionId, boolean accepted) throws android.os.RemoteException;
}
