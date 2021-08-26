/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.app;
/**
 * Callback to find out when we have finished stopping a user.
 * {@hide}
 */
public interface IStopUserCallback extends android.os.IInterface
{
  /** Default implementation for IStopUserCallback. */
  public static class Default implements android.app.IStopUserCallback
  {
    @Override public void userStopped(int userId) throws android.os.RemoteException
    {
    }
    @Override public void userStopAborted(int userId) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.app.IStopUserCallback
  {
    private static final java.lang.String DESCRIPTOR = "android.app.IStopUserCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.app.IStopUserCallback interface,
     * generating a proxy if needed.
     */
    public static android.app.IStopUserCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.app.IStopUserCallback))) {
        return ((android.app.IStopUserCallback)iin);
      }
      return new android.app.IStopUserCallback.Stub.Proxy(obj);
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
        case TRANSACTION_userStopped:
        {
          return "userStopped";
        }
        case TRANSACTION_userStopAborted:
        {
          return "userStopAborted";
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
        case TRANSACTION_userStopped:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.userStopped(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_userStopAborted:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.userStopAborted(_arg0);
          reply.writeNoException();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.app.IStopUserCallback
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
      @Override public void userStopped(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_userStopped, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().userStopped(userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void userStopAborted(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_userStopAborted, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().userStopAborted(userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static android.app.IStopUserCallback sDefaultImpl;
    }
    static final int TRANSACTION_userStopped = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_userStopAborted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(android.app.IStopUserCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.app.IStopUserCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }

  public void userStopped(int userId) throws android.os.RemoteException;
  public void userStopAborted(int userId) throws android.os.RemoteException;
}
