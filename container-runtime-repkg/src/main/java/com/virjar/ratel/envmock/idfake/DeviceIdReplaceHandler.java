package com.virjar.ratel.envmock.idfake;

import android.text.TextUtils;

import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.envmock.PropertiesStoreHolder;
import com.virjar.ratel.runtime.RatelEnvironment;

public class DeviceIdReplaceHandler {
    private String key;
    private FingerPrintModel.ValueHolder<String> valueHolder;

    public DeviceIdReplaceHandler(String key, FingerPrintModel.ValueHolder<String> valueHolder) {
        this.key = key;
        this.valueHolder = valueHolder;
    }

    public String doReplace(String originDeviceInfo) {
        // String originDeviceInfo = (String) param.getResult();
        if (TextUtils.isEmpty(originDeviceInfo)) {
            return originDeviceInfo;
        }
        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.systemPropertiesFakeFile());
        String cache = propertiesHolder.getProperty(key);
        if (cache != null) {
            return cache;
        }
        String value = valueHolder.replace(key, originDeviceInfo);
        if (value == null) {
            return originDeviceInfo;
        }
        propertiesHolder.setProperty(key, value);
        return value;
    }
}
