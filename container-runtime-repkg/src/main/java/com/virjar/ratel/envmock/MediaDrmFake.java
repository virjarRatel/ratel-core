package com.virjar.ratel.envmock;

import android.media.MediaDrm;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.runtime.RatelEnvironment;

import java.lang.reflect.Method;
import java.util.Random;

public class MediaDrmFake {

    private static final String KEY_DEVICE_UNIQUE_ID = "mediaDrmDeviceUniqueId";
    private static final Random RANDOM = new Random();

    public static void fakeDeviceId() {
        try {
            Method getPropertyByteArray = MediaDrm.class.getMethod("getPropertyByteArray", String.class);
            RatelNative.fakeMediaDrmId(getPropertyByteArray, getFakeDeviceId());
        } catch (NoSuchMethodException e) {
            Log.e(RatelToolKit.TAG, "hook android.media.MediaDrm#getPropertyByteArray method error:", e);
        }
    }

    private static byte[] getFakeDeviceId() {
        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.systemPropertiesFakeFile());
        String fakeValue = propertiesHolder.getProperty(KEY_DEVICE_UNIQUE_ID);
        if (!TextUtils.isEmpty(fakeValue)) {
            return Base64.decode(fakeValue, Base64.NO_WRAP);
        }
        FingerPrintModel.ValueHolder<String> holder = RatelToolKit.fingerPrintModel.mediaDrmDeviceId;
        fakeValue = holder.get();
        if (!TextUtils.isEmpty(fakeValue)) {
            return Base64.decode(fakeValue, Base64.NO_WRAP);
        } else {
            byte[] fakeBytes = new byte[32];
            RANDOM.nextBytes(fakeBytes);
            fakeValue = Base64.encodeToString(fakeBytes, Base64.NO_WRAP);
            propertiesHolder.setProperty(KEY_DEVICE_UNIQUE_ID, fakeValue);
            return fakeBytes;
        }
    }

}
