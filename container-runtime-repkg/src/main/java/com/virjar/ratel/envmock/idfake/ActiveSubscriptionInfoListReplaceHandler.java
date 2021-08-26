package com.virjar.ratel.envmock.idfake;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.envmock.PropertiesStoreHolder;
import com.virjar.ratel.runtime.RatelEnvironment;

import java.util.List;

public class ActiveSubscriptionInfoListReplaceHandler {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static void doReplace(List<SubscriptionInfo> result) {
        if (result == null) {
            return;
        }
        //fake iccid

        for (int i = 0; i < result.size(); i++) {
            SubscriptionInfo subscriptionInfo = result.get(i);

            PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.systemPropertiesFakeFile());
            String key = "iccSerialNumber" + i;
            if (i == 0) {
                //第一个需要保持和其他模块同步
                key = "iccSerialNumber";
            }
            String cache = propertiesHolder.getProperty(key);
            if (!TextUtils.isEmpty(cache)) {
                RposedHelpers.setObjectField(subscriptionInfo, "mIccId", cache);
                return;
            }
            String value = RatelToolKit.fingerPrintModel.iccSerialNumber.replace(key, subscriptionInfo.getIccId());
            if (value == null) {
                return;
            }
            propertiesHolder.setProperty(key, value);
            RposedHelpers.setObjectField(subscriptionInfo, "mIccId", value);
        }
    }
}