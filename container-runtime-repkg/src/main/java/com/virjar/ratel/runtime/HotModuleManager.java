package com.virjar.ratel.runtime;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.utils.ProcessUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.io.IOUtils;

/**
 * 管理热发模块
 */
public class HotModuleManager {

    private static Timer mTimer = null;

    private static Map<String, HotModule> moduleMap = new ConcurrentHashMap<>();

    static {
        try {
            init();
        } catch (Exception e) {
            Log.w(Constants.TAG, "HotModuleManager load config failed", e);
        }
    }

    static List<HotModule> getEnableHotModules() {
        List<HotModule> hotModuleList = new ArrayList<>();
        for (HotModule hotModule : moduleMap.values()) {
            if (hotModule.ready()) {
                hotModuleList.add(hotModule);
            }
        }
        return hotModuleList;
    }

    static void startMonitorNewConfig() {
        if (!RatelRuntime.isMainProcess) {
            return;
        }
        if (mTimer != null) {
            return;
        }
        mTimer = new Timer("ratel_hot_module_timer");
        mTimer.scheduleAtFixedRate(new TimerTask() {
                                       @Override
                                       public void run() {
                                           try {
                                               doRefreshConfig();
                                           } catch (Throwable throwable) {
                                               Log.w(Constants.TAG, "refresh ratel hot module failed", throwable);
                                           }
                                       }
                                   }
                , 60 * 1000, 10 * 60 * 1000
                //测试环境下，一分钟执行一次
                // , 1000, 60 * 1000
        );
    }

    public static void init() throws IOException, JSONException {
        File configFile = RatelEnvironment.hotModuleConfigFile();
        if (!configFile.exists()) {
            return;
        }
        String json = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
        parseConfigJson(new JSONObject(json).getJSONArray("data"));
    }


