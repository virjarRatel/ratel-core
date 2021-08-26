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
import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.io.File;
import java.util.Comparator;

/**
 * Information about a storage volume that may be mounted. A volume may be a
 * partition on a physical {@link DiskInfo}, an emulated volume above some other
 * storage medium, or a standalone container like an ASEC or OBB.
 * <p>
 * Volumes may be mounted with various flags:
 * <ul>
 * <li>{@link #MOUNT_FLAG_PRIMARY} means the volume provides primary external
 * storage, historically found at {@code /sdcard}.
 * <li>{@link #MOUNT_FLAG_VISIBLE} means the volume is visible to third-party
 * apps for direct filesystem access. The system should send out relevant
 * storage broadcasts and index any media on visible volumes. Visible volumes
 * are considered a more stable part of the device, which is why we take the
 * time to index them. In particular, transient volumes like USB OTG devices
 * <em>should not</em> be marked as visible; their contents should be surfaced
 * to apps through the Storage Access Framework.
 * </ul>
 *
 * @hide
 */
public class VolumeInfo implements Parcelable {
    public static final String ACTION_VOLUME_STATE_CHANGED =
            "android.os.storage.action.VOLUME_STATE_CHANGED";
    public static final String EXTRA_VOLUME_ID =
            "android.os.storage.extra.VOLUME_ID";
    public static final String EXTRA_VOLUME_STATE =
            "android.os.storage.extra.VOLUME_STATE";

    /** Stub volume representing internal private storage */
    public static final String ID_PRIVATE_INTERNAL = "private";
    /** Real volume representing internal emulated storage */
    public static final String ID_EMULATED_INTERNAL = "emulated";

    public static final int TYPE_PUBLIC = 0;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_EMULATED = 2;
    public static final int TYPE_ASEC = 3;
    public static final int TYPE_OBB = 4;

    public static final int STATE_UNMOUNTED = 0;
    public static final int STATE_CHECKING = 1;
    public static final int STATE_MOUNTED = 2;
    public static final int STATE_MOUNTED_READ_ONLY = 3;
    public static final int STATE_FORMATTING = 4;
    public static final int STATE_EJECTING = 5;
    public static final int STATE_UNMOUNTABLE = 6;
    public static final int STATE_REMOVED = 7;
    public static final int STATE_BAD_REMOVAL = 8;

    public static final int MOUNT_FLAG_PRIMARY = 1 << 0;
    public static final int MOUNT_FLAG_VISIBLE = 1 << 1;

    private static SparseArray<String> sStateToEnvironment = new SparseArray<>();
    private static ArrayMap<String, String> sEnvironmentToBroadcast = new ArrayMap<>();
    private static SparseIntArray sStateToDescrip = new SparseIntArray();

    /** vold state */
    public final String id;
    public final int type;
    public final DiskInfo disk;
    public final String partGuid;
    public int mountFlags = 0;
    public int mountUserId = -1;
    public int state = STATE_UNMOUNTED;
    public String fsType;
    public String fsUuid;
    public String fsLabel;
    public String path;
    public String internalPath;

    public VolumeInfo(String id, int type, DiskInfo disk, String partGuid) {
        throw new UnsupportedOperationException("STUB");
    }

    public VolumeInfo(Parcel parcel) {
        throw new UnsupportedOperationException("STUB");
    }

    public static @NonNull String getEnvironmentForState(int state) {
        throw new UnsupportedOperationException("STUB");
    }

    public static @Nullable
    String getBroadcastForEnvironment(String envState) {
        return sEnvironmentToBroadcast.get(envState);
    }

    public static @Nullable String getBroadcastForState(int state) {
        return getBroadcastForEnvironment(getEnvironmentForState(state));
    }

    public static @NonNull Comparator<VolumeInfo> getDescriptionComparator() {
        throw new UnsupportedOperationException("STUB");
    }

    public @NonNull String getId() {
        return id;
    }

    public @Nullable DiskInfo getDisk() {
        return disk;
    }

    public @Nullable String getDiskId() {
        throw new UnsupportedOperationException("STUB");
    }

    public int getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    public int getStateDescription() {
        return sStateToDescrip.get(state, 0);
    }

    public @Nullable String getFsUuid() {
        return fsUuid;
    }

    public int getMountUserId() {
        return mountUserId;
    }

    public @Nullable String getDescription() {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean isMountedReadable() {
        return state == STATE_MOUNTED || state == STATE_MOUNTED_READ_ONLY;
    }

    public boolean isMountedWritable() {
        return state == STATE_MOUNTED;
    }

    public boolean isPrimary() {
        return (mountFlags & MOUNT_FLAG_PRIMARY) != 0;
    }

    public boolean isPrimaryPhysical() {
        return isPrimary() && (getType() == TYPE_PUBLIC);
    }

    public boolean isVisible() {
        return (mountFlags & MOUNT_FLAG_VISIBLE) != 0;
    }

    public boolean isVisibleForRead(int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public boolean isVisibleForWrite(int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public File getPath() {
        return (path != null) ? new File(path) : null;
    }

    public File getInternalPath() {
        return (internalPath != null) ? new File(internalPath) : null;
    }

    public File getPathForUser(int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    /**
     * Path which is accessible to apps holding
     * {@link android.Manifest.permission#WRITE_MEDIA_STORAGE}.
     */
    public File getInternalPathForUser(int userId) {
        throw new UnsupportedOperationException("STUB");
    }

    public Object buildStorageVolume(Context context, int userId, boolean reportUnmounted) {
        throw new UnsupportedOperationException("STUB");
    }

    public static int buildStableMtpStorageId(String fsUuid) {
        throw new UnsupportedOperationException("STUB");
    }

    // TODO: avoid this layering violation
    private static final String DOCUMENT_AUTHORITY = "com.android.externalstorage.documents";
    private static final String DOCUMENT_ROOT_PRIMARY_EMULATED = "primary";

    /**
     * Build an intent to browse the contents of this volume. Only valid for
     * {@link #TYPE_EMULATED} or {@link #TYPE_PUBLIC}.
     */
    public Intent buildBrowseIntent() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public VolumeInfo clone() {
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

    public static final Creator<VolumeInfo> CREATOR = new Creator<VolumeInfo>() {
        @Override
        public VolumeInfo createFromParcel(Parcel in) {
            return new VolumeInfo(in);
        }

        @Override
        public VolumeInfo[] newArray(int size) {
            return new VolumeInfo[size];
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
