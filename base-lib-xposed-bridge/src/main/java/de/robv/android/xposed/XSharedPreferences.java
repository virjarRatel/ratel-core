package de.robv.android.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.virjar.ratel.api.RatelToolKit;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * 请注意，由于在Android较高版本上文件权限限制问题，XSharedPreferences可能无法使用。建议尽量避免使用XSharedPreferences，使用ContentProvider替代
 */
public class XSharedPreferences implements SharedPreferences {

    private SharedPreferences sharedPreferences;
    private File file;

    /**
     * Read settings from the specified file.
     *
     * @param prefFile The file to read the preferences from.
     */
    public XSharedPreferences(File prefFile) {
        String fileName = "ratel_" + prefFile.getName() + "_preferences.xml";
        sharedPreferences = RatelToolKit.sContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        file = new File(Environment.getDataDirectory(), "data/" + RatelToolKit.sContext.getPackageName() + "/shared_prefs/" + fileName);
    }

    /**
     * Read settings from the default preferences for a package.
     * These preferences are returned by {@link PreferenceManager#getDefaultSharedPreferences}.
     *
     * @param packageName The package name.
     */
    public XSharedPreferences(String packageName) {
        this(packageName, packageName + "_preferences");
    }

    /**
     * Read settings from a custom preferences file for a package.
     * These preferences are returned by {@link Context#getSharedPreferences(String, int)}.
     *
     * @param packageName  The package name.
     * @param prefFileName The file name without ".xml".
     */
    public XSharedPreferences(String packageName, String prefFileName) {
        String fileName = "ratel_" + prefFileName + "_preferences.xml";
        sharedPreferences = RatelToolKit.sContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        file = new File(Environment.getDataDirectory(), "data/" + RatelToolKit.sContext.getPackageName() + "/shared_prefs/" + fileName);
    }

    /**
     * Tries to make the preferences file world-readable.
     *
     * <p><strong>Warning:</strong> This is only meant to work around permission "fix" functions that are part
     * of some recoveries. It doesn't replace the need to open preferences with {@code MODE_WORLD_READABLE}
     * in the module's UI code. Otherwise, Android will set stricter permissions again during the next save.
     *
     * <p>This will only work if executed as root (e.g. {@code initZygote()}) and only if SELinux is disabled.
     *
     * @return {@code true} in case the file could be made world-readable.
     */
    @SuppressLint("SetWorldReadable")
    public boolean makeWorldReadable() {
        return true;
    }

    /**
     * Returns the file that is backing these preferences.
     *
     * <p><strong>Warning:</strong> The file might not be accessible directly.
     */
    public File getFile() {
        return file;
    }

    /**
     * Reload the settings from file if they have changed.
     *
     * <p><strong>Warning:</strong> With enforcing SELinux, this call might be quite expensive.
     */
    public synchronized void reload() {

    }


    @Override
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    @Override
    public String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return sharedPreferences.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    @Override
    public Editor edit() {
        return sharedPreferences.edit();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
