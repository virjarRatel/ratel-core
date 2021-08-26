package com.virjar.ratel.runtime.apibridge;

import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.virjar.ratel.api.providers.ContentProviderFake;
import com.virjar.ratel.api.providers.ContentProviderFakeRegister;
import com.virjar.ratel.envmock.providerhook.MethodBox;
import com.virjar.ratel.envmock.providerhook.ProviderHook;

import java.lang.reflect.InvocationTargetException;

public class ContentProviderFakeRegisterImpl implements ContentProviderFakeRegister {
    @Override
    public void register(String authority, ContentProviderFake contentProviderFake) {
        ProviderHook.registerFetcher(authority, (external, provider) -> new ProviderHook(provider) {
            @Override
            public Bundle call(MethodBox methodBox, String method, String arg, Bundle extras) throws InvocationTargetException {
                return contentProviderFake.call(methodBox.who, methodBox.method, arg, extras);
            }

            @Override
            public Uri insert(MethodBox methodBox, Uri url, ContentValues initialValues) throws InvocationTargetException {
                return contentProviderFake.insert(methodBox.who, methodBox.method, url, initialValues);
            }

            @Override
            public Cursor query(MethodBox methodBox, Uri url, String[] projection, String selection, String[] selectionArgs, String sortOrder, Bundle originQueryArgs) throws InvocationTargetException {
                return contentProviderFake.query(methodBox.who, methodBox.method, url, projection, selection, selectionArgs, sortOrder, originQueryArgs);
            }

            @Override
            public String getType(MethodBox methodBox, Uri url) throws InvocationTargetException {
                return contentProviderFake.getType(methodBox.who, methodBox.method, url);
            }

            @Override
            public int bulkInsert(MethodBox methodBox, Uri url, ContentValues[] initialValues) throws InvocationTargetException {
                return contentProviderFake.bulkInsert(methodBox.who, methodBox.method, url, initialValues);
            }

            @Override
            public int delete(MethodBox methodBox, Uri url, String selection, String[] selectionArgs) throws InvocationTargetException {
                return contentProviderFake.delete(methodBox.who, methodBox.method, url, selection, selectionArgs);
            }

            @Override
            public int update(MethodBox methodBox, Uri url, ContentValues values, String selection, String[] selectionArgs) throws InvocationTargetException {
                return contentProviderFake.update(methodBox.who, methodBox.method, url, values, selection, selectionArgs);
            }

            @Override
            public ParcelFileDescriptor openFile(MethodBox methodBox, Uri url, String mode) throws InvocationTargetException {
                return contentProviderFake.openFile(methodBox.who, methodBox.method, url, mode);
            }

            @Override
            public AssetFileDescriptor openAssetFile(MethodBox methodBox, Uri url, String mode) throws InvocationTargetException {
                return contentProviderFake.openAssetFile(methodBox.who, methodBox.method, url, mode);
            }

        });
    }
}
