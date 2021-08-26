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

package android.content.pm;

import android.os.*;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transfer a large list of Parcelable objects across an IPC.  Splits into
 * multiple transactions if needed.
 * <p>
 * Caveat: for efficiency and security, all elements must be the same concrete type.
 * In order to avoid writing the class name of each object, we must ensure that
 * each object is the same type, or else unparceling then reparceling the data may yield
 * a different result if the class name encoded in the Parcelable is a Base type.
 * See b/17671747.
 *
 * @hide
 */
public class ParceledListSlice<T extends Parcelable> implements Parcelable {
    private static String TAG = "ParceledListSlice";
    private static boolean DEBUG = false;

    /*
     * TODO get this number from somewhere else. For now set it to a quarter of
     * the 1MB limit.
     */
//    private static final int MAX_IPC_SIZE = IBinder.MAX_IPC_SIZE;

    private final List<T> mList;

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        return new ParceledListSlice<T>(Collections.<T>emptyList());
    }

    public ParceledListSlice(List<T> list) {
        mList = list;
    }

    public List<T> getList() {
        return mList;
    }

    @Override
    public int describeContents() {
        int contents = 0;
        for (int i = 0; i < mList.size(); i++) {
            contents |= mList.get(i).describeContents();
        }
        return contents;
    }

    /**
     * Write this to another Parcel. Note that this discards the internal Parcel
     * and should not be used anymore. This is so we can pass this to a Binder
     * where we won't have a chance to call recycle on this.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException("STUB");
    }

    @SuppressWarnings("unchecked")
    public static final Parcelable.ClassLoaderCreator<ParceledListSlice> CREATOR =
            new Parcelable.ClassLoaderCreator<ParceledListSlice>() {
                public ParceledListSlice createFromParcel(Parcel in) {
                    throw new UnsupportedOperationException("STUB");
                }

                @Override
                public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
                    throw new UnsupportedOperationException("STUB");
                }

                public ParceledListSlice[] newArray(int size) {
                    return new ParceledListSlice[size];
                }
            };
}
