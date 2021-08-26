/*
* Copyright (C) 2014 The Android Open Source Project
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

package android.telephony;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Object to indicate the phone radio type and access technology.
 *
 * @hide
 */
public class RadioAccessFamily implements Parcelable {

    // Radio Access Family
    // 2G
    public static final int RAF_UNKNOWN = (1 <<  ServiceState.RIL_RADIO_TECHNOLOGY_UNKNOWN);
    public static final int RAF_GSM = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_GSM);
    public static final int RAF_GPRS = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_GPRS);
    public static final int RAF_EDGE = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_EDGE);
    public static final int RAF_IS95A = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_IS95A);
    public static final int RAF_IS95B = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_IS95B);
    public static final int RAF_1xRTT = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_1xRTT);
    // 3G
    public static final int RAF_EVDO_0 = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_0);
    public static final int RAF_EVDO_A = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_A);
    public static final int RAF_EVDO_B = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_EVDO_B);
    public static final int RAF_EHRPD = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_EHRPD);
    public static final int RAF_HSUPA = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_HSUPA);
    public static final int RAF_HSDPA = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_HSDPA);
    public static final int RAF_HSPA = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_HSPA);
    public static final int RAF_HSPAP = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_HSPAP);
    public static final int RAF_UMTS = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_UMTS);
    public static final int RAF_TD_SCDMA = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_TD_SCDMA);
    // 4G
    public static final int RAF_LTE = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_LTE);
    public static final int RAF_LTE_CA = (1 << ServiceState.RIL_RADIO_TECHNOLOGY_LTE_CA);

    /**
     * Constructor.
     *
     * @param phoneId the phone ID
     * @param radioAccessFamily the phone radio access family defined
     *        in RadioAccessFamily. It's a bit mask value to represent
     *        the support type.
     */
    public RadioAccessFamily(int phoneId, int radioAccessFamily) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get phone ID.
     *
     * @return phone ID
     */
    public int getPhoneId() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * get radio access family.
     *
     * @return radio access family
     */
    public int getRadioAccessFamily() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Implement the Parcelable interface.
     */
    public static final Creator<RadioAccessFamily> CREATOR =
            new Creator<RadioAccessFamily>() {

                @Override
                public RadioAccessFamily createFromParcel(Parcel in) {
                    throw new UnsupportedOperationException("STUB");
                }

                @Override
                public RadioAccessFamily[] newArray(int size) {
                    throw new UnsupportedOperationException("STUB");
                }
            };

    public static int getRafFromNetworkType(int type) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns the highest capability of the RadioAccessFamily (4G > 3G > 2G).
     * @param raf The RadioAccessFamily that we wish to filter
     * @return The highest radio capability
     */
    public static int getHighestRafCapability(int raf) {
        throw new UnsupportedOperationException("STUB");
    }

    public static int getNetworkTypeFromRaf(int raf) {
        throw new UnsupportedOperationException("STUB");
    }

    public static int singleRafTypeFromString(String rafString) {
        throw new UnsupportedOperationException("STUB");
    }

    public static int rafTypeFromString(String rafList) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int describeContents() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        throw new UnsupportedOperationException("STUB");
    }
}
