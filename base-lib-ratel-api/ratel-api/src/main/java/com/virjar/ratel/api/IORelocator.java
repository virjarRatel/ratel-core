package com.virjar.ratel.api;

public interface IORelocator {
    String getRedirectedPath(String origPath);

    void redirectDirectory(String origPath, String newPath);

    void redirectFile(String origPath, String newPath);

    void readOnlyFile(String path);

    void readOnly(String path);

    void whitelistFile(String path);

    void whitelist(String path);

    void forbid(String path, boolean file);

    boolean addMockSystemProperty(String key, String value);

    String queryMockSystemProperty(String key);

    void traceFilePath(String path);
}
