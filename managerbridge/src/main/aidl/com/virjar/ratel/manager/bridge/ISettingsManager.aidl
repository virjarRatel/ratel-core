// ISettingsManager.aidl
package com.virjar.ratel.manager.bridge;

// Declare any non-default types here with import statements

interface ISettingsManager {

    void clearAll();
    void clear(String key);
    boolean hasKey(String key);
    boolean getBoolean(String key, boolean def);
    float getFloat(String key, float def);
    int getInt(String key, int def);
    long getLong(String key, long def);
    String getString(String key, String def);
    List<String> getStringArrayList(String key,in List<String> def);
    void putBoolean(String key, boolean def);
    void putFloat(String key, float def);
    void putInt(String key, int def);
    void putLong(String key, long def);
    void putString(String key, String def);
    void putStringArrayList(String key,in List<String> def);

    Bundle getAll();
    List<String> getKeys();
}
