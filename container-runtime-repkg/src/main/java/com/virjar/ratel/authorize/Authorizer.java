package com.virjar.ratel.authorize;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.virjar.ratel.NativeBridge;
import com.virjar.ratel.buildsrc.Constants;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.XposedModuleLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.lang3.StringUtils;

public class Authorizer {
    public static CertificateModel nowCertificateModel = null;

    public native static int queryNowAuthorateStatusNative();

    public native static CertificateModel parseCertificate(byte[] certificateData);

    public static AuthorizeStatus queryNowAuthorateStatus() {
        int statusNative = queryNowAuthorateStatusNative();
        for (AuthorizeStatus authorizeStatus : AuthorizeStatus.values()) {
            if (authorizeStatus.getStatus() == statusNative) {
                return authorizeStatus;
            }
        }
        throw new IllegalStateException("unknown AuthorizeStatus with code: " + statusNative);
    }

    private static CertificateModel needReplace(CertificateModel originCertificateModel, CertificateModel newCertificateModel) {
        if (!newCertificateModel.valid) {
            return originCertificateModel;
        }
        if (originCertificateModel == null) {
            return newCertificateModel;

        }
        if (!originCertificateModel.licenceId.equals(newCertificateModel.licenceId)) {
            return originCertificateModel;

        }
        if (newCertificateModel.licenceVersion > originCertificateModel.licenceVersion) {
            return newCertificateModel;
        }
        return originCertificateModel;
    }

    private static CertificateModel loadCertificateFromSdcard(CertificateModel originCertificateModel) {
        if (!XposedModuleLoader.testHasSDCardReadPermission()) {
            return null;

        }
        //we can read sdcard ,so load certificate from external storage
        File externalRatelCertificateFile = new File(Environment.getExternalStorageDirectory(), Constants.ratelCertificate);
        if (externalRatelCertificateFile.exists() && externalRatelCertificateFile.canRead()) {
            try {
                CertificateModel tempCertificate = parseCertificate(FileUtils.readFileToByteArray(externalRatelCertificateFile));
                return needReplace(originCertificateModel, tempCertificate);
            } catch (IOException e) {
                Log.w(Constants.TAG, "failed to read ratel certificate file", e);
            }
        }
        return null;
    }

    private static CertificateModel updateCertificateFromRatelManager(CertificateModel originCertificateModel) {
        try {
            Cursor cursor = RatelRuntime.getOriginContext().getContentResolver().query(
                    Uri.parse("content://com.virjar.ratel.manager/certificate"),
                    null,
                    "licenceId=?",
                    new String[]{originCertificateModel.licenceId},
                    null
            );
            boolean needInsert = false;
            if (cursor == null) {
                // this licenceId not registered in the ratel manager,so insert
                needInsert = true;
            } else {
                if (!cursor.moveToNext()) {
                    needInsert = true;
                } else {
                    String certificateBinaryData = cursor.getString(cursor.getColumnIndex("payload"));
                    CertificateModel certificateModel = parseCertificate(certificateBinaryData.getBytes(StandardCharsets.UTF_8));
                    if (!certificateModel.licenceId.equals(originCertificateModel.licenceId)) {
                        Log.w(Constants.TAG, "get a error certificate from ratel manager,now licence Id:" + originCertificateModel.licenceId + " " +
                                "ratel manager licence Id: " + certificateModel.licenceId);
                        return null;
                    }
                    if (originCertificateModel.licenceVersion > certificateModel.licenceVersion) {
                        //本地的比管理器的新，所以需要将本地的插入到管理器
                        needInsert = true;
                    } else if (originCertificateModel.licenceVersion < certificateModel.licenceVersion) {
                        //本地的比管理器的旧，同步管理器的证书到本地
                        return certificateModel;
                    } else {
                        //版本相同，啥事儿都不干
                    }
                }

            }

            if (!needInsert) {
                //
                return null;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put("licenceId", originCertificateModel.licenceId);
            contentValues.put("licenceVersion", originCertificateModel.licenceVersion);
            contentValues.put("licenceProtocolVersion", originCertificateModel.licenceProtocolVersion);
            contentValues.put("expire", originCertificateModel.expire);
            contentValues.put("licenceType", originCertificateModel.licenceType);
            contentValues.put("account", originCertificateModel.account);
            contentValues.put("extra", originCertificateModel.extra);
            if (originCertificateModel.packageList != null) {
                contentValues.put("packageList", StringUtils.join(originCertificateModel.packageList, ","));
            }
            if (originCertificateModel.deviceList != null) {
                contentValues.put("deviceList", StringUtils.join(originCertificateModel.deviceList, ","));
            }
            contentValues.put("payload", new String(originCertificateModel.data, StandardCharsets.UTF_8));


            RatelRuntime.getOriginContext().getContentResolver().insert(
                    Uri.parse("content://com.virjar.ratel.manager/certificate"), contentValues
            );
        } catch (Exception e) {
            Log.w(Constants.TAG, "failed to sync certificate data from ratel manager!!", e);
            return null;
        }
        return null;
    }


    public static void loadCertificate() throws IOException {
        String s = NativeBridge.ratelResourceDir();
        File certificateFile = new File(s, Constants.ratelCertificate);

        CertificateModel originCertificateModel;
        try {
            originCertificateModel = parseCertificate(FileUtils.readFileToByteArray(certificateFile));
        } catch (IOException e) {
            throw new IllegalStateException("failed to parse now certificate");
        }


        //如果可以从sdcard load 新的证书，那么
        CertificateModel newCertificateModel = loadCertificateFromSdcard(originCertificateModel);
        if (newCertificateModel != null && newCertificateModel.licenceId.equals(originCertificateModel.licenceId) && newCertificateModel.licenceVersion > originCertificateModel.licenceVersion) {
            FileUtils.writeByteArrayToFile(certificateFile, newCertificateModel.data);
            originCertificateModel = newCertificateModel;
        }

        //no external storage permission sometimes,so we need import from IPC content provider
        //从ratelManager 1.0.2 (versionCode > 2)之后，支持证书导入机制
        if (XposedModuleLoader.isRatelManagerInstalled() && XposedModuleLoader.getRatelManagerVersionCode() > 2) {
            newCertificateModel = updateCertificateFromRatelManager(originCertificateModel);
            if (newCertificateModel != null && newCertificateModel.licenceId.equals(originCertificateModel.licenceId) && newCertificateModel.licenceVersion > originCertificateModel.licenceVersion) {
                FileUtils.writeByteArrayToFile(certificateFile, newCertificateModel.data);
                originCertificateModel = newCertificateModel;
            }
        }
        nowCertificateModel = originCertificateModel;
    }
}
