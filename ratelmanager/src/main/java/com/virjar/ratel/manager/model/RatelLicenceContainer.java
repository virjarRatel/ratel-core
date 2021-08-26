package com.virjar.ratel.manager.model;

import lombok.Data;

@Data
public class RatelLicenceContainer {
    private String licenceId;
    private int licenceVersion;
    private int licenceProtocolVersion;
    private long expire;
    private int licenceType;
    private String account;
    private String[] packageList;
    private String[] deviceList;
    private String extra;
    private String payload;
}
