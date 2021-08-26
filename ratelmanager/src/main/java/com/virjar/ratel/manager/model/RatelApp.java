package com.virjar.ratel.manager.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(database = DbConf.class)
@EqualsAndHashCode(of = "packageName", callSuper = false)
@Data
public class RatelApp extends BaseModel {
    @PrimaryKey
    private String packageName;
    @Column
    private String versionName;
    @Column
    private int versionCode;

    @Column
    private String engineVersionName;

    @Column
    private int engineVersionCode;

    @Column
    private Long buildTimeStamp;

    @Column
    private String appName;

    @Column
    private boolean enabled = true;

    private Drawable icon;

    private PackageInfo packageInfo;

    @Column
    private String engineName;

    @Column
    private String license;

    @Column
    private boolean daemon = false;

    public Drawable getOrLoadIcon(Context context) {
        if (icon != null) {
            return icon;
        }

        try {
            if (packageInfo == null) {
                packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            }
            icon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return icon;
    }

//    public String getOrLoadAppName(Context context) {
//        try {
//            if (packageInfo == null) {
//                packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
//            }
//            appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new IllegalStateException(e);
//        }
//        return appName;
//    }

    @Override
    public String toString() {
        return appName == null ? "" : appName;
    }

}
