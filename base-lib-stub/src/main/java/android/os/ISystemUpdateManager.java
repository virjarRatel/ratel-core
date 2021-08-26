/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.os;
/** @hide */
public interface ISystemUpdateManager extends android.os.IInterface
{
  /** Default implementation for ISystemUpdateManager. */
  public static class Default implements android.os.ISystemUpdateManager
  {
    @Override public android.os.Bundle retrieveSystemUpdateInfo() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void updateSystemUpdateInfo(Object data) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.os.ISystemUpdateManager
  {
    private static final java.lang.String DESCRIPTOR = "android.os.ISystemUpdateManager";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.os.ISystemUpdateManager interface,
     * generating a proxy if needed.
     */
    public static android.os.ISystemUpdateManager asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.os.ISystemUpdateManager))) {
        return ((android.os.ISystemUpdateManager)iin);
      }
      return new android.os.ISystemUpdateManager.Stub.Proxy(obj);
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
        case TRANSACTION_retrieveSystemUpdateInfo:
        {
          return "retrieveSystemUpdateInfo";
        }
        case TRANSACTION_updateSystemUpdateInfo:
        {
          return "updateSystemUpdateInfo";
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
      throw new UnsupportedOperationException("STUB");
    }
    private static class Proxy implements android.os.ISystemUpdateManager
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
      @Override public android.os.Bundle retrieveSystemUpdateInfo() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_retrieveSystemUpdateInfo, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().retrieveSystemUpdateInfo();
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.os.Bundle.CREATOR.createFromParcel(_reply);
          }
          else {
            _result = null;
          }
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void updateSystemUpdateInfo(Object data) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      public static android.os.ISystemUpdateManager sDefaultImpl;
    }
    static final int TRANSACTION_retrieveSystemUpdateInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_updateSystemUpdateInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(android.os.ISystemUpdateManager impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.os.ISystemUpdateManager getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public android.os.Bundle retrieveSystemUpdateInfo() throws android.os.RemoteException;
  public void updateSystemUpdateInfo(Object data) throws android.os.RemoteException;
}
