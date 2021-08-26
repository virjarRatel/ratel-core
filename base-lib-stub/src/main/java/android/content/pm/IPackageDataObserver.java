/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/content/pm/IPackageDataObserver.aidl
 */
package android.content.pm;

/**
 * API for package data change related callbacks from the Package Manager.
 * Some usage scenarios include deletion of cache directory, generate
 * statistics related to code, data, cache usage(TODO)
 */
public interface IPackageDataObserver extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements IPackageDataObserver {
        private static final String DESCRIPTOR = "android.content.pm.IPackageDataObserver";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.content.pm.IPackageDataObserver interface,
         * generating a proxy if needed.
         */
        public static IPackageDataObserver asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof IPackageDataObserver))) {
                return ((IPackageDataObserver) iin);
            }
            return new Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_onRemoveCompleted: {
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0;
                    _arg0 = data.readString();
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    this.onRemoveCompleted(_arg0, _arg1);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements IPackageDataObserver {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override
            public void onRemoveCompleted(String packageName, boolean succeeded) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(((succeeded) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_onRemoveCompleted, _data, null, android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_onRemoveCompleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void onRemoveCompleted(String packageName, boolean succeeded) throws android.os.RemoteException;
}
