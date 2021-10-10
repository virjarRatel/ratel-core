package com.virjar.ratel.manager.ui;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.virjar.ratel.manager.ManagerInitiazer;
import com.virjar.ratel.manager.R;
import com.virjar.ratel.manager.RatelManagerApp;
import com.virjar.ratel.manager.engine.RatelEngineLoader;

import java.io.File;
import java.lang.reflect.Method;


public class StatusInstallerFragment extends Fragment {

    public static final String ARCH = getArch();


    private static String getArch() {
        if (Build.CPU_ABI.equals("arm64-v8a")) {
            return "arm64";
        } else if (Build.CPU_ABI.equals("x86_64")) {
            return "x86_64";
        } else if (Build.CPU_ABI.equals("mips64")) {
            return "mips64";
        } else if (Build.CPU_ABI.startsWith("x86") || Build.CPU_ABI2.startsWith("x86")) {
            return "x86";
        } else if (Build.CPU_ABI.startsWith("mips")) {
            return "mips";
        } else if (Build.CPU_ABI.startsWith("armeabi-v5") || Build.CPU_ABI.startsWith("armeabi-v6")) {
            return "armv5";
        } else {
            return "arm";
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.status_installer, container, false);

        // Disable switch
        final SwitchCompat disableSwitch = v.findViewById(R.id.disableSwitch);
        disableSwitch.setChecked(true);
        disableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DefaultSharedPreferenceHolder.getInstance(getActivity()).totalSwitch(isChecked);
            }
        });

        // Device info
        TextView androidSdk = v.findViewById(R.id.android_version);
        TextView manufacturer = v.findViewById(R.id.ic_manufacturer);
        TextView cpu = v.findViewById(R.id.cpu);

        androidSdk.setText(getString(R.string.android_sdk, Build.VERSION.RELEASE, getAndroidVersion(), Build.VERSION.SDK_INT));
        manufacturer.setText(getUIFramework());
        cpu.setText(ARCH);
        determineVerifiedBootState(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshInstallStatus();
    }

    private void refreshInstallStatus() {
        View v = getView();
        if (v == null) {
            return;
        }
        TextView txtInstallError = v.findViewById(R.id.framework_install_errors);
        View txtInstallContainer = v.findViewById(R.id.status_container);
        ImageView txtInstallIcon = v.findViewById(R.id.status_icon);

        txtInstallError.setText(getString(R.string.framework_active, RatelEngineLoader.ratelEngineVersionName));
        txtInstallError.setTextColor(getResources().getColor(R.color.darker_green));
        txtInstallContainer.setBackgroundColor(getResources().getColor(R.color.darker_green));
        txtInstallIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_circle));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            v.findViewById(R.id.refresh_ratel_apps).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ManagerInitiazer.refreshRepository(StatusInstallerFragment.this.getActivity());
                    Toast.makeText(
                            StatusInstallerFragment.this.getActivity(),
                            "ratel module & app refresh again",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_installer, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                break;
            case R.id.about:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private String getAndroidVersion() {
        switch (Build.VERSION.SDK_INT) {
            case 15:
                return "Ice Cream Sandwich";
            case 16:
            case 17:
            case 18:
                return "Jelly Bean";
            case 19:
                return "KitKat";
            case 21:
            case 22:
                return "Lollipop";
            case 23:
                return "Marshmallow";
            case 24:
            case 25:
                return "Nougat";
            case 26:
            case 27:
                return "Oreo";
            case 28:
                return "PIE";
            case 29:
                return "Q";
            case 30:
                return "R";
            case 31:
                return "S";
            default:
                return "unknown";
        }
    }

    private String getUIFramework() {
        String manufacturer = Character.toUpperCase(Build.MANUFACTURER.charAt(0)) + Build.MANUFACTURER.substring(1);
        if (!Build.BRAND.equals(Build.MANUFACTURER)) {
            manufacturer += " " + Character.toUpperCase(Build.BRAND.charAt(0)) + Build.BRAND.substring(1);
        }
        manufacturer += " " + Build.MODEL + " ";
        if (manufacturer.contains("Samsung")) {
            manufacturer += new File("/system/framework/twframework.jar").exists() ? "(TouchWiz)" : "(AOSP-based ROM)";
        } else if (manufacturer.contains("Xioami")) {
            manufacturer += new File("/system/framework/framework-miui-res.apk").exists() ? "(MIUI)" : "(AOSP-based ROM)";
        }
        return manufacturer;
    }

    private void determineVerifiedBootState(View v) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method m = c.getDeclaredMethod("get", String.class, String.class);
            m.setAccessible(true);

            String propSystemVerified = (String) m.invoke(null, "partition.system.verified", "0");
            String propState = (String) m.invoke(null, "ro.boot.verifiedbootstate", "");
            File fileDmVerityModule = new File("/sys/module/dm_verity");

            boolean verified = !propSystemVerified.equals("0");
            boolean detected = !propState.isEmpty() || fileDmVerityModule.exists();

            TextView tv = v.findViewById(R.id.dmverity);
            if (verified) {
                tv.setText(R.string.verified_boot_active);
                tv.setTextColor(getResources().getColor(R.color.warning));
            } else if (detected) {
                tv.setText(R.string.verified_boot_deactivated);
                v.findViewById(R.id.dmverity_explanation).setVisibility(View.GONE);
            } else {
                v.findViewById(R.id.dmverity_row).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(RatelManagerApp.TAG, "Could not detect Verified Boot state", e);
        }
    }


}
