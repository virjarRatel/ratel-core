package com.virjar.ratel.demoapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.virjar.ratel.demoapp.construtor_test.ChildClass;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    public static final String LogTag = "RATE-DEMOL";

    public static String nowUserId = "undefine";

    public MainActivity() {
        //android.os.Debug.waitForDebugger();
        Log.i(LogTag, "com.virjar.ratel.demoapp.MainActivity.MainActivity constructor ,class hash:"
                + MainActivity.class.hashCode() +
                " trace: " + Log.getStackTraceString(new Throwable()));
        TestStatic2.flag = true;
        TestStatic.checkFlag();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.index_text_view);

        TextView androidIdTextView = findViewById(R.id.text_android_id);
        String ANDROID_ID = Settings.System.getString(this.getContentResolver(), Settings.System.ANDROID_ID);

        androidIdTextView.setText("androidId: " + ANDROID_ID);

        Button openWebViewBtn = findViewById(R.id.btn_openWebview);
        openWebViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebViewTestActivity.class);
                startActivity(intent);
            }
        });


        Button openAlertDialogBtn = findViewById(R.id.btn_openDialogTest);
        openAlertDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlertDialogTest.class);
                startActivity(intent);
            }
        });

        String nowUserId = MainActivity.getNowUserId();
        textView.setText("nowUser:" + nowUserId);
        MainActivity.setNowUserId(nowUserId);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SIGNATURES);

            Log.i(LogTag, "当前app签名：" + signatureInfo(packageInfo.signatures));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //scanWifi();

        getMemoryInfo();

        getMapsInfo();

        ExecveTest.cmdTest();

        //SocketMonitorTest.testSocketMonitor();


        DualAppCheckTest.checkDualApp((TextView) findViewById(R.id.text_sandbox_check), getApplicationInfo());

        checkConstructorHook();


//        try {
//            Thread.sleep(6000);
//        } catch (InterruptedException interruptedException) {
//            interruptedException.printStackTrace();
//        }
    }


    private static void checkConstructorHook() {
        new ChildClass();
    }

    private static void getMapsInfo() {

        try {
            List<String> s = FileUtils.readLines(new File("/proc/self/maps"), StandardCharsets.UTF_8);
            for (String s1 : s) {
                Log.i(LogTag, "maps: " + s1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void getMemoryInfo() {
        String s = null;
        try {
            s = FileUtils.readFileToString(new File("/proc/meminfo"), StandardCharsets.UTF_8);
            Log.i(LogTag, "meminfo: " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            s = FileUtils.readFileToString(new File("/system/build.prop"), StandardCharsets.UTF_8);
            Log.i(LogTag, "/system/build.prop: " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void scanWifi() {

        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return;
        }
        wifiManager.startScan();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult scanResult : scanResults) {
                    Log.i(LogTag, scanResult.BSSID);
                }
            }
        }, 3000);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult scanResult : scanResults) {
                    Log.i(LogTag, scanResult.BSSID);
                }
            }
        }, 20000);
    }

    public static String signatureInfo(Signature[] signatures) {
        List<String> strings = new ArrayList<>();
        if (signatures == null) {
            return StringUtils.join(strings, "*");// Joiner.on("*").join(strings);
        }
        for (Signature signature : signatures) {
            strings.add(signature.toCharsString());
        }
        return StringUtils.join(strings, "*");
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView textView = findViewById(R.id.indexTextView);
        String text = text();
        Log.i("weijia", "the text: " + text);
        textView.setText(text);

        printEnv();
    }

    private String text() {
        //请注意，如果这个函数只有一个return，那么可能被内联优化
        Log.i(LogTag, "origin call");
        return "原始文本";
    }

    private void printEnv() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e(LogTag, "checkSelfPermission ACCESS_FINE_LOCATION did not show.");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                List<String> allProviders = locationManager.getAllProviders();
                for (String provider : allProviders) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    Log.i(LogTag, "location: provider: " + provider + " location: " + location);
                }
            }
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                Log.e(LogTag, "checkSelfPermission READ_PHONE_STATE did not show.");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
            }
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String imei = telephonyManager.getDeviceId();
                Log.i(LogTag, "imei: " + imei);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    String imei1 = telephonyManager.getImei();
                    Log.i(LogTag, "telephonyManager.getImei(): " + imei1);

                    String meid = telephonyManager.getMeid();
                    Log.i(LogTag, "telephonyManager.getMeid(): " + meid);
                }


            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(LogTag, "serial_Build.getSerial(): " + Build.getSerial());
            Log.i(LogTag, "serial_Build.SERIAL: " + Build.SERIAL);
        } else {
            Log.i(LogTag, "serial: " + Build.SERIAL);
            Log.i(LogTag, "serial from SystemProperties: " + get("ro.serialno"));
        }

        Log.i(LogTag, "android_id: " + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        Log.i(LogTag, "test SystemProperties.get('demo_app_fake'): " + get("demo_app_fake"));


        //向内存卡写文件，用于测试内存卡重定向是否生效
        File ratelDemoDir = getExternalFilesDir("ratel_demo");
        if (!ratelDemoDir.exists()) {
            ratelDemoDir.mkdirs();
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(ratelDemoDir, "test_file.txt"))) {
            fileOutputStream.write("ratel_demo test".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String get(String key) {

        try {
            Class systemPropertiesClass = ClassLoader.getSystemClassLoader().loadClass("android.os.SystemProperties");
            return (String) systemPropertiesClass.getDeclaredMethod("get", new Class[]{String.class}).invoke(key, new Object[]{key});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setNowUserId(String nowUserId) {
        MainActivity.nowUserId = nowUserId;
        Log.i(LogTag, "set nowUserId: " + nowUserId + "  TheApp.nowUserId:" + getNowUserId() + " nowClass:" + MainActivity.class.hashCode());
    }

    public static String getNowUserId() {
        Log.i(LogTag, "get nowUserId: " + MainActivity.nowUserId + " nowClassLoader:" + MainActivity.class.hashCode());
        return nowUserId;
    }
}
