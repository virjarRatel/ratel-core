/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.app.job;
/**
  * IPC interface that supports the app-facing {@link #JobScheduler} api.
  * {@hide}
  */
public interface IJobScheduler extends android.os.IInterface
{
  /** Default implementation for IJobScheduler. */
  public static class Default implements android.app.job.IJobScheduler
  {
    @Override public int schedule(android.app.job.JobInfo job) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public int enqueue(android.app.job.JobInfo job, android.app.job.JobWorkItem work) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public int scheduleAsPackage(android.app.job.JobInfo job, java.lang.String packageName, int userId, java.lang.String tag) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public void cancel(int jobId) throws android.os.RemoteException
    {
    }
    @Override public void cancelAll() throws android.os.RemoteException
    {
    }
    @Override public java.util.List<android.app.job.JobInfo> getAllPendingJobs() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.app.job.JobInfo getPendingJob(int jobId) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.app.job.IJobScheduler
  {
    private static final java.lang.String DESCRIPTOR = "android.app.job.IJobScheduler";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.app.job.IJobScheduler interface,
     * generating a proxy if needed.
     */
    public static android.app.job.IJobScheduler asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.app.job.IJobScheduler))) {
        return ((android.app.job.IJobScheduler)iin);
      }
      return new android.app.job.IJobScheduler.Stub.Proxy(obj);
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
        case TRANSACTION_schedule:
        {
          return "schedule";
        }
        case TRANSACTION_enqueue:
        {
          return "enqueue";
        }
        case TRANSACTION_scheduleAsPackage:
        {
          return "scheduleAsPackage";
        }
        case TRANSACTION_cancel:
        {
          return "cancel";
        }
        case TRANSACTION_cancelAll:
        {
          return "cancelAll";
        }
        case TRANSACTION_getAllPendingJobs:
        {
          return "getAllPendingJobs";
        }
        case TRANSACTION_getPendingJob:
        {
          return "getPendingJob";
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
        case TRANSACTION_schedule:
        {
          data.enforceInterface(descriptor);
          android.app.job.JobInfo _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.app.job.JobInfo.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          int _result = this.schedule(_arg0);
          reply.writeNoException();
          reply.writeInt(_result);
          return true;
        }
        case TRANSACTION_enqueue:
        {
          data.enforceInterface(descriptor);
          android.app.job.JobInfo _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.app.job.JobInfo.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          android.app.job.JobWorkItem _arg1;
          if ((0!=data.readInt())) {
            _arg1 = android.app.job.JobWorkItem.CREATOR.createFromParcel(data);
          }
          else {
            _arg1 = null;
          }
          int _result = this.enqueue(_arg0, _arg1);
          reply.writeNoException();
          reply.writeInt(_result);
          return true;
        }
        case TRANSACTION_scheduleAsPackage:
        {
          data.enforceInterface(descriptor);
          android.app.job.JobInfo _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.app.job.JobInfo.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          java.lang.String _arg1;
          _arg1 = data.readString();
          int _arg2;
          _arg2 = data.readInt();
          java.lang.String _arg3;
          _arg3 = data.readString();
          int _result = this.scheduleAsPackage(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          reply.writeInt(_result);
          return true;
        }
        case TRANSACTION_cancel:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.cancel(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_cancelAll:
        {
          data.enforceInterface(descriptor);
          this.cancelAll();
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_getAllPendingJobs:
        {
          data.enforceInterface(descriptor);
          java.util.List<android.app.job.JobInfo> _result = this.getAllPendingJobs();
          reply.writeNoException();
          reply.writeTypedList(_result);
          return true;
        }
        case TRANSACTION_getPendingJob:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          android.app.job.JobInfo _result = this.getPendingJob(_arg0);
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
    private static class Proxy implements android.app.job.IJobScheduler
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
      @Override public int schedule(android.app.job.JobInfo job) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((job!=null)) {
            _data.writeInt(1);
            job.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_schedule, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().schedule(job);
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
      @Override public int enqueue(android.app.job.JobInfo job, android.app.job.JobWorkItem work) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((job!=null)) {
            _data.writeInt(1);
            job.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((work!=null)) {
            _data.writeInt(1);
            work.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_enqueue, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().enqueue(job, work);
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
      @Override public int scheduleAsPackage(android.app.job.JobInfo job, java.lang.String packageName, int userId, java.lang.String tag) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((job!=null)) {
            _data.writeInt(1);
            job.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeString(packageName);
          _data.writeInt(userId);
          _data.writeString(tag);
          boolean _status = mRemote.transact(Stub.TRANSACTION_scheduleAsPackage, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().scheduleAsPackage(job, packageName, userId, tag);
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
      @Override public void cancel(int jobId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(jobId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_cancel, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().cancel(jobId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void cancelAll() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_cancelAll, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().cancelAll();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public java.util.List<android.app.job.JobInfo> getAllPendingJobs() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.List<android.app.job.JobInfo> _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getAllPendingJobs, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getAllPendingJobs();
          }
          _reply.readException();
          _result = _reply.createTypedArrayList(android.app.job.JobInfo.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public android.app.job.JobInfo getPendingJob(int jobId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.app.job.JobInfo _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(jobId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPendingJob, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getPendingJob(jobId);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.app.job.JobInfo.CREATOR.createFromParcel(_reply);
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
      public static android.app.job.IJobScheduler sDefaultImpl;
    }
    static final int TRANSACTION_schedule = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_enqueue = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_scheduleAsPackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_cancel = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_cancelAll = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_getAllPendingJobs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_getPendingJob = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    public static boolean setDefaultImpl(android.app.job.IJobScheduler impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.app.job.IJobScheduler getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public int schedule(android.app.job.JobInfo job) throws android.os.RemoteException;
  public int enqueue(android.app.job.JobInfo job, android.app.job.JobWorkItem work) throws android.os.RemoteException;
  public int scheduleAsPackage(android.app.job.JobInfo job, java.lang.String packageName, int userId, java.lang.String tag) throws android.os.RemoteException;
  public void cancel(int jobId) throws android.os.RemoteException;
  public void cancelAll() throws android.os.RemoteException;
  public java.util.List<android.app.job.JobInfo> getAllPendingJobs() throws android.os.RemoteException;
  public android.app.job.JobInfo getPendingJob(int jobId) throws android.os.RemoteException;
}
