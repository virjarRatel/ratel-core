package com.virjar.ratel.builder.ratelentry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.virjar.ratel.allcommon.Constants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


class XApkHandler {

    private File apkFile;
    private String baseApkFileName;
    private ZipFile zipFile;

    File releasedBaseApkTempFile;
    private JSONObject manifestJsonConfig;
    private File xapkWorking;
    private Set<String> supportArch = new HashSet<>();
    Map<String, String> apkMaps = new HashMap<>();

    XApkHandler(File apkFile) throws IOException {
        this.apkFile = apkFile;
        parse();
    }

    public Set<String> originAPKSupportArch() {
        return supportArch;
    }

    private static final String MANIFEST = "manifest.json";

    void buildRatelInflectedSplitApk(File ratelBaseApkFile) throws Exception {
        System.out.println("handle xapk(split apk)");
        File workDir = Main.workDir();


        File signatureKeyFile = new File(workDir, Constants.ratelDefaultApkSignatureKey);

        File buildApk = File.createTempFile("ratel-xapk-build", ".apk");
        ZipOutputStream zipOutputStream = new ZipOutputStream(buildApk);

        Enumeration<ZipEntry> entries = zipFile.getEntries();

        long totalApkSize = 0L;
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.equals(MANIFEST)) {
                //需要忽略
                continue;
            }
            if (!zipEntryName.endsWith(".apk")) {
                zipOutputStream.putNextEntry(new ZipEntry(zipEntry));
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                zipOutputStream.write(IOUtils.toByteArray(inputStream));
                inputStream.close();
                continue;
            }

            if (zipEntryName.equals(baseApkFileName)) {
                zipOutputStream.putNextEntry(new ZipEntry(zipEntry));
                // zipOutputStream.write(IOUtils.toByteArray(zipFile.getInputStream(zipEntry)));
                FileInputStream fileInputStream = new FileInputStream(ratelBaseApkFile);
                IOUtils.copy(fileInputStream, zipOutputStream);
                fileInputStream.close();
                totalApkSize += ratelBaseApkFile.length();
                continue;
            }
            //其他的apk，重新签名
            File releaseApkFile = new File(xapkWorking, zipEntryName);

            System.out.println("resign apk: " + zipEntryName);
            HelperZipAndSign.signatureApk(releaseApkFile, signatureKeyFile, null);

            zipOutputStream.putNextEntry(new ZipEntry(zipEntry));
            FileInputStream fileInputStream = new FileInputStream(releaseApkFile);
            IOUtils.copy(fileInputStream, zipOutputStream);
            fileInputStream.close();
            totalApkSize += releaseApkFile.length();
        }
        manifestJsonConfig.put("total_size", totalApkSize);
        zipOutputStream.putNextEntry(new ZipEntry(MANIFEST));
        zipOutputStream.write(manifestJsonConfig.toJSONString().getBytes(StandardCharsets.UTF_8));
        zipOutputStream.close();

