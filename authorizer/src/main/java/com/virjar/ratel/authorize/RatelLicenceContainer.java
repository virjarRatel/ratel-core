package com.virjar.ratel.authorize;

import com.virjar.ratel.authorize.encrypt.RatelLicenceEncryptor;

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

    RatelLicenceContainer(RatelLicence ratelLicence) {
        this.licenceId = ratelLicence.getLicenceId();
        this.licenceVersion = ratelLicence.getLicenceVersion();
        this.licenceProtocolVersion = ratelLicence.getLicenceProtocolVersion();
        this.expire = ratelLicence.getExpire();
        this.licenceType = ratelLicence.getLicenceType();
        this.account = ratelLicence.getAccount();
        this.packageList = ratelLicence.getPackageList();
        this.deviceList = ratelLicence.getDeviceList();
        this.extra = ratelLicence.getExtra();
        this.payload = RatelLicenceEncryptor.encrypt(ratelLicence.encode());
    }

    //反序列化
    public RatelLicenceContainer() {
    }

    public String getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(String licenceId) {
        this.licenceId = licenceId;
    }

    public int getLicenceVersion() {
        return licenceVersion;
    }

    public void setLicenceVersion(int licenceVersion) {
        this.licenceVersion = licenceVersion;
    }

    public int getLicenceProtocolVersion() {
        return licenceProtocolVersion;
    }

    public void setLicenceProtocolVersion(int licenceProtocolVersion) {
        this.licenceProtocolVersion = licenceProtocolVersion;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public int getLicenceType() {
        return licenceType;
    }

    public void setLicenceType(int licenceType) {
        this.licenceType = licenceType;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String[] getPackageList() {
        return packageList;
    }

    public void setPackageList(String[] packageList) {
        this.packageList = packageList;
    }

    public String[] getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(String[] deviceList) {
        this.deviceList = deviceList;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
