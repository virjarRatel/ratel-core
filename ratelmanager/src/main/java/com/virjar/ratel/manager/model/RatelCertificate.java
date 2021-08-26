package com.virjar.ratel.manager.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(database = DbConf.class)
@Data
@EqualsAndHashCode(of = "licenceId", callSuper = false)
public class RatelCertificate extends BaseModel {

    @PrimaryKey
    private String licenceId;
    @Column
    private int licenceVersion;
    @Column
    private int licenceProtocolVersion;
    @Column
    private long expire;
    @Column
    private int licenceType;
    @Column
    private String account;
    @Column
    private String packageList;
    @Column
    private String deviceList;
    @Column
    private String extra;
    @Column
    private String payload;

    @Override
    public String toString() {
        return licenceId == null ? "" : licenceId;
    }

}
