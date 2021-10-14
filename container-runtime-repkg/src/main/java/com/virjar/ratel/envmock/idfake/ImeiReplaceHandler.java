package com.virjar.ratel.envmock.idfake;

import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.envmock.PropertiesStoreHolder;
import com.virjar.ratel.runtime.RatelEnvironment;

public class ImeiReplaceHandler {

    public String doReplace(String originDeviceInfo, Object[] args) {
        if (TextUtils.isEmpty(originDeviceInfo)) {
            return originDeviceInfo;
        }
        PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.systemPropertiesFakeFile());

        int index = 0;
        for (Object obj : args) {
            if (obj instanceof Integer) {
                index = (int) obj;
                break;
            }
        }
        String key;
        FingerPrintModel.ValueHolder<String> valueHolder;

        if (index == 0) {
            key = "imei";
            valueHolder = RatelToolKit.fingerPrintModel.imei;
        } else {
            if (index > 1) {
                Log.w(Constants.TAG, "get imei from slot,with index: " + index);
            }
            key = "imei2";
            valueHolder = RatelToolKit.fingerPrintModel.imei2;
        }

        String cache = propertiesHolder.getProperty(key);
        if (cache != null) {
            return cache;
        }
        String value = valueHolder.replace(key, originDeviceInfo);
        if (value == null) {
            return originDeviceInfo;
        }
        //这里需要对imei修复校验码
        value = ImeiSignUpdater.imeiSignUpdaterInstance.updateSign(value);
        valueHolder.set(value);

        propertiesHolder.setProperty(key, value);
        return value;
    }
}
