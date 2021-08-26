/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.net;
/**
 * Callback class for receiving tethering changed events
 * @hide
 */
public interface ITetheringEventCallback extends android.os.IInterface
{
  /** Default implementation for ITetheringEventCallback. */
  public static class Default implements android.net.ITetheringEventCallback
  {
    @Override public void onUpstreamChanged(Object network) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.net.ITetheringEventCallback
  {
    private static final java.lang.String DESCRIPTOR = "android.net.ITetheringEventCallback";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.net.ITetheringEventCallback interface,
     * generating a proxy if needed.
     */
    public static android.net.ITetheringEventCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.net.ITetheringEventCallback))) {
        return ((android.net.ITetheringEventCallback)iin);
      }
      return new android.net.ITetheringEventCallback.Stub.Proxy(obj);
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
        case TRANSACTION_onUpstreamChanged:
        {
          return "onUpstreamChanged";
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
    private static class Proxy implements android.net.ITetheringEventCallback
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
      @Override public void onUpstreamChanged(Object network) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      public static android.net.ITetheringEventCallback sDefaultImpl;
    }
    static final int TRANSACTION_onUpstreamChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    public static boolean setDefaultImpl(android.net.ITetheringEventCallback impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.net.ITetheringEventCallback getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public void onUpstreamChanged(Object network) throws android.os.RemoteException;
}
