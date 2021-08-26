package com.virjar.ratel.envmock.providerhook;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;

import com.virjar.ratel.api.SuffixTrimUtils;
import com.virjar.ratel.envmock.PropertiesStoreHolder;
import com.virjar.ratel.runtime.RatelEnvironment;

import java.lang.reflect.InvocationTargetException;

public class IDFake_MUI extends ProviderHook {
    private static final String COLUMN_NAME = "uniform_id";

    IDFake_MUI(Object base) {
        super(base);
    }

    @Override
    public Cursor query(MethodBox methodBox, Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder, Bundle originQueryArgs) throws InvocationTargetException {
        Cursor cursor = super.query(methodBox, url, projection, selection, selectionArgs, sortOrder, originQueryArgs);
        return new CursorWrapper(cursor) {
            @Override
            public String getString(int columnIndex) {
                String columnName = getColumnName(columnIndex);
                String originValue = super.getString(columnIndex);
                if (COLUMN_NAME.equals(columnName)) {
                    PropertiesStoreHolder propertiesHolder = PropertiesStoreHolder.getPropertiesHolder(RatelEnvironment.MSAMockFile());

                    String key = "fake_" + originValue;
                    if (propertiesHolder.hasProperty(key)) {
                        originValue = propertiesHolder.getProperty(key);
                    } else {
                        originValue = SuffixTrimUtils.mockSuffix(originValue, 7);
                        propertiesHolder.setProperty(key, originValue);
                    }
                }
                return originValue;
            }
        };
    }
}
