package net.kk.plus.preferences;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.virjar.ratel.manager.bridge.ISettingsManager;

import net.kk.plus.compact.BundleCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    protected SharedPreferences open(String name) {
        return getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    protected SharedPreferences getSharedPreferences(String name) {
        WeakReference<SharedPreferences> weakReference;
        synchronized (mSharedPreferencesMap) {
            weakReference = mSharedPreferencesMap.get(name);
            if (weakReference == null || weakReference.get() == null) {
                weakReference = new WeakReference<>(open(name));
                mSharedPreferencesMap.put(name, weakReference);
            }
        }
        return weakReference.get();
    }

    private final ConcurrentHashMap<String, WeakReference<SharedPreferences>>
            mSharedPreferencesMap = new ConcurrentHashMap<>();


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }


    @Override
    public String getType(Uri uri) {
        return uri.getPath();
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (Method_GetSettingsManager.equals(method)) {
            Uri uri = null;
            if (extras != null && extras.containsKey(URI_VALUE)) {
                uri = Uri.parse(extras.getString(URI_VALUE));
            }
            Bundle bundle = new Bundle();
            BundleCompat.putBinder(bundle, Arg_Binder, new SettingsManager(
                    getContext(), arg, uri, getSharedPreferences(arg)));
            return bundle;
        }
        return null;
    }

    public static final String Method_GetSettingsManager = "getSettingsManager";
    public static final String Arg_Binder = "_binder_";
    public static final String URI_KEY = "key";
    public static final String URI_VALUE = "uri";

    private static class SettingsManager extends ISettingsManager.Stub {
        private Context mContext;
        private final SharedPreferences mSharedPreferences;
        private final String mPrefName;
        private final Uri mUri;

        public SettingsManager(Context context, String name, Uri uri, SharedPreferences sharedPreferences) {
            mContext = context;
            mSharedPreferences = sharedPreferences;
            mPrefName = name;
            mUri = uri;

        }

        @Override
        public void clearAll() throws RemoteException {
            mSharedPreferences.edit().clear().apply();
        }

        @Override
        public void clear(String key) throws RemoteException {
            mSharedPreferences.edit().remove(key).apply();
            onChangedValue(key);
        }

        @Override
        public boolean hasKey(String key) throws RemoteException {
            return mSharedPreferences.contains(key);
        }

        @Override
        public boolean getBoolean(String key, boolean def) throws RemoteException {
            return mSharedPreferences.getBoolean(key, def);
        }

        @Override
        public float getFloat(String key, float def) throws RemoteException {
            return mSharedPreferences.getFloat(key, def);
        }

        @Override
        public int getInt(String key, int def) throws RemoteException {
            return mSharedPreferences.getInt(key, def);
        }

        @Override
        public long getLong(String key, long def) throws RemoteException {
            return mSharedPreferences.getLong(key, def);
        }

        @Override
        public String getString(String key, String def) throws RemoteException {
            return mSharedPreferences.getString(key, def);
        }

        @Override
        public List<String> getStringArrayList(String key, List<String> def) throws RemoteException {
            Set<String> val = mSharedPreferences.getStringSet(key, null);
            List<String> res = new ArrayList<>();
            if (val == null) {
                if (def != null) {
                    res.addAll(def);
                }
            } else {
                res.addAll(val);
            }
            return res;
        }

        private void onChangedValue(String key) {
            Uri uri = mUri.buildUpon().appendPath(mPrefName).appendQueryParameter(URI_KEY, key).build();
            mContext.getContentResolver().notifyChange(uri, null);
        }

        @Override
        public void putBoolean(String key, boolean def) throws RemoteException {
            mSharedPreferences.edit().putBoolean(key, def).apply();
            onChangedValue(key);
        }

        @Override
        public void putFloat(String key, float def) throws RemoteException {
            mSharedPreferences.edit().putFloat(key, def).apply();
            onChangedValue(key);
        }

        @Override
        public void putInt(String key, int def) throws RemoteException {
            mSharedPreferences.edit().putInt(key, def).apply();
            onChangedValue(key);
        }

        @Override
        public void putLong(String key, long def) throws RemoteException {
            mSharedPreferences.edit().putLong(key, def).apply();
            onChangedValue(key);
        }

        @Override
        public void putString(String key, String def) throws RemoteException {
            mSharedPreferences.edit().putString(key, def).apply();
            onChangedValue(key);
        }

        @Override
        public void putStringArrayList(String key, List<String> def) throws RemoteException {
            Set<String> res = new ArraySet<>();
            if (def != null) {
                res.addAll(def);
            }
            mSharedPreferences.edit().putStringSet(key, res).apply();
            onChangedValue(key);
        }

        @Override
        public List<String> getKeys() throws RemoteException {
            Set<String> keys = mSharedPreferences.getAll().keySet();
            return new ArrayList<>(keys);
        }

        @Override
        public Bundle getAll() throws RemoteException {
            Bundle bundle = new Bundle();
            Map<String, ?> map = mSharedPreferences.getAll();
            if (map != null) {
                Iterator<? extends Map.Entry<String, ?>> iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ?> e = iterator.next();
                    Object value = e.getValue();
                    if (value instanceof Integer) {
                        bundle.putInt(e.getKey(), (Integer) value);
                    } else if (value instanceof Float) {
                        bundle.putFloat(e.getKey(), (Float) value);
                    } else if (value instanceof Long) {
                        bundle.putLong(e.getKey(), (Long) value);
                    } else if (value instanceof Boolean) {
                        bundle.putBoolean(e.getKey(), (Boolean) value);
                    } else if (value instanceof String) {
                        bundle.putString(e.getKey(), (String) value);
                    } else if (value instanceof Set) {
                        try {
                            bundle.putStringArrayList(e.getKey(), new ArrayList<String>((Set<String>) value));
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
            }
            return bundle;
        }
    }
}
