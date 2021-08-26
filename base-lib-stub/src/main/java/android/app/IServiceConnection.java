/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.app;
/** @hide */
public interface IServiceConnection extends android.os.IInterface
{
  /** Default implementation for IServiceConnection. */
  public static class Default implements android.app.IServiceConnection
  {
    @Override public void connected(android.content.ComponentName name, android.os.IBinder service, boolean dead) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.app.IServiceConnection
  {
    private static final java.lang.String DESCRIPTOR = "android.app.IServiceConnection";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.app.IServiceConnection interface,
     * generating a proxy if needed.
     */
    public static android.app.IServiceConnection asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.app.IServiceConnection))) {
        return ((android.app.IServiceConnection)iin);
      }
      return new android.app.IServiceConnection.Stub.Proxy(obj);
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
        case TRANSACTION_connected:
        {
          return "connected";
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
        case TRANSACTION_connected:
        {
          data.enforceInterface(descriptor);
          android.content.ComponentName _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.content.ComponentName.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          android.os.IBinder _arg1;
          _arg1 = data.readStrongBinder();
          boolean _arg2;
          _arg2 = (0!=data.readInt());
          this.connected(_arg0, _arg1, _arg2);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.app.IServiceConnection
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
      @Override public void connected(android.content.ComponentName name, android.os.IBinder service, boolean dead) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((name!=null)) {
            _data.writeInt(1);
            name.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeStrongBinder(service);
          _data.writeInt(((dead)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_connected, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().connected(name, service, dead);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.app.IServiceConnection sDefaultImpl;
    }
    static final int TRANSACTION_connected = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(android.app.IServiceConnection impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.app.IServiceConnection getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void connected(android.content.ComponentName name, android.os.IBinder service, boolean dead) throws android.os.RemoteException;
}
