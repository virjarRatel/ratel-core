/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.database;
/**
 * @hide
 */
public interface IContentObserver extends android.os.IInterface
{
  /** Default implementation for IContentObserver. */
  public static class Default implements android.database.IContentObserver
  {
    /**
         * This method is called when an update occurs to the cursor that is being
         * observed. selfUpdate is true if the update was caused by a call to
         * commit on the cursor that is being observed.
         */
    @Override public void onChange(boolean selfUpdate, android.net.Uri uri, int userId) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.database.IContentObserver
  {
    private static final java.lang.String DESCRIPTOR = "android.database.IContentObserver";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.database.IContentObserver interface,
     * generating a proxy if needed.
     */
    public static android.database.IContentObserver asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.database.IContentObserver))) {
        return ((android.database.IContentObserver)iin);
      }
      return new android.database.IContentObserver.Stub.Proxy(obj);
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
        case TRANSACTION_onChange:
        {
          return "onChange";
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
        case TRANSACTION_onChange:
        {
          data.enforceInterface(descriptor);
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          android.net.Uri _arg1;
          if ((0!=data.readInt())) {
            _arg1 = android.net.Uri.CREATOR.createFromParcel(data);
          }
          else {
            _arg1 = null;
          }
          int _arg2;
          _arg2 = data.readInt();
          this.onChange(_arg0, _arg1, _arg2);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.database.IContentObserver
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
      /**
           * This method is called when an update occurs to the cursor that is being
           * observed. selfUpdate is true if the update was caused by a call to
           * commit on the cursor that is being observed.
           */
      @Override public void onChange(boolean selfUpdate, android.net.Uri uri, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((selfUpdate)?(1):(0)));
          if ((uri!=null)) {
            _data.writeInt(1);
            uri.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onChange, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onChange(selfUpdate, uri, userId);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.database.IContentObserver sDefaultImpl;
    }
    static final int TRANSACTION_onChange = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(android.database.IContentObserver impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.database.IContentObserver getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * This method is called when an update occurs to the cursor that is being
       * observed. selfUpdate is true if the update was caused by a call to
       * commit on the cursor that is being observed.
       */
  public void onChange(boolean selfUpdate, android.net.Uri uri, int userId) throws android.os.RemoteException;
}
