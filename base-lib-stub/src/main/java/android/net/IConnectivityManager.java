/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package android.net;
/**
 * Interface that answers queries about, and allows changing, the
 * state of network connectivity.
 *//** {@hide} */
public interface IConnectivityManager extends android.os.IInterface
{
  /** Default implementation for IConnectivityManager. */
  public static class Default implements android.net.IConnectivityManager
  {
    @Override public android.net.Network getActiveNetwork() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkInfo getActiveNetworkInfo() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkInfo getNetworkInfo(int networkType) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkInfo getNetworkInfoForUid(android.net.Network network, int uid, boolean ignoreBlocked) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkInfo[] getAllNetworkInfo() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.Network getNetworkForType(int networkType) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.Network[] getAllNetworks() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public boolean isNetworkSupported(int networkType) throws android.os.RemoteException
    {
      return false;
    }
    @Override public android.net.LinkProperties getActiveLinkProperties() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.LinkProperties getLinkPropertiesForType(int networkType) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.LinkProperties getLinkProperties(android.net.Network network) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkCapabilities getNetworkCapabilities(android.net.Network network) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkState[] getAllNetworkState() throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkQuotaInfo getActiveNetworkQuotaInfo() throws android.os.RemoteException
    {
      return null;
    }
    @Override public boolean isActiveNetworkMetered() throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws android.os.RemoteException
    {
      return false;
    }
    @Override public int tether(java.lang.String iface, java.lang.String callerPkg) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public int untether(java.lang.String iface, java.lang.String callerPkg) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public int getLastTetherError(java.lang.String iface) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public boolean isTetheringSupported(java.lang.String callerPkg) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void startTethering(int type, android.os.ResultReceiver receiver, boolean showProvisioningUi, java.lang.String callerPkg) throws android.os.RemoteException
    {
    }
    @Override public void stopTethering(int type, java.lang.String callerPkg) throws android.os.RemoteException
    {
    }
    @Override public java.lang.String[] getTetherableIfaces() throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String[] getTetheredIfaces() throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String[] getTetheringErroredIfaces() throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String[] getTetheredDhcpRanges() throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String[] getTetherableUsbRegexs() throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String[] getTetherableWifiRegexs() throws android.os.RemoteException
    {
      return null;
    }
    @Override public java.lang.String[] getTetherableBluetoothRegexs() throws android.os.RemoteException
    {
      return null;
    }
    @Override public int setUsbTethering(boolean enable, java.lang.String callerPkg) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public void reportInetCondition(int networkType, int percentage) throws android.os.RemoteException
    {
    }
    @Override public void reportNetworkConnectivity(android.net.Network network, boolean hasConnectivity) throws android.os.RemoteException
    {
    }
    @Override public android.net.ProxyInfo getGlobalProxy() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void setGlobalProxy(android.net.ProxyInfo p) throws android.os.RemoteException
    {
    }
    @Override public android.net.ProxyInfo getProxyForNetwork(android.net.Network nework) throws android.os.RemoteException
    {
      return null;
    }
    @Override public boolean prepareVpn(java.lang.String oldPackage, java.lang.String newPackage, int userId) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void setVpnPackageAuthorization(java.lang.String packageName, int userId, boolean authorized) throws android.os.RemoteException
    {
    }
    @Override public android.os.ParcelFileDescriptor establishVpn(com.android.internal.net.VpnConfig config) throws android.os.RemoteException
    {
      return null;
    }
    @Override public com.android.internal.net.VpnConfig getVpnConfig(int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void startLegacyVpn(com.android.internal.net.VpnProfile profile) throws android.os.RemoteException
    {
    }
    @Override public com.android.internal.net.LegacyVpnInfo getLegacyVpnInfo(int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public boolean updateLockdownVpn() throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean isAlwaysOnVpnPackageSupported(int userId, java.lang.String packageName) throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean setAlwaysOnVpnPackage(int userId, java.lang.String packageName, boolean lockdown, java.util.List<java.lang.String> lockdownWhitelist) throws android.os.RemoteException
    {
      return false;
    }
    @Override public java.lang.String getAlwaysOnVpnPackage(int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public boolean isVpnLockdownEnabled(int userId) throws android.os.RemoteException
    {
      return false;
    }
    @Override public java.util.List<java.lang.String> getVpnLockdownWhitelist(int userId) throws android.os.RemoteException
    {
      return null;
    }
    @Override public int checkMobileProvisioning(int suggestedTimeOutMs) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public java.lang.String getMobileProvisioningUrl() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void setProvisioningNotificationVisible(boolean visible, int networkType, java.lang.String action) throws android.os.RemoteException
    {
    }
    @Override public void setAirplaneMode(boolean enable) throws android.os.RemoteException
    {
    }
    @Override public int registerNetworkFactory(android.os.Messenger messenger, java.lang.String name) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public boolean requestBandwidthUpdate(android.net.Network network) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void unregisterNetworkFactory(android.os.Messenger messenger) throws android.os.RemoteException
    {
    }
    @Override public int registerNetworkAgent(android.os.Messenger messenger, android.net.NetworkInfo ni, android.net.LinkProperties lp, android.net.NetworkCapabilities nc, int score, android.net.NetworkMisc misc, int factorySerialNumber) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public android.net.NetworkRequest requestNetwork(android.net.NetworkCapabilities networkCapabilities, android.os.Messenger messenger, int timeoutSec, android.os.IBinder binder, int legacy) throws android.os.RemoteException
    {
      return null;
    }
    @Override public android.net.NetworkRequest pendingRequestForNetwork(android.net.NetworkCapabilities networkCapabilities, android.app.PendingIntent operation) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void releasePendingNetworkRequest(android.app.PendingIntent operation) throws android.os.RemoteException
    {
    }
    @Override public android.net.NetworkRequest listenForNetwork(android.net.NetworkCapabilities networkCapabilities, android.os.Messenger messenger, android.os.IBinder binder) throws android.os.RemoteException
    {
      return null;
    }
    @Override public void pendingListenForNetwork(android.net.NetworkCapabilities networkCapabilities, android.app.PendingIntent operation) throws android.os.RemoteException
    {
    }
    @Override public void releaseNetworkRequest(android.net.NetworkRequest networkRequest) throws android.os.RemoteException
    {
    }
    @Override public void setAcceptUnvalidated(android.net.Network network, boolean accept, boolean always) throws android.os.RemoteException
    {
    }
    @Override public void setAcceptPartialConnectivity(android.net.Network network, boolean accept, boolean always) throws android.os.RemoteException
    {
    }
    @Override public void setAvoidUnvalidated(android.net.Network network) throws android.os.RemoteException
    {
    }
    @Override public void startCaptivePortalApp(android.net.Network network) throws android.os.RemoteException
    {
    }
    @Override public void startCaptivePortalAppInternal(android.net.Network network, android.os.Bundle appExtras) throws android.os.RemoteException
    {
    }
    @Override public boolean shouldAvoidBadWifi() throws android.os.RemoteException
    {
      return false;
    }
    @Override public int getMultipathPreference(android.net.Network Network) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public android.net.NetworkRequest getDefaultRequest() throws android.os.RemoteException
    {
      return null;
    }
    @Override public int getRestoreDefaultNetworkDelay(int networkType) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public boolean addVpnAddress(java.lang.String address, int prefixLength) throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean removeVpnAddress(java.lang.String address, int prefixLength) throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean setUnderlyingNetworksForVpn(android.net.Network[] networks) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void factoryReset() throws android.os.RemoteException
    {
    }
    @Override public void startNattKeepalive(android.net.Network network, int intervalSeconds, android.net.ISocketKeepaliveCallback cb, java.lang.String srcAddr, int srcPort, java.lang.String dstAddr) throws android.os.RemoteException
    {
    }
    @Override public void startNattKeepaliveWithFd(android.net.Network network, java.io.FileDescriptor fd, int resourceId, int intervalSeconds, android.net.ISocketKeepaliveCallback cb, java.lang.String srcAddr, java.lang.String dstAddr) throws android.os.RemoteException
    {
    }
    @Override public void startTcpKeepalive(android.net.Network network, java.io.FileDescriptor fd, int intervalSeconds, android.net.ISocketKeepaliveCallback cb) throws android.os.RemoteException
    {
    }
    @Override public void stopKeepalive(android.net.Network network, int slot) throws android.os.RemoteException
    {
    }
    @Override public java.lang.String getCaptivePortalServerUrl() throws android.os.RemoteException
    {
      return null;
    }
    @Override public byte[] getNetworkWatchlistConfigHash() throws android.os.RemoteException
    {
      return null;
    }
    @Override public int getConnectionOwnerUid(android.net.ConnectionInfo connectionInfo) throws android.os.RemoteException
    {
      return 0;
    }
    @Override public boolean isCallerCurrentAlwaysOnVpnApp() throws android.os.RemoteException
    {
      return false;
    }
    @Override public boolean isCallerCurrentAlwaysOnVpnLockdownApp() throws android.os.RemoteException
    {
      return false;
    }
    @Override public void getLatestTetheringEntitlementResult(int type, android.os.ResultReceiver receiver, boolean showEntitlementUi, java.lang.String callerPkg) throws android.os.RemoteException
    {
    }
    @Override public void registerTetheringEventCallback(android.net.ITetheringEventCallback callback, java.lang.String callerPkg) throws android.os.RemoteException
    {
    }
    @Override public void unregisterTetheringEventCallback(android.net.ITetheringEventCallback callback, java.lang.String callerPkg) throws android.os.RemoteException
    {
    }
    @Override public android.os.IBinder startOrGetTestNetworkService() throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.net.IConnectivityManager
  {
    private static final java.lang.String DESCRIPTOR = "android.net.IConnectivityManager";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.net.IConnectivityManager interface,
     * generating a proxy if needed.
     */
    public static android.net.IConnectivityManager asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.net.IConnectivityManager))) {
        return ((android.net.IConnectivityManager)iin);
      }
      return new android.net.IConnectivityManager.Stub.Proxy(obj);
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
        case TRANSACTION_getActiveNetwork:
        {
          return "getActiveNetwork";
        }
        case TRANSACTION_getActiveNetworkForUid:
        {
          return "getActiveNetworkForUid";
        }
        case TRANSACTION_getActiveNetworkInfo:
        {
          return "getActiveNetworkInfo";
        }
        case TRANSACTION_getActiveNetworkInfoForUid:
        {
          return "getActiveNetworkInfoForUid";
        }
        case TRANSACTION_getNetworkInfo:
        {
          return "getNetworkInfo";
        }
        case TRANSACTION_getNetworkInfoForUid:
        {
          return "getNetworkInfoForUid";
        }
        case TRANSACTION_getAllNetworkInfo:
        {
          return "getAllNetworkInfo";
        }
        case TRANSACTION_getNetworkForType:
        {
          return "getNetworkForType";
        }
        case TRANSACTION_getAllNetworks:
        {
          return "getAllNetworks";
        }
        case TRANSACTION_getDefaultNetworkCapabilitiesForUser:
        {
          return "getDefaultNetworkCapabilitiesForUser";
        }
        case TRANSACTION_isNetworkSupported:
        {
          return "isNetworkSupported";
        }
        case TRANSACTION_getActiveLinkProperties:
        {
          return "getActiveLinkProperties";
        }
        case TRANSACTION_getLinkPropertiesForType:
        {
          return "getLinkPropertiesForType";
        }
        case TRANSACTION_getLinkProperties:
        {
          return "getLinkProperties";
        }
        case TRANSACTION_getNetworkCapabilities:
        {
          return "getNetworkCapabilities";
        }
        case TRANSACTION_getAllNetworkState:
        {
          return "getAllNetworkState";
        }
        case TRANSACTION_getActiveNetworkQuotaInfo:
        {
          return "getActiveNetworkQuotaInfo";
        }
        case TRANSACTION_isActiveNetworkMetered:
        {
          return "isActiveNetworkMetered";
        }
        case TRANSACTION_requestRouteToHostAddress:
        {
          return "requestRouteToHostAddress";
        }
        case TRANSACTION_tether:
        {
          return "tether";
        }
        case TRANSACTION_untether:
        {
          return "untether";
        }
        case TRANSACTION_getLastTetherError:
        {
          return "getLastTetherError";
        }
        case TRANSACTION_isTetheringSupported:
        {
          return "isTetheringSupported";
        }
        case TRANSACTION_startTethering:
        {
          return "startTethering";
        }
        case TRANSACTION_stopTethering:
        {
          return "stopTethering";
        }
        case TRANSACTION_getTetherableIfaces:
        {
          return "getTetherableIfaces";
        }
        case TRANSACTION_getTetheredIfaces:
        {
          return "getTetheredIfaces";
        }
        case TRANSACTION_getTetheringErroredIfaces:
        {
          return "getTetheringErroredIfaces";
        }
        case TRANSACTION_getTetheredDhcpRanges:
        {
          return "getTetheredDhcpRanges";
        }
        case TRANSACTION_getTetherableUsbRegexs:
        {
          return "getTetherableUsbRegexs";
        }
        case TRANSACTION_getTetherableWifiRegexs:
        {
          return "getTetherableWifiRegexs";
        }
        case TRANSACTION_getTetherableBluetoothRegexs:
        {
          return "getTetherableBluetoothRegexs";
        }
        case TRANSACTION_setUsbTethering:
        {
          return "setUsbTethering";
        }
        case TRANSACTION_reportInetCondition:
        {
          return "reportInetCondition";
        }
        case TRANSACTION_reportNetworkConnectivity:
        {
          return "reportNetworkConnectivity";
        }
        case TRANSACTION_getGlobalProxy:
        {
          return "getGlobalProxy";
        }
        case TRANSACTION_setGlobalProxy:
        {
          return "setGlobalProxy";
        }
        case TRANSACTION_getProxyForNetwork:
        {
          return "getProxyForNetwork";
        }
        case TRANSACTION_prepareVpn:
        {
          return "prepareVpn";
        }
        case TRANSACTION_setVpnPackageAuthorization:
        {
          return "setVpnPackageAuthorization";
        }
        case TRANSACTION_establishVpn:
        {
          return "establishVpn";
        }
        case TRANSACTION_getVpnConfig:
        {
          return "getVpnConfig";
        }
        case TRANSACTION_startLegacyVpn:
        {
          return "startLegacyVpn";
        }
        case TRANSACTION_getLegacyVpnInfo:
        {
          return "getLegacyVpnInfo";
        }
        case TRANSACTION_updateLockdownVpn:
        {
          return "updateLockdownVpn";
        }
        case TRANSACTION_isAlwaysOnVpnPackageSupported:
        {
          return "isAlwaysOnVpnPackageSupported";
        }
        case TRANSACTION_setAlwaysOnVpnPackage:
        {
          return "setAlwaysOnVpnPackage";
        }
        case TRANSACTION_getAlwaysOnVpnPackage:
        {
          return "getAlwaysOnVpnPackage";
        }
        case TRANSACTION_isVpnLockdownEnabled:
        {
          return "isVpnLockdownEnabled";
        }
        case TRANSACTION_getVpnLockdownWhitelist:
        {
          return "getVpnLockdownWhitelist";
        }
        case TRANSACTION_checkMobileProvisioning:
        {
          return "checkMobileProvisioning";
        }
        case TRANSACTION_getMobileProvisioningUrl:
        {
          return "getMobileProvisioningUrl";
        }
        case TRANSACTION_setProvisioningNotificationVisible:
        {
          return "setProvisioningNotificationVisible";
        }
        case TRANSACTION_setAirplaneMode:
        {
          return "setAirplaneMode";
        }
        case TRANSACTION_registerNetworkFactory:
        {
          return "registerNetworkFactory";
        }
        case TRANSACTION_requestBandwidthUpdate:
        {
          return "requestBandwidthUpdate";
        }
        case TRANSACTION_unregisterNetworkFactory:
        {
          return "unregisterNetworkFactory";
        }
        case TRANSACTION_registerNetworkAgent:
        {
          return "registerNetworkAgent";
        }
        case TRANSACTION_requestNetwork:
        {
          return "requestNetwork";
        }
        case TRANSACTION_pendingRequestForNetwork:
        {
          return "pendingRequestForNetwork";
        }
        case TRANSACTION_releasePendingNetworkRequest:
        {
          return "releasePendingNetworkRequest";
        }
        case TRANSACTION_listenForNetwork:
        {
          return "listenForNetwork";
        }
        case TRANSACTION_pendingListenForNetwork:
        {
          return "pendingListenForNetwork";
        }
        case TRANSACTION_releaseNetworkRequest:
        {
          return "releaseNetworkRequest";
        }
        case TRANSACTION_setAcceptUnvalidated:
        {
          return "setAcceptUnvalidated";
        }
        case TRANSACTION_setAcceptPartialConnectivity:
        {
          return "setAcceptPartialConnectivity";
        }
        case TRANSACTION_setAvoidUnvalidated:
        {
          return "setAvoidUnvalidated";
        }
        case TRANSACTION_startCaptivePortalApp:
        {
          return "startCaptivePortalApp";
        }
        case TRANSACTION_startCaptivePortalAppInternal:
        {
          return "startCaptivePortalAppInternal";
        }
        case TRANSACTION_shouldAvoidBadWifi:
        {
          return "shouldAvoidBadWifi";
        }
        case TRANSACTION_getMultipathPreference:
        {
          return "getMultipathPreference";
        }
        case TRANSACTION_getDefaultRequest:
        {
          return "getDefaultRequest";
        }
        case TRANSACTION_getRestoreDefaultNetworkDelay:
        {
          return "getRestoreDefaultNetworkDelay";
        }
        case TRANSACTION_addVpnAddress:
        {
          return "addVpnAddress";
        }
        case TRANSACTION_removeVpnAddress:
        {
          return "removeVpnAddress";
        }
        case TRANSACTION_setUnderlyingNetworksForVpn:
        {
          return "setUnderlyingNetworksForVpn";
        }
        case TRANSACTION_factoryReset:
        {
          return "factoryReset";
        }
        case TRANSACTION_startNattKeepalive:
        {
          return "startNattKeepalive";
        }
        case TRANSACTION_startNattKeepaliveWithFd:
        {
          return "startNattKeepaliveWithFd";
        }
        case TRANSACTION_startTcpKeepalive:
        {
          return "startTcpKeepalive";
        }
        case TRANSACTION_stopKeepalive:
        {
          return "stopKeepalive";
        }
        case TRANSACTION_getCaptivePortalServerUrl:
        {
          return "getCaptivePortalServerUrl";
        }
        case TRANSACTION_getNetworkWatchlistConfigHash:
        {
          return "getNetworkWatchlistConfigHash";
        }
        case TRANSACTION_getConnectionOwnerUid:
        {
          return "getConnectionOwnerUid";
        }
        case TRANSACTION_isCallerCurrentAlwaysOnVpnApp:
        {
          return "isCallerCurrentAlwaysOnVpnApp";
        }
        case TRANSACTION_isCallerCurrentAlwaysOnVpnLockdownApp:
        {
          return "isCallerCurrentAlwaysOnVpnLockdownApp";
        }
        case TRANSACTION_getLatestTetheringEntitlementResult:
        {
          return "getLatestTetheringEntitlementResult";
        }
        case TRANSACTION_registerTetheringEventCallback:
        {
          return "registerTetheringEventCallback";
        }
        case TRANSACTION_unregisterTetheringEventCallback:
        {
          return "unregisterTetheringEventCallback";
        }
        case TRANSACTION_startOrGetTestNetworkService:
        {
          return "startOrGetTestNetworkService";
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
    private static class Proxy implements android.net.IConnectivityManager
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
      @Override public android.net.Network getActiveNetwork() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkInfo getActiveNetworkInfo() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkInfo getNetworkInfo(int networkType) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkInfo getNetworkInfoForUid(android.net.Network network, int uid, boolean ignoreBlocked) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkInfo[] getAllNetworkInfo() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.Network getNetworkForType(int networkType) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.Network[] getAllNetworks() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean isNetworkSupported(int networkType) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.LinkProperties getActiveLinkProperties() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.LinkProperties getLinkPropertiesForType(int networkType) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.LinkProperties getLinkProperties(android.net.Network network) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkCapabilities getNetworkCapabilities(android.net.Network network) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkState[] getAllNetworkState() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.NetworkQuotaInfo getActiveNetworkQuotaInfo() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean isActiveNetworkMetered() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public int tether(java.lang.String iface, java.lang.String callerPkg) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public int untether(java.lang.String iface, java.lang.String callerPkg) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public int getLastTetherError(java.lang.String iface) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean isTetheringSupported(java.lang.String callerPkg) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void startTethering(int type, android.os.ResultReceiver receiver, boolean showProvisioningUi, java.lang.String callerPkg) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void stopTethering(int type, java.lang.String callerPkg) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String[] getTetherableIfaces() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String[] getTetheredIfaces() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String[] getTetheringErroredIfaces() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String[] getTetheredDhcpRanges() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String[] getTetherableUsbRegexs() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String[] getTetherableWifiRegexs() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String[] getTetherableBluetoothRegexs() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public int setUsbTethering(boolean enable, java.lang.String callerPkg) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void reportInetCondition(int networkType, int percentage) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void reportNetworkConnectivity(android.net.Network network, boolean hasConnectivity) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.net.ProxyInfo getGlobalProxy() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void setGlobalProxy(android.net.ProxyInfo p) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((p!=null)) {
            _data.writeInt(1);
            p.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_setGlobalProxy, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setGlobalProxy(p);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public android.net.ProxyInfo getProxyForNetwork(android.net.Network nework) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.net.ProxyInfo _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((nework!=null)) {
            _data.writeInt(1);
            nework.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_getProxyForNetwork, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getProxyForNetwork(nework);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.net.ProxyInfo.CREATOR.createFromParcel(_reply);
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
      @Override public boolean prepareVpn(java.lang.String oldPackage, java.lang.String newPackage, int userId) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void setVpnPackageAuthorization(java.lang.String packageName, int userId, boolean authorized) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public android.os.ParcelFileDescriptor establishVpn(com.android.internal.net.VpnConfig config) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public com.android.internal.net.VpnConfig getVpnConfig(int userId) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void startLegacyVpn(com.android.internal.net.VpnProfile profile) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public com.android.internal.net.LegacyVpnInfo getLegacyVpnInfo(int userId) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean updateLockdownVpn() throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean isAlwaysOnVpnPackageSupported(int userId, java.lang.String packageName) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean setAlwaysOnVpnPackage(int userId, java.lang.String packageName, boolean lockdown, java.util.List<java.lang.String> lockdownWhitelist) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String getAlwaysOnVpnPackage(int userId) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public boolean isVpnLockdownEnabled(int userId) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.util.List<java.lang.String> getVpnLockdownWhitelist(int userId) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public int checkMobileProvisioning(int suggestedTimeOutMs) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public java.lang.String getMobileProvisioningUrl() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getMobileProvisioningUrl, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getMobileProvisioningUrl();
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
      @Override public void setProvisioningNotificationVisible(boolean visible, int networkType, java.lang.String action) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((visible)?(1):(0)));
          _data.writeInt(networkType);
          _data.writeString(action);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setProvisioningNotificationVisible, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setProvisioningNotificationVisible(visible, networkType, action);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setAirplaneMode(boolean enable) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((enable)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAirplaneMode, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setAirplaneMode(enable);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public int registerNetworkFactory(android.os.Messenger messenger, java.lang.String name) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((messenger!=null)) {
            _data.writeInt(1);
            messenger.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeString(name);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerNetworkFactory, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().registerNetworkFactory(messenger, name);
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
      @Override public boolean requestBandwidthUpdate(android.net.Network network) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((network!=null)) {
            _data.writeInt(1);
            network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestBandwidthUpdate, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().requestBandwidthUpdate(network);
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
      @Override public void unregisterNetworkFactory(android.os.Messenger messenger) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((messenger!=null)) {
            _data.writeInt(1);
            messenger.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterNetworkFactory, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().unregisterNetworkFactory(messenger);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public int registerNetworkAgent(android.os.Messenger messenger, android.net.NetworkInfo ni, android.net.LinkProperties lp, android.net.NetworkCapabilities nc, int score, android.net.NetworkMisc misc, int factorySerialNumber) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((messenger!=null)) {
            _data.writeInt(1);
            messenger.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((ni!=null)) {
            _data.writeInt(1);
            ni.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((lp!=null)) {
            _data.writeInt(1);
            lp.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((nc!=null)) {
            _data.writeInt(1);
            nc.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(score);
          if ((misc!=null)) {
            _data.writeInt(1);
            misc.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(factorySerialNumber);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerNetworkAgent, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().registerNetworkAgent(messenger, ni, lp, nc, score, misc, factorySerialNumber);
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
      @Override public android.net.NetworkRequest requestNetwork(android.net.NetworkCapabilities networkCapabilities, android.os.Messenger messenger, int timeoutSec, android.os.IBinder binder, int legacy) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.net.NetworkRequest _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((networkCapabilities!=null)) {
            _data.writeInt(1);
            networkCapabilities.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((messenger!=null)) {
            _data.writeInt(1);
            messenger.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(timeoutSec);
          _data.writeStrongBinder(binder);
          _data.writeInt(legacy);
          boolean _status = mRemote.transact(Stub.TRANSACTION_requestNetwork, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().requestNetwork(networkCapabilities, messenger, timeoutSec, binder, legacy);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.net.NetworkRequest.CREATOR.createFromParcel(_reply);
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
      @Override public android.net.NetworkRequest pendingRequestForNetwork(android.net.NetworkCapabilities networkCapabilities, android.app.PendingIntent operation) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.net.NetworkRequest _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((networkCapabilities!=null)) {
            _data.writeInt(1);
            networkCapabilities.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((operation!=null)) {
            _data.writeInt(1);
            operation.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_pendingRequestForNetwork, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().pendingRequestForNetwork(networkCapabilities, operation);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.net.NetworkRequest.CREATOR.createFromParcel(_reply);
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
      @Override public void releasePendingNetworkRequest(android.app.PendingIntent operation) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((operation!=null)) {
            _data.writeInt(1);
            operation.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_releasePendingNetworkRequest, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().releasePendingNetworkRequest(operation);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public android.net.NetworkRequest listenForNetwork(android.net.NetworkCapabilities networkCapabilities, android.os.Messenger messenger, android.os.IBinder binder) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.net.NetworkRequest _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((networkCapabilities!=null)) {
            _data.writeInt(1);
            networkCapabilities.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((messenger!=null)) {
            _data.writeInt(1);
            messenger.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeStrongBinder(binder);
          boolean _status = mRemote.transact(Stub.TRANSACTION_listenForNetwork, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().listenForNetwork(networkCapabilities, messenger, binder);
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.net.NetworkRequest.CREATOR.createFromParcel(_reply);
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
      @Override public void pendingListenForNetwork(android.net.NetworkCapabilities networkCapabilities, android.app.PendingIntent operation) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((networkCapabilities!=null)) {
            _data.writeInt(1);
            networkCapabilities.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((operation!=null)) {
            _data.writeInt(1);
            operation.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_pendingListenForNetwork, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().pendingListenForNetwork(networkCapabilities, operation);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void releaseNetworkRequest(android.net.NetworkRequest networkRequest) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((networkRequest!=null)) {
            _data.writeInt(1);
            networkRequest.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_releaseNetworkRequest, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().releaseNetworkRequest(networkRequest);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setAcceptUnvalidated(android.net.Network network, boolean accept, boolean always) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((network!=null)) {
            _data.writeInt(1);
            network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(((accept)?(1):(0)));
          _data.writeInt(((always)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAcceptUnvalidated, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setAcceptUnvalidated(network, accept, always);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setAcceptPartialConnectivity(android.net.Network network, boolean accept, boolean always) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((network!=null)) {
            _data.writeInt(1);
            network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(((accept)?(1):(0)));
          _data.writeInt(((always)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAcceptPartialConnectivity, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setAcceptPartialConnectivity(network, accept, always);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void setAvoidUnvalidated(android.net.Network network) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((network!=null)) {
            _data.writeInt(1);
            network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAvoidUnvalidated, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().setAvoidUnvalidated(network);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void startCaptivePortalApp(android.net.Network network) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((network!=null)) {
            _data.writeInt(1);
            network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_startCaptivePortalApp, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().startCaptivePortalApp(network);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void startCaptivePortalAppInternal(android.net.Network network, android.os.Bundle appExtras) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((network!=null)) {
            _data.writeInt(1);
            network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          if ((appExtras!=null)) {
            _data.writeInt(1);
            appExtras.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_startCaptivePortalAppInternal, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().startCaptivePortalAppInternal(network, appExtras);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public boolean shouldAvoidBadWifi() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_shouldAvoidBadWifi, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().shouldAvoidBadWifi();
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
      @Override public int getMultipathPreference(android.net.Network Network) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((Network!=null)) {
            _data.writeInt(1);
            Network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_getMultipathPreference, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getMultipathPreference(Network);
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
      @Override public android.net.NetworkRequest getDefaultRequest() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.net.NetworkRequest _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getDefaultRequest, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getDefaultRequest();
          }
          _reply.readException();
          if ((0!=_reply.readInt())) {
            _result = android.net.NetworkRequest.CREATOR.createFromParcel(_reply);
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
      @Override public int getRestoreDefaultNetworkDelay(int networkType) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(networkType);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRestoreDefaultNetworkDelay, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getRestoreDefaultNetworkDelay(networkType);
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
      @Override public boolean addVpnAddress(java.lang.String address, int prefixLength) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(address);
          _data.writeInt(prefixLength);
          boolean _status = mRemote.transact(Stub.TRANSACTION_addVpnAddress, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().addVpnAddress(address, prefixLength);
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
      @Override public boolean removeVpnAddress(java.lang.String address, int prefixLength) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(address);
          _data.writeInt(prefixLength);
          boolean _status = mRemote.transact(Stub.TRANSACTION_removeVpnAddress, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().removeVpnAddress(address, prefixLength);
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
      @Override public boolean setUnderlyingNetworksForVpn(android.net.Network[] networks) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeTypedArray(networks, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setUnderlyingNetworksForVpn, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().setUnderlyingNetworksForVpn(networks);
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
      @Override public void factoryReset() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_factoryReset, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().factoryReset();
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void startNattKeepalive(android.net.Network network, int intervalSeconds, android.net.ISocketKeepaliveCallback cb, java.lang.String srcAddr, int srcPort, java.lang.String dstAddr) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void startNattKeepaliveWithFd(android.net.Network network, java.io.FileDescriptor fd, int resourceId, int intervalSeconds, android.net.ISocketKeepaliveCallback cb, java.lang.String srcAddr, java.lang.String dstAddr) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void startTcpKeepalive(android.net.Network network, java.io.FileDescriptor fd, int intervalSeconds, android.net.ISocketKeepaliveCallback cb) throws android.os.RemoteException
      {
        throw new UnsupportedOperationException("STUB");
      }
      @Override public void stopKeepalive(android.net.Network network, int slot) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((network!=null)) {
            _data.writeInt(1);
            network.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(slot);
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopKeepalive, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().stopKeepalive(network, slot);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public java.lang.String getCaptivePortalServerUrl() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCaptivePortalServerUrl, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getCaptivePortalServerUrl();
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
      @Override public byte[] getNetworkWatchlistConfigHash() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getNetworkWatchlistConfigHash, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getNetworkWatchlistConfigHash();
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
      @Override public int getConnectionOwnerUid(android.net.ConnectionInfo connectionInfo) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((connectionInfo!=null)) {
            _data.writeInt(1);
            connectionInfo.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_getConnectionOwnerUid, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getConnectionOwnerUid(connectionInfo);
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
      @Override public boolean isCallerCurrentAlwaysOnVpnApp() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isCallerCurrentAlwaysOnVpnApp, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().isCallerCurrentAlwaysOnVpnApp();
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
      @Override public boolean isCallerCurrentAlwaysOnVpnLockdownApp() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isCallerCurrentAlwaysOnVpnLockdownApp, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().isCallerCurrentAlwaysOnVpnLockdownApp();
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
      @Override public void getLatestTetheringEntitlementResult(int type, android.os.ResultReceiver receiver, boolean showEntitlementUi, java.lang.String callerPkg) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(type);
          if ((receiver!=null)) {
            _data.writeInt(1);
            receiver.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          _data.writeInt(((showEntitlementUi)?(1):(0)));
          _data.writeString(callerPkg);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getLatestTetheringEntitlementResult, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().getLatestTetheringEntitlementResult(type, receiver, showEntitlementUi, callerPkg);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void registerTetheringEventCallback(android.net.ITetheringEventCallback callback, java.lang.String callerPkg) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
          _data.writeString(callerPkg);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerTetheringEventCallback, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().registerTetheringEventCallback(callback, callerPkg);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public void unregisterTetheringEventCallback(android.net.ITetheringEventCallback callback, java.lang.String callerPkg) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
          _data.writeString(callerPkg);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterTetheringEventCallback, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().unregisterTetheringEventCallback(callback, callerPkg);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public android.os.IBinder startOrGetTestNetworkService() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.IBinder _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startOrGetTestNetworkService, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().startOrGetTestNetworkService();
          }
          _reply.readException();
          _result = _reply.readStrongBinder();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public static android.net.IConnectivityManager sDefaultImpl;
    }
    static final int TRANSACTION_getActiveNetwork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getActiveNetworkForUid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getActiveNetworkInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getActiveNetworkInfoForUid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_getNetworkInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_getNetworkInfoForUid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_getAllNetworkInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_getNetworkForType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_getAllNetworks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_getDefaultNetworkCapabilitiesForUser = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_isNetworkSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_getActiveLinkProperties = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_getLinkPropertiesForType = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_getLinkProperties = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
    static final int TRANSACTION_getNetworkCapabilities = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
    static final int TRANSACTION_getAllNetworkState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
    static final int TRANSACTION_getActiveNetworkQuotaInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
    static final int TRANSACTION_isActiveNetworkMetered = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
    static final int TRANSACTION_requestRouteToHostAddress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
    static final int TRANSACTION_tether = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
    static final int TRANSACTION_untether = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
    static final int TRANSACTION_getLastTetherError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
    static final int TRANSACTION_isTetheringSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
    static final int TRANSACTION_startTethering = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
    static final int TRANSACTION_stopTethering = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
    static final int TRANSACTION_getTetherableIfaces = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
    static final int TRANSACTION_getTetheredIfaces = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
    static final int TRANSACTION_getTetheringErroredIfaces = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
    static final int TRANSACTION_getTetheredDhcpRanges = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
    static final int TRANSACTION_getTetherableUsbRegexs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
    static final int TRANSACTION_getTetherableWifiRegexs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
    static final int TRANSACTION_getTetherableBluetoothRegexs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
    static final int TRANSACTION_setUsbTethering = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
    static final int TRANSACTION_reportInetCondition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
    static final int TRANSACTION_reportNetworkConnectivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 34);
    static final int TRANSACTION_getGlobalProxy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 35);
    static final int TRANSACTION_setGlobalProxy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 36);
    static final int TRANSACTION_getProxyForNetwork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 37);
    static final int TRANSACTION_prepareVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 38);
    static final int TRANSACTION_setVpnPackageAuthorization = (android.os.IBinder.FIRST_CALL_TRANSACTION + 39);
    static final int TRANSACTION_establishVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 40);
    static final int TRANSACTION_getVpnConfig = (android.os.IBinder.FIRST_CALL_TRANSACTION + 41);
    static final int TRANSACTION_startLegacyVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 42);
    static final int TRANSACTION_getLegacyVpnInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 43);
    static final int TRANSACTION_updateLockdownVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 44);
    static final int TRANSACTION_isAlwaysOnVpnPackageSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 45);
    static final int TRANSACTION_setAlwaysOnVpnPackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 46);
    static final int TRANSACTION_getAlwaysOnVpnPackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 47);
    static final int TRANSACTION_isVpnLockdownEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 48);
    static final int TRANSACTION_getVpnLockdownWhitelist = (android.os.IBinder.FIRST_CALL_TRANSACTION + 49);
    static final int TRANSACTION_checkMobileProvisioning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 50);
    static final int TRANSACTION_getMobileProvisioningUrl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 51);
    static final int TRANSACTION_setProvisioningNotificationVisible = (android.os.IBinder.FIRST_CALL_TRANSACTION + 52);
    static final int TRANSACTION_setAirplaneMode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 53);
    static final int TRANSACTION_registerNetworkFactory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 54);
    static final int TRANSACTION_requestBandwidthUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 55);
    static final int TRANSACTION_unregisterNetworkFactory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 56);
    static final int TRANSACTION_registerNetworkAgent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 57);
    static final int TRANSACTION_requestNetwork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 58);
    static final int TRANSACTION_pendingRequestForNetwork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 59);
    static final int TRANSACTION_releasePendingNetworkRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 60);
    static final int TRANSACTION_listenForNetwork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 61);
    static final int TRANSACTION_pendingListenForNetwork = (android.os.IBinder.FIRST_CALL_TRANSACTION + 62);
    static final int TRANSACTION_releaseNetworkRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 63);
    static final int TRANSACTION_setAcceptUnvalidated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 64);
    static final int TRANSACTION_setAcceptPartialConnectivity = (android.os.IBinder.FIRST_CALL_TRANSACTION + 65);
    static final int TRANSACTION_setAvoidUnvalidated = (android.os.IBinder.FIRST_CALL_TRANSACTION + 66);
    static final int TRANSACTION_startCaptivePortalApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 67);
    static final int TRANSACTION_startCaptivePortalAppInternal = (android.os.IBinder.FIRST_CALL_TRANSACTION + 68);
    static final int TRANSACTION_shouldAvoidBadWifi = (android.os.IBinder.FIRST_CALL_TRANSACTION + 69);
    static final int TRANSACTION_getMultipathPreference = (android.os.IBinder.FIRST_CALL_TRANSACTION + 70);
    static final int TRANSACTION_getDefaultRequest = (android.os.IBinder.FIRST_CALL_TRANSACTION + 71);
    static final int TRANSACTION_getRestoreDefaultNetworkDelay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 72);
    static final int TRANSACTION_addVpnAddress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 73);
    static final int TRANSACTION_removeVpnAddress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 74);
    static final int TRANSACTION_setUnderlyingNetworksForVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 75);
    static final int TRANSACTION_factoryReset = (android.os.IBinder.FIRST_CALL_TRANSACTION + 76);
    static final int TRANSACTION_startNattKeepalive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 77);
    static final int TRANSACTION_startNattKeepaliveWithFd = (android.os.IBinder.FIRST_CALL_TRANSACTION + 78);
    static final int TRANSACTION_startTcpKeepalive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 79);
    static final int TRANSACTION_stopKeepalive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 80);
    static final int TRANSACTION_getCaptivePortalServerUrl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 81);
    static final int TRANSACTION_getNetworkWatchlistConfigHash = (android.os.IBinder.FIRST_CALL_TRANSACTION + 82);
    static final int TRANSACTION_getConnectionOwnerUid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 83);
    static final int TRANSACTION_isCallerCurrentAlwaysOnVpnApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 84);
    static final int TRANSACTION_isCallerCurrentAlwaysOnVpnLockdownApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 85);
    static final int TRANSACTION_getLatestTetheringEntitlementResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 86);
    static final int TRANSACTION_registerTetheringEventCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 87);
    static final int TRANSACTION_unregisterTetheringEventCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 88);
    static final int TRANSACTION_startOrGetTestNetworkService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 89);
    public static boolean setDefaultImpl(android.net.IConnectivityManager impl) {
      if (Stub.Proxy.sDefaultImpl == null && impl != null) {
        Stub.Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static android.net.IConnectivityManager getDefaultImpl() {
      return Stub.Proxy.sDefaultImpl;
    }
  }
  public android.net.Network getActiveNetwork() throws android.os.RemoteException;
  public android.net.Network getActiveNetworkForUid(int uid, boolean ignoreBlocked) throws android.os.RemoteException;
  
  public android.net.NetworkInfo getActiveNetworkInfo() throws android.os.RemoteException;
  public android.net.NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws android.os.RemoteException;
  public android.net.NetworkInfo getNetworkInfo(int networkType) throws android.os.RemoteException;
  public android.net.NetworkInfo getNetworkInfoForUid(android.net.Network network, int uid, boolean ignoreBlocked) throws android.os.RemoteException;
  
  public android.net.NetworkInfo[] getAllNetworkInfo() throws android.os.RemoteException;
  public android.net.Network getNetworkForType(int networkType) throws android.os.RemoteException;
  public android.net.Network[] getAllNetworks() throws android.os.RemoteException;
  public android.net.NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId) throws android.os.RemoteException;
  public boolean isNetworkSupported(int networkType) throws android.os.RemoteException;
  
  public android.net.LinkProperties getActiveLinkProperties() throws android.os.RemoteException;
  public android.net.LinkProperties getLinkPropertiesForType(int networkType) throws android.os.RemoteException;
  public android.net.LinkProperties getLinkProperties(android.net.Network network) throws android.os.RemoteException;
  public android.net.NetworkCapabilities getNetworkCapabilities(android.net.Network network) throws android.os.RemoteException;
  
  public android.net.NetworkState[] getAllNetworkState() throws android.os.RemoteException;
  public android.net.NetworkQuotaInfo getActiveNetworkQuotaInfo() throws android.os.RemoteException;
  public boolean isActiveNetworkMetered() throws android.os.RemoteException;
  public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws android.os.RemoteException;
  public int tether(java.lang.String iface, java.lang.String callerPkg) throws android.os.RemoteException;
  public int untether(java.lang.String iface, java.lang.String callerPkg) throws android.os.RemoteException;
  
  public int getLastTetherError(java.lang.String iface) throws android.os.RemoteException;
  public boolean isTetheringSupported(java.lang.String callerPkg) throws android.os.RemoteException;
  public void startTethering(int type, android.os.ResultReceiver receiver, boolean showProvisioningUi, java.lang.String callerPkg) throws android.os.RemoteException;
  public void stopTethering(int type, java.lang.String callerPkg) throws android.os.RemoteException;
  
  public java.lang.String[] getTetherableIfaces() throws android.os.RemoteException;
  
  public java.lang.String[] getTetheredIfaces() throws android.os.RemoteException;
  
  public java.lang.String[] getTetheringErroredIfaces() throws android.os.RemoteException;
  public java.lang.String[] getTetheredDhcpRanges() throws android.os.RemoteException;
  
  public java.lang.String[] getTetherableUsbRegexs() throws android.os.RemoteException;
  
  public java.lang.String[] getTetherableWifiRegexs() throws android.os.RemoteException;
  public java.lang.String[] getTetherableBluetoothRegexs() throws android.os.RemoteException;
  public int setUsbTethering(boolean enable, java.lang.String callerPkg) throws android.os.RemoteException;
  public void reportInetCondition(int networkType, int percentage) throws android.os.RemoteException;
  public void reportNetworkConnectivity(android.net.Network network, boolean hasConnectivity) throws android.os.RemoteException;
  public android.net.ProxyInfo getGlobalProxy() throws android.os.RemoteException;
  public void setGlobalProxy(android.net.ProxyInfo p) throws android.os.RemoteException;
  public android.net.ProxyInfo getProxyForNetwork(android.net.Network nework) throws android.os.RemoteException;
  public boolean prepareVpn(java.lang.String oldPackage, java.lang.String newPackage, int userId) throws android.os.RemoteException;
  public void setVpnPackageAuthorization(java.lang.String packageName, int userId, boolean authorized) throws android.os.RemoteException;
  public android.os.ParcelFileDescriptor establishVpn(com.android.internal.net.VpnConfig config) throws android.os.RemoteException;
  public com.android.internal.net.VpnConfig getVpnConfig(int userId) throws android.os.RemoteException;
  
  public void startLegacyVpn(com.android.internal.net.VpnProfile profile) throws android.os.RemoteException;
  public com.android.internal.net.LegacyVpnInfo getLegacyVpnInfo(int userId) throws android.os.RemoteException;
  public boolean updateLockdownVpn() throws android.os.RemoteException;
  public boolean isAlwaysOnVpnPackageSupported(int userId, java.lang.String packageName) throws android.os.RemoteException;
  public boolean setAlwaysOnVpnPackage(int userId, java.lang.String packageName, boolean lockdown, java.util.List<java.lang.String> lockdownWhitelist) throws android.os.RemoteException;
  public java.lang.String getAlwaysOnVpnPackage(int userId) throws android.os.RemoteException;
  public boolean isVpnLockdownEnabled(int userId) throws android.os.RemoteException;
  public java.util.List<java.lang.String> getVpnLockdownWhitelist(int userId) throws android.os.RemoteException;
  public int checkMobileProvisioning(int suggestedTimeOutMs) throws android.os.RemoteException;
  public java.lang.String getMobileProvisioningUrl() throws android.os.RemoteException;
  public void setProvisioningNotificationVisible(boolean visible, int networkType, java.lang.String action) throws android.os.RemoteException;
  public void setAirplaneMode(boolean enable) throws android.os.RemoteException;
  public int registerNetworkFactory(android.os.Messenger messenger, java.lang.String name) throws android.os.RemoteException;
  public boolean requestBandwidthUpdate(android.net.Network network) throws android.os.RemoteException;
  public void unregisterNetworkFactory(android.os.Messenger messenger) throws android.os.RemoteException;
  public int registerNetworkAgent(android.os.Messenger messenger, android.net.NetworkInfo ni, android.net.LinkProperties lp, android.net.NetworkCapabilities nc, int score, android.net.NetworkMisc misc, int factorySerialNumber) throws android.os.RemoteException;
  public android.net.NetworkRequest requestNetwork(android.net.NetworkCapabilities networkCapabilities, android.os.Messenger messenger, int timeoutSec, android.os.IBinder binder, int legacy) throws android.os.RemoteException;
  public android.net.NetworkRequest pendingRequestForNetwork(android.net.NetworkCapabilities networkCapabilities, android.app.PendingIntent operation) throws android.os.RemoteException;
  public void releasePendingNetworkRequest(android.app.PendingIntent operation) throws android.os.RemoteException;
  public android.net.NetworkRequest listenForNetwork(android.net.NetworkCapabilities networkCapabilities, android.os.Messenger messenger, android.os.IBinder binder) throws android.os.RemoteException;
  public void pendingListenForNetwork(android.net.NetworkCapabilities networkCapabilities, android.app.PendingIntent operation) throws android.os.RemoteException;
  public void releaseNetworkRequest(android.net.NetworkRequest networkRequest) throws android.os.RemoteException;
  public void setAcceptUnvalidated(android.net.Network network, boolean accept, boolean always) throws android.os.RemoteException;
  public void setAcceptPartialConnectivity(android.net.Network network, boolean accept, boolean always) throws android.os.RemoteException;
  public void setAvoidUnvalidated(android.net.Network network) throws android.os.RemoteException;
  public void startCaptivePortalApp(android.net.Network network) throws android.os.RemoteException;
  public void startCaptivePortalAppInternal(android.net.Network network, android.os.Bundle appExtras) throws android.os.RemoteException;
  public boolean shouldAvoidBadWifi() throws android.os.RemoteException;
  public int getMultipathPreference(android.net.Network Network) throws android.os.RemoteException;
  public android.net.NetworkRequest getDefaultRequest() throws android.os.RemoteException;
  public int getRestoreDefaultNetworkDelay(int networkType) throws android.os.RemoteException;
  public boolean addVpnAddress(java.lang.String address, int prefixLength) throws android.os.RemoteException;
  public boolean removeVpnAddress(java.lang.String address, int prefixLength) throws android.os.RemoteException;
  public boolean setUnderlyingNetworksForVpn(android.net.Network[] networks) throws android.os.RemoteException;
  public void factoryReset() throws android.os.RemoteException;
  public void startNattKeepalive(android.net.Network network, int intervalSeconds, android.net.ISocketKeepaliveCallback cb, java.lang.String srcAddr, int srcPort, java.lang.String dstAddr) throws android.os.RemoteException;
  public void startNattKeepaliveWithFd(android.net.Network network, java.io.FileDescriptor fd, int resourceId, int intervalSeconds, android.net.ISocketKeepaliveCallback cb, java.lang.String srcAddr, java.lang.String dstAddr) throws android.os.RemoteException;
  public void startTcpKeepalive(android.net.Network network, java.io.FileDescriptor fd, int intervalSeconds, android.net.ISocketKeepaliveCallback cb) throws android.os.RemoteException;
  public void stopKeepalive(android.net.Network network, int slot) throws android.os.RemoteException;
  public java.lang.String getCaptivePortalServerUrl() throws android.os.RemoteException;
  public byte[] getNetworkWatchlistConfigHash() throws android.os.RemoteException;
  public int getConnectionOwnerUid(android.net.ConnectionInfo connectionInfo) throws android.os.RemoteException;
  public boolean isCallerCurrentAlwaysOnVpnApp() throws android.os.RemoteException;
  public boolean isCallerCurrentAlwaysOnVpnLockdownApp() throws android.os.RemoteException;
  public void getLatestTetheringEntitlementResult(int type, android.os.ResultReceiver receiver, boolean showEntitlementUi, java.lang.String callerPkg) throws android.os.RemoteException;
  public void registerTetheringEventCallback(android.net.ITetheringEventCallback callback, java.lang.String callerPkg) throws android.os.RemoteException;
  public void unregisterTetheringEventCallback(android.net.ITetheringEventCallback callback, java.lang.String callerPkg) throws android.os.RemoteException;
  public android.os.IBinder startOrGetTestNetworkService() throws android.os.RemoteException;
}
