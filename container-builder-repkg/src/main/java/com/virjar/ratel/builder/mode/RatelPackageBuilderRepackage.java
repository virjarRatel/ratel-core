package com.virjar.ratel.builder.mode;


import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.builder.BootstrapCodeInjector;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.DexMergeFailedException;
import com.virjar.ratel.builder.DexSplitter;
import com.virjar.ratel.builder.Param;
import com.virjar.ratel.builder.ShellDetector;
import com.virjar.ratel.builder.SmaliRebuildFailedException;
import com.virjar.ratel.builder.Util;
import com.virjar.ratel.builder.manifesthandler.AXmlEditorCmdHandler;
import com.virjar.ratel.builder.manifesthandler.EnableDebug;
import com.virjar.ratel.builder.manifesthandler.RequestLegacyExternalStorage;

import net.dongliu.apk.parser.utils.Pair;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.pxb.android.axml.AxmlReader;
import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.AxmlWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.directory.DirectoryException;

public class RatelPackageBuilderRepackage {
    public static void handleTask(File workDir, Param param, BuildParamMeta buildParamMeta,
                                  Properties ratelBuildProperties,
                                  ZipOutputStream zos, CommandLine cmd

    ) throws IOException, AndrolibException, DirectoryException {
        File bootstrapDecodeDir = new File(workDir, "ratel_bootstrap_apk");
        decodeBootstrapAPK(new File(workDir, Constants.bootstrapAPKPath), bootstrapDecodeDir);


        System.out.println("work dir: " + workDir.getCanonicalPath());
        System.out.println("bootstrap apk decode dir: " + bootstrapDecodeDir.getCanonicalPath());
        System.out.println("apk info [packageName:" + buildParamMeta.packageName
                + ",appEntryClass: "
                + buildParamMeta.appEntryClass
                + "] ");
        File splitDex = null;

        if (StringUtils.isNotBlank(buildParamMeta.androidAppComponentFactory)) {
            // inject for android 10
            for (String dexIndex : buildParamMeta.dexClassesMap.keySet()) {
                File dexImage = createOrGetDex(dexIndex, param.originApk, workDir);
                DexBackedDexFile dexBackedDexFile = DexFileFactory.loadDexFile(dexImage, Opcodes.getDefault());
                //Map<String, DexBackedClassDef> classDefMap = Maps.newHashMap();
                for (DexBackedClassDef backedClassDef : dexBackedDexFile.getClasses()) {
                    // classDefMap.put(Util.descriptorToDot(backedClassDef.getType()), backedClassDef);
                    if (buildParamMeta.androidAppComponentFactory.equals(
                            Util.descriptorToDot(backedClassDef.getType())
                    )) {
                        boolean decodeAllSmali = false;
                        boolean hasSplitDex = false;
                        // inject androidAppComponentFactory
                        for (int i = 0; i < 4; i++) {
                            try {
                                File entryDexImageFile =
                                        BootstrapCodeInjector.injectCInit(
                                                dexImage, workDir, bootstrapDecodeDir, backedClassDef.getType(), decodeAllSmali
                                        );
                                FileUtils.forceDelete(dexImage);
                                FileUtils.moveFile(entryDexImageFile, dexImage);
                                break;
                            } catch (SmaliRebuildFailedException e) {
                                if (decodeAllSmali) {
                                    throw e;
                                }
                                decodeAllSmali = true;
                                System.out.println("decodeAllSmali");
                            } catch (DexMergeFailedException e) {
                                if (hasSplitDex) {
                                    throw e;
                                }
                                System.out.println("split dex");
                                hasSplitDex = true;
                                splitDex = DexSplitter.splitDex(dexImage, workDir, buildParamMeta);
                            }
                        }
                        break;
                    }
                }


            }
        }


        File dexImage = createOrGetDex(buildParamMeta.appEntryClassDex, param.originApk, workDir);


        File entryDexImageFile = null;
        boolean injectLogComponent = cmd.hasOption('l');
        boolean decodeAllSmali = false;
        boolean hasSplitDex = false;
        for (int i = 0; i < 4; i++) {
            try {
                entryDexImageFile = BootstrapCodeInjector.injectBootstrapCode(dexImage, workDir, bootstrapDecodeDir, buildParamMeta, injectLogComponent, decodeAllSmali);
                break;
            } catch (SmaliRebuildFailedException e) {
                if (decodeAllSmali) {
                    throw e;
                }
                decodeAllSmali = true;
                System.out.println("decodeAllSmali");
            } catch (DexMergeFailedException e) {
                if (hasSplitDex) {
                    throw e;
                }
                System.out.println("split dex");
                hasSplitDex = true;
                splitDex = DexSplitter.splitDex(dexImage, workDir, buildParamMeta);
            }
        }


        Pair<String, String> shellEntry = ShellDetector.findShellEntry(param.originApk);
        if (shellEntry != null) {
            ratelBuildProperties.setProperty(Constants.shellFeatureFileKey, shellEntry.getLeft());
            ratelBuildProperties.setProperty(Constants.shellName, shellEntry.getRight());
        }

        ZipFile originAPKZip = new ZipFile(param.originApk);
        Enumeration<ZipEntry> entries = originAPKZip.getEntries();
        int maxIndex = 1;

        while (entries.hasMoreElements()) {
            ZipEntry originEntry = entries.nextElement();
            if (originEntry.getName().startsWith("META-INF/")) {
                continue;
            }
            if (Util.isRatelUnSupportArch(originEntry.getName())) {
                //过滤掉不支持的架构
                continue;
            }
            //i will edit androidManifest.xml ,so skip it now
            if (originEntry.getName().equals(Constants.manifestFileName)) {
                zos.putNextEntry(new ZipEntry(originEntry));
                boolean isTargetAndroidR = NumberUtils.toInt(buildParamMeta.apkMeta.getTargetSdkVersion()) >= 30;
                zos.write(handleManifestEditor(buildParamMeta, IOUtils.toByteArray(originAPKZip.getInputStream(originEntry))
                        , cmd.hasOption('d')
                        , buildParamMeta.axmlEditorCommand, isTargetAndroidR
                ));
                continue;
            }

            Matcher matcher = Util.classesIndexPattern.matcher(originEntry.getName());
            if (matcher.matches()) {
                int nowIndex = NumberUtils.toInt(matcher.group(1));
                if (nowIndex > maxIndex) {
                    maxIndex = nowIndex;
                }
            }


            if (originEntry.getName().equals(buildParamMeta.appEntryClassDex)) {
                ZipEntry zipEntry = new ZipEntry(buildParamMeta.appEntryClassDex);
                zos.putNextEntry(zipEntry);

                byte[] dexClassesFile = FileUtils.readFileToByteArray(entryDexImageFile);
                zos.write(dexClassesFile);
            } else {
                zos.putNextEntry(new ZipEntry(originEntry));
                zos.write(IOUtils.toByteArray(originAPKZip.getInputStream(originEntry)));
            }
        }

        maxIndex++;

        if (splitDex != null) {
            zos.putNextEntry(new ZipEntry("classes" + maxIndex + ".dex"));
            zos.write(FileUtils.readFileToByteArray(splitDex));
        }

        //close
        originAPKZip.close();

        Util.copyAssets(zos, new File(workDir, Constants.RATEL_ENGINE_JAR), Constants.RATEL_ENGINE_JAR);
    }


