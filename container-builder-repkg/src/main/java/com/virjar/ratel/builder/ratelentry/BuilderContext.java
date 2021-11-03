package com.virjar.ratel.builder.ratelentry;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.builder.injector.DexFiles;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import static com.virjar.ratel.builder.ratelentry.Main.loadDocument;

/**
 * 上下文，包含一个任务执行的所有数据
 */
public class BuilderContext implements Closeable {

    public ApkAsset infectApk;
    public ApkAsset xpModuleApk;

    public CommandLine cmd;

    public File theWorkDir = null;

    public List<String> propertiesConfig = new ArrayList<>();
    public List<String> axmlEditorCommand = new ArrayList<>();

    public File outFile;

    public File rawOriginApk;
    public boolean hasRatelWrapper;
    public Properties ratelBuildProperties = new Properties();

    public Set<String> arch;

    public DexFiles dexFiles;

    @Override
    public void close() throws IOException {
        if (infectApk != null) {
            infectApk.close();
        }
        if (xpModuleApk != null) {
            xpModuleApk.close();
        }
    }

    public static class ApkAsset implements Closeable {
        public File file;
        public ApkFile apkFile;
        public ApkMeta apkMeta;
        public ZipFile zipFile;
        public boolean isXpModule;
        public String manifestXml;

        @Override
        public void close() throws IOException {
            apkFile.close();
            //zipFile.close();
        }
    }

    public void prepare() throws IOException, ParserConfigurationException, SAXException {
        // apk文件可能已经被改包过，所以这个时候需要抽取apk，然后再处理
        extractOriginApkFromRatelApkIfNeed();
        // 如果有模块，从模块中抽取配置
        parseManifestMetaData();

        // 从命令行带过来的配置
        loadBuilderProperties();

        // 重打包工具构建过程产生的配置
        //load engine properties from engine build output config
        ratelBuildProperties.load(Main.class.getClassLoader().getResourceAsStream(Constants.ratelEngineProperties));
    }

    private void loadBuilderProperties() throws IOException {
        if (propertiesConfig.isEmpty()) {
            return;
        }
        //reload properties if has additional params
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

    private void extractOriginApkFromRatelApkIfNeed() throws IOException {
        ZipEntry zipEntry = infectApk.zipFile.getEntry("assets/ratel_serialNo.txt");
        if (zipEntry == null) {
            //就是一个普通的apk
            return;
        }

        File tempFile = File.createTempFile(infectApk.file.getName(), ".apk");
        System.out.println("this is a ratel repkg apk file, extract origin into : " + tempFile.getAbsolutePath() + " and process it");
        FileUtils.forceDeleteOnExit(tempFile);

        ZipEntry originApkEntry = infectApk.zipFile.getEntry("assets/" + Constants.originAPKFileName);
        try (final InputStream inputStream = infectApk.zipFile.getInputStream(originApkEntry)) {
            FileUtils.copyInputStreamToFile(inputStream, tempFile);
        }
        infectApk.close();
        infectApk = BuilderContextParser.parseApkFile(tempFile);
        assert infectApk != null;
        rawOriginApk = infectApk.file;
        hasRatelWrapper = true;
    }

    //解析MainfestMetaData的数据
    private void parseManifestMetaData() throws IOException, ParserConfigurationException, SAXException {
        if (xpModuleApk == null) {
            return;
        }

        Document document = loadDocument(new ByteArrayInputStream(xpModuleApk.manifestXml.getBytes()));
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
                ratelBuildProperties.setProperty(key, value);
            }
        }
    }

    public void resolveArch(XApkHandler xApkHandler) throws IOException {
        Set<String> supportArch;
        if (xApkHandler != null) {
            // xapk 模式下，首先解码外部的apk，外部为空，可以尝试内部apk
            supportArch = xApkHandler.originAPKSupportArch();
            if (supportArch.isEmpty()) {
                supportArch = originAPKSupportArch(infectApk.zipFile);
            }
        } else {
            supportArch = originAPKSupportArch(infectApk.zipFile);
        }
        if (cmd.hasOption("abi")) {
            // 命令行参数指定使用abi，那么手动控制下
            String[] cmdAbis = cmd.getOptionValue("abi").split(",");
            supportArch.retainAll(Arrays.asList(cmdAbis));
        }
        arch = supportArch;
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
}
