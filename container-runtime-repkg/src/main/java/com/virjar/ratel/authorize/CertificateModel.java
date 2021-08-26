package com.virjar.ratel.authorize;

import android.support.annotation.Keep;

@Keep
public class CertificateModel {
    public String licenceId;
    public int licenceVersion;
    public int licenceProtocolVersion;
    public long expire;
    public int licenceType;
    public String account;
    public String[] packageList;
    public String[] deviceList;
    public String extra;
    public boolean valid = false;
    public byte[] data = null;
}
