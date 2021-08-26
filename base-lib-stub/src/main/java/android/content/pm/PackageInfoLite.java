/*
 * Copyright (C) 2007 The Android Open Source Project
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

/**
 * Basic information about a package as specified in its manifest.
 * Utility class used in PackageManager methods
 * @hide
 */
public class PackageInfoLite implements Parcelable {
    /**
     * The name of this package.  From the &lt;manifest&gt; tag's "name"
     * attribute.
     */
    public String packageName;

    /** Names of any split APKs, ordered by parsed splitName */
    public String[] splitNames;

    /**
     * The android:versionCode of the package.
     */
    public int versionCode;

    /** Revision code of base APK */
    public int baseRevisionCode;
    /** Revision codes of any split APKs, ordered by parsed splitName */
    public int[] splitRevisionCodes;

    /**
     * The android:multiArch flag from the package manifest. If set,
     * we will extract all native libraries for the given app, not just those
     * from the preferred ABI.
     */
    public boolean multiArch;

    public int recommendedInstallLocation;
    public int installLocation;

    public VerifierInfo[] verifiers;

    public PackageInfoLite() {
    }

    public String toString() {
        return "PackageInfoLite{"
                + Integer.toHexString(System.identityHashCode(this))
                + " " + packageName + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final Parcelable.Creator<PackageInfoLite> CREATOR
            = new Parcelable.Creator<PackageInfoLite>() {
        public PackageInfoLite createFromParcel(Parcel source) {
            return new PackageInfoLite(source);
        }

        public PackageInfoLite[] newArray(int size) {
            return new PackageInfoLite[size];
        }
    };

    private PackageInfoLite(Parcel source) {
        throw new UnsupportedOperationException("STUB");
    }
}
