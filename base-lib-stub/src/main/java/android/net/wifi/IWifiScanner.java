/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.net.wifi;
/**
 * {@hide}
 */
public interface IWifiScanner extends android.os.IInterface
{
  /** Default implementation for IWifiScanner. */
  public static class Default implements android.net.wifi.IWifiScanner
  {
    @Override public android.os.Messenger getMessenger() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.os.Bundle getAvailableChannels(int band) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.net.wifi.IWifiScanner
  {
    private static final java.lang.String DESCRIPTOR = "android.net.wifi.IWifiScanner";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.net.wifi.IWifiScanner interface,
     * generating a proxy if needed.
     */
    public static android.net.wifi.IWifiScanner asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.net.wifi.IWifiScanner))) {
        return ((android.net.wifi.IWifiScanner)iin);
      }
      return new android.net.wifi.IWifiScanner.Stub.Proxy(obj);
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
        case TRANSACTION_getMessenger:
        {
          return "getMessenger";
        }
        case TRANSACTION_getAvailableChannels:
        {
          return "getAvailableChannels";
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
        case TRANSACTION_getMessenger:
        {
          data.enforceInterface(descriptor);
          android.os.Messenger _result = this.getMessenger();
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        case TRANSACTION_getAvailableChannels:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          android.os.Bundle _result = this.getAvailableChannels(_arg0);
          reply.writeNoException();
          if ((_result!=null)) {
            reply.writeInt(1);
            _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          }
          else {
            reply.writeInt(0);
          }
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.net.wifi.IWifiScanner
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
      @Override public android.os.Messenger getMessenger() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Messenger _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getMessenger, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getMessenger();
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.os.Messenger.CREATOR.createFromParcel(_reply);
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
      @Override public android.os.Bundle getAvailableChannels(int band) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(band);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getAvailableChannels, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getAvailableChannels(band);
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
      public static android.net.wifi.IWifiScanner sDefaultImpl;
    }
    static final int TRANSACTION_getMessenger = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getAvailableChannels = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(android.net.wifi.IWifiScanner impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.net.wifi.IWifiScanner getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public android.os.Messenger getMessenger() throws android.os.RemoteException;
  public android.os.Bundle getAvailableChannels(int band) throws android.os.RemoteException;
}
