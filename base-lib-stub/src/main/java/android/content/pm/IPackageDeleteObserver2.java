/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.content.pm;
/** {@hide} */
public interface IPackageDeleteObserver2 extends android.os.IInterface
{
  /** Default implementation for IPackageDeleteObserver2. */
  public static class Default implements android.content.pm.IPackageDeleteObserver2
  {
    @Override public void onUserActionRequired(android.content.Intent intent) throws android.os.RemoteException
    {
    }
    @Override public void onPackageDeleted(java.lang.String packageName, int returnCode, java.lang.String msg) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.content.pm.IPackageDeleteObserver2
  {
    private static final java.lang.String DESCRIPTOR = "android.content.pm.IPackageDeleteObserver2";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.content.pm.IPackageDeleteObserver2 interface,
     * generating a proxy if needed.
     */
    public static android.content.pm.IPackageDeleteObserver2 asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.content.pm.IPackageDeleteObserver2))) {
        return ((android.content.pm.IPackageDeleteObserver2)iin);
      }
      return new android.content.pm.IPackageDeleteObserver2.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    /** @hide */
    public static java.lang.String getDefaultTransactionName(int transactionCode)
    {
      switch (transactionCode)
      {
        case TRANSACTION_onUserActionRequired:
        {
          return "onUserActionRequired";
        }
        case TRANSACTION_onPackageDeleted:
        {
          return "onPackageDeleted";
        }
        default:
        {
          return null;
        }
      }
    }
    /** @hide */
    public java.lang.String getTransactionName(int transactionCode)
    {
      return this.getDefaultTransactionName(transactionCode);
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_onUserActionRequired:
        {
          data.enforceInterface(descriptor);
          android.content.Intent _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.content.Intent.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          this.onUserActionRequired(_arg0);
          return true;
        }
        case TRANSACTION_onPackageDeleted:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          java.lang.String _arg2;
          _arg2 = data.readString();
          this.onPackageDeleted(_arg0, _arg1, _arg2);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.content.pm.IPackageDeleteObserver2
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public void onUserActionRequired(android.content.Intent intent) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((intent!=null)) {
            _data.writeInt(1);
            intent.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_onUserActionRequired, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onUserActionRequired(intent);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onPackageDeleted(java.lang.String packageName, int returnCode, java.lang.String msg) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packageName);
          _data.writeInt(returnCode);
          _data.writeString(msg);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPackageDeleted, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onPackageDeleted(packageName, returnCode, msg);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.content.pm.IPackageDeleteObserver2 sDefaultImpl;
    }
    static final int TRANSACTION_onUserActionRequired = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onPackageDeleted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(android.content.pm.IPackageDeleteObserver2 impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.content.pm.IPackageDeleteObserver2 getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void onUserActionRequired(android.content.Intent intent) throws android.os.RemoteException;

  public void onPackageDeleted(java.lang.String packageName, int returnCode, java.lang.String msg) throws android.os.RemoteException;
}
