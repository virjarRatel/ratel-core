package com.virjar.ratel.api.providers;

import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ContentProviderFake {
    public Bundle call(Object thisObject, Method method, String arg, Bundle extras) throws InvocationTargetException {
        return call(thisObject, method, new Object[]{arg, extras});
    }

    public Uri insert(Object thisObject, Method method, Uri url, ContentValues initialValues) throws InvocationTargetException {
        return (Uri) call(thisObject, method, new Object[]{url, initialValues});
    }

    public Cursor query(Object thisObject, Method method, Uri url, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder, Bundle originQueryArgs) throws InvocationTargetException {
        return (Cursor) call(thisObject, method, new Object[]{url, projection, selection, selectionArgs, sortOrder, originQueryArgs});
    }

    public String getType(Object thisObject, Method method, Uri url) throws InvocationTargetException {
        return (String) call(thisObject, method, new Object[]{url});
    }

    public int bulkInsert(Object thisObject, Method method, Uri url, ContentValues[] initialValues) throws InvocationTargetException {
        return (int) call(thisObject, method, new Object[]{url, initialValues});
    }

    public int delete(Object thisObject, Method method, Uri url, String selection, String[] selectionArgs) throws InvocationTargetException {
        return (int) call(thisObject, method, new Object[]{url, selection, selectionArgs});
    }

    public int update(Object thisObject, Method method, Uri url, ContentValues values, String selection,
                      String[] selectionArgs) throws InvocationTargetException {
        return (int) call(thisObject, method, new Object[]{url, values, selection, selectionArgs});
    }

    public ParcelFileDescriptor openFile(Object thisObject, Method method, Uri url, String mode) throws InvocationTargetException {
        return (ParcelFileDescriptor) call(thisObject, method, new Object[]{url, mode});
    }

    public AssetFileDescriptor openAssetFile(Object thisObject, Method method, Uri url, String mode) throws InvocationTargetException {
        return (AssetFileDescriptor) call(thisObject, method, new Object[]{url, mode});
    }

    @SuppressWarnings("unchecked")
    public <T> T call(Object who, Method method, Object[] args) throws InvocationTargetException {
        try {
            return (T) method.invoke(who, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
