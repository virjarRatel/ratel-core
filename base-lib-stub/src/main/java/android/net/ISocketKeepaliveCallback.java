/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.net;
/**
 * Callback to provide status changes of keepalive offload.
 *
 * @hide
 */
public interface ISocketKeepaliveCallback extends android.os.IInterface
{
  /** Default implementation for ISocketKeepaliveCallback. */
  public static class Default implements android.net.ISocketKeepaliveCallback
  {
    /** The keepalive was successfully started. */
    @Override public void onStarted(int slot) throws android.os.RemoteException
    {
    }
    /** The keepalive was successfully stopped. */
    @Override public void onStopped() throws android.os.RemoteException
    {
    }
    /** The keepalive was stopped because of an error. */
    @Override public void onError(int error) throws android.os.RemoteException
    {
    }
    /** The keepalive on a TCP socket was stopped because the socket received data. */
    @Override public void onDataReceived() throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.net.ISocketKeepaliveCallback
  {
    private static final java.lang.String DESCRIPTOR = "android.net.ISocketKeepaliveCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.net.ISocketKeepaliveCallback interface,
     * generating a proxy if needed.
     */
    public static android.net.ISocketKeepaliveCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.net.ISocketKeepaliveCallback))) {
        return ((android.net.ISocketKeepaliveCallback)iin);
      }
      return new android.net.ISocketKeepaliveCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onStarted:
        {
          return "onStarted";
        }
        case TRANSACTION_onStopped:
        {
          return "onStopped";
        }
        case TRANSACTION_onError:
        {
          return "onError";
        }
        case TRANSACTION_onDataReceived:
        {
          return "onDataReceived";
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
        case TRANSACTION_onStarted:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.onStarted(_arg0);
          return true;
        }
        case TRANSACTION_onStopped:
        {
          data.enforceInterface(descriptor);
          this.onStopped();
          return true;
        }
        case TRANSACTION_onError:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.onError(_arg0);
          return true;
        }
        case TRANSACTION_onDataReceived:
        {
          data.enforceInterface(descriptor);
          this.onDataReceived();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.net.ISocketKeepaliveCallback
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
      /** The keepalive was successfully started. */
      @Override public void onStarted(int slot) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(slot);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onStarted, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onStarted(slot);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      /** The keepalive was successfully stopped. */
      @Override public void onStopped() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onStopped, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onStopped();
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      /** The keepalive was stopped because of an error. */
      @Override public void onError(int error) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(error);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onError, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onError(error);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      /** The keepalive on a TCP socket was stopped because the socket received data. */
      @Override public void onDataReceived() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onDataReceived, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onDataReceived();
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.net.ISocketKeepaliveCallback sDefaultImpl;
    }
    static final int TRANSACTION_onStarted = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onStopped = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_onError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_onDataReceived = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    public static boolean setDefaultImpl(android.net.ISocketKeepaliveCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.net.ISocketKeepaliveCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /** The keepalive was successfully started. */
  public void onStarted(int slot) throws android.os.RemoteException;
  /** The keepalive was successfully stopped. */
  public void onStopped() throws android.os.RemoteException;
  /** The keepalive was stopped because of an error. */
  public void onError(int error) throws android.os.RemoteException;
  /** The keepalive on a TCP socket was stopped because the socket received data. */
  public void onDataReceived() throws android.os.RemoteException;
}
