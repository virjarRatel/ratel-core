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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple container used to carry information in VpnBuilder, VpnDialogs,
 * and com.android.server.connectivity.Vpn. Internal use only.
 *
 * @hide
 */
public class VpnConfig implements Parcelable {

    public static final String SERVICE_INTERFACE = "android.net.VpnService";

    public static final String DIALOGS_PACKAGE = "com.android.vpndialogs";

    public static final String LEGACY_VPN = "[Legacy VPN]";

    public static Intent getIntentForConfirmation() {
        throw new UnsupportedOperationException("STUB");
    }

    /** NOTE: This should only be used for legacy VPN. */
    public static PendingIntent getIntentForStatusPanel(Context context) {
        throw new UnsupportedOperationException("STUB");
    }

    public static CharSequence getVpnLabel(Context context, String packageName)
            throws NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(SERVICE_INTERFACE);
        intent.setPackage(packageName);
        List<ResolveInfo> services = pm.queryIntentServices(intent, 0 /* flags */);
        if (services != null && services.size() == 1) {
            // This app contains exactly one VPN service. Call loadLabel, which will attempt to
            // load the service's label, and fall back to the app label if none is present.
            return services.get(0).loadLabel(pm);
        } else {
            return pm.getApplicationInfo(packageName, 0).loadLabel(pm);
        }
    }

    public String user;
    public String interfaze;
    public String session;
    public int mtu = -1;
    public List<Object> addresses = new ArrayList<Object>();
    public List<Object> routes = new ArrayList<Object>();
    public List<String> dnsServers;
    public List<String> searchDomains;
    public List<String> allowedApplications;
    public List<String> disallowedApplications;
    public PendingIntent configureIntent;
    public long startTime = -1;
    public boolean legacy;
    public boolean blocking;
    public boolean allowBypass;
    public boolean allowIPv4;
    public boolean allowIPv6;
    public boolean isMetered = true;
    public Object[] underlyingNetworks;
    public Object proxyInfo;

    public void updateAllowedFamilies(InetAddress address) {
        if (address instanceof Inet4Address) {
            allowIPv4 = true;
        } else {
            allowIPv6 = true;
        }
    }

    public void addLegacyRoutes(String routesStr) {
        throw new UnsupportedOperationException("STUB");
    }

    public void addLegacyAddresses(String addressesStr) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final Parcelable.Creator<VpnConfig> CREATOR =
            new Parcelable.Creator<VpnConfig>() {
        @Override
        public VpnConfig createFromParcel(Parcel in) {
//            VpnConfig config = new VpnConfig();
//            config.user = in.readString();
//            config.interfaze = in.readString();
//            config.session = in.readString();
//            config.mtu = in.readInt();
//            in.readTypedList(config.addresses, LinkAddress.CREATOR);
//            in.readTypedList(config.routes, RouteInfo.CREATOR);
//            config.dnsServers = in.createStringArrayList();
//            config.searchDomains = in.createStringArrayList();
//            config.allowedApplications = in.createStringArrayList();
//            config.disallowedApplications = in.createStringArrayList();
//            config.configureIntent = in.readParcelable(null);
//            config.startTime = in.readLong();
//            config.legacy = in.readInt() != 0;
//            config.blocking = in.readInt() != 0;
//            config.allowBypass = in.readInt() != 0;
//            config.allowIPv4 = in.readInt() != 0;
//            config.allowIPv6 = in.readInt() != 0;
//            config.isMetered = in.readInt() != 0;
//            config.underlyingNetworks = in.createTypedArray(Network.CREATOR);
//            config.proxyInfo = in.readParcelable(null);
//            return config;
            throw new UnsupportedOperationException("STUB");
        }

        @Override
        public VpnConfig[] newArray(int size) {
            return new VpnConfig[size];
        }
    };

    @Override
    public String toString() {
        return new StringBuilder()
                .append("VpnConfig")
                .append("{ user=").append(user)
                .append(", interface=").append(interfaze)
                .append(", session=").append(session)
                .append(", mtu=").append(mtu)
                .append(", addresses=").append(toString(addresses))
                .append(", routes=").append(toString(routes))
                .append(", dns=").append(toString(dnsServers))
                .append(", searchDomains=").append(toString(searchDomains))
                .append(", allowedApps=").append(toString(allowedApplications))
                .append(", disallowedApps=").append(toString(disallowedApplications))
                .append(", configureIntent=").append(configureIntent)
                .append(", startTime=").append(startTime)
                .append(", legacy=").append(legacy)
                .append(", blocking=").append(blocking)
                .append(", allowBypass=").append(allowBypass)
                .append(", allowIPv4=").append(allowIPv4)
                .append(", allowIPv6=").append(allowIPv6)
                .append(", underlyingNetworks=").append(Arrays.toString(underlyingNetworks))
                .append(", proxyInfo=").append(proxyInfo.toString())
                .append("}")
                .toString();
    }

    static <T> String toString(List<T> ls) {
        if (ls == null) {
            return "null";
        }
        return Arrays.toString(ls.toArray());
    }
}
