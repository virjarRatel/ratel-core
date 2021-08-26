package net.kk.plus.compact;

import android.os.Bundle;
import android.os.IBinder;

/**
 * @author Lody
 */
public class BundleCompat {

    public static IBinder getBinder(Bundle bundle, String key) {
        return bundle.getBinder(key);
    }

    public static void putBinder(Bundle bundle, String key, IBinder value) {
        bundle.putBinder(key, value);
    }

}
