package com.virjar.ratel.manager.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(database = DbConf.class)
@Data
@EqualsAndHashCode(of = "packageName", callSuper = false)
public class RatelModule extends BaseModel {
    @PrimaryKey
    private String packageName;
    @Column
    private String versionName;
    @Column
    private int versionCode;
    @Column
    private int minVersion;
    @Column
    private String appName;
    @Column
    private String description;

    @Column
    private boolean enable;

    private Drawable icon;

    public Drawable getOrLoadIcon(Context context) {
        if (icon != null) {
            return icon;
        }
        try {
            icon = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA).applicationInfo.loadIcon(context.getPackageManager());
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return icon;
    }

    private Set<String> forAppPackage;

    @Override
    public String toString() {
        return appName == null ? "" : appName;
    }
}
