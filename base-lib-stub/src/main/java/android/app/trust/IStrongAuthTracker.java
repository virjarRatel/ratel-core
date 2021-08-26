/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.app.trust;
/**
 * Private API to be notified about strong auth changes
 *
 * {@hide}
 */
public interface IStrongAuthTracker extends android.os.IInterface
{
  /** Default implementation for IStrongAuthTracker. */
  public static class Default implements android.app.trust.IStrongAuthTracker
  {
    @Override public void onStrongAuthRequiredChanged(int strongAuthRequired, int userId) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.app.trust.IStrongAuthTracker
  {
    private static final java.lang.String DESCRIPTOR = "android.app.trust.IStrongAuthTracker";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.app.trust.IStrongAuthTracker interface,
     * generating a proxy if needed.
     */
    public static android.app.trust.IStrongAuthTracker asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.app.trust.IStrongAuthTracker))) {
        return ((android.app.trust.IStrongAuthTracker)iin);
      }
      return new android.app.trust.IStrongAuthTracker.Stub.Proxy(obj);
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
        case TRANSACTION_onStrongAuthRequiredChanged:
        {
          return "onStrongAuthRequiredChanged";
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
        case TRANSACTION_onStrongAuthRequiredChanged:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          int _arg1;
          _arg1 = data.readInt();
          this.onStrongAuthRequiredChanged(_arg0, _arg1);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.app.trust.IStrongAuthTracker
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
      @Override public void onStrongAuthRequiredChanged(int strongAuthRequired, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(strongAuthRequired);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onStrongAuthRequiredChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onStrongAuthRequiredChanged(strongAuthRequired, userId);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.app.trust.IStrongAuthTracker sDefaultImpl;
    }
    static final int TRANSACTION_onStrongAuthRequiredChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(android.app.trust.IStrongAuthTracker impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.app.trust.IStrongAuthTracker getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void onStrongAuthRequiredChanged(int strongAuthRequired, int userId) throws android.os.RemoteException;
}