    private static File createOrGetDex(String dexIndex, File originApk, File workDir) throws IOException {
        File dexImage = new File(workDir, dexIndex);
        if (dexImage.exists()) {
            return dexImage;
        }
        // ZipFile
        try (org.apache.tools.zip.ZipFile zipFile = new org.apache.tools.zip.ZipFile(originApk)) {
            // 需要重新编译这个文件
            ZipEntry dexEntry = zipFile.getEntry(dexIndex);

            try (InputStream inputStream = zipFile.getInputStream(dexEntry)) {
                FileUtils.writeByteArrayToFile(dexImage, IOUtils.toByteArray(inputStream));
            }
            return dexImage;
        }
    }


    private static void decodeBootstrapAPK(File bootstrapAPK, File outDir) throws AndrolibException, IOException, DirectoryException {
        System.out.println("decode oringin apk:" + bootstrapAPK.getAbsolutePath() + "...");
        ApkDecoder decoder = new ApkDecoder();
        decoder.setApkFile(bootstrapAPK);

        decoder.setOutDir(outDir);
        //不对源码进行解码
        decoder.setDecodeResources(ApkDecoder.DECODE_RESOURCES_NONE);
        decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_SMALI);
        // decoder.setKeepBrokenResources(true);
        decoder.setForceDelete(true);
        decoder.setForceDecodeManifest(ApkDecoder.FORCE_DECODE_MANIFEST_NONE);
        decoder.setDecodeAssets(ApkDecoder.DECODE_ASSETS_NONE);
        decoder.decode();
        decoder.close();
    }


    private static byte[] handleManifestEditor(BuildParamMeta buildParamMeta, byte[] manifestFileData, boolean enableDebug,
                                               List<String> xmlEditCmd, boolean isTargetAndroidR
    ) throws IOException {
        AxmlReader rd = new AxmlReader(manifestFileData);
        AxmlWriter wr = new AxmlWriter();
        AxmlVisitor axmlVisitor = wr;
        if (enableDebug) {
            axmlVisitor = new EnableDebug(axmlVisitor);
        }
        if (isTargetAndroidR) {
            //axmlVisitor = new AddQueriesForPackage(axmlVisitor, Constants.RATEL_MANAGER_PACKAGE);
            if (!xmlEditCmd.contains("add_permission_QUERY_ALL_PACKAGES")) {
                xmlEditCmd.add("add_permission_QUERY_ALL_PACKAGES");
            }
        }
        axmlVisitor = AXmlEditorCmdHandler.handleCmd(axmlVisitor, xmlEditCmd);

        if (NumberUtils.toInt(buildParamMeta.apkMeta.getTargetSdkVersion()) >= 29) {
            // android 10,无法访问内存卡，临时放开
            axmlVisitor = new RequestLegacyExternalStorage(axmlVisitor);
        }


        rd.accept(axmlVisitor);
        return wr.toByteArray();
    }
}
