package com.virjar.ratel.envmock.binder;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public class ParcelUtils {

    public static <T extends Parcelable> List<T> copyListParcel(List<T> input) {
        Parcel parcel = Parcel.obtain();
        List<T> copyList = new ArrayList<>();
        try {
            parcel.writeTypedList(input);
            parcel.setDataPosition(0);
            parcel.readTypedList(copyList, BinderHookManager.getCreator(ScanResult.class.getName()));
        } finally {
            parcel.recycle();
        }
        return copyList;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Parcelable> T copyParcel(T input) {
        Parcel parcel = Parcel.obtain();
        try {
            input.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            return (T) BinderHookManager.getCreator(input.getClass().getName())
                    .createFromParcel(parcel);
        } finally {
            parcel.recycle();
        }
    }
}
