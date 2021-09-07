package com.virjar.ratel.builder.ratelentry;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.allcommon.NewConstants;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.DexMakerOpt;
import com.virjar.ratel.builder.Util;
import com.virjar.ratel.builder.mode.RatelPackageBuilderAppendDex;
import com.virjar.ratel.builder.mode.RatelPackageBuilderRepackage;
import com.virjar.ratel.builder.mode.RatelPackageBuilderShell;
import com.virjar.ratel.builder.mode.RatelPackageBuilderZelda;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.ApkParsers;
import net.dongliu.apk.parser.bean.DexClass;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.DexParser;
import net.dongliu.apk.parser.struct.AndroidConstants;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

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
        XApkHandler xApkHandler = new XApkHandler(new File(args[xApkIndex]));

        args[xApkIndex] = xApkHandler.releasedBaseApkTempFile.getAbsolutePath();
        File file = ratelMain(args, xApkHandler);
        if (file == null || !file.exists()) {
            //build失败的
            return;
        }
        xApkHandler.buildRatelInflectedSplitApk(file);
        Main.cleanWorkDir();
    }

    private static File ratelMain(String[] args, XApkHandler xApkHandler) throws Exception {
        System.out.println("ratel build param: " + Joiner.on(" ").join(args));

        restoreConstants();

        try (BuilderContext context = BuilderContextParser.parse(args, xApkHandler != null)) {
            if (context.cmd.hasOption('h')) {
                HelpFormatter hf = new HelpFormatter();
                hf.setWidth(110);
                hf.printHelp("ratel", BuilderContextParser.setupOptions());
                return null;
            }

            if (context.cmd.hasOption('t')) {
                System.out.println(context.outFile.getAbsolutePath());
                return null;
            }
            return ratelMain(context, xApkHandler);
        }
    }

    private static File ratelMain(BuilderContext context, XApkHandler xApkHandler) throws Exception {
        if (context.cmd.hasOption('x')) {
            extractOriginAPk(context);
            return null;
        }

        context.prepare();

        theWorkDir = context.theWorkDir;
        File workDir = cleanWorkDir();
        if (xApkHandler != null) {
            System.out.println("release split apks");
            xApkHandler.releaseApks();
        }

        BindingResourceManager.extract(workDir);

        Util.setupRatelSupportArch(context.ratelBuildProperties.getProperty("ratel_support_abis", "arm64-v8a,armeabi-v7a"));

        //inject bootstrap code into origin apk
        BuildParamMeta buildParamMeta = parseManifest(context.infectApk.file);

        Set<String> supportArch;
        if (xApkHandler != null) {
            // xapk 模式下，首先解码外部的apk，外部为空，可以尝试内部apk
            supportArch = xApkHandler.originAPKSupportArch();
            if (supportArch.isEmpty()) {
                supportArch = originAPKSupportArch(context.infectApk.zipFile);
            }
        } else {
            supportArch = originAPKSupportArch(context.infectApk.zipFile);
        }

        String engine = Constants.RATEL_ENGINE_ENUM_REBUILD;
        if (context.cmd.hasOption('e')) {
            engine = context.cmd.getOptionValue('e');
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
        context.ratelBuildProperties.setProperty(Constants.serialNoKey, serialNo);
        context.ratelBuildProperties.setProperty(Constants.buildTimestampKey, buildParamMeta.buildTimestamp);

        if (StringUtils.isNotBlank(buildParamMeta.androidAppComponentFactory)) {
            //暂时把这个带过来
            context.ratelBuildProperties.setProperty(Constants.android_AppComponentFactoryKey, buildParamMeta.androidAppComponentFactory);
        }


        //create new apk file
        ZipOutputStream zos = new ZipOutputStream(context.outFile);
        zos.setEncoding("UTF8");

        switch (engine) {
            case Constants.RATEL_ENGINE_ENUM_APPENDEX:
                RatelPackageBuilderAppendDex.handleTask(workDir, context, buildParamMeta, context.cmd, zos);
                break;
            case Constants.RATEL_ENGINE_ENUM_REBUILD:
                if (buildParamMeta.appEntryClassDex == null) {
                    //其他模式下，可以不存在 entry application
                    throw new RuntimeException("unsupported apk , no entry point application or entry point launch activity found");
                }
                RatelPackageBuilderRepackage.handleTask(workDir, context, buildParamMeta, context.ratelBuildProperties, zos, context.cmd);
                break;
            case Constants.RATEL_ENGINE_ENUM_SHELL:
                RatelPackageBuilderShell.handleTask(workDir, context, buildParamMeta, context.cmd, zos);
                break;
            case Constants.RATEL_ENGINE_ENUM_ZELDA:
                RatelPackageBuilderZelda.handleTask(workDir, context, buildParamMeta, context.cmd, zos);
                break;
            default:
                throw new IllegalStateException("unknown ratel build engine type: " + engine);
        }


        // now insert apk into assets directory
        Util.copyAssets(zos, context.infectApk.file, Constants.originAPKFileName);
        if (context.xpModuleApk != null) {
            Util.copyAssets(zos, context.xpModuleApk.file, Constants.xposedBridgeApkFileName);
        }

        Util.copyAssets(zos, BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.XPOSED_BRIDGE_JAR_FILE), Constants.xposedApiBridgeJarFileName);
        //请注意，shell模式下，不需要copy driver的资源

        context.ratelBuildProperties.setProperty(Constants.hasEmbedXposedModuleKey, String.valueOf(context.xpModuleApk != null));
        if (context.xpModuleApk != null) {
            ApkFile xposedApkFile = context.xpModuleApk.apkFile;
            context.ratelBuildProperties.setProperty(Constants.xposedModuleApkPackageNameKey, xposedApkFile.getApkMeta().getPackageName());
        }

        context.ratelBuildProperties.setProperty("supportArch", Joiner.on(",").join(supportArch));

        if (context.xpModuleApk != null) {
            Util.copyLibrary(zos, supportArch, context.xpModuleApk.file);
        }
        Util.copyLibrary(zos, supportArch, BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.RUNTIME_JAR_FILE));

        //so文件需要插入到asset目录，因为 execve 注入会依赖so，且依赖多个版本
        insertRatelNativeLib(zos, BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.RUNTIME_JAR_FILE));

        //add dexmaker opt file if exist
        File dexMakerOptFile = DexMakerOpt.genDexMakerOptFile(workDir);
        if (dexMakerOptFile != null && dexMakerOptFile.exists()) {
            Util.copyAssets(zos, dexMakerOptFile, Constants.dexmakerOptFileName);
        }


        //add build serial number
        System.out.println("build serialNo: " + buildParamMeta.serialNo);
        zos.putNextEntry(new ZipEntry("assets/" + Constants.serialNoFile));
        zos.write(buildParamMeta.serialNo.getBytes());


        //append certificate file
        // todo 删除证书的逻辑
        String ratelCertificate = Constants.debugCertificate;
        zos.putNextEntry(new ZipEntry("assets/" + Constants.ratelCertificate));
        zos.write(ratelCertificate.getBytes(StandardCharsets.UTF_8));

        zos.putNextEntry(new ZipEntry("assets/" + Constants.ratelConfigFileName));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        context.ratelBuildProperties.store(byteArrayOutputStream, "auto generated by ratel repakcage builder");
        zos.write(byteArrayOutputStream.toByteArray());


        zos.close();
        // FileUtils.deleteDirectory(workDir);
        System.out.println("the new apk file ：" + context.outFile.getAbsolutePath());

        if (context.hasRatelWrapper) {
            // if this is a rebuilded apk,we can insert addson resource (such as dex/assets) with RDP|APKTool
            // we need merge this resource into the final apk
            HelperAppendAddsOn.mergeAddsOnFiles(context.rawOriginApk, context.infectApk.file, context.outFile);
        }

        if (context.cmd.hasOption('s') && !context.cmd.hasOption('D')) {
            //do not need signature apk if create a decompile project
            HelperZipAndSign.zipAndSign(
                    context,
                    BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.DEFAULT_SIGN_KEY),
                    workDir);
        }


        context.ratelBuildProperties.store(new FileOutputStream(new File(context.outFile.getParentFile(), "ratelConfig.properties")), "auto generated by virjar@ratel");

        if (context.cmd.hasOption('D')) {
            HelperCreateRDP.createRatelDecompileProject(context.outFile, context, context.cmd, context.ratelBuildProperties);
        }

        if (xApkHandler == null) {
            System.out.println("clean working directory..");
            FileUtils.deleteDirectory(workDir);
        }
        return context.outFile;
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


    private static Set<String> originAPKSupportArch(ZipFile zipFile) throws IOException {
        Set<String> ret = Sets.newHashSet();
        //ret.add("armeabi");
        //ret.add("armeabi-v7a");

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


    private static File cleanWorkDir() {
        FileUtils.deleteQuietly(theWorkDir);
        try {
            FileUtils.forceMkdir(theWorkDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return theWorkDir;
    }


    private static void extractOriginAPk(BuilderContext context) throws IOException {
        ZipEntry zipEntry =
                context.infectApk.zipFile.getEntry("assets/" + Constants.originAPKFileName);
        try (InputStream inputStream = context.infectApk.zipFile.getInputStream(zipEntry)) {
            FileUtils.copyInputStreamToFile(inputStream, context.outFile);
        }
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

    public static File workDir() {
        return theWorkDir;
    }
}
