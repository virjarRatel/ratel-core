package com.virjar.ratel.manager.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.virjar.ratel.manager.repo.RatelAppRepo;
import com.virjar.ratel.manager.repo.RatelModuleRepo;


public class PackageChangeReceiver extends BroadcastReceiver {

    private static String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        return (uri != null) ? uri.getSchemeSpecificPart() : null;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction()) && intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
            // Ignore existing packages being removed in order to be updated
            return;

        String packageName = getPackageName(intent);
        if (packageName == null)
            return;

        if (Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())) {
            // make sure that the change is for the complete package, not only a
            // component
            String[] components = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST);
            if (components != null) {
                boolean isForPackage = false;
                for (String component : components) {
                    if (packageName.equals(component)) {
                        isForPackage = true;
                        break;
                    }
                }
                if (!isForPackage)
                    return;
            }
        }


        RatelModuleRepo.reloadSingleModule(context, packageName);
        RatelAppRepo.reloadSingleRatelApp(context, packageName);


    }
}
