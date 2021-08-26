/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.app.job;
/**
 * Interface that the framework uses to communicate with application code that implements a
 * JobService.  End user code does not implement this interface directly; instead, the app's
 * service implementation will extend android.app.job.JobService.
 * {@hide}
 */
public interface IJobService extends android.os.IInterface
{
  /** Default implementation for IJobService. */
  public static class Default implements android.app.job.IJobService
  {
    /** Begin execution of application's job. */
    @Override public void startJob(Object jobParams) throws android.os.RemoteException
    {
    }
    /** Stop execution of application's job. */
    @Override public void stopJob(Object jobParams) throws android.os.RemoteException
    {
      throw new UnsupportedOperationException("STUB");
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.app.job.IJobService
  {
    private static final java.lang.String DESCRIPTOR = "android.app.job.IJobService";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.app.job.IJobService interface,
     * generating a proxy if needed.
     */
    public static android.app.job.IJobService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.app.job.IJobService))) {
        return ((android.app.job.IJobService)iin);
      }
      return new android.app.job.IJobService.Stub.Proxy(obj);
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
        case TRANSACTION_startJob:
        {
          return "startJob";
        }
        case TRANSACTION_stopJob:
        {
          return "stopJob";
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
      throw new UnsupportedOperationException("STUB");
    }
    private static class Proxy implements android.app.job.IJobService
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
      /** Begin execution of application's job. */
      @Override public void startJob(Object jobParams) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      /** Stop execution of application's job. */
      @Override public void stopJob(Object jobParams) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      public static android.app.job.IJobService sDefaultImpl;
    }
    static final int TRANSACTION_startJob = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_stopJob = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(android.app.job.IJobService impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.app.job.IJobService getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /** Begin execution of application's job. */
  
  public void startJob(Object jobParams) throws android.os.RemoteException;
  /** Stop execution of application's job. */
  
  public void stopJob(Object jobParams) throws android.os.RemoteException;
}
