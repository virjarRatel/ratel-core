/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.content.pm;
/** {@hide} */
public interface IPackageInstallerCallback extends android.os.IInterface
{
  /** Default implementation for IPackageInstallerCallback. */
  public static class Default implements android.content.pm.IPackageInstallerCallback
  {
    @Override public void onSessionCreated(int sessionId) throws android.os.RemoteException
    {
    }
    @Override public void onSessionBadgingChanged(int sessionId) throws android.os.RemoteException
    {
    }
    @Override public void onSessionActiveChanged(int sessionId, boolean active) throws android.os.RemoteException
    {
    }
    @Override public void onSessionProgressChanged(int sessionId, float progress) throws android.os.RemoteException
    {
    }
    @Override public void onSessionFinished(int sessionId, boolean success) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.content.pm.IPackageInstallerCallback
  {
    private static final java.lang.String DESCRIPTOR = "android.content.pm.IPackageInstallerCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.content.pm.IPackageInstallerCallback interface,
     * generating a proxy if needed.
     */
    public static android.content.pm.IPackageInstallerCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.content.pm.IPackageInstallerCallback))) {
        return ((android.content.pm.IPackageInstallerCallback)iin);
      }
      return new android.content.pm.IPackageInstallerCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onSessionCreated:
        {
          return "onSessionCreated";
        }
        case TRANSACTION_onSessionBadgingChanged:
        {
          return "onSessionBadgingChanged";
        }
        case TRANSACTION_onSessionActiveChanged:
        {
          return "onSessionActiveChanged";
        }
        case TRANSACTION_onSessionProgressChanged:
        {
          return "onSessionProgressChanged";
        }
        case TRANSACTION_onSessionFinished:
        {
          return "onSessionFinished";
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
        case TRANSACTION_onSessionCreated:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.onSessionCreated(_arg0);
          return true;
        }
        case TRANSACTION_onSessionBadgingChanged:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.onSessionBadgingChanged(_arg0);
          return true;
        }
        case TRANSACTION_onSessionActiveChanged:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.onSessionActiveChanged(_arg0, _arg1);
          return true;
        }
        case TRANSACTION_onSessionProgressChanged:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          float _arg1;
          _arg1 = data.readFloat();
          this.onSessionProgressChanged(_arg0, _arg1);
          return true;
        }
        case TRANSACTION_onSessionFinished:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.onSessionFinished(_arg0, _arg1);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.content.pm.IPackageInstallerCallback
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
      @Override public void onSessionCreated(int sessionId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(sessionId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSessionCreated, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onSessionCreated(sessionId);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onSessionBadgingChanged(int sessionId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(sessionId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSessionBadgingChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onSessionBadgingChanged(sessionId);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onSessionActiveChanged(int sessionId, boolean active) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(sessionId);
          _data.writeInt(((active)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSessionActiveChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onSessionActiveChanged(sessionId, active);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onSessionProgressChanged(int sessionId, float progress) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(sessionId);
          _data.writeFloat(progress);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSessionProgressChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onSessionProgressChanged(sessionId, progress);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void onSessionFinished(int sessionId, boolean success) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(sessionId);
          _data.writeInt(((success)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSessionFinished, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onSessionFinished(sessionId, success);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.content.pm.IPackageInstallerCallback sDefaultImpl;
    }
    static final int TRANSACTION_onSessionCreated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onSessionBadgingChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_onSessionActiveChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_onSessionProgressChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_onSessionFinished = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    public static boolean setDefaultImpl(android.content.pm.IPackageInstallerCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.content.pm.IPackageInstallerCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }

  public void onSessionCreated(int sessionId) throws android.os.RemoteException;

  public void onSessionBadgingChanged(int sessionId) throws android.os.RemoteException;

  public void onSessionActiveChanged(int sessionId, boolean active) throws android.os.RemoteException;

  public void onSessionProgressChanged(int sessionId, float progress) throws android.os.RemoteException;

  public void onSessionFinished(int sessionId, boolean success) throws android.os.RemoteException;
}
