/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.content;
/**
 * @hide
 */
public interface ISyncStatusObserver extends android.os.IInterface
{
  /** Default implementation for ISyncStatusObserver. */
  public static class Default implements android.content.ISyncStatusObserver
  {
    @Override public void onStatusChanged(int which) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.content.ISyncStatusObserver
  {
    private static final java.lang.String DESCRIPTOR = "android.content.ISyncStatusObserver";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.content.ISyncStatusObserver interface,
     * generating a proxy if needed.
     */
    public static android.content.ISyncStatusObserver asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.content.ISyncStatusObserver))) {
        return ((android.content.ISyncStatusObserver)iin);
      }
      return new android.content.ISyncStatusObserver.Stub.Proxy(obj);
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
        case TRANSACTION_onStatusChanged:
        {
          return "onStatusChanged";
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
        case TRANSACTION_onStatusChanged:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.onStatusChanged(_arg0);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.content.ISyncStatusObserver
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
      @Override public void onStatusChanged(int which) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(which);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onStatusChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onStatusChanged(which);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.content.ISyncStatusObserver sDefaultImpl;
    }
    static final int TRANSACTION_onStatusChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(android.content.ISyncStatusObserver impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.content.ISyncStatusObserver getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }

  public void onStatusChanged(int which) throws android.os.RemoteException;
}
