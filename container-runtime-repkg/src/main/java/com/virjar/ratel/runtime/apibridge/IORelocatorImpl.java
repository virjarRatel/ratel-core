package com.virjar.ratel.runtime.apibridge;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.api.IORelocator;
import com.virjar.ratel.envmock.PropertiesMockItem;
import com.virjar.ratel.runtime.RatelRuntime;

public class IORelocatorImpl implements IORelocator {
    @Override
    public String getRedirectedPath(String origPath) {
        return RatelNative.getRedirectedPath(origPath);
    }

    @Override
    public void redirectDirectory(String origPath, String newPath) {
        if (RatelRuntime.isStartCompleted()) {
            throw new IllegalStateException("can not call ioRelocator after ratel start completed!");
        }
        RatelNative.redirectDirectory(origPath, newPath);
    }

    @Override
    public void redirectFile(String origPath, String newPath) {
        if (RatelRuntime.isStartCompleted()) {
            throw new IllegalStateException("can not call ioRelocator after ratel start completed!");
        }
        RatelNative.redirectFile(origPath, newPath);
    }

    @Override
    public void readOnlyFile(String path) {
        RatelNative.readOnlyFile(path);
    }

    @Override
    public void readOnly(String path) {
        RatelNative.readOnly(path);
    }

    @Override
    public void whitelistFile(String path) {
        RatelNative.whitelist(path);
    }

    @Override
    public void whitelist(String path) {
        RatelNative.whitelist(path);
    }

    @Override
    public void forbid(String path, boolean file) {
        RatelNative.forbid(path, file);
    }

    @Override
    public boolean addMockSystemProperty(String key, String value) {
        RatelNative.nativeAddMockSystemProperty(key, value);
        return true;
    }

    @Override
    public String queryMockSystemProperty(String key) {
        PropertiesMockItem propertiesMockItem = RatelNative.nativeQueryMockSystemProperty(key);
        if (propertiesMockItem == null) {
            return null;
        }
        return propertiesMockItem.getProperties_value();
    }

    @Override
    public void traceFilePath(String path) {
        RatelNative.traceFilePath(path);
    }
}
