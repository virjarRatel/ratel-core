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

package android.os.storage;

import android.annotation.NonNull;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Information about a physical disk which may contain one or more
 * {@link VolumeInfo}.
 *
 * @hide
 */
public class DiskInfo implements Parcelable {
    public static final String ACTION_DISK_SCANNED =
            "android.os.storage.action.DISK_SCANNED";
    public static final String EXTRA_DISK_ID =
            "android.os.storage.extra.DISK_ID";
    public static final String EXTRA_VOLUME_COUNT =
            "android.os.storage.extra.VOLUME_COUNT";

    public static final int FLAG_ADOPTABLE = 1 << 0;
    public static final int FLAG_DEFAULT_PRIMARY = 1 << 1;
    public static final int FLAG_SD = 1 << 2;
    public static final int FLAG_USB = 1 << 3;

    public final String id;
    public final int flags;
    public long size;
    public String label;
    /** Hacky; don't rely on this count */
    public int volumeCount;
    public String sysPath;

    public DiskInfo(String id, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public DiskInfo(Parcel parcel) {
        throw new UnsupportedOperationException("STUB");
    }

    public @NonNull String getId() {
        return id;
    }

    private boolean isInteresting(String label) {
        throw new UnsupportedOperationException("STUB");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean isAdoptable() {
        return (flags & FLAG_ADOPTABLE) != 0;
    }

    public boolean isDefaultPrimary() {
        return (flags & FLAG_DEFAULT_PRIMARY) != 0;
    }

    public boolean isSd() {
        return (flags & FLAG_SD) != 0;
    }

    public boolean isUsb() {
        return (flags & FLAG_USB) != 0;
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public DiskInfo clone() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static final Creator<DiskInfo> CREATOR = new Creator<DiskInfo>() {
        @Override
        public DiskInfo createFromParcel(Parcel in) {
            return new DiskInfo(in);
        }

        @Override
        public DiskInfo[] newArray(int size) {
            return new DiskInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        throw new UnsupportedOperationException("STUB");
    }
}
