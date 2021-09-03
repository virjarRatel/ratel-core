package com.virjar.ratel.builder.ratelentry;

import com.alibaba.fastjson.JSONObject;
import com.android.apksig.ApkSigner;
import com.android.apksig.apk.MinSdkVersionException;
import com.android.apksigner.PasswordRetriever;
import com.android.apksigner.SignerParams;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.allcommon.NewConstants;
import com.virjar.ratel.allcommon.StandardEncryptor;
import com.virjar.ratel.builder.AndroidJarUtil;
import com.virjar.ratel.builder.BootstrapCodeInjector;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.DexMakerOpt;
import com.virjar.ratel.builder.DexSplitter;
import com.virjar.ratel.builder.PackageTrie;
import com.virjar.ratel.builder.Param;
import com.virjar.ratel.builder.mode.RatelPackageBuilderAppendDex;
import com.virjar.ratel.builder.mode.RatelPackageBuilderRepackage;
import com.virjar.ratel.builder.mode.RatelPackageBuilderShell;
import com.virjar.ratel.builder.mode.RatelPackageBuilderZelda;
import com.virjar.ratel.builder.ReNameEntry;
import com.virjar.ratel.builder.Util;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.ApkParsers;
import net.dongliu.apk.parser.bean.DexClass;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.DexParser;
import net.dongliu.apk.parser.struct.AndroidConstants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 通过重打包的方案进行代码植入
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ClassNames.BUILDER_MAIN.check(Main.class);
        int xApkIndex = -1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].endsWith(".xapk")) {
                xApkIndex = i;
                break;
            }
        }
        if (xApkIndex <= 0) {
            ratelMain(args, null);
            return;
        }
        com.virjar.ratel.builder.ratelentry.XApkHandler xApkHandler = new com.virjar.ratel.builder.ratelentry.XApkHandler(new File(args[xApkIndex]));

        args[xApkIndex] = xApkHandler.releasedBaseApkTempFile.getAbsolutePath();
        File file = ratelMain(args, xApkHandler);
        if (file == null || !file.exists()) {
            //build失败的
            return;
        }
        xApkHandler.buildRatelInflectedSplitApk(file);
        Main.cleanWorkDir();
    }

    private static File ratelMain(String[] args, com.virjar.ratel.builder.ratelentry.XApkHandler xApkHandler) throws Exception {
        restoreConstants();
        final Options options = new Options();
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


        DefaultParser parser = new DefaultParser();


        //else if (str.startsWith("-P") && str.length() > 2) {
        //                System.out.println("skip gradle build param:{ " + str + " }");
        //            }

        ArrayList<String> originParam = new ArrayList<String>();

        for (String str : args) {
            if (str.startsWith("-P") && str.length() > 2) {
                //nothing
            } else {
                originParam.add(str);
            }
        }


        CommandLine cmd = parser.parse(options, originParam.toArray(new String[]{}), false);

        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("ratel", options);
            return null;
        }

        if (cmd.hasOption('w')) {
            theWorkDir = new File(cmd.getOptionValue('w'));
        }

        List<String> argList = cmd.getArgList();

        List<String> reserved = new ArrayList<>();
        List<String> propertiesConfig = new ArrayList<>();
        List<String> axmlEditorCommand = new ArrayList<>();
        String AXML_EDITOR_CMD_PREFFIX = "ratel_axml_";
        for (String str : argList) {
            if (str.startsWith(Constants.ratelPropertiesSuffix)) {
                propertiesConfig.add(str.substring(Constants.ratelPropertiesSuffix.length()));
            } else if (str.startsWith(AXML_EDITOR_CMD_PREFFIX)) {
                axmlEditorCommand.add(str.substring(AXML_EDITOR_CMD_PREFFIX.length()));
            } else {
                reserved.add(str);
            }
        }

        String[] reservedArgs = reserved.toArray(new String[0]);

        Param param = Param.parseAndCheck(reservedArgs);
        if (param == null) {
            //参数检查失败
            return null;
        }


        //工作目录准备
        File outFile;
        if (cmd.hasOption('D')) {
            outFile = File.createTempFile("ratel_decompile_temp", ".apk");
            outFile.deleteOnExit();
        } else if (cmd.hasOption('o')) {
            outFile = new File(cmd.getOptionValue('o'));
        } else {
            String base = param.originApkMeta.getPackageName() + "_" + param.originApkMeta.getVersionName() + "_" + param.originApkMeta.getVersionCode();
            if (xApkHandler != null) {
                outFile = new File(base + "_ratel.xapk");
            } else {
                outFile = new File(base + "_ratel.apk");
            }
        }


        if (cmd.hasOption('t')) {
            System.out.println(outFile.getAbsolutePath());
            return null;
        }

        if (cmd.hasOption('x')) {
            extractOriginAPk(param, outFile);
            return null;
        }

        File rawOriginApk = param.originApk;
        boolean hasRatelWrapper = extractOriginApkFromRatelApkIfNeed(param);

        //解读Meta信息，获取preferHookProviderKey
        Properties ratelBuildProperties = parseManifestMetaData(param.xposedApk);

        //reload properties if has additional params
        if (propertiesConfig.size() > 0) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            for (String config : propertiesConfig) {
                bufferedWriter.write(config);
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            outputStreamWriter.close();
            byteArrayOutputStream.close();

            ratelBuildProperties.load(byteArrayOutputStream.toInputStream());
        }

        if (cmd.hasOption('h')) {
            ratelBuildProperties.setProperty(Constants.preferHookProviderKey, StringUtils.trimToEmpty(cmd.getOptionValue('h')));
        }

        System.out.println("ratel build param: " + Joiner.on(" ").join(args));

        File workDir = cleanWorkDir();

        if (xApkHandler != null) {
            System.out.println("release split apks");
            xApkHandler.releaseApks();
        }


        System.out.println("clean working directory:" + workDir.getAbsolutePath());

        BindingResourceManager.extract(workDir);

        File signatureKeyFile = new File(workDir, Constants.ratelDefaultApkSignatureKey);
        System.out.println("release ratel default apk signature key into : " + signatureKeyFile.getAbsolutePath());
        copyAndClose(Main.class.getClassLoader().getResourceAsStream(Constants.ratelDefaultApkSignatureKey), new FileOutputStream(signatureKeyFile));

        //load engine properties from engine build output config
        ratelBuildProperties.load(Main.class.getClassLoader().getResourceAsStream(Constants.ratelEngineProperties));

        Util.setupRatelSupportArch(ratelBuildProperties.getProperty("ratel_support_abis", "arm64-v8a,armeabi-v7a"));

        //inject bootstrap code into origin apk
        BuildParamMeta buildParamMeta = parseManifest(param.originApk);
        buildParamMeta.axmlEditorCommand = axmlEditorCommand;

        Set<String> supportArch;
        if (xApkHandler != null) {
            // xapk 模式下，首先解码外部的apk，外部为空，可以尝试内部apk
            supportArch = xApkHandler.originAPKSupportArch();
            if (supportArch.isEmpty()) {
                supportArch = originAPKSupportArch(param.originApk);
            }
        } else {
            supportArch = originAPKSupportArch(param.originApk);
        }

        String engine = Constants.RATEL_ENGINE_ENUM_REBUILD;
        if (cmd.hasOption('e')) {
            engine = cmd.getOptionValue('e');
        } else {
            //auto
            if (buildParamMeta.originApplicationClass == null) {
                //没有 application的时候，只能使用重编译dex入口的方案
                engine = Constants.RATEL_ENGINE_ENUM_REBUILD;
            }
            //TODO 如果是乐固，目前只能使用 apppend模式
        }
        System.out.println("use ratel engine: " + engine);

        //add build serial number
        String serialNo = Constants.ratelPreffix + UUID.randomUUID().toString();
        System.out.println("build serialNo: " + serialNo);
        buildParamMeta.serialNo = serialNo;
        buildParamMeta.buildTimestamp = String.valueOf(System.currentTimeMillis());
        ratelBuildProperties.setProperty(Constants.serialNoKey, serialNo);
        ratelBuildProperties.setProperty(Constants.buildTimestampKey, buildParamMeta.buildTimestamp);

        if (StringUtils.isNotBlank(buildParamMeta.androidAppComponentFactory)) {
            //暂时把这个带过来
            ratelBuildProperties.setProperty(Constants.android_AppComponentFactoryKey, buildParamMeta.androidAppComponentFactory);
        }

        buildParamMeta.cmd = cmd;
        buildParamMeta.apkMeta = param.originApkMeta;
        buildParamMeta.ratelBuildProperties = ratelBuildProperties;

        //create new apk file
        ZipOutputStream zos = new ZipOutputStream(outFile);
        zos.setEncoding("UTF8");

        switch (engine) {
            case Constants.RATEL_ENGINE_ENUM_APPENDEX:
                RatelPackageBuilderAppendDex.handleTask(workDir, param, buildParamMeta, cmd, zos);
                break;
            case Constants.RATEL_ENGINE_ENUM_REBUILD:
                if (buildParamMeta.appEntryClassDex == null) {
                    //其他模式下，可以不存在 entry application
                    throw new RuntimeException("unsupported apk , no entry point application or entry point launch activity found");
                }
                RatelPackageBuilderRepackage.handleTask(workDir, param, buildParamMeta, ratelBuildProperties, zos, cmd);
                break;
            case Constants.RATEL_ENGINE_ENUM_SHELL:
                RatelPackageBuilderShell.handleTask(workDir, param, buildParamMeta, cmd, zos);
                break;
            case Constants.RATEL_ENGINE_ENUM_ZELDA:
                RatelPackageBuilderZelda.handleTask(workDir, param, buildParamMeta, cmd, zos);
                break;
            default:
                throw new IllegalStateException("unknown ratel build engine type: " + engine);
        }


        // now insert apk into assets directory
        Util.copyAssets(zos, param.originApk, Constants.originAPKFileName);
        if (param.xposedApk != null) {
            Util.copyAssets(zos, param.xposedApk, Constants.xposedBridgeApkFileName);
        }

        Util.copyAssets(zos, BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.XPOSED_BRIDGE_JAR_FILE), Constants.xposedApiBridgeJarFileName);
        //请注意，shell模式下，不需要copy driver的资源

        ratelBuildProperties.setProperty(Constants.hasEmbedXposedModuleKey, String.valueOf(param.xposedApk != null));
        if (param.xposedApk != null) {
            ApkFile xposedApkFile = new ApkFile(param.xposedApk);
            ratelBuildProperties.setProperty(Constants.xposedModuleApkPackageNameKey, xposedApkFile.getApkMeta().getPackageName());
        }

        ratelBuildProperties.setProperty("supportArch", Joiner.on(",").join(supportArch));
        // if (xApkHandler == null || xApkHandler.originAPKSupportArch().isEmpty()) {
        // 普通模式，以及xapkso在主包的情况。so需要copy到主包
        // now copy native library
        if (param.xposedApk != null) {
            Util.copyLibrary(zos, supportArch, param.xposedApk);
        }
        Util.copyLibrary(zos, supportArch, BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.RUNTIME_JAR_FILE));
