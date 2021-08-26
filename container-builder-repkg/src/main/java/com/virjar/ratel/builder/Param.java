package com.virjar.ratel.builder;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by virjar on 2018/10/13.
 */

public class Param {
    public File xposedApk;
    public File originApk;
    public ApkMeta originApkMeta;

    public Param(File xposedApk, File originApk, ApkMeta originApkMeta) {
        this.xposedApk = xposedApk;
        this.originApk = originApk;
        this.originApkMeta = originApkMeta;
    }

    public static Param parseAndCheck(String[] args) throws IOException {
        if (args.length == 2) {
            return handleEmbedModuleParam(args);
        }
        if (args.length == 1) {
            return handleExternalModuleParam(args);
        }

        throw new IllegalArgumentException("can not handle params:  {" + StringUtils.join(args, ",") + "}");

    }

    private static Param handleExternalModuleParam(String[] args) throws IOException {
        File apkFile = new File(args[0]);
        if (!checkAPKFile(apkFile)) {
            System.out.println(args[0] + " is not a illegal apk file");
            return null;
        }
        try (ApkFile apkFile1 = new ApkFile(apkFile)) {
//            byte[] xposedConfig = apkFile1.getFileData("assets/xposed_init");
//            if (xposedConfig != null) {
//                System.out.println("can not pass a xposed module apk!!");
//                return null;
//            }

            return new Param(null, apkFile, apkFile1.getApkMeta());
        }

    }

    private static Param handleEmbedModuleParam(String[] args) throws IOException {
        //检查输入参数
        File apk1 = new File(args[0]);
        File apk2 = new File(args[1]);
        if (!checkAPKFile(apk1)) {
            System.out.println(args[0] + " is not a illegal apk file");
            return null;
        }

        if (!checkAPKFile(apk2)) {
            System.out.println(args[1] + " is not a illegal apk file");
            return null;
        }

        //确定那个apk是原始apk，那个是xposed模块apk

        File xposedApk;
        File originApk;


        ApkFile apkFile1 = new ApkFile(apk1);
        byte[] xposedConfig = apkFile1.getFileData("assets/xposed_init");

        ApkFile apkFile2 = new ApkFile(apk2);
        byte[] xposedConfig2 = apkFile2.getFileData("assets/xposed_init");

        if (xposedConfig == null && xposedConfig2 == null) {
            System.out.println("两个文件必须有一个是xposed模块apk");
            return null;
        }
        if (xposedConfig != null && xposedConfig2 != null) {
            System.out.println("两个文件都是xposed模块apk");
            return null;
        }
        ApkMeta apkMeta;
        // byte[] ratelFlag = null;
        if (xposedConfig == null) {
            xposedApk = apk2;
            originApk = apk1;
            apkMeta = apkFile1.getApkMeta();
            //ratelFlag = apkFile1.getFileData("assets/ratel_serialNo.txt");
        } else {
            xposedApk = apk1;
            originApk = apk2;
            apkMeta = apkFile2.getApkMeta();
            //ratelFlag = apkFile2.getFileData("assets/ratel_serialNo.txt");
        }

//        if (ratelFlag != null) {
//            System.out.println("the apk" + originApk + " processed by ratel already!!");
//            return null;
//        }


        IOUtils.closeQuietly(apkFile1);
        IOUtils.closeQuietly(apkFile2);
        return new Param(xposedApk, originApk, apkMeta);
    }

    private static boolean checkAPKFile(File file) {
        try (ApkFile apkFile = new ApkFile(file)) {
            apkFile.getApkMeta();
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }
}
