package com.virjar.ratel.manager.component;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.manager.ManagerInitiazer;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.model.FakeSignature;
import com.virjar.ratel.manager.model.FakeSignature_Table;
import com.virjar.ratel.manager.model.RatelApp;
import com.virjar.ratel.manager.model.RatelAppModulesRelation;
import com.virjar.ratel.manager.model.RatelAppModulesRelation_Table;
import com.virjar.ratel.manager.model.RatelCertificate;
import com.virjar.ratel.manager.model.RatelCertificate_Table;
import com.virjar.ratel.manager.model.RatelModule;
import com.virjar.ratel.manager.model.RatelModule_Table;
import com.virjar.ratel.manager.model.SchedulerTask;
import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.repo.RatelCertificateRepo;
import com.virjar.ratel.manager.repo.SchedulerTaskRepo;
import com.virjar.ratel.manager.scheduler.TaskSchedulerService;
import com.virjar.ratel.manager.ui.DefaultSharedPreferenceHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleListProvider extends ContentProvider {
    public static final String AUTOHORITY = "com.virjar.ratel.manager";
    public static final String PUBLICDATA_TABLE_NAME = "module_list";
    public static final String PUBLICDATA_CERTIFICATE = "certificate";
    public static final String PUBLICDATA_FAKE_SIGNATURE = "fake_signature";

    private static final UriMatcher mMatcher;

    private static final int dataCodeModuleList = 1;
    private static final int dataCodeCertificate = 2;
    private static final int dataCodeSignature = 3;

    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 初始化
        mMatcher.addURI(AUTOHORITY, PUBLICDATA_TABLE_NAME, dataCodeModuleList);
        mMatcher.addURI(AUTOHORITY, PUBLICDATA_CERTIFICATE, dataCodeCertificate);
        mMatcher.addURI(AUTOHORITY, PUBLICDATA_FAKE_SIGNATURE, dataCodeSignature);
    }

    @Override
    public boolean onCreate() {
        ManagerInitiazer.init(getContext());
        return true;
    }


    private Cursor handleModuleQuery(String[] selectionArgs) {
        if (selectionArgs.length != 1) {
            throw new IllegalArgumentException("unknown param" + Arrays.toString(selectionArgs));
        }

        if (!DefaultSharedPreferenceHolder.getInstance(getContext()).isRatelSwitchOn()) {
            Log.i(RatelManagerApp.TAG, "ratel switch down");
            return null;
        }

        String fromPackageName = selectionArgs[0];
        RatelApp ratelApp = RatelAppRepo.findByPackage(fromPackageName);
        if (ratelApp != null && !ratelApp.isEnabled()) {
            //ratelApp 可能为null么？？？
            Log.i(RatelManagerApp.TAG, "ratelApp not enabled: " + ratelApp);
            return null;
        }

        ArrayList<Cursor> mergedCursor = new ArrayList<>();

        FlowCursor query = SQLite.select(
                RatelAppModulesRelation_Table.xposedModulePackageName.as("modulePackage")
        ).from(RatelAppModulesRelation.class)
                .where(RatelAppModulesRelation_Table.appPackageName.eq(fromPackageName))
                .and(RatelAppModulesRelation_Table.enable.eq(true))
                .query();
        if (query != null) {
            mergedCursor.add(query);
        }

        List<RatelAppModulesRelation> ratelAppModulesRelations = SQLite.select(RatelAppModulesRelation_Table.xposedModulePackageName)
                .distinct().from(
                        RatelAppModulesRelation.class
                ).queryList();
        Set<String> existConfigModules = new HashSet<>();
        for (RatelAppModulesRelation ratelAppModulesRelation : ratelAppModulesRelations) {
            existConfigModules.add(ratelAppModulesRelation.getXposedModulePackageName());
        }


        query = SQLite.select(RatelModule_Table.packageName.as("modulePackage"))
                .from(RatelModule.class)
                .where(RatelModule_Table.packageName.notIn(existConfigModules))
                .and(RatelModule_Table.enable.eq(true))
                .query();
        if (query != null) {
            mergedCursor.add(query);
        }
        return new MergeCursor(mergedCursor.toArray(new Cursor[]{}));
    }


    private Cursor handleCertificateQuery(String[] selectionArgs) {
        if (selectionArgs.length < 1) {
            return null;
        }
        String certificateId = selectionArgs[0];
        RatelCertificate ratelCertificate = RatelCertificateRepo.queryById(certificateId);
        if (ratelCertificate == null) {
            return null;
        }
        return SQLite.select()
                .from(RatelCertificate.class)
                .where(RatelCertificate_Table.licenceId.eq(certificateId))
                .query();
    }

    private Cursor handleSignatureQuery(String[] selectionArgs) {
        if (selectionArgs.length < 1) {
            return null;
        }
        String packageName = selectionArgs[0];
        return SQLite.select()
                .from(FakeSignature.class)
                .where(FakeSignature_Table.packageName.eq(packageName))
                .query();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(RatelManagerApp.TAG, "receive a contentProvider query: " + uri);
        int match = mMatcher.match(uri);
        switch (match) {
            case dataCodeModuleList:
                return handleModuleQuery(selectionArgs);
            case dataCodeCertificate:
                return handleCertificateQuery(selectionArgs);
            case dataCodeSignature:
                return handleSignatureQuery(selectionArgs);
            default:
                throw new IllegalStateException("unknown operator uri: " + uri);
        }
    }


    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = mMatcher.match(uri);
        if (match != dataCodeCertificate) {
            throw new UnsupportedOperationException();
        }

        String licenceId = values.getAsString("licenceId");
        if (TextUtils.isEmpty(licenceId)) {
            throw new IllegalArgumentException("need licenceId");
        }
        Integer licenceVersion = values.getAsInteger("licenceVersion");
        if (licenceVersion == null) {
            throw new IllegalArgumentException("need licenceVersion");
        }

        Integer licenceProtocolVersion = values.getAsInteger("licenceProtocolVersion");

        if (licenceProtocolVersion == null) {
            throw new IllegalArgumentException("need licenceProtocolVersion");
        }
        Long expire = values.getAsLong("expire");

        if (expire == null) {
            throw new IllegalArgumentException("need expire");
        }
        Integer licenceType = values.getAsInteger("licenceType");


        if (licenceType == null) {
            throw new IllegalArgumentException("need licenceType");
        }

        String account = values.getAsString("account");
        if (TextUtils.isEmpty(account)) {
            throw new IllegalArgumentException("need account");
        }

        String packageList = values.getAsString("packageList");
        String deviceList = values.getAsString("deviceList");
        String extra = values.getAsString("extra");
        String payload = values.getAsString("payload");

        if (TextUtils.isEmpty(payload)) {
            throw new IllegalArgumentException("need payload");
        }

        RatelCertificate certificate = RatelCertificateRepo.queryById(licenceId);
        if (certificate != null && certificate.getLicenceVersion() >= licenceVersion) {
            Log.w(Constants.TAG, "insert a lowdown certificate from: " + Binder.getCallingPid());
            return null;
        }

        if (certificate == null) {
            certificate = new RatelCertificate();
        }

        certificate.setPayload(payload);
        certificate.setLicenceType(licenceType);
        certificate.setLicenceVersion(licenceVersion);
        certificate.setLicenceProtocolVersion(licenceProtocolVersion);
        certificate.setLicenceId(licenceId);
        certificate.setPackageList(packageList);
        certificate.setDeviceList(deviceList);
        certificate.setExtra(extra);
        certificate.setExpire(expire);
        certificate.setAccount(account);

        RatelCertificateRepo.addCertificate(certificate);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (Constants.METHOD_QUERY_SCHEDULER_TASK.equals(method)) {
            return querySchedulerTask(arg);
        } else if (Constants.METHOD_FINISH_SCHEDULER_TASK.equals(method)) {
            finishSchedulerTask(arg);
        }
        return super.call(method, arg, extras);
    }

    private void finishSchedulerTask(String taskId) {
        Log.i(Constants.SCHEDULER_TAG, "receive a task finish call:" + taskId);
        SchedulerTask schedulerTask = SchedulerTaskRepo.queryById(taskId);
        if (schedulerTask == null) {
            Log.w(Constants.SCHEDULER_TAG, "can not find this task");
            return;
        }
        schedulerTask.setTaskStatus(SchedulerTask.TASK_STATUS_STOP);
        schedulerTask.update();
        Log.i(Constants.SCHEDULER_TAG, "receive a task reset task status success for task:" + taskId);
    }

    private Bundle querySchedulerTask(String mPackage) {
        Map<String, String> map = TaskSchedulerService.consumeSchedulerTask(mPackage);

        if (map == null) {
            return null;
        }
        Bundle bundle = new Bundle();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        return bundle;
    }
}
