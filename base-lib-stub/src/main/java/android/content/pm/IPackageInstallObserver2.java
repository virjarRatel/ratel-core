/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.content.pm;
/**
 * API for installation callbacks from the Package Manager.  In certain result cases
 * additional information will be provided.
 * @hide
 */
public interface IPackageInstallObserver2 extends android.os.IInterface
{
  /** Default implementation for IPackageInstallObserver2. */
  public static class Default implements android.content.pm.IPackageInstallObserver2
  {
    @Override public void onUserActionRequired(android.content.Intent intent) throws android.os.RemoteException
    {
    }
    /**
         * The install operation has completed.  {@code returnCode} holds a numeric code
         * indicating success or failure.  In certain cases the {@code extras} Bundle will
         * contain additional details:
         *
         * <p><table>
         * <tr>
         *   <td>INSTALL_FAILED_DUPLICATE_PERMISSION</td>
         *   <td>Two strings are provided in the extras bundle: EXTRA_EXISTING_PERMISSION
         *       is the name of the permission that the app is attempting to define, and
         *       EXTRA_EXISTING_PACKAGE is the package name of the app which has already
         *       defined the permission.</td>
         * </tr>
         * </table>
         */
    @Override public void onPackageInstalled(java.lang.String basePackageName, int returnCode, java.lang.String msg, android.os.Bundle extras) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.content.pm.IPackageInstallObserver2
  {
    private static final java.lang.String DESCRIPTOR = "android.content.pm.IPackageInstallObserver2";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.content.pm.IPackageInstallObserver2 interface,
     * generating a proxy if needed.
     */
    public static android.content.pm.IPackageInstallObserver2 asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.content.pm.IPackageInstallObserver2))) {
        return ((android.content.pm.IPackageInstallObserver2)iin);
      }
      return new android.content.pm.IPackageInstallObserver2.Stub.Proxy(obj);
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
        case TRANSACTION_onUserActionRequired:
        {
          return "onUserActionRequired";
        }
        case TRANSACTION_onPackageInstalled:
        {
          return "onPackageInstalled";
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
        case TRANSACTION_onUserActionRequired:
        {
          data.enforceInterface(descriptor);
          android.content.Intent _arg0;
          if ((0!=data.readInt())) {
            _arg0 = android.content.Intent.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          this.onUserActionRequired(_arg0);
          return true;
        }
        case TRANSACTION_onPackageInstalled:
        {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          java.lang.String _arg2;
          _arg2 = data.readString();
          android.os.Bundle _arg3;
          if ((0!=data.readInt())) {
            _arg3 = android.os.Bundle.CREATOR.createFromParcel(data);
          }
          else {
            _arg3 = null;
          }
          this.onPackageInstalled(_arg0, _arg1, _arg2, _arg3);
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements android.content.pm.IPackageInstallObserver2
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
      @Override public void onUserActionRequired(android.content.Intent intent) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((intent!=null)) {
            _data.writeInt(1);
            intent.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_onUserActionRequired, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onUserActionRequired(intent);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      /**
           * The install operation has completed.  {@code returnCode} holds a numeric code
           * indicating success or failure.  In certain cases the {@code extras} Bundle will
           * contain additional details:
           *
           * <p><table>
           * <tr>
           *   <td>INSTALL_FAILED_DUPLICATE_PERMISSION</td>
           *   <td>Two strings are provided in the extras bundle: EXTRA_EXISTING_PERMISSION
           *       is the name of the permission that the app is attempting to define, and
           *       EXTRA_EXISTING_PACKAGE is the package name of the app which has already
           *       defined the permission.</td>
           * </tr>
           * </table>
           */
      @Override public void onPackageInstalled(java.lang.String basePackageName, int returnCode, java.lang.String msg, android.os.Bundle extras) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(basePackageName);
          _data.writeInt(returnCode);
          _data.writeString(msg);
          if ((extras!=null)) {
            _data.writeInt(1);
            extras.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_onPackageInstalled, _data, null, android.os.IBinder.FLAG_ONEWAY);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onPackageInstalled(basePackageName, returnCode, msg, extras);
            return;
          }
        }
        finally {
          _data.recycle();
        }
      }
      public static android.content.pm.IPackageInstallObserver2 sDefaultImpl;
    }
    static final int TRANSACTION_onUserActionRequired = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onPackageInstalled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(android.content.pm.IPackageInstallObserver2 impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.content.pm.IPackageInstallObserver2 getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }

  public void onUserActionRequired(android.content.Intent intent) throws android.os.RemoteException;
  /**
       * The install operation has completed.  {@code returnCode} holds a numeric code
       * indicating success or failure.  In certain cases the {@code extras} Bundle will
       * contain additional details:
       *
       * <p><table>
       * <tr>
       *   <td>INSTALL_FAILED_DUPLICATE_PERMISSION</td>
       *   <td>Two strings are provided in the extras bundle: EXTRA_EXISTING_PERMISSION
       *       is the name of the permission that the app is attempting to define, and
       *       EXTRA_EXISTING_PACKAGE is the package name of the app which has already
       *       defined the permission.</td>
       * </tr>
       * </table>
       */

  public void onPackageInstalled(java.lang.String basePackageName, int returnCode, java.lang.String msg, android.os.Bundle extras) throws android.os.RemoteException;
}
