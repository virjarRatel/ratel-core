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

package android.app;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

/**
 * System private API for passing profiler settings.
 *
 * {@hide}
 */
public class ProfilerInfo implements Parcelable {

    /* Name of profile output file. */
    public final String profileFile;

    /* File descriptor for profile output file, can be null. */
    public ParcelFileDescriptor profileFd;

    /* Indicates sample profiling when nonzero, interval in microseconds. */
    public final int samplingInterval;

    /* Automatically stop the profiler when the app goes idle. */
    public final boolean autoStopProfiler;

    /* Indicates whether to stream the profiling info to the out file continuously. */
    public final boolean streamingOutput;

    public ProfilerInfo(String filename, ParcelFileDescriptor fd, int interval, boolean autoStop,
                        boolean streaming) {
        profileFile = filename;
        profileFd = fd;
        samplingInterval = interval;
        autoStopProfiler = autoStop;
        streamingOutput = streaming;
    }

    public int describeContents() {
        throw new UnsupportedOperationException("STUB");
    }

    public void writeToParcel(Parcel out, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final Parcelable.Creator<ProfilerInfo> CREATOR =
            new Parcelable.Creator<ProfilerInfo>() {
        public ProfilerInfo createFromParcel(Parcel in) {
            throw new UnsupportedOperationException("STUB");
        }

        public ProfilerInfo[] newArray(int size) {
            return new ProfilerInfo[size];
        }
    };
}
