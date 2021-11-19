package com.virjar.ratel.rdp;

import com.android.apksig.ApkSigner;
import com.android.apksig.apk.MinSdkVersionException;
import com.android.apksigner.PasswordRetriever;
import com.android.apksigner.SignerParams;
import com.google.common.collect.Lists;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class RDPBuilder {
    //TODO RDP之后，需要修改序列号
    public static void main(String[] args) throws Exception {

        final Options options = new Options();
        options.addOption(new Option("patch", "patch", true, "path to a patch apk"));
        options.addOption(new Option("duplicate", "duplicate", true, "the duplicate class strategy,with:  keepPatch | keepHost"));
        options.addOption(new Option("h", "help", false, "show help message"));


        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, false);
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("EngineBinTransformer", options);
            return;
        }


        File workDir = null;
        String jarPath = RDPBuilder.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (jarPath != null) {
            jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name());
            workDir = new File(jarPath).getParentFile().getParentFile();
        }

        if (workDir == null || !workDir.exists()) {
            workDir = new File("test.txt").getParentFile().getParentFile();
        }
        System.out.println("workDir:" + workDir.getAbsolutePath());

        File buildPropertiesConfigFile = new File(workDir, "ratel_resource/ratelConfig.properties");

        if (!buildPropertiesConfigFile.exists()) {
            System.out.println("enb bad,can not find RDP config file: " + buildPropertiesConfigFile.getAbsolutePath());
            return;
        }

        Properties ratelBuildProperties = new Properties();
        ratelBuildProperties.load(new FileInputStream(buildPropertiesConfigFile));

        File outputDir = new File(workDir, "out");
        outputDir.mkdirs();

        File outputApk = new File(outputDir,
                ratelBuildProperties.getProperty("app.packageName") + "_" +
                        ratelBuildProperties.getProperty("app.versionName") + "_" +
                        ratelBuildProperties.getProperty("app.versionCode") + "_" +
                        "RDP_" + System.currentTimeMillis() + ".apk"
        );

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputApk));

        //copy raws resource
        File rawResourceDir = new File(workDir, "raws");
        File[] files = rawResourceDir.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("broken file image");
            return;
        }
        Map<String, ZipEntry> zipEntryMap = originApkZipParams(workDir);
        for (File file : files) {
            buildRawResource(rawResourceDir, file, zipOutputStream, zipEntryMap);
        }

        //build all smali files into dex format

        File[] smaliDirs = workDir.listFiles();
        if (smaliDirs == null) {
            System.out.println("error to access files in directory: " + workDir.getAbsolutePath());
            return;
        }

        File tempDir = new File(workDir, "tmp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        List<Callable<File>> callables = Lists.newLinkedList();
        for (File smaliDir : smaliDirs) {
            String name = smaliDir.getName();
            String fileName = null;
            if (name.startsWith("smali_")) {
                fileName = name.substring(name.indexOf("_") + 1) + ".dex";
            } else if (name.equals("smali")) {
                fileName = "classes.dex";
            }
            if (fileName != null) {
                File tempDex = new File(tempDir, fileName);
                Callable<File> callable = () -> {
                    System.out.println("build smali dir: " + smaliDir.getAbsolutePath());
                    SmaliBuilder.build(smaliDir, tempDex, Opcodes.getDefault().api, false);
                    return tempDex;
                };
                callables.add(callable);
            }
        }
        List<Future<File>> futures = threadPool.invokeAll(callables);
        threadPool.shutdown();
        for (Future<File> future : futures) {
            File file = future.get();
            addZipData(file, zipOutputStream, file.getName(), zipEntryMap);
        }

        zipOutputStream.closeEntry();
        zipOutputStream.close();

        if (cmd.hasOption("patch")) {
            handlePatchMergeTasks(cmd, outputApk);
        }

        //now zip align and sign it
        File signatureKeyFile = new File(tempDir, "hermes_key");
        System.out.println("release ratel default apk signature key into : " + signatureKeyFile.getAbsolutePath());
        copyAndClose(RDPBuilder.class.getClassLoader().getResourceAsStream("hermes_key"), new FileOutputStream(signatureKeyFile));

        try {
            zipalign(outputApk, tempDir);
            signatureApk(outputApk, signatureKeyFile);
        } catch (Exception e) {
            // we need remove final output apk if sign failed,because of out shell think a illegal apk format file as task success flag,
            // android system think signed apk as really right apk,we can not install this apk on the android cell phone if rebuild success but sign failed
            FileUtils.forceDelete(outputApk);
            throw e;
        }
        FileUtils.forceDelete(tempDir);
    }


    private static void handlePatchMergeTasks(CommandLine cmd, File buildOutputApkFile) throws IOException {
        String[] patches = cmd.getOptionValues("patch");

        boolean duplicateKeepHost = true;
        if (cmd.hasOption("duplicate")) {
            duplicateKeepHost = "keepHost".equalsIgnoreCase(cmd.getOptionValue("duplicate"));
        }

        List<File> patchFiles = Lists.newArrayList();
        for (String patch : patches) {
            File patchFile = new File(patch);
            if (!patchFile.exists() || !patchFile.isFile() || !patchFile.canRead()) {
                System.out.println("can not read patch file: " + patchFile.getAbsolutePath());
                continue;
            }
            patchFiles.add(patchFile);
        }

        if (patchFiles.isEmpty()) {
            return;
        }


        File tmpOutputApk = File.createTempFile("createPatch", ".apk");
        tmpOutputApk.deleteOnExit();

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tmpOutputApk));

        int maxIndex = 1;

        Set<String> definedClass = new HashSet<>();

        try (ZipFile zipFile = new ZipFile(buildOutputApkFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                zipOutputStream.putNextEntry(zipEntry);
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                Matcher matcher = classesIndexPattern.matcher(zipEntry.getName());
                if (matcher.matches()) {
                    byte[] bytes = IOUtils.toByteArray(inputStream);
                    // 分析重复class
                    DexBackedDexFile dexBackedDexFile = new DexBackedDexFile(Opcodes.getDefault(), bytes);
                    Set<? extends DexBackedClassDef> allClasses = dexBackedDexFile.getClasses();
                    for (DexBackedClassDef dexBackedClassDef : allClasses) {
                        definedClass.add(dexBackedClassDef.getType());
                    }
                    zipOutputStream.write(bytes);
                    int nowIndex = NumberUtils.toInt(matcher.group(1), 0);
                    if (nowIndex > maxIndex) {
                        maxIndex = nowIndex;
                    }
                } else {
                    IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
                }
            }
        }
        maxIndex++;
        // 加入新的dex
        for (File patchApkFile : patchFiles) {
            try (ZipFile zipFile = new ZipFile(patchApkFile)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    Matcher matcher = classesIndexPattern.matcher(zipEntry.getName());
                    if (!matcher.matches()) {
                        continue;
                    }

                    DexPool dexPool = new DexPool(Opcodes.getDefault());
                    int dexClassSize = 0;
                    byte[] bytes = IOUtils.toByteArray(zipFile.getInputStream(zipEntry));
                    // 分析重复class
                    DexBackedDexFile dexBackedDexFile = new DexBackedDexFile(Opcodes.getDefault(), bytes);
                    Set<? extends DexBackedClassDef> allClasses = dexBackedDexFile.getClasses();

                    for (DexBackedClassDef dexBackedClassDef : allClasses) {
                        if (definedClass.contains(dexBackedClassDef.getType())) {
                            continue;
                        }
                        dexPool.internClass(dexBackedClassDef);
                        definedClass.add(dexBackedClassDef.getType());
                        dexClassSize++;
                    }

                    if (dexClassSize == 0) {
                        continue;
                    }
                    MemoryDataStore memoryDataStore = new MemoryDataStore();
                    dexPool.writeTo(memoryDataStore);
                    memoryDataStore.close();

                    zipOutputStream.putNextEntry(new ZipEntry("classes" + maxIndex + ".dex"));
                    zipOutputStream.write(memoryDataStore.getData());
                    maxIndex++;
                }
            }
        }
        zipOutputStream.closeEntry();
        zipOutputStream.close();

        Files.move(
                tmpOutputApk.toPath(), buildOutputApkFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println(buildOutputApkFile.getAbsolutePath() + " add patch apk success");

    }


    private static final Pattern classesIndexPattern = Pattern.compile("classes(\\d*)\\.dex");

    private static Map<String, ZipEntry> originApkZipParams(File workDir) throws IOException {
        Map<String, ZipEntry> ret = new HashMap<>();
        try (ZipFile zipFile = new ZipFile(new File(workDir, "raws/assets/ratel_container_origin_apk.apk"))) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                ret.put(zipEntry.getName(), zipEntry);
            }
        }
        return ret;
    }

    private static void addZipData(File file, ZipOutputStream zipOutputStream, String entryName, Map<String, ZipEntry> zipParam) throws IOException {
        ZipEntry zipEntry = null;
        if (zipParam != null) {
            zipEntry = zipParam.get(entryName);
        }
        if (zipEntry == null) {
            zipEntry = new ZipEntry(entryName);
        } else {
            //copy constructor
            ZipEntry originZipEntry = zipEntry;
            zipEntry = new ZipEntry(originZipEntry);
            zipEntry.setSize(file.length());
            zipEntry.setCompressedSize(-1);
        }
        zipOutputStream.putNextEntry(zipEntry);
        FileInputStream fileInputStream = new FileInputStream(file);
        IOUtils.copy(fileInputStream, zipOutputStream);
        fileInputStream.close();
    }

    private static void buildRawResource(File rootDir, File nowFile, ZipOutputStream zipOutputStream, Map<String, ZipEntry> zipEntryMap) throws IOException {
        if (nowFile.isFile()) {
            String entryName = nowFile.getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1);
            addZipData(nowFile, zipOutputStream, entryName, zipEntryMap);
            return;
        }
        File[] files = nowFile.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            buildRawResource(rootDir, file, zipOutputStream, zipEntryMap);
        }
    }


    private static void zipalign(File outApk, File theWorkDir) throws IOException, InterruptedException {
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
        copyAndClose(RDPBuilder.class.getClassLoader().getResourceAsStream(zipalignBinPath), new FileOutputStream(unzipDestFile));
        if (isLinux) {
            String libCPlusPlusPath = "zipalign/linux/lib64/libc++.so";
            File libCPlusPlusFile = new File(theWorkDir, libCPlusPlusPath);
            libCPlusPlusFile.getParentFile().mkdirs();
            copyAndClose(RDPBuilder.class.getClassLoader().getResourceAsStream(libCPlusPlusPath), new FileOutputStream(libCPlusPlusFile));
        }

        unzipDestFile.setExecutable(true);

        File tmpOutputApk = File.createTempFile("zipalign", ".apk");
        tmpOutputApk.deleteOnExit();

        String command = unzipDestFile.getAbsolutePath() + " -f  4 " + outApk.getAbsolutePath() + " " + tmpOutputApk.getAbsolutePath();
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

    private static void signatureApk(File outApk, File keyStoreFile) throws Exception {
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
                        .setV1SigningEnabled(true)
                        .setV2SigningEnabled(true)
                        .setV3SigningEnabled(true)
                        //这个需要设置为true，否则debug版本的apk无法签名
                        .setDebuggableApkPermitted(true);
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

    private static void copyAndClose(final InputStream input, final OutputStream output) throws IOException {
        int copy = IOUtils.copy(input, output);
        input.close();
        output.close();
    }
}
