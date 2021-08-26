package com.virjar.ratel.manager.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = {"packageName", "versionCode"}, callSuper = true)
@Table(database = DbConf.class)
@Data
public class FakeSignature extends BaseModel {
    @PrimaryKey
    private String packageName;

    @Column
    private String signature;


    @Column
    private Long versionCode;

}
