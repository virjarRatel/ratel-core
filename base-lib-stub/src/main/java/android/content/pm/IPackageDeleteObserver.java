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

public interface IPackageDeleteObserver extends IInterface {
    void packageDeleted(String var1, int var2) throws RemoteException;

    public abstract static class Stub extends Binder implements IPackageDeleteObserver {
        private static final String DESCRIPTOR = "android.content.pm.IPackageDeleteObserver";
        static final int TRANSACTION_packageDeleted = 1;

        public Stub() {
            this.attachInterface(this, "android.content.pm.IPackageDeleteObserver");
        }

        public static IPackageDeleteObserver asInterface(IBinder obj) {
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
