/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Contains phone state and service related information.
 *
 * The following phone information is included in returned ServiceState:
 *
 * <ul>
 *   <li>Service state: IN_SERVICE, OUT_OF_SERVICE, EMERGENCY_ONLY, POWER_OFF
 *   <li>Roaming indicator
 *   <li>Operator name, short name and numeric id
 *   <li>Network selection mode
 * </ul>
 */
public class ServiceState implements Parcelable {
    /**
     * Normal operation condition, the phone is registered
     * with an operator either in home network or in roaming.
     */
    public static final int STATE_IN_SERVICE = 0;

    /**
     * Phone is not registered with any operator, the phone
     * can be currently searching a new operator to register to, or not
     * searching to registration at all, or registration is denied, or radio
     * signal is not available.
     */
    public static final int STATE_OUT_OF_SERVICE = 1;

    /**
     * The phone is registered and locked.  Only emergency numbers are allowed. {@more}
     */
    public static final int STATE_EMERGENCY_ONLY = 2;

    /**
     * Radio of telephony is explicitly powered off.
     */
    public static final int STATE_POWER_OFF = 3;

    /**
     * RIL level registration state values from ril.h
     * ((const char **)response)[0] is registration state 0-6,
     *              0 - Not registered, MT is not currently searching
     *                  a new operator to register
     *              1 - Registered, home network
     *              2 - Not registered, but MT is currently searching
     *                  a new operator to register
     *              3 - Registration denied
     *              4 - Unknown
     *              5 - Registered, roaming
     *             10 - Same as 0, but indicates that emergency calls
     *                  are enabled.
     *             12 - Same as 2, but indicates that emergency calls
     *                  are enabled.
     *             13 - Same as 3, but indicates that emergency calls
     *                  are enabled.
     *             14 - Same as 4, but indicates that emergency calls
     *                  are enabled.
     * @hide
     */
    public static final int RIL_REG_STATE_NOT_REG = 0;
    /** @hide */
    public static final int RIL_REG_STATE_HOME = 1;
    /** @hide */
    public static final int RIL_REG_STATE_SEARCHING = 2;
    /** @hide */
    public static final int RIL_REG_STATE_DENIED = 3;
    /** @hide */
    public static final int RIL_REG_STATE_UNKNOWN = 4;
    /** @hide */
    public static final int RIL_REG_STATE_ROAMING = 5;
    /** @hide */
    public static final int RIL_REG_STATE_NOT_REG_EMERGENCY_CALL_ENABLED = 10;
    /** @hide */
    public static final int RIL_REG_STATE_SEARCHING_EMERGENCY_CALL_ENABLED = 12;
    /** @hide */
    public static final int RIL_REG_STATE_DENIED_EMERGENCY_CALL_ENABLED = 13;
    /** @hide */
    public static final int RIL_REG_STATE_UNKNOWN_EMERGENCY_CALL_ENABLED = 14;

    /**
     * Available radio technologies for GSM, UMTS and CDMA.
     * Duplicates the constants from hardware/radio/include/ril.h
     * This should only be used by agents working with the ril.  Others
     * should use the equivalent TelephonyManager.NETWORK_TYPE_*
     */
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_UNKNOWN = 0;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_GPRS = 1;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_EDGE = 2;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_UMTS = 3;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_IS95A = 4;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_IS95B = 5;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_1xRTT = 6;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_0 = 7;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_A = 8;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_HSDPA = 9;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_HSUPA = 10;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_HSPA = 11;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_EVDO_B = 12;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_EHRPD = 13;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_LTE = 14;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_HSPAP = 15;
    /**
     * GSM radio technology only supports voice. It does not support data.
     * @hide
     */
    public static final int RIL_RADIO_TECHNOLOGY_GSM = 16;
    /** @hide */
    public static final int RIL_RADIO_TECHNOLOGY_TD_SCDMA = 17;
    /**
     * IWLAN
     * @hide
     */
    public static final int RIL_RADIO_TECHNOLOGY_IWLAN = 18;

    /**
     * LTE_CA
     * @hide
     */
    public static final int RIL_RADIO_TECHNOLOGY_LTE_CA = 19;

    /** @hide */
    public static final int RIL_RADIO_CDMA_TECHNOLOGY_BITMASK =
            (1 << (RIL_RADIO_TECHNOLOGY_IS95A - 1))
                    | (1 << (RIL_RADIO_TECHNOLOGY_IS95B - 1))
                    | (1 << (RIL_RADIO_TECHNOLOGY_1xRTT - 1))
                    | (1 << (RIL_RADIO_TECHNOLOGY_EVDO_0 - 1))
                    | (1 << (RIL_RADIO_TECHNOLOGY_EVDO_A - 1))
                    | (1 << (RIL_RADIO_TECHNOLOGY_EVDO_B - 1))
                    | (1 << (RIL_RADIO_TECHNOLOGY_EHRPD - 1));

