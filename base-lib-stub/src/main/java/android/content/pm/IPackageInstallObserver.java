//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageInstallObserver extends IInterface {
    void packageInstalled(String var1, int var2) throws RemoteException;

    public abstract static class Stub extends Binder implements IPackageInstallObserver {
        private static final String DESCRIPTOR = "android.content.pm.IPackageInstallObserver";
        static final int TRANSACTION_packageInstalled = 1;

        public Stub() {
            this.attachInterface(this, "android.content.pm.IPackageInstallObserver");
        }

        public static IPackageInstallObserver asInterface(IBinder obj) {
            throw new UnsupportedOperationException("STUB");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            throw new UnsupportedOperationException("STUB");
        }
    }
}
