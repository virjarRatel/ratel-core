/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.android.internal.widget;
/** {@hide} */
public interface ILockSettings extends android.os.IInterface
{
  /** Default implementation for ILockSettings. */
  public static class Default implements com.android.internal.widget.ILockSettings
  {
    @Override public void setBoolean(java.lang.String key, boolean value, int userId) throws android.os.RemoteException
    {
    }
    @Override public void setLong(java.lang.String key, long value, int userId) throws android.os.RemoteException
    {
    }
    @Override public void setString(java.lang.String key, java.lang.String value, int userId) throws android.os.RemoteException
    {
    }
    @Override public boolean getBoolean(java.lang.String key, boolean defaultValue, int userId) throws android.os.RemoteException
    {
      return false;
    }
    @Override public long getLong(java.lang.String key, long defaultValue, int userId) throws android.os.RemoteException
    {
      return 0L;
    }
    @Override public java.lang.String getString(java.lang.String key, java.lang.String defaultValue, int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void setLockCredential(java.lang.String credential, int type, java.lang.String savedCredential, int requestedQuality, int userId) throws android.os.RemoteException
    {
    }
    @Override public void resetKeyStore(int userId) throws android.os.RemoteException
    {
    }
    @Override public com.android.internal.widget.VerifyCredentialResponse checkCredential(java.lang.String credential, int type, int userId, com.android.internal.widget.ICheckCredentialProgressCallback progressCallback) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.android.internal.widget.VerifyCredentialResponse verifyCredential(java.lang.String credential, int type, long challenge, int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.android.internal.widget.VerifyCredentialResponse verifyTiedProfileChallenge(java.lang.String credential, int type, long challenge, int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public boolean checkVoldPassword(int userId) throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean havePattern(int userId) throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean havePassword(int userId) throws android.os.RemoteException
    {
      return false;
    }
    @Override public byte[] getHashFactor(java.lang.String currentCredential, int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, java.lang.String managedUserPassword) throws android.os.RemoteException
    {
    }
    @Override public boolean getSeparateProfileChallengeEnabled(int userId) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void registerStrongAuthTracker(android.app.trust.IStrongAuthTracker tracker) throws android.os.RemoteException
    {
    }
    @Override public void unregisterStrongAuthTracker(android.app.trust.IStrongAuthTracker tracker) throws android.os.RemoteException
    {
    }
    @Override public void requireStrongAuth(int strongAuthReason, int userId) throws android.os.RemoteException
    {
    }
    @Override public void systemReady() throws android.os.RemoteException
    {
    }
    @Override public void userPresent(int userId) throws android.os.RemoteException
    {
    }
    @Override public int getStrongAuthForUser(int userId) throws android.os.RemoteException
    {
      return 0;
    }
    // Keystore RecoveryController methods.
    // {@code ServiceSpecificException} may be thrown to signal an error, which caller can
    // convert to  {@code RecoveryManagerException}.

    @Override public void initRecoveryServiceWithSigFile(java.lang.String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws android.os.RemoteException
    {
    }
    @Override public android.security.keystore.recovery.KeyChainSnapshot getKeyChainSnapshot() throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String generateKey(java.lang.String alias) throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String importKey(java.lang.String alias, byte[] keyBytes) throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String getKey(java.lang.String alias) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void removeKey(java.lang.String alias) throws android.os.RemoteException
    {
    }
    @Override public void setSnapshotCreatedPendingIntent(android.app.PendingIntent intent) throws android.os.RemoteException
    {
    }
    @Override public void setServerParams(byte[] serverParams) throws android.os.RemoteException
    {
    }
    @Override public void setRecoveryStatus(java.lang.String alias, int status) throws android.os.RemoteException
    {
    }
    @Override public java.util.Map getRecoveryStatus() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void setRecoverySecretTypes(int[] secretTypes) throws android.os.RemoteException
    {
    }
    @Override public int[] getRecoverySecretTypes() throws android.os.RemoteException
    {
      return null;
    }
    @Override public byte[] startRecoverySessionWithCertPath(java.lang.String sessionId, java.lang.String rootCertificateAlias, android.security.keystore.recovery.RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, java.util.List<android.security.keystore.recovery.KeyChainProtectionParams> secrets) throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.util.Map recoverKeyChainSnapshot(java.lang.String sessionId, byte[] recoveryKeyBlob, java.util.List<android.security.keystore.recovery.WrappedApplicationKey> applicationKeys) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void closeSession(java.lang.String sessionId) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.android.internal.widget.ILockSettings
  {
    private static final java.lang.String DESCRIPTOR = "com.android.internal.widget.ILockSettings";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.internal.widget.ILockSettings interface,
     * generating a proxy if needed.
     */
    public static com.android.internal.widget.ILockSettings asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.android.internal.widget.ILockSettings))) {
        return ((com.android.internal.widget.ILockSettings)iin);
      }
      return new com.android.internal.widget.ILockSettings.Stub.Proxy(obj);
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
        case TRANSACTION_setBoolean:
        {
          return "setBoolean";
        }
        case TRANSACTION_setLong:
        {
          return "setLong";
        }
        case TRANSACTION_setString:
        {
          return "setString";
        }
        case TRANSACTION_getBoolean:
        {
          return "getBoolean";
        }
        case TRANSACTION_getLong:
        {
          return "getLong";
        }
        case TRANSACTION_getString:
        {
          return "getString";
        }
        case TRANSACTION_setLockCredential:
        {
          return "setLockCredential";
        }
        case TRANSACTION_resetKeyStore:
        {
          return "resetKeyStore";
        }
        case TRANSACTION_checkCredential:
        {
          return "checkCredential";
        }
        case TRANSACTION_verifyCredential:
        {
          return "verifyCredential";
        }
        case TRANSACTION_verifyTiedProfileChallenge:
        {
          return "verifyTiedProfileChallenge";
        }
        case TRANSACTION_checkVoldPassword:
        {
          return "checkVoldPassword";
        }
        case TRANSACTION_havePattern:
        {
          return "havePattern";
        }
        case TRANSACTION_havePassword:
        {
          return "havePassword";
        }
        case TRANSACTION_getHashFactor:
        {
          return "getHashFactor";
        }
        case TRANSACTION_setSeparateProfileChallengeEnabled:
        {
          return "setSeparateProfileChallengeEnabled";
        }
        case TRANSACTION_getSeparateProfileChallengeEnabled:
        {
          return "getSeparateProfileChallengeEnabled";
        }
        case TRANSACTION_registerStrongAuthTracker:
        {
          return "registerStrongAuthTracker";
        }
        case TRANSACTION_unregisterStrongAuthTracker:
        {
          return "unregisterStrongAuthTracker";
        }
        case TRANSACTION_requireStrongAuth:
        {
          return "requireStrongAuth";
        }
        case TRANSACTION_systemReady:
        {
          return "systemReady";
        }
        case TRANSACTION_userPresent:
        {
          return "userPresent";
        }
        case TRANSACTION_getStrongAuthForUser:
        {
          return "getStrongAuthForUser";
        }
        case TRANSACTION_initRecoveryServiceWithSigFile:
        {
          return "initRecoveryServiceWithSigFile";
        }
        case TRANSACTION_getKeyChainSnapshot:
        {
          return "getKeyChainSnapshot";
        }
        case TRANSACTION_generateKey:
        {
          return "generateKey";
        }
        case TRANSACTION_importKey:
        {
          return "importKey";
        }
        case TRANSACTION_getKey:
        {
          return "getKey";
        }
        case TRANSACTION_removeKey:
        {
          return "removeKey";
        }
        case TRANSACTION_setSnapshotCreatedPendingIntent:
        {
          return "setSnapshotCreatedPendingIntent";
        }
        case TRANSACTION_setServerParams:
        {
          return "setServerParams";
        }
        case TRANSACTION_setRecoveryStatus:
        {
          return "setRecoveryStatus";
        }
        case TRANSACTION_getRecoveryStatus:
        {
          return "getRecoveryStatus";
        }
        case TRANSACTION_setRecoverySecretTypes:
        {
          return "setRecoverySecretTypes";
        }
        case TRANSACTION_getRecoverySecretTypes:
        {
          return "getRecoverySecretTypes";
        }
        case TRANSACTION_startRecoverySessionWithCertPath:
        {
          return "startRecoverySessionWithCertPath";
        }
        case TRANSACTION_recoverKeyChainSnapshot:
        {
          return "recoverKeyChainSnapshot";
        }
        case TRANSACTION_closeSession:
        {
          return "closeSession";
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
    private static class Proxy implements com.android.internal.widget.ILockSettings
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
      @Override public void setBoolean(java.lang.String key, boolean value, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(key);
          _data.writeInt(((value)?(1):(0)));
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setBoolean, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setBoolean(key, value, userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setLong(java.lang.String key, long value, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(key);
          _data.writeLong(value);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setLong, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setLong(key, value, userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setString(java.lang.String key, java.lang.String value, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(key);
          _data.writeString(value);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setString, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setString(key, value, userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public boolean getBoolean(java.lang.String key, boolean defaultValue, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(key);
          _data.writeInt(((defaultValue)?(1):(0)));
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getBoolean, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getBoolean(key, defaultValue, userId);
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
      @Override public long getLong(java.lang.String key, long defaultValue, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        long _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(key);
          _data.writeLong(defaultValue);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getLong, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getLong(key, defaultValue, userId);
          }
          _reply.readException();
          _result = _reply.readLong();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public java.lang.String getString(java.lang.String key, java.lang.String defaultValue, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(key);
          _data.writeString(defaultValue);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getString, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getString(key, defaultValue, userId);
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void setLockCredential(java.lang.String credential, int type, java.lang.String savedCredential, int requestedQuality, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(credential);
          _data.writeInt(type);
          _data.writeString(savedCredential);
          _data.writeInt(requestedQuality);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setLockCredential, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setLockCredential(credential, type, savedCredential, requestedQuality, userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void resetKeyStore(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_resetKeyStore, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().resetKeyStore(userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public com.android.internal.widget.VerifyCredentialResponse checkCredential(java.lang.String credential, int type, int userId, com.android.internal.widget.ICheckCredentialProgressCallback progressCallback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.android.internal.widget.VerifyCredentialResponse _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(credential);
          _data.writeInt(type);
          _data.writeInt(userId);
          _data.writeStrongBinder((((progressCallback!=null))?(progressCallback.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_checkCredential, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().checkCredential(credential, type, userId, progressCallback);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = com.android.internal.widget.VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
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
      @Override public com.android.internal.widget.VerifyCredentialResponse verifyCredential(java.lang.String credential, int type, long challenge, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.android.internal.widget.VerifyCredentialResponse _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(credential);
          _data.writeInt(type);
          _data.writeLong(challenge);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_verifyCredential, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().verifyCredential(credential, type, challenge, userId);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = com.android.internal.widget.VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
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
      @Override public com.android.internal.widget.VerifyCredentialResponse verifyTiedProfileChallenge(java.lang.String credential, int type, long challenge, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        com.android.internal.widget.VerifyCredentialResponse _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(credential);
          _data.writeInt(type);
          _data.writeLong(challenge);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_verifyTiedProfileChallenge, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().verifyTiedProfileChallenge(credential, type, challenge, userId);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = com.android.internal.widget.VerifyCredentialResponse.CREATOR.createFromParcel(_reply);
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
      @Override public boolean checkVoldPassword(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_checkVoldPassword, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().checkVoldPassword(userId);
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
      @Override public boolean havePattern(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_havePattern, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().havePattern(userId);
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
      @Override public boolean havePassword(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_havePassword, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().havePassword(userId);
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
      @Override public byte[] getHashFactor(java.lang.String currentCredential, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(currentCredential);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getHashFactor, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getHashFactor(currentCredential, userId);
          }
          _reply.readException();
          _result = _reply.createByteArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, java.lang.String managedUserPassword) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          _data.writeInt(((enabled)?(1):(0)));
          _data.writeString(managedUserPassword);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setSeparateProfileChallengeEnabled, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setSeparateProfileChallengeEnabled(userId, enabled, managedUserPassword);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public boolean getSeparateProfileChallengeEnabled(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSeparateProfileChallengeEnabled, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getSeparateProfileChallengeEnabled(userId);
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
      @Override public void registerStrongAuthTracker(android.app.trust.IStrongAuthTracker tracker) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder((((tracker!=null))?(tracker.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerStrongAuthTracker, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().registerStrongAuthTracker(tracker);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void unregisterStrongAuthTracker(android.app.trust.IStrongAuthTracker tracker) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder((((tracker!=null))?(tracker.asBinder()):(null)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterStrongAuthTracker, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().unregisterStrongAuthTracker(tracker);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void requireStrongAuth(int strongAuthReason, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(strongAuthReason);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requireStrongAuth, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().requireStrongAuth(strongAuthReason, userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void systemReady() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_systemReady, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().systemReady();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void userPresent(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_userPresent, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().userPresent(userId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public int getStrongAuthForUser(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getStrongAuthForUser, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getStrongAuthForUser(userId);
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
      // Keystore RecoveryController methods.
      // {@code ServiceSpecificException} may be thrown to signal an error, which caller can
      // convert to  {@code RecoveryManagerException}.

      @Override public void initRecoveryServiceWithSigFile(java.lang.String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(rootCertificateAlias);
          _data.writeByteArray(recoveryServiceCertFile);
          _data.writeByteArray(recoveryServiceSigFile);
          boolean _status = mRemote.transact(Stub.TRANSACTION_initRecoveryServiceWithSigFile, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().initRecoveryServiceWithSigFile(rootCertificateAlias, recoveryServiceCertFile, recoveryServiceSigFile);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public android.security.keystore.recovery.KeyChainSnapshot getKeyChainSnapshot() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.security.keystore.recovery.KeyChainSnapshot _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getKeyChainSnapshot, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getKeyChainSnapshot();
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.security.keystore.recovery.KeyChainSnapshot.CREATOR.createFromParcel(_reply);
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
      @Override public java.lang.String generateKey(java.lang.String alias) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          boolean _status = mRemote.transact(Stub.TRANSACTION_generateKey, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().generateKey(alias);
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public java.lang.String importKey(java.lang.String alias, byte[] keyBytes) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          _data.writeByteArray(keyBytes);
          boolean _status = mRemote.transact(Stub.TRANSACTION_importKey, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().importKey(alias, keyBytes);
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public java.lang.String getKey(java.lang.String alias) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getKey, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getKey(alias);
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void removeKey(java.lang.String alias) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          boolean _status = mRemote.transact(Stub.TRANSACTION_removeKey, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().removeKey(alias);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setSnapshotCreatedPendingIntent(android.app.PendingIntent intent) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((intent!=null)) {
            _data.writeInt(1);
            intent.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_setSnapshotCreatedPendingIntent, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setSnapshotCreatedPendingIntent(intent);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setServerParams(byte[] serverParams) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeByteArray(serverParams);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setServerParams, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setServerParams(serverParams);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setRecoveryStatus(java.lang.String alias, int status) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          _data.writeInt(status);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setRecoveryStatus, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setRecoveryStatus(alias, status);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public java.util.Map getRecoveryStatus() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.Map _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRecoveryStatus, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getRecoveryStatus();
          }
          _reply.readException();
          java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
          _result = _reply.readHashMap(cl);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void setRecoverySecretTypes(int[] secretTypes) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeIntArray(secretTypes);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setRecoverySecretTypes, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setRecoverySecretTypes(secretTypes);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public int[] getRecoverySecretTypes() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRecoverySecretTypes, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getRecoverySecretTypes();
          }
          _reply.readException();
          _result = _reply.createIntArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public byte[] startRecoverySessionWithCertPath(java.lang.String sessionId, java.lang.String rootCertificateAlias, android.security.keystore.recovery.RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, java.util.List<android.security.keystore.recovery.KeyChainProtectionParams> secrets) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.util.Map recoverKeyChainSnapshot(java.lang.String sessionId, byte[] recoveryKeyBlob, java.util.List<android.security.keystore.recovery.WrappedApplicationKey> applicationKeys) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void closeSession(java.lang.String sessionId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_closeSession, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().closeSession(sessionId);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static com.android.internal.widget.ILockSettings sDefaultImpl;
    }
    static final int TRANSACTION_setBoolean = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_setLong = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_setString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getBoolean = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_getLong = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_getString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_setLockCredential = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_resetKeyStore = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_checkCredential = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_verifyCredential = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_verifyTiedProfileChallenge = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_checkVoldPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_havePattern = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_havePassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
    static final int TRANSACTION_getHashFactor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
    static final int TRANSACTION_setSeparateProfileChallengeEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
    static final int TRANSACTION_getSeparateProfileChallengeEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
    static final int TRANSACTION_registerStrongAuthTracker = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
    static final int TRANSACTION_unregisterStrongAuthTracker = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
    static final int TRANSACTION_requireStrongAuth = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
    static final int TRANSACTION_systemReady = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
    static final int TRANSACTION_userPresent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
    static final int TRANSACTION_getStrongAuthForUser = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
    static final int TRANSACTION_initRecoveryServiceWithSigFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
    static final int TRANSACTION_getKeyChainSnapshot = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
    static final int TRANSACTION_generateKey = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
    static final int TRANSACTION_importKey = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
    static final int TRANSACTION_getKey = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
    static final int TRANSACTION_removeKey = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
    static final int TRANSACTION_setSnapshotCreatedPendingIntent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
    static final int TRANSACTION_setServerParams = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
    static final int TRANSACTION_setRecoveryStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
    static final int TRANSACTION_getRecoveryStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
    static final int TRANSACTION_setRecoverySecretTypes = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
    static final int TRANSACTION_getRecoverySecretTypes = (android.os.IBinder.FIRST_CALL_TRANSACTION + 34);
    static final int TRANSACTION_startRecoverySessionWithCertPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 35);
    static final int TRANSACTION_recoverKeyChainSnapshot = (android.os.IBinder.FIRST_CALL_TRANSACTION + 36);
    static final int TRANSACTION_closeSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 37);
    public static boolean setDefaultImpl(com.android.internal.widget.ILockSettings impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static com.android.internal.widget.ILockSettings getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }

  public void setBoolean(java.lang.String key, boolean value, int userId) throws android.os.RemoteException;

  public void setLong(java.lang.String key, long value, int userId) throws android.os.RemoteException;

  public void setString(java.lang.String key, java.lang.String value, int userId) throws android.os.RemoteException;

  public boolean getBoolean(java.lang.String key, boolean defaultValue, int userId) throws android.os.RemoteException;

  public long getLong(java.lang.String key, long defaultValue, int userId) throws android.os.RemoteException;

  public java.lang.String getString(java.lang.String key, java.lang.String defaultValue, int userId) throws android.os.RemoteException;
  public void setLockCredential(java.lang.String credential, int type, java.lang.String savedCredential, int requestedQuality, int userId) throws android.os.RemoteException;
  public void resetKeyStore(int userId) throws android.os.RemoteException;
  public com.android.internal.widget.VerifyCredentialResponse checkCredential(java.lang.String credential, int type, int userId, com.android.internal.widget.ICheckCredentialProgressCallback progressCallback) throws android.os.RemoteException;
  public com.android.internal.widget.VerifyCredentialResponse verifyCredential(java.lang.String credential, int type, long challenge, int userId) throws android.os.RemoteException;
  public com.android.internal.widget.VerifyCredentialResponse verifyTiedProfileChallenge(java.lang.String credential, int type, long challenge, int userId) throws android.os.RemoteException;
  public boolean checkVoldPassword(int userId) throws android.os.RemoteException;

  public boolean havePattern(int userId) throws android.os.RemoteException;

  public boolean havePassword(int userId) throws android.os.RemoteException;
  public byte[] getHashFactor(java.lang.String currentCredential, int userId) throws android.os.RemoteException;
  public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, java.lang.String managedUserPassword) throws android.os.RemoteException;
  public boolean getSeparateProfileChallengeEnabled(int userId) throws android.os.RemoteException;
  public void registerStrongAuthTracker(android.app.trust.IStrongAuthTracker tracker) throws android.os.RemoteException;
  public void unregisterStrongAuthTracker(android.app.trust.IStrongAuthTracker tracker) throws android.os.RemoteException;
  public void requireStrongAuth(int strongAuthReason, int userId) throws android.os.RemoteException;
  public void systemReady() throws android.os.RemoteException;
  public void userPresent(int userId) throws android.os.RemoteException;
  public int getStrongAuthForUser(int userId) throws android.os.RemoteException;
  // Keystore RecoveryController methods.
  // {@code ServiceSpecificException} may be thrown to signal an error, which caller can
  // convert to  {@code RecoveryManagerException}.

  public void initRecoveryServiceWithSigFile(java.lang.String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws android.os.RemoteException;
  public android.security.keystore.recovery.KeyChainSnapshot getKeyChainSnapshot() throws android.os.RemoteException;
  public java.lang.String generateKey(java.lang.String alias) throws android.os.RemoteException;
  public java.lang.String importKey(java.lang.String alias, byte[] keyBytes) throws android.os.RemoteException;
  public java.lang.String getKey(java.lang.String alias) throws android.os.RemoteException;
  public void removeKey(java.lang.String alias) throws android.os.RemoteException;
  public void setSnapshotCreatedPendingIntent(android.app.PendingIntent intent) throws android.os.RemoteException;
  public void setServerParams(byte[] serverParams) throws android.os.RemoteException;
  public void setRecoveryStatus(java.lang.String alias, int status) throws android.os.RemoteException;
  public java.util.Map getRecoveryStatus() throws android.os.RemoteException;
  public void setRecoverySecretTypes(int[] secretTypes) throws android.os.RemoteException;
  public int[] getRecoverySecretTypes() throws android.os.RemoteException;
  public byte[] startRecoverySessionWithCertPath(java.lang.String sessionId, java.lang.String rootCertificateAlias, android.security.keystore.recovery.RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, java.util.List<android.security.keystore.recovery.KeyChainProtectionParams> secrets) throws android.os.RemoteException;
  public java.util.Map recoverKeyChainSnapshot(java.lang.String sessionId, byte[] recoveryKeyBlob, java.util.List<android.security.keystore.recovery.WrappedApplicationKey> applicationKeys) throws android.os.RemoteException;
  public void closeSession(java.lang.String sessionId) throws android.os.RemoteException;
}
