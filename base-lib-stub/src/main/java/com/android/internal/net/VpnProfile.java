/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.net;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Parcel-like entity class for VPN profiles. To keep things simple, all
 * fields are package private. Methods are provided for serialization, so
 * storage can be implemented easily. Two rules are set for this class.
 * First, all fields must be kept non-null. Second, always make a copy
 * using clone() before modifying.
 *
 * @hide
 */
public class VpnProfile implements Cloneable, Parcelable {
    private static final String TAG = "VpnProfile";

    // Match these constants with R.array.vpn_types.
    public static final int TYPE_PPTP = 0;
    public static final int TYPE_L2TP_IPSEC_PSK = 1;
    public static final int TYPE_L2TP_IPSEC_RSA = 2;
    public static final int TYPE_IPSEC_XAUTH_PSK = 3;
    public static final int TYPE_IPSEC_XAUTH_RSA = 4;
    public static final int TYPE_IPSEC_HYBRID_RSA = 5;
    public static final int TYPE_MAX = 5;

    // Match these constants with R.array.vpn_proxy_settings.
    public static final int PROXY_NONE = 0;
    public static final int PROXY_MANUAL = 1;

    // Entity fields.
    @UnsupportedAppUsage
    public final String key;           // -1
    @UnsupportedAppUsage
    public String name = "";           // 0
    @UnsupportedAppUsage
    public int type = TYPE_PPTP;       // 1
    public String server = "";         // 2
    @UnsupportedAppUsage
    public String username = "";       // 3
    public String password = "";       // 4
    public String dnsServers = "";     // 5
    public String searchDomains = "";  // 6
    public String routes = "";         // 7
    public boolean mppe = true;        // 8
    public String l2tpSecret = "";     // 9
    public String ipsecIdentifier = "";// 10
    public String ipsecSecret = "";    // 11
    public String ipsecUserCert = "";  // 12
    public String ipsecCaCert = "";    // 13
    public String ipsecServerCert = "";// 14
    public Object proxy = null;     // 15~18

    // Helper fields.
    @UnsupportedAppUsage
    public boolean saveLogin = false;

    public VpnProfile(String key) {
        this.key = key;
    }

    @UnsupportedAppUsage
    public VpnProfile(Parcel in) {
        key = in.readString();
        name = in.readString();
        type = in.readInt();
        server = in.readString();
        username = in.readString();
        password = in.readString();
        dnsServers = in.readString();
        searchDomains = in.readString();
        routes = in.readString();
        mppe = in.readInt() != 0;
        l2tpSecret = in.readString();
        ipsecIdentifier = in.readString();
        ipsecSecret = in.readString();
        ipsecUserCert = in.readString();
        ipsecCaCert = in.readString();
        ipsecServerCert = in.readString();
        saveLogin = in.readInt() != 0;
        proxy = in.readParcelable(null);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
//        out.writeString(key);
//        out.writeString(name);
//        out.writeInt(type);
//        out.writeString(server);
//        out.writeString(username);
//        out.writeString(password);
//        out.writeString(dnsServers);
//        out.writeString(searchDomains);
//        out.writeString(routes);
//        out.writeInt(mppe ? 1 : 0);
//        out.writeString(l2tpSecret);
//        out.writeString(ipsecIdentifier);
//        out.writeString(ipsecSecret);
//        out.writeString(ipsecUserCert);
//        out.writeString(ipsecCaCert);
//        out.writeString(ipsecServerCert);
//        out.writeInt(saveLogin ? 1 : 0);
//        out.writeParcelable(proxy, flags);
        throw new UnsupportedOperationException("STUB");
    }

    @UnsupportedAppUsage
    public static VpnProfile decode(String key, byte[] value) {
        throw new UnsupportedOperationException("STUB");
    }

    public byte[] encode() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Tests if profile is valid for lockdown, which requires IPv4 address for
     * both server and DNS. Server hostnames would require using DNS before
     * connection.
     */
    public boolean isValidLockdownProfile() {
        return isTypeValidForLockdown()
                && isServerAddressNumeric()
                && hasDns()
                && areDnsAddressesNumeric();
    }

    /**
     * Returns {@code true} if the VPN type is valid for lockdown.
     */
    public boolean isTypeValidForLockdown() {
        // b/7064069: lockdown firewall blocks ports used for PPTP
        return type != TYPE_PPTP;
    }

    /**
     * Returns {@code true} if the server address is numeric, e.g. 8.8.8.8
     */
    public boolean isServerAddressNumeric() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns {@code true} if one or more DNS servers are specified.
     */
    public boolean hasDns() {
        return !TextUtils.isEmpty(dnsServers);
    }

    /**
     * Returns {@code true} if all DNS servers have numeric addresses,
     * e.g. 8.8.8.8
     */
    public boolean areDnsAddressesNumeric() {
        throw new UnsupportedOperationException("STUB");
    }

    public static final Creator<VpnProfile> CREATOR = new Creator<VpnProfile>() {
        @Override
        public VpnProfile createFromParcel(Parcel in) {
            return new VpnProfile(in);
        }

        @Override
        public VpnProfile[] newArray(int size) {
            return new VpnProfile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
