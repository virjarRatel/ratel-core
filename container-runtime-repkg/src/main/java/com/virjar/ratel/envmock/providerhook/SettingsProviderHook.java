package com.virjar.ratel.envmock.providerhook;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.virjar.ratel.envmock.MSAFake;
import com.virjar.ratel.runtime.RatelEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

public class SettingsProviderHook extends ProviderHook {
    private static final String ANDROID_ID = "android_id";
    private static final String PPS_OAID = "pps_oaid";
    public static String fakeAndroidId =
            Long.toHexString(new Random((RatelEnvironment.userIdentifier() + "ANDROID_ID").hashCode()).nextLong());

    private static final String VALUE = "value";

    public SettingsProviderHook(Object base) {
        super(base);
    }

    @Override
    public Bundle call(MethodBox methodBox, String method, String arg, Bundle extras) throws InvocationTargetException {
        Bundle ret = super.call(methodBox, method, arg, extras);
        switch (method) {
            case "GET_secure":
                switch (arg) {
                    case ANDROID_ID:
                        ret.putString(VALUE, fakeAndroidId);
                }
                break;
            case "GET_global":
                switch (arg) {
                    case "adb_enabled":
                        ret.putInt(VALUE, 0);
                        break;
                    case PPS_OAID:
                        // 这是华为oaid的另一个后门
                        // 04-28 20:42:59.264 18195 18246 I RATEL   : get settings value for key: pps_oaid value: ee6fbef7-5dfb-1aca-d6e6-fffefeff884b  new value: ee6fbef7-5dfb-1aca-d6e6-fffefeff884b
                        //04-28 20:42:59.265 18195 18246 I RATEL   : content provider call: GET_global arg:pps_track_limit  extras:null
                        //04-28 20:42:59.268 18195 18246 I RATEL   : get settings value for key: pps_track_limit value: false  new value: false
                        //04-28 20:42:59.268 18195 18246 I KS9_HOOK: hint value: ee6fbef7-5dfb-1aca-d6e6-fffefeff884b key:oai_ll_sn_d

                        String ppsOaid = ret.getString(VALUE);
                        if (!TextUtils.isEmpty(ppsOaid)) {
                            ret.putString(VALUE, MSAFake.mockMSA(ppsOaid));
                        }
                }
                break;
            case "GET_system":
                switch (arg) {
                    case Settings.System.SCREEN_BRIGHTNESS:
                        int value = ret.getInt(VALUE);
                        while (value < 500) {
                            value += 100;
                        }
                        ret.putInt(VALUE, value);
                        break;
                }
        }
        return ret;
    }

    @Override
    public Cursor query(MethodBox methodBox, Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder, Bundle originQueryArgs) throws InvocationTargetException {
        Cursor cursor = super.query(methodBox, url, projection, selection, selectionArgs, sortOrder, originQueryArgs);
        return new CursorWrapper(cursor) {
            @Override
            public String getString(int columnIndex) {
                String columnName = getColumnName(columnIndex);
                String originValue = super.getString(columnIndex);
                if (ANDROID_ID.equals(columnName)) {
                    originValue = fakeAndroidId;
                }
                return originValue;
            }
        };
    }
}
