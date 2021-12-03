package com.virjar.ratel.builder.ratelentry;

import com.virjar.ratel.allcommon.Constants;

import net.dongliu.apk.parser.ApkFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BuilderContextParser {
    public static BuilderContext parse(String[] args, boolean isXApk) throws ParseException, IOException {
        BuilderContext builderContext = new BuilderContext();
        // 命令行参数解析
        builderContext.cmd = cmdParse(args);

        resolveWorkDir(builderContext);

        // 一些特殊参数，
        List<String> leftParams = resolveSpecialParams(builderContext);

        // 平头哥就是一个改包工具，所以最后一定是处理apk文件
        resolveApkInfo(builderContext, leftParams);

        // 在最后做一次参数合法性检查
        check(builderContext);
        if (!builderContext.cmd.hasOption('h')) {
            resolveOutputFile(builderContext, isXApk);
        }

        return builderContext;
    }

    private static void resolveOutputFile(BuilderContext builderContext, boolean isXApk) throws IOException {
        //工作目录准备
        File outFile;
        if (builderContext.cmd.hasOption('D')) {
            outFile = File.createTempFile("ratel_decompile_temp", ".apk");
            outFile.deleteOnExit();
        } else if (builderContext.cmd.hasOption('o')) {
            outFile = new File(builderContext.cmd.getOptionValue('o'));
        } else {
            String base = builderContext.infectApk.apkMeta.getPackageName() + "_" + builderContext.infectApk.apkMeta.getVersionName() + "_" + builderContext.infectApk.apkMeta.getVersionCode();
            if (isXApk) {
                outFile = new File(base + "_ratel.xapk");
            } else {
                outFile = new File(base + "_ratel.apk");
            }
        }
        builderContext.outFile = outFile;
    }

    private static void check(BuilderContext builderContext) {
        if (builderContext.cmd.hasOption('h')) {
            return;
        }
        if (builderContext.infectApk == null) {
            if (builderContext.xpModuleApk != null) {
                throw new IllegalStateException("can not infect xposed module!!");
            }
            throw new IllegalStateException("no infect apk passed");
        }
    }

    private static void resolveApkInfo(BuilderContext builderContext, List<String> leftParams) {
        for (String apkFilePath : leftParams) {
            File file = new File(apkFilePath);
            if (!file.exists()) {
                throw new IllegalStateException("the apk file not exist: " + apkFilePath);
            }
            BuilderContext.ApkAsset apkAsset = parseApkFile(file);
            if (apkAsset == null) {
                throw new IllegalStateException("apk parse failed " + apkFilePath);
            }
            if (apkAsset.isXpModule) {
                if (builderContext.xpModuleApk != null) {
                    // 不能两个apk都是xp module
                    throw new IllegalStateException("duplicate module apk passed");
                }
                builderContext.xpModuleApk = apkAsset;
            } else if (builderContext.infectApk != null) {
                throw new IllegalStateException("duplicate  apk passed");
            } else {
                builderContext.infectApk = apkAsset;
            }
        }
    }


    public static BuilderContext.ApkAsset parseApkFile(File file) {
        try {
            ApkFile apkFile = new ApkFile(file);
            BuilderContext.ApkAsset apkAsset = new BuilderContext.ApkAsset();
            apkAsset.apkFile = apkFile;
            apkAsset.file = file;
            apkAsset.apkMeta = apkFile.getApkMeta();
            apkAsset.isXpModule = apkFile.getFileData("assets/xposed_init") != null;
            apkAsset.zipFile = apkFile.getZipFile();
            apkAsset.manifestXml = apkFile.getManifestXml();
            return apkAsset;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<String> resolveSpecialParams(BuilderContext builderContext) {
        List<String> reserved = new ArrayList<>();
        String AXML_EDITOR_CMD_PREFFIX = "ratel_axml_";
        for (String str : builderContext.cmd.getArgList()) {
            if (str.startsWith(Constants.ratelPropertiesSuffix)) {
                builderContext.propertiesConfig.add(str.substring(Constants.ratelPropertiesSuffix.length()));
            } else if (str.startsWith(AXML_EDITOR_CMD_PREFFIX)) {
                builderContext.axmlEditorCommand.add(str.substring(AXML_EDITOR_CMD_PREFFIX.length()));
            } else {
                reserved.add(str);
            }
        }
        return reserved;
    }

    private static void resolveWorkDir(BuilderContext builderContext) {
        if (builderContext.cmd.hasOption('w')) {
            builderContext.theWorkDir = new File(builderContext.cmd.getOptionValue('w'));
        } else {
            builderContext.theWorkDir = new File("ratel_work_dir");
        }
    }

    private static CommandLine cmdParse(String[] args) throws ParseException {
        Options options = setupOptions();
        DefaultParser parser = new DefaultParser();
        ArrayList<String> originParam = new ArrayList<>();
        for (String str : args) {
            // 不允许-Pxxx参数传递进来，目前我们认为这个可能是Gradle脚本传进来
            // 由于存在通过脚本debug模式进行改包，所以调试模式下可能存在-P参数
            if (!str.startsWith("-P") || str.length() <= 2) {
                originParam.add(str);
            }
        }
        return parser.parse(options, originParam.toArray(new String[]{}), false);
    }

    public static Options setupOptions() {
        Options options = new Options();
        options.addOption(new Option("t", "tell", false, "tell me the output apk file path"));
        options.addOption(new Option("o", "output", true, "the output apk path or output project path"));
        options.addOption(new Option("d", "debug", false, "add debuggable flag on androidManifest.xml "));
        options.addOption(new Option("w", "workdir", true, "set a ratel working dir"));
        options.addOption(new Option("p", "hookProvider", true, "no use , a deprecate flag,which ratel framework integrate sandhook"));
        options.addOption(new Option("e", "engine", true, "the ratel engine,now support appendDex|rebuildDex|shell"));
        options.addOption(new Option("h", "help", false, "print help message"));
        options.addOption(new Option("c", "certificate", true, "authorizer certificate file"));
        options.addOption(new Option("x", "extract", false, "extract origin apk from a ratel output apks"));
        options.addOption(new Option("l", "log", false, "add log component,we can call it from apktool baksmali"));
        options.addOption(new Option("s", "signature", false, "signature apk with ratel default KeyStore"));
        options.addOption(new Option("D", "decompile", false, "create a RDP(ratel decompile project) like \"apktool output project\"," +
                "this project only contains smali files and with capability to bypass signature check with ratel framework"));
        options.addOption(new Option("", "abi", true, "设置ABI，可以可以指定app运行在32位机器上，或者运行在64位机器上。可选值: armeabi-v7a/arm64-v8a"));
        return options;
    }
}
