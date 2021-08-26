package net.kk.plus.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.virjar.ratel.manager.bridge.ISettingsManager;

import net.kk.plus.compact.BundleCompat;
import net.kk.plus.compact.ContentProviderCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class SettingsPreference implements SharedPreferences, SharedPreferences.Editor {
    private static final String AUTH = ".settings.preference";
    private ISettingsManager mSettingsManager;
    private Context mContext;
    private final Uri notifyUri;
    private boolean registered;
    private final List<OnSharedPreferenceChangeListener> mChangeListeners
            = new ArrayList<>();

    public SettingsPreference(Context context, String name) {
        this(context, context.getPackageName() + AUTH, name);
    }

    public SettingsPreference(Context context, String auth, String prefName) {
        mContext = context;
        Uri uri = Uri.parse("content://" + auth);
        Bundle args = new Bundle();
        args.putString(SettingsProvider.URI_VALUE, "content://" + auth);
        Bundle res = ContentProviderCompat.call(context, uri, SettingsProvider.Method_GetSettingsManager,
                prefName, args);
        if (res != null) {
            IBinder clientBinder = BundleCompat.getBinder(res, SettingsProvider.Arg_Binder);
            mSettingsManager = ISettingsManager.Stub.asInterface(clientBinder);
        }
        notifyUri = uri.buildUpon().appendPath(prefName).build();
    }

    public boolean isOpen() {
        return mSettingsManager != null;
    }

    public void clearAll() {
        if (isOpen()) {
            try {
                mSettingsManager.clearAll();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void clear(String key) {
        if (isOpen()) {
            try {
                mSettingsManager.clear(key);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasKey(String key) {
        if (isOpen()) {
            try {
                return mSettingsManager.hasKey(key);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        if (isOpen()) {
            try {
                return mSettingsManager.getBoolean(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return def;
    }

    @Override
    public float getFloat(String key, float def) {
        if (isOpen()) {
            try {
                return mSettingsManager.getFloat(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return def;
    }

    @Override
    public int getInt(String key, int def) {
        if (isOpen()) {
            try {
                return mSettingsManager.getInt(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return def;
    }

    @Override
    public long getLong(String key, long def) {
        if (isOpen()) {
            try {
                return mSettingsManager.getLong(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return def;
    }

    @Override
    public String getString(String key, String def) {
        if (isOpen()) {
            try {
                return mSettingsManager.getString(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return def;
    }

    public List<String> getStringArrayList(String key, List<String> def) {
        if (isOpen()) {
            try {
                return mSettingsManager.getStringArrayList(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return def;
    }

    @Override
    public Editor putBoolean(String key, boolean def) {
        if (isOpen()) {
            try {
                mSettingsManager.putBoolean(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public Editor putFloat(String key, float def) {
        if (isOpen()) {
            try {
                mSettingsManager.putFloat(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public Editor putInt(String key, int def) {
        if (isOpen()) {
            try {
                mSettingsManager.putInt(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public Editor putLong(String key, long def) {
        if (isOpen()) {
            try {
                mSettingsManager.putLong(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public Editor putString(String key, String def) {
        if (isOpen()) {
            try {
                mSettingsManager.putString(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public Editor putStringArrayList(String key, List<String> def) {
        if (isOpen()) {
            try {
                mSettingsManager.putStringArrayList(key, def);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public boolean contains(String key) {
        return hasKey(key);
    }

    @Deprecated
    @Override
    public Map<String, Object> getAll() {
        Bundle bundle = null;
        try {
            bundle = mSettingsManager.getAll();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        List<String> keys = null;
        try {
            keys = mSettingsManager.getKeys();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Map<String, Object> map = new ConcurrentHashMap<>();
        if (bundle != null && keys != null) {
            for (String key : keys) {
                Object val = bundle.get(key);
                if (val instanceof List) {
                    Set<String> set = new ArraySet<>();
                    try {
                        set.addAll((List<String>) val);
                    } catch (Exception e) {
                        //ignore
                    }
                    map.put(key, set);
                } else {
                    map.put(key, val);
                }

            }
        }
        return map;
    }

    @Deprecated
    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (mChangeListeners) {
            mChangeListeners.add(listener);
            if (registered) {
                return;
            }
            registered = true;
            if (mContentObserver == null) {
                mContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        String key = uri.getQueryParameter(SettingsProvider.URI_KEY);
                        if (key != null) {
                            synchronized (mChangeListeners) {
                                for (OnSharedPreferenceChangeListener listener : mChangeListeners) {
                                    listener.onSharedPreferenceChanged(SettingsPreference.this, key);
                                }
                            }
                        }
                    }
                };
            }
        }
        mContext.getContentResolver().registerContentObserver(notifyUri, true, mContentObserver);
    }

    @Deprecated
    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (listener == null) {
            return;
        }
        boolean needUnreg = false;
        synchronized (mChangeListeners) {
            mChangeListeners.remove(listener);
            if (mChangeListeners.size() == 0 && registered) {
                registered = false;
                needUnreg = true;
            }
        }
        if (needUnreg) {
            mContext.getContentResolver().unregisterContentObserver(mContentObserver);
        }
    }

    private ContentObserver mContentObserver;

    @Override
    public Editor edit() {
        return this;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        List<String> rs = getStringArrayList(key, null);
        if (rs == null) {
            return defValues;
        }
        Set<String> sets = new ArraySet<>();
        sets.addAll(rs);
        return sets;
    }

    @Override
    public Editor putStringSet(String key, Set<String> values) {
        List<String> val = new ArrayList<>();
        if (values != null) {
            val.addAll(values);
        }
        return putStringArrayList(key, val);
    }

    @Override
    public Editor remove(String key) {
        clear(key);
        return this;
    }

    @Override
    public Editor clear() {
        clearAll();
        return this;
    }

    @Override
    public boolean commit() {
        return true;
    }

    @Override
    public void apply() {
        //none
    }
}
