/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;

import static android.content.pm.PackageManager.*;

public final class IntentFilterVerificationInfo implements Parcelable {
    private static final String TAG = IntentFilterVerificationInfo.class.getName();

    private static final String TAG_DOMAIN = "domain";
    private static final String ATTR_DOMAIN_NAME = "name";
    private static final String ATTR_PACKAGE_NAME = "packageName";
    private static final String ATTR_STATUS = "status";

    private ArraySet<String> mDomains = new ArraySet<>();
    private String mPackageName;
    private int mMainStatus;

    public IntentFilterVerificationInfo() {
        mPackageName = null;
        mMainStatus = INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_UNDEFINED;
    }

    public IntentFilterVerificationInfo(String packageName, ArrayList<String> domains) {
        mPackageName = packageName;
        mDomains.addAll(domains);
        mMainStatus = INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_UNDEFINED;
    }

    public IntentFilterVerificationInfo(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        readFromXml(parser);
    }

    public IntentFilterVerificationInfo(Parcel source) {
        readFromParcel(source);
    }

    public String getPackageName() {
        return mPackageName;
    }

    public int getStatus() {
        return mMainStatus;
    }

    public void setStatus(int s) {
        throw new UnsupportedOperationException("STUB");
    }

    public ArraySet<String> getDomains() {
        return mDomains;
    }

    public void setDomains(ArrayList<String> list) {
    }

    public String getDomainsString() {
        throw new UnsupportedOperationException("STUB");
    }

    String getStringFromXml(XmlPullParser parser, String attribute, String defaultValue) {
        throw new UnsupportedOperationException("STUB");
    }

    int getIntFromXml(XmlPullParser parser, String attribute, int defaultValue) {
        throw new UnsupportedOperationException("STUB");
    }

    public void readFromXml(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        throw new UnsupportedOperationException("STUB");
    }

    public void writeToXml(XmlSerializer serializer) throws IOException {
        throw new UnsupportedOperationException("STUB");
    }

    public String getStatusString() {
        return getStatusStringFromValue(mMainStatus);
    }

    public static String getStatusStringFromValue(long val) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel source) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final Creator<IntentFilterVerificationInfo> CREATOR =
            new Creator<IntentFilterVerificationInfo>() {
                public IntentFilterVerificationInfo createFromParcel(Parcel source) {
                    return new IntentFilterVerificationInfo(source);
                }
                public IntentFilterVerificationInfo[] newArray(int size) {
                    return new IntentFilterVerificationInfo[size];
                }
            };
}
