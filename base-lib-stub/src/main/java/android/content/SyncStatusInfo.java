/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.content;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @hide
 */
public class SyncStatusInfo implements Parcelable {
    public final int authorityId;
    public long totalElapsedTime;
    public int numSyncs;
    public int numSourcePoll;
    public int numSourceServer;
    public int numSourceLocal;
    public int numSourceUser;
    public int numSourcePeriodic;
    public long lastSuccessTime;
    public int lastSuccessSource;
    public long lastFailureTime;
    public int lastFailureSource;
    public String lastFailureMesg;
    public long initialFailureTime;
    public boolean pending;
    public boolean initialize;

    public SyncStatusInfo(int authorityId) {
        this.authorityId = authorityId;
    }

    public int getLastFailureMesgAsInt(int def) {
        throw new UnsupportedOperationException("STUB");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public SyncStatusInfo(Parcel parcel) {
        throw new UnsupportedOperationException("STUB");
    }

    public SyncStatusInfo(SyncStatusInfo other) {
        throw new UnsupportedOperationException("STUB");
    }

    public void setPeriodicSyncTime(int index, long when) {
        throw new UnsupportedOperationException("STUB");
    }

    public long getPeriodicSyncTime(int index) {
        throw new UnsupportedOperationException("STUB");
    }

    public void removePeriodicSyncTime(int index) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final Creator<SyncStatusInfo> CREATOR = new Creator<SyncStatusInfo>() {
        public SyncStatusInfo createFromParcel(Parcel in) {
            return new SyncStatusInfo(in);
        }

        public SyncStatusInfo[] newArray(int size) {
            return new SyncStatusInfo[size];
        }
    };
}