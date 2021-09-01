package com.virjar.ratel.envmock;

import android.net.NetworkCapabilities;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.runtime.RatelRuntime;

import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;


class NetworkMockController {

    private static final String IConnectivityManagerIPCClassName = "android.net.IConnectivityManager$Stub$Proxy";

    void doMock() {
        if (RatelRuntime.getSdkInt() < 21) {
            //TOOD 还不支持4.4 不过4.4之前应该检测
            //java.net.NetworkInterface.getNetworkInterfaces()
            return;
        }
        //android.net.IConnectivityManager.Stub.Proxy#getNetworkCapabilities
        Class<?> connectivityManagerProxyClass = RposedHelpers.findClassIfExists(IConnectivityManagerIPCClassName, ClassLoader.getSystemClassLoader());
        if (connectivityManagerProxyClass == null) {
            Log.w(Constants.VENV_TAG, "can not find IConnectivityManager ipc class: " + IConnectivityManagerIPCClassName);
            return;
        }

        RposedBridge.hookAllMethods(connectivityManagerProxyClass, "getNetworkCapabilities", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //如果手机挂了vpn，那么删除vpn的痕迹
                NetworkCapabilities networkCapabilities = (NetworkCapabilities) param.getResult();
                if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return;
                }
                RposedHelpers.callMethod(networkCapabilities, "removeTransportType", NetworkCapabilities.TRANSPORT_VPN);
                try {
                    RposedHelpers.callMethod(networkCapabilities, "addTransportType", NetworkCapabilities.NET_CAPABILITY_NOT_VPN);
                } catch (Throwable throwable) {
                    //ignore
                }
            }
        });

//        try {
//            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while (networkInterfaces.hasMoreElements()) {
//                NetworkInterface networkInterface = networkInterfaces.nextElement();
//                String name = networkInterface.getName();
//                if (name == null) {
//                    continue;
//                }
//                if (name.contains("wlan")) {
//                    Log.i("weijia", name + " wlan_addr:" + byte2Hex(networkInterface.getHardwareAddress()));
//                }
//            }
//
//        } catch (IOException e) {
//            Log.e("weijia", "error", e);
//        }


        // 现在网卡地址模拟迁移到c层实现
        RatelNative.enableMacAddressFake();

        Method getAllMethod = RposedHelpers.findMethodExactIfExists(NetworkInterface.class, "getAll");
        if (getAllMethod != null) {
            //TODO Android6上面没有这个函数
            //java.net.NetworkInterface#getAll
            RposedBridge.hookMethod(getAllMethod, new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    NetworkInterface[] networkInterfaces = (NetworkInterface[]) param.getResult();
                    List<NetworkInterface> modified = new ArrayList<>();
                    for (NetworkInterface networkInterface : networkInterfaces) {
                        String interfaceName = networkInterface.getName();
                        // 过滤虚拟网卡
                        if (interfaceName.contains("other0")
                                || interfaceName.contains("tun0")
                                || interfaceName.contains("pptp0")) {
                            continue;
                        }
                        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                        while (inetAddresses.hasMoreElements()) {
                            InetAddress inetAddress = inetAddresses.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                                // ipv6的地址需要模拟
                                Inet6Address inet6Address = (Inet6Address) inetAddress;
                                if (!inet6Address.isIPv4CompatibleAddress()) {
                                    //inet6Address.getAddress();
                                    byte[] address = RposedHelpers.getObjectField(
                                            RposedHelpers.getObjectField(inet6Address, "holder6")
                                            , "ipaddress");
                                    Random random = new Random(RatelToolKit.userIdentifierSeedInt + 100);
                                    for (int i = 1; i <= 3; i++) {
                                        address[address.length - i] = (byte) (address[address.length - i] ^ random.nextInt(512));
                                    }
                                }
                            }
                        }

                        //modifyHardwareAddress(networkInterface);
                        modified.add(networkInterface);
                    }

                    //TODO update network index
                    param.setResult(modified.toArray(new NetworkInterface[0]));
                }

            });
        }

        Class<?> Inet6AddressHolderClass = RposedHelpers.findClassIfExists("java.net.Inet6Address$Inet6AddressHolder", ClassLoader.getSystemClassLoader());
        //java.net.Inet6Address.Inet6AddressHolder.init(byte[], java.net.NetworkInterface)
        if (Inet6AddressHolderClass != null) {
            RposedHelpers.findAndHookMethod(
                    Inet6AddressHolderClass,
                    "init",
                    byte[].class, NetworkInterface.class, new RC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            byte[] address = (byte[]) param.args[0];
                            Random random = new Random(RatelToolKit.userIdentifierSeedInt + 100);
                            for (int i = 1; i <= 3; i++) {
                                address[address.length - i] = (byte) (address[address.length - i] ^ random.nextInt(512));
                            }

                        }
                    }
            );
        }

    }

    private static String byte2Hex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
            sb.append(":");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString().trim();
    }
//
//    /**
//     * hex转byte数组
//     *
//     * @param hex
//     * @return
//     */
//    public static byte[] hexToByte(String hex) {
//        hex = hex.replaceAll(":", "");
//        int m = 0, n = 0;
//        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
//        byte[] ret = new byte[byteLen];
//        for (int i = 0; i < byteLen; i++) {
//            m = i * 2 + 1;
//            n = m + 1;
//            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
//            ret[i] = (byte) intVal;
//        }
//        return ret;
//    }
}
