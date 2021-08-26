/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.content;
/**
 * Callback for {@link ISyncAdapter#onUnsyncableAccount}
 * @hide
 */
public interface ISyncAdapterUnsyncableAccountCallback extends android.os.IInterface
{
  /** Default implementation for ISyncAdapterUnsyncableAccountCallback. */
  public static class Default implements android.content.ISyncAdapterUnsyncableAccountCallback
  {
    /**
         * Deliver the result for {@link ISyncAdapter#onUnsyncableAccount}
         *
         * @param isReady Iff {@code false} account is not synced.
         */
    @Override public void onUnsyncableAccountDone(boolean isReady) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.content.ISyncAdapterUnsyncableAccountCallback
  {
    private static final java.lang.String DESCRIPTOR = "android.content.ISyncAdapterUnsyncableAccountCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.content.ISyncAdapterUnsyncableAccountCallback interface,
     * generating a proxy if needed.
     */
    public static android.content.ISyncAdapterUnsyncableAccountCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.content.ISyncAdapterUnsyncableAccountCallback))) {
        return ((android.content.ISyncAdapterUnsyncableAccountCallback)iin);
      }
      return new android.content.ISyncAdapterUnsyncableAccountCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onUnsyncableAccountDone:
        {
          return "onUnsyncableAccountDone";
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
        case TRANSACTION_onUnsyncableAccountDone:
        {
          data.enforceInterface(descriptor);
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.onUnsyncableAccountDone(_arg0);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.content.ISyncAdapterUnsyncableAccountCallback
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
           * Deliver the result for {@link ISyncAdapter#onUnsyncableAccount}
           *
           * @param isReady Iff {@code false} account is not synced.
           */
      @Override public void onUnsyncableAccountDone(boolean isReady) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((isReady)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_onUnsyncableAccountDone, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onUnsyncableAccountDone(isReady);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.content.ISyncAdapterUnsyncableAccountCallback sDefaultImpl;
    }
    static final int TRANSACTION_onUnsyncableAccountDone = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(android.content.ISyncAdapterUnsyncableAccountCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.content.ISyncAdapterUnsyncableAccountCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  /**
       * Deliver the result for {@link ISyncAdapter#onUnsyncableAccount}
       *
       * @param isReady Iff {@code false} account is not synced.
       */
  public void onUnsyncableAccountDone(boolean isReady) throws android.os.RemoteException;
}
