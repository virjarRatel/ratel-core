package com.virjar.ratel.manager.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(database = DbConf.class)
@Data
@EqualsAndHashCode(of = {"appPackageName", "xposedModulePackageName"}, callSuper = false)
public class RatelAppModulesRelation extends BaseModel {

    @Column
    @PrimaryKey
    private String appPackageName;

    @Column
    @PrimaryKey
    private String xposedModulePackageName;

    @Column
    private boolean enable;

}
