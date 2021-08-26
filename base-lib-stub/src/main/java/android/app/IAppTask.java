/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.app;
/** @hide */
public interface IAppTask extends android.os.IInterface
{
  /** Default implementation for IAppTask. */
  public static class Default implements android.app.IAppTask
  {
    @Override public void finishAndRemoveTask() throws android.os.RemoteException
    {
    }
    @Override public android.app.ActivityManager.RecentTaskInfo getTaskInfo() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void moveToFront() throws android.os.RemoteException
    {
    }
    @Override public int startActivity(android.os.IBinder whoThread, java.lang.String callingPackage, android.content.Intent intent, java.lang.String resolvedType, android.os.Bundle options) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public void setExcludeFromRecents(boolean exclude) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.app.IAppTask
  {
    private static final java.lang.String DESCRIPTOR = "android.app.IAppTask";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.app.IAppTask interface,
     * generating a proxy if needed.
     */
    public static android.app.IAppTask asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.app.IAppTask))) {
        return ((android.app.IAppTask)iin);
      }
      return new android.app.IAppTask.Stub.Proxy(obj);
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
        case TRANSACTION_finishAndRemoveTask:
        {
          return "finishAndRemoveTask";
        }
        case TRANSACTION_getTaskInfo:
        {
          return "getTaskInfo";
        }
        case TRANSACTION_moveToFront:
        {
          return "moveToFront";
        }
        case TRANSACTION_startActivity:
        {
          return "startActivity";
        }
        case TRANSACTION_setExcludeFromRecents:
        {
          return "setExcludeFromRecents";
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
        case TRANSACTION_finishAndRemoveTask:
        {
          data.enforceInterface(descriptor);
          this.finishAndRemoveTask();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_getTaskInfo:
        {
          data.enforceInterface(descriptor);
          android.app.ActivityManager.RecentTaskInfo _result = this.getTaskInfo();
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
        case TRANSACTION_moveToFront:
        {
          data.enforceInterface(descriptor);
          this.moveToFront();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_startActivity:
        {
          data.enforceInterface(descriptor);
          android.os.IBinder _arg0;
          _arg0 = data.readStrongBinder();
          java.lang.String _arg1;
          _arg1 = data.readString();
          android.content.Intent _arg2;
          if ((0!=data.readInt())) {
            _arg2 = android.content.Intent.CREATOR.createFromParcel(data);
          }
          else {
            _arg2 = null;
          }
          java.lang.String _arg3;
          _arg3 = data.readString();
          android.os.Bundle _arg4;
          if ((0!=data.readInt())) {
            _arg4 = android.os.Bundle.CREATOR.createFromParcel(data);
          }
          else {
            _arg4 = null;
          }
          int _result = this.startActivity(_arg0, _arg1, _arg2, _arg3, _arg4);
          reply.writeNoException();
          reply.writeInt(_result);
          return true;
        }
        case TRANSACTION_setExcludeFromRecents:
        {
          data.enforceInterface(descriptor);
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.setExcludeFromRecents(_arg0);
          reply.writeNoException();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.app.IAppTask
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
      @Override public void finishAndRemoveTask() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_finishAndRemoveTask, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().finishAndRemoveTask();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public android.app.ActivityManager.RecentTaskInfo getTaskInfo() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.app.ActivityManager.RecentTaskInfo _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getTaskInfo, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getTaskInfo();
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.app.ActivityManager.RecentTaskInfo.CREATOR.createFromParcel(_reply);
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
      @Override public void moveToFront() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_moveToFront, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().moveToFront();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public int startActivity(android.os.IBinder whoThread, java.lang.String callingPackage, android.content.Intent intent, java.lang.String resolvedType, android.os.Bundle options) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder(whoThread);
          _data.writeString(callingPackage);
          if ((intent!=null)) {
            _data.writeInt(1);
            intent.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeString(resolvedType);
          if ((options!=null)) {
            _data.writeInt(1);
            options.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_startActivity, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().startActivity(whoThread, callingPackage, intent, resolvedType, options);
          }
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void setExcludeFromRecents(boolean exclude) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((exclude)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_setExcludeFromRecents, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setExcludeFromRecents(exclude);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static android.app.IAppTask sDefaultImpl;
    }
    static final int TRANSACTION_finishAndRemoveTask = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getTaskInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_moveToFront = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_startActivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_setExcludeFromRecents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    public static boolean setDefaultImpl(android.app.IAppTask impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.app.IAppTask getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void finishAndRemoveTask() throws android.os.RemoteException;
  public android.app.ActivityManager.RecentTaskInfo getTaskInfo() throws android.os.RemoteException;
  public void moveToFront() throws android.os.RemoteException;
  public int startActivity(android.os.IBinder whoThread, java.lang.String callingPackage, android.content.Intent intent, java.lang.String resolvedType, android.os.Bundle options) throws android.os.RemoteException;
  public void setExcludeFromRecents(boolean exclude) throws android.os.RemoteException;
}