    /**
     * Available registration states for GSM, UMTS and CDMA.
     */
    /** @hide */
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_NOT_SEARCHING = 0;
    /** @hide */
    public static final int REGISTRATION_STATE_HOME_NETWORK = 1;
    /** @hide */
    public static final int REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING = 2;
    /** @hide */
    public static final int REGISTRATION_STATE_REGISTRATION_DENIED = 3;
    /** @hide */
    public static final int REGISTRATION_STATE_UNKNOWN = 4;
    /** @hide */
    public static final int REGISTRATION_STATE_ROAMING = 5;

    /**
     * Roaming type
     * HOME : in home network
     * @hide
     */
    public static final int ROAMING_TYPE_NOT_ROAMING = 0;
    /**
     * Roaming type
     * UNKNOWN : in a roaming network, but we can not tell if it's domestic or international
     * @hide
     */
    public static final int ROAMING_TYPE_UNKNOWN = 1;
    /**
     * Roaming type
     * DOMESTIC : in domestic roaming network
     * @hide
     */
    public static final int ROAMING_TYPE_DOMESTIC = 2;
    /**
     * Roaming type
     * INTERNATIONAL : in international roaming network
     * @hide
     */
    public static final int ROAMING_TYPE_INTERNATIONAL = 3;

    /**
     * get String description of roaming type
     * @hide
     */
    public static final String getRoamingLogString(int roamingType) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Create a new ServiceState from a intent notifier Bundle
     *
     * This method is used by PhoneStateIntentReceiver and maybe by
     * external applications.
     *
     * @param m Bundle from intent notifier
     * @return newly created ServiceState
     * @hide
     */
    public static ServiceState newFromBundle(Bundle m) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Empty constructor
     */
    public ServiceState() {
    }

    /**
     * Copy constructors
     *
     * @param s Source service state
     */
    public ServiceState(ServiceState s) {
        copyFrom(s);
    }

    protected void copyFrom(ServiceState s) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Construct a ServiceState object from the given parcel.
     */
    public ServiceState(Parcel in) {
        throw new UnsupportedOperationException("STUB");
    }