//        Files.move(
//                buildApk.toPath(), ratelBaseApkFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String absolutePath = ratelBaseApkFile.getAbsolutePath();

        File newFile = ratelBaseApkFile;
        if (absolutePath.endsWith(".apk")) {
            int index = absolutePath.lastIndexOf(".");
            newFile = new File(absolutePath.substring(0, index) + ".xapk");
        }
        Files.move(buildApk.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if (!newFile.equals(ratelBaseApkFile)) {
            FileUtils.forceDelete(ratelBaseApkFile);
        }
        System.out.println("xapk build finished with output:  " + newFile);
    }

    public void releaseApks() throws IOException {

        xapkWorking = new File(Main.workDir(), "xapk-working");
        if (!xapkWorking.exists()) {
            xapkWorking.mkdirs();
        }

        JSONArray split_apks = manifestJsonConfig.getJSONArray("split_apks");
        for (int i = 0; i < split_apks.size(); i++) {
            JSONObject jsonObject = split_apks.getJSONObject(i);
            String file = jsonObject.getString("file");
            File outFile = new File(xapkWorking, file);
            ZipEntry entry = zipFile.getEntry(file);

            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            InputStream inputStream = zipFile.getInputStream(entry);
            IOUtils.copy(inputStream, fileOutputStream);
            inputStream.close();
            fileOutputStream.close();
        }

    }

    private void parse() throws IOException {
        zipFile = new ZipFile(apkFile);
        ZipEntry manifestEntry = zipFile.getEntry(MANIFEST);
        InputStream inputStream = zipFile.getInputStream(manifestEntry);
        manifestJsonConfig = JSON.parseObject(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        inputStream.close();


        String packageName = manifestJsonConfig.getString("package_name");

        JSONArray split_apks = manifestJsonConfig.getJSONArray("split_apks");
        for (int i = 0; i < split_apks.size(); i++) {
            JSONObject jsonObject = split_apks.getJSONObject(i);
            String id = jsonObject.getString("id");
            if ("base".equalsIgnoreCase(id)) {
                baseApkFileName = jsonObject.getString("file");
            }
            apkMaps.put(jsonObject.getString("id"), jsonObject.getString("file"));
        }

        if (StringUtils.isBlank(packageName)
                || StringUtils.isBlank(baseApkFileName)) {
            throw new IllegalStateException("broken xapk file,can not read package_name or base apk config");
        }
        File tempFile = File.createTempFile("ratel-xapk-", ".apk");
        tempFile.deleteOnExit();

        ZipEntry baseApkZipEntry = zipFile.getEntry(baseApkFileName);
        InputStream baseApkEntryInputStream = zipFile.getInputStream(baseApkZipEntry);
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        IOUtils.copy(baseApkEntryInputStream, fileOutputStream);
        baseApkEntryInputStream.close();
        fileOutputStream.close();

        releasedBaseApkTempFile = tempFile;


        JSONArray splitConfigs = manifestJsonConfig.getJSONArray("split_configs");
        for (int i = 0; i < splitConfigs.size(); i++) {
            String archConfig = splitConfigs.getString(i);
            String arch = xapkArchMapping.get(archConfig);
            if (arch != null) {
                supportArch.add(arch);
            }
        }
    }

    private static Map<String, String> xapkArchMapping = new HashMap<>();

    static {
        xapkArchMapping.put("config.arm64_v8a", "arm64-v8a");
        xapkArchMapping.put("config.armeabi_v7a", "armeabi-v7a");
    }

    void insertSo(Set<String> supportArch, File file) throws IOException, InterruptedException {
        for (String arch : supportArch) {
            File targetApk = new File(xapkWorking, apkMaps.get("config." + arch.replaceAll("-", "_")));
            if (!targetApk.exists()) {
                throw new IllegalStateException("the file :" + targetApk.getAbsolutePath() + " not exist");
            }

            File saveApk = File.createTempFile("xapk-temp", "apk");
            saveApk.deleteOnExit();

            //复制原apk内容
            ZipOutputStream zipOutputStream = new ZipOutputStream(saveApk);
            ZipFile targetZipFile = new ZipFile(targetApk);
            Enumeration<ZipEntry> entries = targetZipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.startsWith("META-INF/")) {
                    continue;
                }

                zipOutputStream.putNextEntry(new ZipEntry(zipEntry));
                InputStream inputStream = targetZipFile.getInputStream(zipEntry);
                IOUtils.copy(inputStream, zipOutputStream);
                inputStream.close();
            }
            targetZipFile.close();

            //置入新的apk内部的so文件
            ZipFile appendApkZipFile = new ZipFile(file);
            entries = appendApkZipFile.getEntries();
            String prefix = "lib/" + arch + "/";
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (!name.startsWith(prefix)) {
                    continue;
                }

                ZipEntry injectSoEntry = new ZipEntry(zipEntry);
                injectSoEntry.setMethod(ZipEntry.STORED);
                zipOutputStream.putNextEntry(injectSoEntry);
                InputStream inputStream = appendApkZipFile.getInputStream(zipEntry);
                IOUtils.copy(inputStream, zipOutputStream);
                inputStream.close();
            }
            appendApkZipFile.close();
            zipOutputStream.close();
            Files.move(saveApk.toPath(), targetApk.toPath(), StandardCopyOption.REPLACE_EXISTING);
            //操作完成后需要对齐
            HelperZipAndSign.zipalign(targetApk, Main.workDir());
        }
    }
}
/**
 * {
 * "icon":"icon.png",
 * "min_sdk_version":"21",
 * "name":"Bravonovel",
 * "package_name":"com.zhangyun.bravo",
 * "permissions":[
 * "android.permission.WAKE_LOCK",
 * "android.permission.INTERNET",
 * "android.permission.ACCESS_NETWORK_STATE",
 * "android.permission.ACCESS_WIFI_STATE",
 * "android.permission.READ_LOGS",
 * "android.permission.READ_EXTERNAL_STORAGE",
 * "android.permission.WRITE_EXTERNAL_STORAGE",
 * "android.permission.CAMERA",
 * "com.android.vending.BILLING",
 * "com.google.android.c2dm.permission.RECEIVE",
 * "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"],
 * "split_apks":[
 * {
 * "file":"com.zhangyun.bravo.apk",
 * "id":"base"
 * },
 * {
 * "file":"config.th.apk",
 * "id":"config.th"
 * },
 * {
 * "file":"config.in.apk",
 * "id":"config.in"
 * },
 * {
 * "file":"config.ar.apk",
 * "id":"config.ar"
 * },
 * {
 * "file":"config.tr.apk",
 * "id":"config.tr"
 * },
 * {
 * "file":"config.ja.apk",
 * "id":"config.ja"
 * },
 * {
 * "file":"config.es.apk",
 * "id":"config.es"
 * },
 * {
 * "file":"config.my.apk",
 * "id":"config.my"
 * },
 * {
 * "file":"config.en.apk",
 * "id":"config.en"
 * },
 * {
 * "file":"config.zh.apk",
 * "id":"config.zh"
 * },
 * {
 * "file":"config.arm64_v8a.apk",
 * "id":"config.arm64_v8a"
 * },
 * {
 * "file":"config.de.apk",
 * "id":"config.de"
 * },
 * {
 * "file":"config.fr.apk",
 * "id":"config.fr"
 * },
 * {
 * "file":"config.hi.apk",
 * "id":"config.hi"
 * },
 * {
 * "file":"config.vi.apk",
 * "id":"config.vi"
 * },
 * {
 * "file":"config.ko.apk",
 * "id":"config.ko"
 * },
 * {
 * "file":"config.it.apk",
 * "id":"config.it"
 * },
 * {
 * "file":"config.pt.apk",
 * "id":"config.pt"
 * },
 * {
 * "file":"config.ru.apk",
 * "id":"config.ru"
 * },
 * {
 * "file":"config.xxxhdpi.apk",
 * "id":"config.xxxhdpi"
 * }],
 * "split_configs":[
 * "config.th",
 * "config.in",
 * "config.ar",
 * "config.tr",
 * "config.ja",
 * "config.es",
 * "config.my",
 * "config.en",
 * "config.zh",
 * "config.arm64_v8a",
 * "config.de",
 * "config.fr",
 * "config.hi",
 * "config.vi",
 * "config.ko",
 * "config.it",
 * "config.pt",
 * "config.ru",
 * "config.xxxhdpi"],
 * "target_sdk_version":"28",
 * "total_size":51237647,
 * "version_code":"119",
 * "version_name":"1.1.9",
 * "xapk_version":2
 * }
 */