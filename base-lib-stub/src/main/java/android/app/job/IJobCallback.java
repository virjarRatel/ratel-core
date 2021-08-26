/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.app.job;
/**
 * The server side of the JobScheduler IPC protocols.  The app-side implementation
 * invokes on this interface to indicate completion of the (asynchronous) instructions
 * issued by the server.
 *
 * In all cases, the 'who' parameter is the caller's service binder, used to track
 * which Job Service instance is reporting.
 *
 * {@hide}
 */
public interface IJobCallback extends android.os.IInterface
{
  /** Default implementation for IJobCallback. */
  public static class Default implements android.app.job.IJobCallback
  {
    /**
         * Immediate callback to the system after sending a start signal, used to quickly detect ANR.
         *
         * @param jobId Unique integer used to identify this job.
         * @param ongoing True to indicate that the client is processing the job. False if the job is
         * complete
         */
    @Override public void acknowledgeStartMessage(int jobId, boolean ongoing) throws android.os.RemoteException
    {
    }
    /**
         * Immediate callback to the system after sending a stop signal, used to quickly detect ANR.
         *
         * @param jobId Unique integer used to identify this job.
         * @param reschedule Whether or not to reschedule this job.
         */
    @Override public void acknowledgeStopMessage(int jobId, boolean reschedule) throws android.os.RemoteException
    {
    }
    /*
         * Called to deqeue next work item for the job.
         */
    @Override public android.app.job.JobWorkItem dequeueWork(int jobId) throws android.os.RemoteException
    {
      return null;
    }
    /*
         * Called to report that job has completed processing a work item.
         */
    @Override public boolean completeWork(int jobId, int workId) throws android.os.RemoteException
    {
      return false;
    }
    /*
         * Tell the job manager that the client is done with its execution, so that it can go on to
         * the next one and stop attributing wakelock time to us etc.
         *
         * @param jobId Unique integer used to identify this job.
         * @param reschedule Whether or not to reschedule this job.
         */
    @Override public void jobFinished(int jobId, boolean reschedule) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.app.job.IJobCallback
  {
    private static final java.lang.String DESCRIPTOR = "android.app.job.IJobCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.app.job.IJobCallback interface,
     * generating a proxy if needed.
     */
    public static android.app.job.IJobCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.app.job.IJobCallback))) {
        return ((android.app.job.IJobCallback)iin);
      }
      return new android.app.job.IJobCallback.Stub.Proxy(obj);
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
        case TRANSACTION_acknowledgeStartMessage:
        {
          return "acknowledgeStartMessage";
        }
        case TRANSACTION_acknowledgeStopMessage:
        {
          return "acknowledgeStopMessage";
        }
        case TRANSACTION_dequeueWork:
        {
          return "dequeueWork";
        }
        case TRANSACTION_completeWork:
        {
          return "completeWork";
        }
        case TRANSACTION_jobFinished:
        {
          return "jobFinished";
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
        case TRANSACTION_acknowledgeStartMessage:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.acknowledgeStartMessage(_arg0, _arg1);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_acknowledgeStopMessage:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.acknowledgeStopMessage(_arg0, _arg1);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_dequeueWork:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          android.app.job.JobWorkItem _result = this.dequeueWork(_arg0);
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
        case TRANSACTION_completeWork:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          int _arg1;
          _arg1 = data.readInt();
          boolean _result = this.completeWork(_arg0, _arg1);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          return true;
        }
        case TRANSACTION_jobFinished:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          this.jobFinished(_arg0, _arg1);
          reply.writeNoException();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.app.job.IJobCallback
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
           * Immediate callback to the system after sending a start signal, used to quickly detect ANR.
           *
           * @param jobId Unique integer used to identify this job.
           * @param ongoing True to indicate that the client is processing the job. False if the job is
           * complete
           */
      @Override public void acknowledgeStartMessage(int jobId, boolean ongoing) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(jobId);
          _data.writeInt(((ongoing)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_acknowledgeStartMessage, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().acknowledgeStartMessage(jobId, ongoing);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
           * Immediate callback to the system after sending a stop signal, used to quickly detect ANR.
           *
           * @param jobId Unique integer used to identify this job.
           * @param reschedule Whether or not to reschedule this job.
           */
      @Override public void acknowledgeStopMessage(int jobId, boolean reschedule) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(jobId);
          _data.writeInt(((reschedule)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_acknowledgeStopMessage, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().acknowledgeStopMessage(jobId, reschedule);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /*
           * Called to deqeue next work item for the job.
           */
      @Override public android.app.job.JobWorkItem dequeueWork(int jobId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.app.job.JobWorkItem _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(jobId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_dequeueWork, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().dequeueWork(jobId);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.app.job.JobWorkItem.CREATOR.createFromParcel(_reply);
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
      /*
           * Called to report that job has completed processing a work item.
           */
      @Override public boolean completeWork(int jobId, int workId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(jobId);
          _data.writeInt(workId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_completeWork, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().completeWork(jobId, workId);
          }
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /*
           * Tell the job manager that the client is done with its execution, so that it can go on to
           * the next one and stop attributing wakelock time to us etc.
           *
           * @param jobId Unique integer used to identify this job.
           * @param reschedule Whether or not to reschedule this job.
           */
      @Override public void jobFinished(int jobId, boolean reschedule) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(jobId);
          _data.writeInt(((reschedule)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_jobFinished, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().jobFinished(jobId, reschedule);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static android.app.job.IJobCallback sDefaultImpl;
    }
    static final int TRANSACTION_acknowledgeStartMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_acknowledgeStopMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_dequeueWork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_completeWork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_jobFinished = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    public static boolean setDefaultImpl(android.app.job.IJobCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.app.job.IJobCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * Immediate callback to the system after sending a start signal, used to quickly detect ANR.
       *
       * @param jobId Unique integer used to identify this job.
       * @param ongoing True to indicate that the client is processing the job. False if the job is
       * complete
       */
  
  public void acknowledgeStartMessage(int jobId, boolean ongoing) throws android.os.RemoteException;
  /**
       * Immediate callback to the system after sending a stop signal, used to quickly detect ANR.
       *
       * @param jobId Unique integer used to identify this job.
       * @param reschedule Whether or not to reschedule this job.
       */
  
  public void acknowledgeStopMessage(int jobId, boolean reschedule) throws android.os.RemoteException;
  /*
       * Called to deqeue next work item for the job.
       */
  
  public android.app.job.JobWorkItem dequeueWork(int jobId) throws android.os.RemoteException;
  /*
       * Called to report that job has completed processing a work item.
       */
  
  public boolean completeWork(int jobId, int workId) throws android.os.RemoteException;
  /*
       * Tell the job manager that the client is done with its execution, so that it can go on to
       * the next one and stop attributing wakelock time to us etc.
       *
       * @param jobId Unique integer used to identify this job.
       * @param reschedule Whether or not to reschedule this job.
       */
  
  public void jobFinished(int jobId, boolean reschedule) throws android.os.RemoteException;
}