    public void writeToParcel(Parcel out, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ServiceState> CREATOR =
            new Parcelable.Creator<ServiceState>() {
        public ServiceState createFromParcel(Parcel in) {
            return new ServiceState(in);
        }

        public ServiceState[] newArray(int size) {
            return new ServiceState[size];
        }
    };

    /**
     * Get current voice service state
     */
    public int getState() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current voice service state
     *
     * @see #STATE_IN_SERVICE
     * @see #STATE_OUT_OF_SERVICE
     * @see #STATE_EMERGENCY_ONLY
     * @see #STATE_POWER_OFF
     *
     * @hide
     */
    public int getVoiceRegState() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current data service state
     *
     * @see #STATE_IN_SERVICE
     * @see #STATE_OUT_OF_SERVICE
     * @see #STATE_EMERGENCY_ONLY
     * @see #STATE_POWER_OFF
     *
     * @hide
     */
    public int getDataRegState() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current roaming indicator of phone
     * (note: not just decoding from TS 27.007 7.2)
     *
     * @return true if TS 27.007 7.2 roaming is true
     *              and ONS is different from SPN
     */
    public boolean getRoaming() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current voice network roaming status
     * @return roaming status
     * @hide
     */
    public boolean getVoiceRoaming() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current voice network roaming type
     * @return roaming type
     * @hide
     */
    public int getVoiceRoamingType() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current data network roaming type
     * @return roaming type
     * @hide
     */
    public boolean getDataRoaming() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Set whether data network registration state is roaming
     *
     * This should only be set to the roaming value received
     * once the data registration phase has completed.
     * @hide
     */
    public void setDataRoamingFromRegistration(boolean dataRoaming) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get whether data network registration state is roaming
     * @return true if registration indicates roaming, false otherwise
     * @hide
     */
    public boolean getDataRoamingFromRegistration() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current data network roaming type
     * @return roaming type
     * @hide
     */
    public int getDataRoamingType() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public boolean isEmergencyOnly() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public int getCdmaRoamingIndicator(){
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public int getCdmaDefaultRoamingIndicator(){
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public int getCdmaEriIconIndex() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public int getCdmaEriIconMode() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered operator name in long alphanumeric format.
     *
     * In GSM/UMTS, long format can be up to 16 characters long.
     * In CDMA, returns the ERI text, if set. Otherwise, returns the ONS.
     *
     * @return long name of operator, null if unregistered or unknown
     */
    public String getOperatorAlphaLong() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered voice network operator name in long alphanumeric format.
     * @return long name of operator
     * @hide
     */
    public String getVoiceOperatorAlphaLong() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered data network operator name in long alphanumeric format.
     * @return long name of voice operator
     * @hide
     */
    public String getDataOperatorAlphaLong() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered operator name in short alphanumeric format.
     *
     * In GSM/UMTS, short format can be up to 8 characters long.
     *
     * @return short name of operator, null if unregistered or unknown
     */
    public String getOperatorAlphaShort() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered voice network operator name in short alphanumeric format.
     * @return short name of operator, null if unregistered or unknown
     * @hide
     */
    public String getVoiceOperatorAlphaShort() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered data network operator name in short alphanumeric format.
     * @return short name of operator, null if unregistered or unknown
     * @hide
     */
    public String getDataOperatorAlphaShort() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered operator numeric id.
     *
     * In GSM/UMTS, numeric format is 3 digit country code plus 2 or 3 digit
     * network code.
     *
     * @return numeric format of operator, null if unregistered or unknown
     */
    /*
     * The country code can be decoded using
     * {@link com.android.internal.telephony.MccTable#countryCodeForMcc(int)}.
     */
    public String getOperatorNumeric() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered voice network operator numeric id.
     * @return numeric format of operator, null if unregistered or unknown
     * @hide
     */
    public String getVoiceOperatorNumeric() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current registered data network operator numeric id.
     * @return numeric format of operator, null if unregistered or unknown
     * @hide
     */
    public String getDataOperatorNumeric() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Get current network selection mode.
     *
     * @return true if manual mode, false if automatic mode
     */
    public boolean getIsManualSelection() {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Convert radio technology to String
     *
     * @param rt
     * @return String representation of the RAT
     *
     * @hide
     */
    public static String rilRadioTechnologyToString(int rt) {
        throw new UnsupportedOperationException("STUB");
    }

    public void setStateOutOfService() {
        throw new UnsupportedOperationException("STUB");
    }

    public void setStateOff() {
        throw new UnsupportedOperationException("STUB");
    }

    public void setState(int state) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setVoiceRegState(int state) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setDataRegState(int state) {
        throw new UnsupportedOperationException("STUB");
    }

    public void setRoaming(boolean roaming) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setVoiceRoaming(boolean roaming) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setVoiceRoamingType(int type) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setDataRoaming(boolean dataRoaming) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setDataRoamingType(int type) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public void setEmergencyOnly(boolean emergencyOnly) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public void setCdmaRoamingIndicator(int roaming) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public void setCdmaDefaultRoamingIndicator (int roaming) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public void setCdmaEriIconIndex(int index) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * @hide
     */
    public void setCdmaEriIconMode(int mode) {
        throw new UnsupportedOperationException("STUB");
    }

    public void setOperatorName(String longName, String shortName, String numeric) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setVoiceOperatorName(String longName, String shortName, String numeric) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setDataOperatorName(String longName, String shortName, String numeric) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * In CDMA, mOperatorAlphaLong can be set from the ERI text.
     * This is done from the GsmCdmaPhone and not from the ServiceStateTracker.
     *
     * @hide
     */
    public void setOperatorAlphaLong(String longName) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setVoiceOperatorAlphaLong(String longName) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setDataOperatorAlphaLong(String longName) {
        throw new UnsupportedOperationException("STUB");
    }

    public void setIsManualSelection(boolean isManual) {
        throw new UnsupportedOperationException("STUB");
    }


    /**
     * Set intent notifier Bundle based on service state.
     *
     * @param m intent notifier Bundle
     * @hide
     */
    public void fillInNotifierBundle(Bundle m) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setRilVoiceRadioTechnology(int rt) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setRilDataRadioTechnology(int rt) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public boolean isUsingCarrierAggregation() {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setIsUsingCarrierAggregation(boolean ca) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setCssIndicator(int css) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public void setSystemAndNetworkId(int systemId, int networkId) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public int getRilVoiceRadioTechnology() {
        throw new UnsupportedOperationException("STUB");
    }
    /** @hide */
    public int getRilDataRadioTechnology() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * @hide
     * @Deprecated to be removed Q3 2013 use {@link #getRilDataRadioTechnology} or
     * {@link #getRilVoiceRadioTechnology}
     */
    public int getRadioTechnology() {
        throw new UnsupportedOperationException("STUB");
    }


    /**
     * @Deprecated to be removed Q3 2013 use {@link #getVoiceNetworkType}
     * @hide
     */
    public int getNetworkType() {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public int getDataNetworkType() {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public int getVoiceNetworkType() {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public int getCssIndicator() {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public int getNetworkId() {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public int getSystemId() {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public static boolean isGsm(int radioTechnology) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public static boolean isCdma(int radioTechnology) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public static boolean isLte(int radioTechnology) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public static boolean bearerBitmapHasCdma(int radioTechnologyBitmap) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public static boolean bitmaskHasTech(int bearerBitmask, int radioTech) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public static int getBitmaskForTech(int radioTech) {
        throw new UnsupportedOperationException("STUB");
    }

    /** @hide */
    public static int getBitmaskFromString(String bearerList) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Returns a merged ServiceState consisting of the base SS with voice settings from the
     * voice SS. The voice SS is only used if it is IN_SERVICE (otherwise the base SS is returned).
     * @hide
     * */
    public static ServiceState mergeServiceStates(ServiceState baseSs, ServiceState voiceSs) {
        throw new UnsupportedOperationException("STUB");
    }
}
