/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.android.internal.widget;
/** {@hide} */
public interface ICheckCredentialProgressCallback extends android.os.IInterface
{
  /** Default implementation for ICheckCredentialProgressCallback. */
  public static class Default implements com.android.internal.widget.ICheckCredentialProgressCallback
  {
    @Override public void onCredentialVerified() throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.android.internal.widget.ICheckCredentialProgressCallback
  {
    private static final java.lang.String DESCRIPTOR = "com.android.internal.widget.ICheckCredentialProgressCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.internal.widget.ICheckCredentialProgressCallback interface,
     * generating a proxy if needed.
     */
    public static com.android.internal.widget.ICheckCredentialProgressCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.android.internal.widget.ICheckCredentialProgressCallback))) {
        return ((com.android.internal.widget.ICheckCredentialProgressCallback)iin);
      }
      return new com.android.internal.widget.ICheckCredentialProgressCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onCredentialVerified:
        {
          return "onCredentialVerified";
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
        case TRANSACTION_onCredentialVerified:
        {
          data.enforceInterface(descriptor);
          this.onCredentialVerified();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements com.android.internal.widget.ICheckCredentialProgressCallback
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
      @Override public void onCredentialVerified() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onCredentialVerified, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onCredentialVerified();
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static com.android.internal.widget.ICheckCredentialProgressCallback sDefaultImpl;
    }
    static final int TRANSACTION_onCredentialVerified = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(com.android.internal.widget.ICheckCredentialProgressCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static com.android.internal.widget.ICheckCredentialProgressCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void onCredentialVerified() throws android.os.RemoteException;
}
