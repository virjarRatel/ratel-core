/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.location;
/**
 * {@hide}
 */
public interface ILocationListener extends android.os.IInterface
{
  /** Default implementation for ILocationListener. */
  public static class Default implements android.location.ILocationListener
  {
    @Override public void onLocationChanged(android.location.Location location) throws android.os.RemoteException
    {
    }
    @Override public void onStatusChanged(java.lang.String provider, int status, android.os.Bundle extras) throws android.os.RemoteException
    {
    }
    @Override public void onProviderEnabled(java.lang.String provider) throws android.os.RemoteException
    {
    }
    @Override public void onProviderDisabled(java.lang.String provider) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.location.ILocationListener
  {
    private static final java.lang.String DESCRIPTOR = "android.location.ILocationListener";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.location.ILocationListener interface,
     * generating a proxy if needed.
     */
    public static android.location.ILocationListener asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.location.ILocationListener))) {
        return ((android.location.ILocationListener)iin);
      }
      return new android.location.ILocationListener.Stub.Proxy(obj);
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
        case TRANSACTION_onLocationChanged:
        {
          return "onLocationChanged";
        }
        case TRANSACTION_onStatusChanged:
        {
          return "onStatusChanged";
        }
        case TRANSACTION_onProviderEnabled:
        {
          return "onProviderEnabled";
        }
        case TRANSACTION_onProviderDisabled:
        {
          return "onProviderDisabled";
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
        case TRANSACTION_onLocationChanged:
        {
          data.enforceInterface(descriptor);
          android.location.Location _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.location.Location.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          this.onLocationChanged(_arg0);
          return true;
        }
        case TRANSACTION_onStatusChanged:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          android.os.Bundle _arg2;
          if ((0!=data.readInt())) {
            _arg2 = android.os.Bundle.CREATOR.createFromParcel(data);
          }
          else {
            _arg2 = null;
          }
          this.onStatusChanged(_arg0, _arg1, _arg2);
          return true;
        }
        case TRANSACTION_onProviderEnabled:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.onProviderEnabled(_arg0);
          return true;
        }
        case TRANSACTION_onProviderDisabled:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.onProviderDisabled(_arg0);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.location.ILocationListener
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
      @Override public void onLocationChanged(android.location.Location location) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((location!=null)) {
            _data.writeInt(1);
            location.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_onLocationChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onLocationChanged(location);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onStatusChanged(java.lang.String provider, int status, android.os.Bundle extras) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(provider);
          _data.writeInt(status);
          if ((extras!=null)) {
            _data.writeInt(1);
            extras.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_onStatusChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onStatusChanged(provider, status, extras);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onProviderEnabled(java.lang.String provider) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(provider);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onProviderEnabled, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onProviderEnabled(provider);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onProviderDisabled(java.lang.String provider) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(provider);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onProviderDisabled, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onProviderDisabled(provider);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.location.ILocationListener sDefaultImpl;
    }
    static final int TRANSACTION_onLocationChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onStatusChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_onProviderEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_onProviderDisabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    public static boolean setDefaultImpl(android.location.ILocationListener impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.location.ILocationListener getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }

  public void onLocationChanged(android.location.Location location) throws android.os.RemoteException;

  public void onStatusChanged(java.lang.String provider, int status, android.os.Bundle extras) throws android.os.RemoteException;

  public void onProviderEnabled(java.lang.String provider) throws android.os.RemoteException;

  public void onProviderDisabled(java.lang.String provider) throws android.os.RemoteException;
}
