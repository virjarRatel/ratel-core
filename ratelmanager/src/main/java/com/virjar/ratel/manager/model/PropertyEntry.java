package com.virjar.ratel.manager.model;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Table(database = DbConf.class)
@Data
public class PropertyEntry extends BaseModel {
    @PrimaryKey(autoincrement = true)
    private Integer id;
    private String fromPackage;
    private String key;
    private String value;
}