    private static void doRefreshConfig() throws IOException, JSONException {
        String url = hotModuleConfigURL();
        // 当前证书id
        String licenceId = RatelConfig.getConfig("licenceId");//Authorizer.nowCertificateModel.licenceId;
        String group = RatelConfig.getConfig(Constants.RATEL_KEY_HOT_MODULE_GROUP);
        String mPackage = RatelRuntime.originPackageName;

        //http://ratel.virjar.com/api/ratel/ratel-hot-module/hotModuleConfig?certificateId=adc&mPackage=com.tencent.mm&group=a
        url = url + "?certificateId=" + URLEncoder.encode(licenceId, StandardCharsets.UTF_8.name()) + "&mPackage=" + URLEncoder.encode(mPackage, StandardCharsets.UTF_8.name());
        if (!TextUtils.isEmpty(group)) {
            url = url + "&group=" + URLEncoder.encode(group, StandardCharsets.UTF_8.name());
        }
        if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "request hot module config with request: " + url);
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.e(Constants.TAG, "access hot module config url failed: " + url);
            connection.disconnect();
            return;
        }
        try (InputStream inputStream = connection.getInputStream()) {
            String jsonConfig = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            if (RatelRuntime.isRatelDebugBuild) {
                Log.i(Constants.TAG, "the module config response: " + jsonConfig);
            }
            JSONObject jsonObject = new JSONObject(jsonConfig);
            int status = jsonObject.getInt("status");
            if (status != 0) {
                Log.w(Constants.TAG, "bad response for server: " + jsonConfig);
                return;
            }
            //save
            FileUtils.write(RatelEnvironment.hotModuleConfigFile(), jsonConfig, StandardCharsets.UTF_8);

            JSONArray jsonArray = jsonObject.getJSONArray("data");
            if (jsonArray == null) {
                return;
            }
            parseConfigJson(jsonArray);

            for (HotModule hotModule : moduleMap.values()) {
                downloadHandleOneHotModule(hotModule);
            }

        } finally {
            connection.disconnect();
        }

    }


    private static void downloadHandleOneHotModule(HotModule item) {

        if (item.localFile != null && item.localFile.exists()) {
            // 文件已经下载
            return;
        }

        String url = item.ossUrl;
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (item.fileSize > 10 * 1024) {
            //插件大于10M的时候，我们通过系统 DownloadManager 进行下载
            //因为系统提供的下载器更加稳定
            downloadWithDownloadManager(item);
        } else {
            //插件文件很小的时候，直接下载，主要是因为系统的下载器，可有流量弹窗提醒,
            //这可能需要用户有一些界面操作
            try {
                downloadWithURLConnection(item);
            } catch (IOException e) {
                Log.e(Constants.TAG, "file download failed", e);
            }
        }

    }


    private static void downloadWithURLConnection(HotModule item) throws IOException {
        URL url = new URL(item.ossUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.e(Constants.TAG, "access hot module config url failed: " + item.ossUrl);
            connection.disconnect();
            return;
        }
        try (InputStream inputStream = connection.getInputStream()) {
            String path = url.getPath();
            path = path.trim();
            int indexOfSlash = path.lastIndexOf("/");
            if (indexOfSlash > 0) {
                path = path.substring(indexOfSlash + 1);
            }
            //like com.virjar.ratel.hotmodule.tencent_1.0_1_a5c388c5e8c9ac69b673c56f4da846fe.apk
            String saveFileName = path;

            File saveFile = new File(RatelEnvironment.hotModuleDir(), saveFileName);

            File saveFileBak = new File(RatelEnvironment.hotModuleDir(), saveFileName + ".bak");

            FileOutputStream fileOutputStream = new FileOutputStream(saveFileBak);
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.close();

            if (!saveFileBak.renameTo(saveFile)) {
                //使用rename是为了避免文件下载不完整，比如磁盘空间不足，可能数据写一半。这个时候文件存在，但是文件结构被破坏
                Log.w(Constants.TAG, "can not rename file from: " + saveFileBak.getAbsolutePath() + " to:" + saveFile.getAbsolutePath());

            }
            Log.i(Constants.TAG, "apk download finished: " + saveFile.getAbsolutePath());

            if (!checkHotModule(saveFile)) {
                Log.i(Constants.TAG, "the hot module apk error,force remove");
                FileUtils.forceDelete(saveFile);
                return;
            }

            Log.i(Constants.TAG, "restart app now");
            ProcessUtil.killMe();

        } finally {
            connection.disconnect();
        }

    }

    private static boolean checkHotModule(File file) {
        if (file == null) {
            return false;
        }
        if (!file.exists()) {
            return false;
        }
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = zipFile.getEntry("assets/xposed_init");
            return entry != null;
        } catch (IOException e) {
            return false;
        }
    }


    private static void downloadWithDownloadManager(HotModule item) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(item.ossUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("RatelHotModuleDownloader");
        request.setDescription("downloading: " + item.modulePkgName);
        request.setAllowedOverRoaming(false);
        //设置文件存放目录
        request.setDestinationInExternalFilesDir(RatelRuntime.originContext, Environment.DIRECTORY_DOWNLOADS, "RatelHotModules");
        DownloadManager downManager = (DownloadManager) RatelRuntime.originContext.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downManager == null) {
            throw new IllegalStateException("can not find system service : DOWNLOAD_SERVICE");
        }

        registerDownloadFinishedReceiver();

        downManager.enqueue(request);
    }


    private static boolean register = false;

    private static void registerDownloadFinishedReceiver() {
        if (register) {
            return;
        }
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id < 0) {
                        return;
                    }
                    try {
                        handleDownloadComplete(id);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, "handleDownloadComplete task failed", e);
                    }
                }
            }
        };

        RatelRuntime.originContext.registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        register = true;
    }


    private static void handleDownloadComplete(long id) throws IOException {
        DownloadManager downManager = (DownloadManager) RatelRuntime.originContext.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downManager == null) {
            throw new IllegalStateException("can not find system service : DOWNLOAD_SERVICE");
        }

        boolean success = false;
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
        try (Cursor cursor = downManager.query(query)) {
            while (cursor.moveToNext()) {
                // String localFileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                String localFileUrl = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                String remoteUrl = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_FAILED) {
                    String failedReason = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                    Log.w(Constants.TAG, "download failed for :" + localFileUrl + " failed reason:" + failedReason);
                    return;
                }

                Log.i(Constants.TAG, "download module success for :" + localFileUrl);
                ParcelFileDescriptor descriptor = RatelRuntime.originContext.getContentResolver().openFileDescriptor(Uri.parse(localFileUrl), "r");
                if (descriptor == null) {
                    Log.w(Constants.TAG, "query openFileDescriptor return null for uri:" + localFileUrl);
                    return;
                }
                FileInputStream fileInputStream = new FileInputStream(descriptor.getFileDescriptor());
                //like /com.virjar.ratel.hotmodule.tencent_1.0_1_a5c388c5e8c9ac69b673c56f4da846fe.apk
                String path = new URL(remoteUrl).getPath();

                if (TextUtils.isEmpty(path)) {
                    Log.w(Constants.TAG, "query url not illegal :" + localFileUrl + " url:" + remoteUrl);
                    return;
                }

                path = path.trim();
                int indexOfSlash = path.lastIndexOf("/");
                if (indexOfSlash > 0) {
                    path = path.substring(indexOfSlash + 1);
                }
                //like com.virjar.ratel.hotmodule.tencent_1.0_1_a5c388c5e8c9ac69b673c56f4da846fe.apk
                String saveFileName = path;

                File saveFile = new File(RatelEnvironment.hotModuleDir(), saveFileName);

                File saveFileBak = new File(RatelEnvironment.hotModuleDir(), saveFileName + ".bak");

                FileOutputStream fileOutputStream = new FileOutputStream(saveFileBak);
                IOUtils.copy(fileInputStream, fileOutputStream);
                fileInputStream.close();
                fileOutputStream.close();

                if (!saveFileBak.renameTo(saveFile)) {
                    //使用rename是为了避免文件下载不完整，比如磁盘空间不足，可能数据写一半。这个时候文件存在，但是文件结构被破坏
                    Log.w(Constants.TAG, "can not rename file from: " + saveFileBak.getAbsolutePath() + " to:" + saveFile.getAbsolutePath());
                }

                Log.i(Constants.TAG, "apk download finished: " + saveFile.getAbsolutePath());
                success = true;
            }
        }

        if (!success) {
            return;
        }

        boolean hasTask = false;
        //如果有成功的，切是最后一个任务，那么重启应用
        query = new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
        try (Cursor cursor = downManager.query(query)) {
            if (cursor.moveToNext()) {
                hasTask = true;
            }
        }

        if (!hasTask) {
            Log.i(Constants.TAG, "all module download finished,restart app");
            ProcessUtil.killMe();
        }

        //如果没有其他task，那么可能存在中断任务，10分钟后强制重启app
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.i(Constants.TAG, "one HotModule download finished,restart app");
            ProcessUtil.killMe();
        }, 10 * 60 * 1000);
    }


    private static String hotModuleConfigURL() {
        String configURL = RatelConfig.getConfig(Constants.RATEL_KEY_SERVER_URL);
        if (TextUtils.isEmpty(configURL)) {
            configURL = Constants.DEFAULT_RATEL_SERVER_URL;
        }

        if (configURL.endsWith("/")) {
            //remove last /
            configURL = configURL.substring(0, configURL.length() - 1);
        }
        configURL = configURL + "/api/ratel/ratel-hot-module/hotModuleConfig";

        try {
            new URL(configURL);
            return configURL;
        } catch (MalformedURLException e) {
            Log.e(Constants.TAG, "error hotModuleConfigURL format: " + configURL);
        }
        return Constants.DEFAULT_RATEL_SERVER_URL + "/api/ratel/ratel-hot-module/hotModuleConfig";

    }

    public static class HotModule {
        public String fileHash;

        public String ossUrl;

        public String modulePkgName;

        public String moduleVersion;

        public Long moduleVersionCode;

        public String certificateId;

        public String forRatelApp;

        public String ratelGroup;

        public Long userId;

        public String userName;

        public Boolean enable;

        public File localFile;

        public Long fileSize = 2000L;

        public boolean ready() {
            return enable && localFile != null && localFile.exists();
        }
    }

    private static void parseConfigJson(JSONArray jsonArray) {
        if (jsonArray == null) {
            return;
        }

        Map<String, HotModule> originMap = HotModuleManager.moduleMap;

        Map<String, HotModule> moduleMap = new ConcurrentHashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);

            HotModule hotModule = new HotModule();
            hotModule.fileHash = jsonObject.optString("fileHash");
            hotModule.ossUrl = jsonObject.optString("ossUrl");
            hotModule.modulePkgName = jsonObject.optString("modulePkgName");
            hotModule.moduleVersion = jsonObject.optString("moduleVersion");
            hotModule.moduleVersionCode = jsonObject.optLong("moduleVersionCode");
            hotModule.certificateId = jsonObject.optString("certificateId");
            hotModule.forRatelApp = jsonObject.optString("forRatelApp");
            hotModule.ratelGroup = jsonObject.optString("ratelGroup");
            hotModule.userId = jsonObject.optLong("userId");
            hotModule.userName = jsonObject.optString("userName");
            hotModule.enable = jsonObject.optBoolean("enable");
            hotModule.fileSize = jsonObject.optLong("fileSize");
            moduleMap.put(hotModule.fileHash, hotModule);

            HotModule originHotModule = originMap.get(hotModule.fileHash);
            if (originHotModule != null && originHotModule.localFile != null && originHotModule.localFile.exists()) {
                hotModule.localFile = originHotModule.localFile;
            }
        }

        File moduleDir = RatelEnvironment.hotModuleDir();
        File[] files = moduleDir.listFiles();
        if (files == null) {
            return;
        }

        //scan
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".bak")) {
                continue;
            }
            for (HotModule hotModule : moduleMap.values()) {
                if (fileName.contains(hotModule.fileHash)) {
                    hotModule.localFile = file;
                    break;
                }
            }
        }

        HotModuleManager.moduleMap = moduleMap;
    }
}
