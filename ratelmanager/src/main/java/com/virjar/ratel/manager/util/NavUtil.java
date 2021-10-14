package com.virjar.ratel.manager.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Browser;
import android.support.customtabs.CustomTabsIntent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.Toast;

import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.ui.DefaultSharedPreferenceHolder;

import java.util.List;

public final class NavUtil {
    public static final String SETTINGS_CATEGORY = "de.robv.android.xposed.category.MODULE_SETTINGS";

    private static Uri parseURL(String str) {
        if (str == null || str.isEmpty())
            return null;

        Spannable spannable = new SpannableString(str);
        Linkify.addLinks(spannable, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        return (spans.length > 0) ? Uri.parse(spans[0].getURL()) : null;
    }

    private static void startURL(Activity activity, Uri uri) {
        if (!DefaultSharedPreferenceHolder.getInstance(activity).getPreferences().getBoolean("chrome_tabs", true)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity.getPackageName());
            activity.startActivity(intent);
            return;
        }

        CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
        customTabsIntent.setShowTitle(true);
        customTabsIntent.setToolbarColor(activity.getResources().getColor(R.color.colorPrimary));
        customTabsIntent.build().launchUrl(activity, uri);
    }

    public static void startURL(Activity activity, String url) {
        startURL(activity, parseURL(url));
    }

    public static void startApp(Context context, String packageName) {
        Intent launchIntent = getSettingsIntent(context, packageName);
        if (launchIntent != null) {
            // 优先使用 Shizuku
            // launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName component = launchIntent.getComponent();
            if (component != null) {
                Intent intent = new Intent();
                intent.setComponent(component);
                if (ShizukuToolkit.startActivity(intent)) {
                    Log.i(RatelManagerApp.TAG, "start app use Shizuku success");
                    return;
                }
            }

            context.startActivity(launchIntent);
        } else {
            Toast.makeText(context,
                    context.getString(R.string.module_no_ui),
                    Toast.LENGTH_LONG).show();
        }
    }


    private static Intent getSettingsIntent(Context activity, String packageName) {
        // taken from
        // ApplicationPackageManager.getLaunchIntentForPackage(String)
        // first looks for an Xposed-specific category, falls back to
        // getLaunchIntentForPackage
        PackageManager pm = activity.getPackageManager();

        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(SETTINGS_CATEGORY);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = pm.queryIntentActivities(intentToResolve, 0);

        if (ris == null || ris.size() <= 0) {
            return pm.getLaunchIntentForPackage(packageName);
        }

        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }
}
