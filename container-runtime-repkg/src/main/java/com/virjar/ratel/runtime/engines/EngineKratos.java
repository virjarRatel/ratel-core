package com.virjar.ratel.runtime.engines;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelEngine;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.sandhook.SandHookConfig;
import com.virjar.ratel.utils.HiddenAPIEnforcementPolicyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import external.org.apache.commons.io.IOUtils;
import external.org.apache.commons.lang3.StringUtils;

public class EngineKratos {
    public static void applicationAttachWithKratosEngine(Context context, Context ratelEnvContext) throws Throwable {

        //如果apk本身存在改包，那么不允许通过kratos引擎执行
        try {
            InputStream stream = context.getAssets().open(Constants.serialNoFile);
            String serialNo = IOUtils.toString(stream, StandardCharsets.UTF_8);
            IOUtils.closeQuietly(stream);
            if (StringUtils.isBlank(serialNo)) {
                return;
            }
        } catch (IOException e) {
            //ignore
            return;
        }

        RatelRuntime.ratelEngine = RatelEngine.KEATOS;
        if (BuildConfig.DEBUG) {
            Log.i(Constants.TAG, "KEATOS entry");
        }
        if (SandHookConfig.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenAPIEnforcementPolicyUtils.passApiCheck();
        }

        RatelRuntime.init(null, context, ratelEnvContext);
        RatelRuntime.ratelStartUp(context);
    }
}