//        } else {
//            // xapk模式下，so不能植入到主包,而是置入到分包
//            if (param.xposedApk != null) {
//                xApkHandler.insertSo(supportArch, param.xposedApk);
//            }
//            xApkHandler.insertSo(supportArch, new File(workDir, Constants.RATEL_ENGINE_APK));
//            //Util.copyLibrary(zos, supportArch, new File(workDir, Constants.RATEL_ENGINE_APK));
//        }

        //so文件需要插入到asset目录，因为 execve 注入会依赖so，且依赖多个版本
        insertRatelNativeLib(zos, BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.RUNTIME_JAR_FILE));

        //add dexmaker opt file if exist
        File dexMakerOptFile = DexMakerOpt.genDexMakerOptFile(workDir());
        if (dexMakerOptFile != null && dexMakerOptFile.exists()) {
            Util.copyAssets(zos, dexMakerOptFile, Constants.dexmakerOptFileName);
        }


        //add build serial number
        System.out.println("build serialNo: " + buildParamMeta.serialNo);
        zos.putNextEntry(new ZipEntry("assets/" + Constants.serialNoFile));
        zos.write(buildParamMeta.serialNo.getBytes());


        //append certificate file
        String ratelCertificate = Constants.debugCertificate;
        if (cmd.hasOption('c')) {
            String certificateContent = cmd.getOptionValue('c');
            //first think as a file
            File file = new File(certificateContent);
            if (file.exists() && file.canRead()) {
                System.out.println("use certificate file: " + file.getAbsolutePath());
                certificateContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                if (certificateContent != null) {
                    certificateContent = certificateContent.trim();
                }
            }
            try {
                certificateContent = certificateContent.trim();
                byte[] bytes = StandardEncryptor.standardRSADecrypt(certificateContent);
                JSONObject parse = JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8));
                certificateContent = parse.getString("payload");

                //setup certificate information
                ratelBuildProperties.setProperty("ratel_certificate_id", parse.getString("licenceId"));
                ratelBuildProperties.setProperty("ratel_certificate_version", parse.getString("licenceVersion"));
                ratelBuildProperties.setProperty("ratel_certificate_expire", String.valueOf(parse.getLongValue("expire")));
                ratelBuildProperties.setProperty("ratel_certificate_account", parse.getString("account"));
            } catch (Exception e) {
                //ignore
                e.printStackTrace();
            }

            ratelCertificate = certificateContent;
        }
        if (ratelCertificate == null) {
            ratelCertificate = Constants.debugCertificate;
        }
        zos.putNextEntry(new ZipEntry("assets/" + Constants.ratelCertificate));
        zos.write(ratelCertificate.getBytes(StandardCharsets.UTF_8));


        zos.putNextEntry(new ZipEntry("assets/" + Constants.ratelConfigFileName));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ratelBuildProperties.store(byteArrayOutputStream, "auto generated by ratel repakcage builder");
        zos.write(byteArrayOutputStream.toByteArray());


        zos.close();
        // FileUtils.deleteDirectory(workDir);
        System.out.println("the new apk file ：" + outFile.getAbsolutePath());

        if (hasRatelWrapper) {
            // if this is a rebuilded apk,we can insert addson resource (such as dex/assets) with RDP|APKTool
            // we need merge this resource into the final apk
            mergeAddsOnFiles(rawOriginApk, param.originApk, outFile);
        }

        if (cmd.hasOption('s') && !cmd.hasOption('D')) {
            //do not need signature apk if create a decompile project
            try {
                if (useV1Sign(buildParamMeta)) {
                    //7.0之前，走V1签名，所以要先签名再对齐
                    signatureApk(outFile, signatureKeyFile, buildParamMeta);
                    zipalign(outFile, workDir);
                } else {
                    zipalign(outFile, workDir);
                    signatureApk(outFile, signatureKeyFile, buildParamMeta);
                }
            } catch (Exception e) {
                // we need remove final output apk if sign failed,because of out shell think a illegal apk format file as task success flag,
                // android system think signed apk as really right apk,we can not install this apk on the android cell phone if rebuild success but sign failed
                FileUtils.forceDelete(outFile);
                throw e;
            }
        }


        ratelBuildProperties.store(new FileOutputStream(new File(outFile.getParentFile(), "ratelConfig.properties")), "auto generated by virjar@ratel");

        if (cmd.hasOption('D')) {
            createRatelDecompileProject(outFile, param, cmd, ratelBuildProperties);
        }

        if (xApkHandler == null) {
            System.out.println("clean working directory..");
            FileUtils.deleteDirectory(workDir);
        }
        return outFile;
    }

    private static void insertRatelNativeLib(ZipOutputStream zos, File runtimeApkFile) throws IOException {
        try (ZipFile zipFile = new ZipFile(runtimeApkFile)) {
            System.out.println("path: " + "lib/armeabi-v7a/lib" + Constants.RATEL_NATIVE_LIB_NAME + ".so");
            ZipEntry armeabiEntry = zipFile.getEntry("lib/armeabi-v7a/lib" + Constants.RATEL_NATIVE_LIB_NAME + ".so");
            zos.putNextEntry(new ZipEntry("assets/" + Constants.RATEL_NATIVE_LIB_32));
            InputStream inputStream = zipFile.getInputStream(armeabiEntry);
            IOUtils.copy(inputStream, zos);
            IOUtils.closeQuietly(inputStream);

            ZipEntry arm64Entry = zipFile.getEntry("lib/arm64-v8a/lib" + Constants.RATEL_NATIVE_LIB_NAME + ".so");
            zos.putNextEntry(new ZipEntry("assets/" + Constants.RATEL_NATIVE_LIB_64));
            inputStream = zipFile.getInputStream(arm64Entry);
            IOUtils.copy(inputStream, zos);
            IOUtils.closeQuietly(inputStream);

        }
    }

    private static void mergeAddsOnFiles(File rawOriginApk, File nowOriginApk, File outputApk) throws IOException {
        ZipFile rawOriginApkZip = new ZipFile(rawOriginApk);
        ZipFile nowOriginApkZip = new ZipFile(nowOriginApk);
        ZipFile outputApkZip = new ZipFile(outputApk);

        File tmpOutputApk = File.createTempFile("mergeAddsOn", ".apk");
        tmpOutputApk.deleteOnExit();
        ZipOutputStream mergeAddsOnZipOutputStream = new ZipOutputStream(tmpOutputApk);

        try {
            // first calc diff for rawOriginApk and nowOriginApk
            Set<String> addsOnAndRatel = new HashSet<>();
            Enumeration<ZipEntry> entries = rawOriginApkZip.getEntries();
            while (entries.hasMoreElements()) {
                addsOnAndRatel.add(entries.nextElement().getName());
            }

            entries = nowOriginApkZip.getEntries();
            while (entries.hasMoreElements()) {
                addsOnAndRatel.remove(entries.nextElement().getName());
            }

            Set<String> definedClasses = new HashSet<>();

            int maxIndex = 1;


            // calc only addsOn
            entries = outputApkZip.getEntries();
            while (entries.hasMoreElements()) {
                //remove ratel file
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                Matcher matcher = Util.classesIndexPattern.matcher(name);
                if (!matcher.matches() && !"classes.dex".equals(name)) {
                    addsOnAndRatel.remove(name);
                } else {
                    byte[] bytes = IOUtils.toByteArray(outputApkZip.getInputStream(zipEntry));
                    // 分析重复class
                    DexBackedDexFile dexBackedDexFile = new DexBackedDexFile(Opcodes.getDefault(), bytes);
                    Set<? extends DexBackedClassDef> allClasses = dexBackedDexFile.getClasses();
                    for (DexBackedClassDef dexBackedClassDef : allClasses) {
                        definedClasses.add(dexBackedClassDef.getType());
                    }
                    if (matcher.matches()) {
                        int nowIndex = NumberUtils.toInt(matcher.group(1), 0);
                        if (nowIndex > maxIndex) {
                            maxIndex = nowIndex;
                        }
                    }
                }
                mergeAddsOnZipOutputStream.putNextEntry(zipEntry);
                IOUtils.copy(outputApkZip.getInputStream(zipEntry), mergeAddsOnZipOutputStream);
            }

            Set<String> addsOn = addsOnAndRatel;

            Set<String> needRemoveAddsOnDes = new HashSet<>();

            // dex 需要单独filter
            for (String entry : addsOn) {
                if (Util.classesIndexPattern.matcher(entry).matches()) {
                    ZipEntry zipEntry = rawOriginApkZip.getEntry(entry);
                    byte[] bytes = IOUtils.toByteArray(rawOriginApkZip.getInputStream(zipEntry));
                    DexBackedDexFile dexBackedDexFile = new DexBackedDexFile(Opcodes.getDefault(), bytes);
                    int duplicate = 0;
                    boolean isRatelSplitDex = false;
                    Set<? extends DexBackedClassDef> allClasses = dexBackedDexFile.getClasses();
                    for (DexBackedClassDef dexBackedClassDef : allClasses) {
                        if (DexSplitter.isRatelSplitDex(dexBackedClassDef.getType())) {
                            isRatelSplitDex = true;
                            break;
                        }
                        if (definedClasses.contains(dexBackedClassDef.getType())) {
                            duplicate++;
                        }
                    }
                    if (isRatelSplitDex || duplicate >= allClasses.size() / 5) {
                        needRemoveAddsOnDes.add(entry);
                    }
                }
                if (entry.startsWith("META-INF/")) {
                    //we need remove signature info
                    needRemoveAddsOnDes.add(entry);
                }
            }
            addsOn.removeAll(needRemoveAddsOnDes);

            maxIndex++;

            for (String entry : addsOn) {
                System.out.println("addsOn resource: " + entry);
                ZipEntry zipEntry = rawOriginApkZip.getEntry(entry);
                if (Util.classesIndexPattern.matcher(entry).matches()) {
                    mergeAddsOnZipOutputStream.putNextEntry(new ReNameEntry(zipEntry, "classes" + maxIndex + ".dex"));
                    maxIndex++;
                } else {
                    mergeAddsOnZipOutputStream.putNextEntry(zipEntry);
                }
                IOUtils.copy(rawOriginApkZip.getInputStream(zipEntry), mergeAddsOnZipOutputStream);
            }


            mergeAddsOnZipOutputStream.closeEntry();
            mergeAddsOnZipOutputStream.close();
        } finally {
            IOUtils.closeQuietly(rawOriginApkZip);
            IOUtils.closeQuietly(nowOriginApkZip);
            IOUtils.closeQuietly(outputApkZip);
        }

        Files.move(
                tmpOutputApk.toPath(), outputApk.toPath(), StandardCopyOption.REPLACE_EXISTING);

    }

    private final static String SMALI_DIRNAME = "smali";

    private static void createRatelDecompileProject(File outputApkFile, Param param, CommandLine cmd, Properties ratelBuildProperties) throws IOException {
        //1. first calculate an output project directory
        File outProject;
        if (cmd.hasOption('o')) {
            outProject = new File(cmd.getOptionValue('o'));
        } else {
            outProject = new File(
                    param.originApkMeta.getPackageName() + "_" + param.originApkMeta.getVersionName() + "_" + param.originApkMeta.getVersionCode() + "_ratel_decompile");
        }

        System.out.println("output project is:" + outProject.getAbsolutePath());

        FileUtils.deleteDirectory(outProject);
        outProject.mkdirs();

        File rawResource = new File(outProject, "raws");


        //2. then decompile all *.dex file like apktool
        try (ZipFile zipFile = new ZipFile(outputApkFile)) {
            Enumeration<ZipEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                if (entryName.endsWith(".dex") && entryName.startsWith("classes")) {
                    //decompile dex file,and skip asset dex files,this is a bad case to decompile assets/*.dex for apktool
                    File smaliDir;
                    if (entryName.equalsIgnoreCase("classes.dex")) {
                        smaliDir = new File(outProject, SMALI_DIRNAME);
                    } else {
                        smaliDir = new File(outProject, SMALI_DIRNAME + "_" + entryName.substring(0, entryName.indexOf(".")));
                    }
                    FileUtils.deleteDirectory(smaliDir);
                    smaliDir.mkdirs();
                    System.out.println("Baksmaling " + entryName + "...");
                    DexBackedDexFile dexFile = DexFileFactory.loadDexEntry(outputApkFile,
                            entryName, true, Opcodes.getDefault()
                    ).getDexFile();
                    BootstrapCodeInjector.baksmali(null, dexFile, smaliDir);
                } else {
                    File destResourceFile = new File(rawResource, entryName);
                    FileUtils.forceMkdirParent(destResourceFile);
                    copyAndClose(zipFile.getInputStream(zipEntry), new FileOutputStream(destResourceFile));
                }
            }
        }

        //3. add rebuild script int the decompile project,ratel decompile project can be migrate to other computer event through there is no ratel environment
        File ratelConfigDir = new File(outProject, "ratel_resource");
        ratelConfigDir.mkdirs();

        ratelBuildProperties.setProperty("app.packageName", param.originApkMeta.getPackageName());
        ratelBuildProperties.setProperty("app.versionName", param.originApkMeta.getVersionName());
        ratelBuildProperties.setProperty("app.versionCode", String.valueOf(param.originApkMeta.getVersionCode()));
        ratelBuildProperties.setProperty("app.appName", param.originApkMeta.getName());

        ratelBuildProperties.store(new FileOutputStream(new File(ratelConfigDir, "ratelConfig.properties")), "auto generated by virjar@ratel");

        copyAndClose(Main.class.getClassLoader().getResourceAsStream(Constants.RDP_BIN_JAR_NAME), new FileOutputStream(new File(ratelConfigDir, "RDP-1.0.jar")));

        File rdpShellFile = new File(ratelConfigDir, "rdp.sh");
        FileUtils.write(rdpShellFile, "#!/usr/bin/env bash\n" +
                "\n# this bash shell auto generated  by RDP(ratel decompile project) component, do not edit this file" +
                "\n# powered by virjar@RatelGroup" +
                "\n" +
                "now_dir=`pwd`\n" +
                "cd `dirname $0`\n" +
                "script_dir=`pwd`\n" +
                "cd ..\n" +
                "\n" +
                "rdp_jar=${script_dir}/RDP-1.0.jar\n" +
                "\n" +
                "if [ -f ${rdp_jar} ] ;then\n" +
                "    echo \"use ${rdp_jar}\"\n" +
                "else\n" +
                "    echo \"can not find rdp jar in path:${rdp_jar}\"\n" +
                "    echo -1\n" +
                "fi\n" +
                "\n" +
                "cd ${now_dir}\n" +
                "java -jar ${rdp_jar}  $*\n", StandardCharsets.UTF_8);
        rdpShellFile.setExecutable(true);

        File gitIgnoreFile = new File(outProject, ".gitignore");
        FileUtils.writeStringToFile(gitIgnoreFile,
                ".idea/\n" +
                        "out/\n", StandardCharsets.UTF_8);
    }

    public static void zipalign(File outApk, File theWorkDir) throws IOException, InterruptedException {
        System.out.println("zip align output apk: " + outApk);
        //use different executed binary file with certain OS platforms
        String osName = System.getProperty("os.name").toLowerCase();
        String zipalignBinPath;
        boolean isLinux = false;
        if (osName.startsWith("Mac OS".toLowerCase())) {
            zipalignBinPath = "zipalign/mac/zipalign";
        } else if (osName.startsWith("Windows".toLowerCase())) {
            zipalignBinPath = "zipalign/windows/zipalign.exe";
        } else {
            zipalignBinPath = "zipalign/linux/zipalign";
            isLinux = true;
        }
        File unzipDestFile = new File(theWorkDir, zipalignBinPath);
        unzipDestFile.getParentFile().mkdirs();
        copyAndClose(Main.class.getClassLoader().getResourceAsStream(zipalignBinPath), new FileOutputStream(unzipDestFile));
        if (isLinux) {
            String libCPlusPlusPath = "zipalign/linux/lib64/libc++.so";
            File libCPlusPlusFile = new File(theWorkDir, libCPlusPlusPath);
            libCPlusPlusFile.getParentFile().mkdirs();
            copyAndClose(Main.class.getClassLoader().getResourceAsStream(libCPlusPlusPath), new FileOutputStream(libCPlusPlusFile));
        }

        unzipDestFile.setExecutable(true);

        File tmpOutputApk = File.createTempFile("zipalign", ".apk");
        tmpOutputApk.deleteOnExit();

        String command = unzipDestFile.getAbsolutePath() + " -f -p  4 " + outApk.getAbsolutePath() + " " + tmpOutputApk.getAbsolutePath();
        System.out.println("zip align apk with command: " + command);

        String[] envp = new String[]{"LANG=zh_CN.UTF-8", "LANGUAGE=zh_CN.UTF-8"};
        Process process = Runtime.getRuntime().exec(command, envp, null);
        autoFillBuildLog(process.getInputStream(), "zipalign-stand");
        autoFillBuildLog(process.getErrorStream(), "zipalign-error");

        process.waitFor();

        Files.move(
                tmpOutputApk.toPath(), outApk.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println(outApk.getAbsolutePath() + " has been zipalign ");
    }

    private static void autoFillBuildLog(InputStream inputStream, String type) {
        new Thread("read-" + type) {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(type + " : " + line);
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static void signatureApk(File outApk, File keyStoreFile, BuildParamMeta buildParamMeta) throws Exception {
        System.out.println("auto sign apk with ratel KeyStore");
        File tmpOutputApk = File.createTempFile("apksigner", ".apk");
        tmpOutputApk.deleteOnExit();

        SignerParams signerParams = new SignerParams();
        signerParams.setKeystoreFile(keyStoreFile.getAbsolutePath());
        signerParams.setKeystoreKeyAlias("hermes");
        signerParams.setKeystorePasswordSpec("pass:hermes");
        //signerParams.setKeyPasswordSpec("hermes");


        signerParams.setName("ratelSign");
        PasswordRetriever passwordRetriever = new PasswordRetriever();
        signerParams.loadPrivateKeyAndCerts(passwordRetriever);

        String v1SigBasename;
        if (signerParams.getV1SigFileBasename() != null) {
            v1SigBasename = signerParams.getV1SigFileBasename();
        } else if (signerParams.getKeystoreKeyAlias() != null) {
            v1SigBasename = signerParams.getKeystoreKeyAlias();
        } else if (signerParams.getKeyFile() != null) {
            String keyFileName = new File(signerParams.getKeyFile()).getName();
            int delimiterIndex = keyFileName.indexOf('.');
            if (delimiterIndex == -1) {
                v1SigBasename = keyFileName;
            } else {
                v1SigBasename = keyFileName.substring(0, delimiterIndex);
            }
        } else {
            throw new RuntimeException(
                    "Neither KeyStore key alias nor private key file available");
        }

        ApkSigner.SignerConfig signerConfig =
                new ApkSigner.SignerConfig.Builder(
                        v1SigBasename, signerParams.getPrivateKey(), signerParams.getCerts())
                        .build();

        ApkSigner.Builder apkSignerBuilder =
                new ApkSigner.Builder(Lists.newArrayList(signerConfig))
                        .setInputApk(outApk)
                        .setOutputApk(tmpOutputApk)
                        .setOtherSignersSignaturesPreserved(false)
                        //这个需要设置为true，否则debug版本的apk无法签名
                        .setDebuggableApkPermitted(true);
        if (useV1Sign(buildParamMeta)) {
            //低版本需要支持V1签名，Android7.0之后，Android系统才切换为V2签名
            apkSignerBuilder.setV1SigningEnabled(true);
            apkSignerBuilder.setV2SigningEnabled(false);
            apkSignerBuilder.setV3SigningEnabled(false);
            System.out.println("[signApk] use v1 sign");
        } else {
            apkSignerBuilder.setV1SigningEnabled(false)
                    .setV2SigningEnabled(true)
                    .setV3SigningEnabled(true);
            System.out.println("[signApk] use v2&v3 sign");
        }
        // .setSigningCertificateLineage(lineage);
        ApkSigner apkSigner = apkSignerBuilder.build();
        try {
            apkSigner.sign();
        } catch (MinSdkVersionException e) {
            System.out.println("Failed to determine APK's minimum supported platform version,use default skd api level 21");
            apkSigner = apkSignerBuilder.setMinSdkVersion(21).build();
            apkSigner.sign();
        }

        Files.move(
                tmpOutputApk.toPath(), outApk.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println(outApk.getAbsolutePath() + " has been Signed");
    }

    private static boolean useV1Sign(BuildParamMeta buildParamMeta) {
        // targetSdkVersion为安卓11的必须使用v2签名
        // 安卓7之前使用v1签名
        return buildParamMeta != null && NumberUtils.toInt(buildParamMeta.apkMeta.getMinSdkVersion(), 24) <= 23 && NumberUtils.toInt(buildParamMeta.apkMeta.getTargetSdkVersion()) < 30;
    }

    private static boolean extractOriginApkFromRatelApkIfNeed(Param param) throws IOException {
        if (param.originApk == null) {
            return false;
        }
        try (ZipFile zipFile = new ZipFile(param.originApk)) {
            ZipEntry zipEntry = zipFile.getEntry("assets/ratel_serialNo.txt");
            if (zipEntry == null) {
                //就是一个普通的apk
                return false;
            }

            File tempFile = File.createTempFile(param.originApk.getName(), ".apk");
            System.out.println("this is a ratel repkg apk file, extract origin into : " + tempFile.getAbsolutePath() + " and process it");
            FileUtils.forceDeleteOnExit(tempFile);

            ZipEntry originApkEntry = zipFile.getEntry("assets/" + Constants.originAPKFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

            IOUtils.copy(zipFile.getInputStream(originApkEntry), fileOutputStream);
            fileOutputStream.close();
            param.originApk = tempFile;
            return true;
        }
    }


    private static Set<String> originAPKSupportArch(File originAPK) throws IOException {
        Set<String> ret = Sets.newHashSet();
        //ret.add("armeabi");
        //ret.add("armeabi-v7a");

        ZipFile zipFile = new ZipFile(originAPK);
        Enumeration<ZipEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().startsWith("lib/")) {
                List<String> pathSegment = Splitter.on("/").splitToList(zipEntry.getName());
                ret.add(pathSegment.get(1));
            }

        }
        zipFile.close();

        return ret;
    }


    //解析MainfestMetaData的数据
    private static Properties parseManifestMetaData(File xposedApk) throws IOException, ParserConfigurationException, SAXException {
        Properties metaDataProperties = new Properties();
        if (xposedApk == null) {
            return metaDataProperties;
        }

        String originAPKManifestXml = ApkParsers.getManifestXml(xposedApk);
        Document document = loadDocument(new ByteArrayInputStream(originAPKManifestXml.getBytes()));
        Element applicationElement = (Element) document.getElementsByTagName("application").item(0);

        NodeList applicationChildNodes = applicationElement.getChildNodes();

        for (int i = 0; i < applicationChildNodes.getLength(); i++) {
            Node node = applicationChildNodes.item(i);
            if (!(node instanceof Element)) {
                continue;
            }

            Element element = (Element) node;

            if ("meta-data".equals(element.getTagName())) {
                String key = element.getAttribute("android:name");
                String value = element.getAttribute("android:value");
                metaDataProperties.setProperty(key, value);
            }
        }
        return metaDataProperties;
    }


    private static BuildParamMeta parseManifest(File originApk) throws ParserConfigurationException, SAXException, IOException {

        String originAPKManifestXml = ApkParsers.getManifestXml(originApk);
        //
        Document document = loadDocument(new ByteArrayInputStream(originAPKManifestXml.getBytes(StandardCharsets.UTF_8)));

        BuildParamMeta buildParamMeta = new BuildParamMeta();

        buildParamMeta.androidManifestXml = document;

        Element manifestElement = (Element) document.getElementsByTagName("manifest").item(0);
        buildParamMeta.packageName = manifestElement.getAttribute("package");

        Element applicationElement = (Element) document.getElementsByTagName("application").item(0);
        buildParamMeta.appEntryClass = applicationElement.getAttribute("android:name");
        if (StringUtils.isBlank(buildParamMeta.appEntryClass)) {
            buildParamMeta.appEntryClass = applicationElement.getAttribute("name");
        }
        if (StringUtils.startsWith(buildParamMeta.appEntryClass, ".")) {
            buildParamMeta.appEntryClass = buildParamMeta.packageName + buildParamMeta.appEntryClass;
        }
        if ("android.app.Application".equals(buildParamMeta.appEntryClass)) {
            //reset if use android default application class
            buildParamMeta.appEntryClass = null;
        }

        buildParamMeta.androidAppComponentFactory = applicationElement.getAttribute("android:appComponentFactory");
        buildParamMeta.originApplicationClass = buildParamMeta.appEntryClass;
        buildParamMeta.launcherActivityClass = findLaunchActivityClass(applicationElement.getElementsByTagName("activity"));
        if (StringUtils.startsWith(buildParamMeta.launcherActivityClass, ".")) {
            buildParamMeta.launcherActivityClass = buildParamMeta.packageName + buildParamMeta.launcherActivityClass;
        }

        if (StringUtils.isBlank(buildParamMeta.appEntryClass)) {
            buildParamMeta.appEntryClass = buildParamMeta.launcherActivityClass;
        }

        ApkFile apkFile = new ApkFile(originApk);
        buildParamMeta.dexClassesMap = parseDexClasses(apkFile);
        buildParamMeta.appEntryClassDex = queryDexEntry(buildParamMeta.dexClassesMap, buildParamMeta.appEntryClass);

        if (buildParamMeta.appEntryClassDex == null && !StringUtils.contains(buildParamMeta.appEntryClass, ".")) {
            buildParamMeta.appEntryClassDex = queryDexEntry(buildParamMeta.dexClassesMap, buildParamMeta.packageName + "." + buildParamMeta.appEntryClass);
            if (buildParamMeta.appEntryClassDex != null) {
                buildParamMeta.appEntryClass = buildParamMeta.packageName + "." + buildParamMeta.appEntryClass;
            }
        }

        return buildParamMeta;
    }

    private static String queryDexEntry(Map<String, DexClass[]> dexClassesMap, String targetClassName) {
        if (StringUtils.isBlank(targetClassName)) {
            return null;
        }
        for (Map.Entry<String, DexClass[]> entry : dexClassesMap.entrySet()) {
            String dexPath = entry.getKey();
            DexClass[] classes = entry.getValue();
            for (DexClass dexClass : classes) {
                String className = Util.descriptorToDot(dexClass.getClassType());
                if (className.equals(targetClassName)) {
                    return dexPath;
                }
            }
        }
        return null;
    }


    private static Map<String, DexClass[]> parseDexClasses(ApkFile apkFile) throws IOException {
        Map<String, DexClass[]> ret = Maps.newHashMap();

        byte[] firstDex = apkFile.getFileData(AndroidConstants.DEX_FILE);
        if (firstDex == null) {
            String msg = String.format("Dex file %s not found", AndroidConstants.DEX_FILE);
            throw new ParserException(msg);
        }
        ByteBuffer buffer = ByteBuffer.wrap(firstDex);
        DexParser dexParser = new DexParser(buffer);
        DexClass[] parse = dexParser.parse();
        ret.put(AndroidConstants.DEX_FILE, parse);


        for (int i = 2; i < 1000; i++) {
            try {
                String path = String.format(Locale.ENGLISH, AndroidConstants.DEX_ADDITIONAL, i);
                byte[] fileData = apkFile.getFileData(path);
                if (fileData == null) {
                    break;
                }
                buffer = ByteBuffer.wrap(fileData);
                dexParser = new DexParser(buffer);
                parse = dexParser.parse();
                ret.put(path, parse);
            } catch (Exception e) {
                //ignore because of entry point class can not be encrypted
            }
        }
        return ret;
    }

    private static String findLaunchActivityClass(NodeList activityNodeList) {
        //find launcher activity
        //String categoryInfoActivity = null;
        String categoryLauncherActivity = null;

        for (int i = 0; i < activityNodeList.getLength(); i++) {
            Node item = activityNodeList.item(i);
            if (!(item instanceof Element)) {
                continue;
            }
            Element activityElement = (Element) item;
            //intent filter 可能有多个
            NodeList intentFilterNodeList = activityElement.getElementsByTagName("intent-filter");
            if (intentFilterNodeList == null || intentFilterNodeList.getLength() == 0) {
                continue;
            }
            for (int j = 0; j < intentFilterNodeList.getLength(); j++) {
                Node item1 = intentFilterNodeList.item(j);
                if (!(item1 instanceof Element)) {
                    continue;
                }
                Element intentFilterElement = (Element) item1;
                NodeList actionNodeList = intentFilterElement.getElementsByTagName("action");
                boolean hint = false;
                for (int k = 0; k < actionNodeList.getLength(); k++) {
                    Node item2 = actionNodeList.item(k);
                    if (!(item2 instanceof Element)) {
                        continue;
                    }
                    Element actionElement = (Element) item2;
                    if ("android.intent.action.MAIN".equals(actionElement.getAttribute("android:name"))) {
                        hint = true;
                        break;
                    }
                }
                if (!hint) {
                    continue;
                }

                // hint android.intent.action.MAIN
                // first step try android.intent.category.INFO then retry with android.intent.category.LAUNCHER

                NodeList categoryNodeList = intentFilterElement.getElementsByTagName("category");
                for (int k = 0; k < categoryNodeList.getLength(); k++) {
                    Node item2 = categoryNodeList.item(k);
                    if (!(item2 instanceof Element)) {
                        continue;
                    }
                    Element categoryElement = (Element) item2;
//                    if ("android.intent.category.INFO".equals(categoryElement.getAttribute("android:name"))) {
//                        categoryInfoActivity = activityElement.getAttribute("android:name");
//                        break;
//                    } else
                    if ("android.intent.category.LAUNCHER".equals(categoryElement.getAttribute("android:name"))) {
                        return activityElement.getAttribute("android:name");

                    }
                }

            }
        }
        return null;
    }


    private static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
    private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
    private static final String FEATURE_LOAD_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    private static final String FEATURE_DISABLE_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

    /**
     * @param file File to load into Document
     * @return Document
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Document loadDocument(InputStream file)
            throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setFeature(FEATURE_DISABLE_DOCTYPE_DECL, true);
        docFactory.setFeature(FEATURE_LOAD_DTD, false);

        try {
            docFactory.setAttribute(ACCESS_EXTERNAL_DTD, " ");
            docFactory.setAttribute(ACCESS_EXTERNAL_SCHEMA, " ");
        } catch (IllegalArgumentException ex) {
            System.out.println("JAXP 1.5 Support is required to validate XML");
        }

        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // Not using the parse(File) method on purpose, so that we can control when
        // to close it. Somehow parse(File) does not seem to close the file in all cases.
        try {
            return docBuilder.parse(file);
        } finally {
            file.close();
        }
    }


    private static File theWorkDir = null;

    static File workDir() {
        if (theWorkDir == null) {
            theWorkDir = new File("ratel_work_dir_repkg");
        }
        return theWorkDir;
    }

    private static File cleanWorkDir() {
        File workDir = workDir();
        FileUtils.deleteQuietly(workDir);
        try {
            FileUtils.forceMkdir(workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return workDir;
    }


    private static void extractOriginAPk(Param param, File outFile) throws IOException {
        File originApk = param.originApk;
        byte[] bytes;
        try (ZipFile zipFile = new ZipFile(originApk)) {
            ZipEntry entry = zipFile.getEntry("assets/" + Constants.originAPKFileName);
            bytes = IOUtils.toByteArray(zipFile.getInputStream(entry));
        }
        FileUtils.writeByteArrayToFile(outFile, bytes);

    }

    public static int copyAndClose(final InputStream input, final OutputStream output) throws IOException {
        int copy = IOUtils.copy(input, output);
        input.close();
        output.close();
        return copy;
    }

    private static void restoreConstants() throws Exception {
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(Constants.CONSTANTS_DEFINE_PROPERTIES);
        if (inputStream == null) {
            return;
        }
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        for (Field field : Constants.class.getDeclaredFields()) {
            if (field.isSynthetic()) {
                continue;
            }
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            String value = properties.getProperty(Constants.RATEL_CONSTANTS_PREFIX + field.getName());
            if (value == null) {
                continue;
            }

            Object castValue = Util.primitiveCast(value, field.getType());

            if (castValue == null) {
                continue;
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(null, castValue);
        }
    }
}
