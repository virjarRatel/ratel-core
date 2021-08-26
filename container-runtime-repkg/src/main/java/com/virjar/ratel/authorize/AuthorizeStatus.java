package com.virjar.ratel.authorize;

/**
 * #define LicenceCheckPassed 0
 * #define LicenceCheckExpired 1
 * #define LicenceCheckPackageNotAllow 2
 * #define LicenceCheckDeviceNotAllow 3
 * #define LicenceNotCheck 4
 */
public enum AuthorizeStatus {
    LicenceCheckPassed(0),
    LicenceCheckExpired(1),
    LicenceCheckPackageNotAllow(2),
    LicenceCheckDeviceNotAllow(3),
    LicenceNotCheck(4);
    private int status;

    AuthorizeStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }}
