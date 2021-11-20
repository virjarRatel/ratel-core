package com.virjar.ratel.builder.mode;


import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.allcommon.NewConstants;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.ShellDetector;
import com.virjar.ratel.builder.Util;
import com.virjar.ratel.builder.injector.CodeInjectorV2;
import com.virjar.ratel.builder.injector.DexFiles;
import com.virjar.ratel.builder.manifesthandler.AXmlEditorCmdHandler;
import com.virjar.ratel.builder.manifesthandler.EnableDebug;
import com.virjar.ratel.builder.manifesthandler.RequestLegacyExternalStorage;
import com.virjar.ratel.builder.ratelentry.BindingResourceManager;
import com.virjar.ratel.builder.ratelentry.BuilderContext;

import net.dongliu.apk.parser.utils.Pair;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.pxb.android.axml.AxmlReader;
import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.AxmlWriter;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;


public class RatelPackageBuilderRepackage {
    public static void handleTask(File workDir, BuilderContext context, BuildParamMeta buildParamMeta,
                                  Properties ratelBuildProperties,
                                  ZipOutputStream zos

    ) throws IOException {


        System.out.println("work dir: " + workDir.getCanonicalPath());
        System.out.println("apk info [packageName:" + buildParamMeta.packageName
                + ",appEntryClass: "
                + buildParamMeta.appEntryClass
                + "] ");
        DexFiles dexFiles = context.dexFiles;

        if (StringUtils.isNotBlank(buildParamMeta.androidAppComponentFactory)) {
            // inject for android 10
            DexFiles.DexFile dex = context.dexFiles.findClassInDex(buildParamMeta.androidAppComponentFactory);
            if (dex != null) {
                CodeInjectorV2.doInject(dexFiles, buildParamMeta.androidAppComponentFactory, buildParamMeta);
            }
        }

        CodeInjectorV2.doInject(dexFiles, buildParamMeta.appEntryClass, buildParamMeta);

        for (DexFiles.DexFile dexFile : dexFiles.dexFiles()) {
            String name;
            if (dexFile.getIndex() == 0) {
                name = "classes.dex";
            } else {
                name = "classes" + dexFile.getIndex() + ".dex";
            }
            ZipEntry zipEntry = new ZipEntry(name);
            zos.putNextEntry(zipEntry);
            zos.write(dexFile.getRawData());
        }

        @Deprecated
        Pair<String, String> shellEntry = ShellDetector.findShellEntry(context.infectApk.file);
        if (shellEntry != null) {
            ratelBuildProperties.setProperty(Constants.shellFeatureFileKey, shellEntry.getLeft());
            ratelBuildProperties.setProperty(Constants.shellName, shellEntry.getRight());
        }

        ZipFile originAPKZip = new ZipFile(context.infectApk.file);
        Enumeration<ZipEntry> entries = originAPKZip.getEntries();


        while (entries.hasMoreElements()) {
            ZipEntry originEntry = entries.nextElement();
            // META-INF/MANIFEST.MF
            // META-INF/*.RSA
            // META-INF/*.DSA
            // 请注意，META-INF 不能暴力删除，根据java规范，spi配置也会存在于META-INF中。在海外的标准app中，经常会存储SPI配置在META-INFO中
            //
            if (Util.isCertificateOrMANIFEST(originEntry.getName())) {
                continue;
            }
            if (Util.isRatelUnSupportArch(originEntry.getName())) {
                //过滤掉不支持的架构
                continue;
            }
            //i will edit androidManifest.xml ,so skip it now
            if (originEntry.getName().equals(Constants.manifestFileName)) {
                zos.putNextEntry(new ZipEntry(originEntry));
                boolean isTargetAndroidR = NumberUtils.toInt(context.infectApk.apkMeta.getTargetSdkVersion()) >= 30;
                zos.write(handleManifestEditor(context, buildParamMeta, IOUtils.toByteArray(originAPKZip.getInputStream(originEntry))
                        , context.cmd.hasOption('d')
                        , context.axmlEditorCommand, isTargetAndroidR
                ));
                continue;
            }

            Matcher matcher = Util.classesIndexPattern.matcher(originEntry.getName());
            if (matcher.matches()) {
                continue;
            }
            if (originEntry.getName().equals("classes.dex")) {
                continue;
            }


            String entryName = originEntry.getName();
            // 可能用户排除一些arch
            boolean needCopy = false;
            if (!entryName.startsWith("lib") || context.arch.isEmpty()) {
                needCopy = true;
            } else {
                for (String str : context.arch) {
                    if (entryName.startsWith("lib/" + str)) {
                        needCopy = true;
                        break;
                    }
                }
            }
            if (needCopy) {
                zos.putNextEntry(new ZipEntry(originEntry));
                zos.write(IOUtils.toByteArray(originAPKZip.getInputStream(originEntry)));
            }
        }

        //close
        originAPKZip.close();

        Util.copyAssets(zos, BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.RUNTIME_JAR_FILE), Constants.RATEL_ENGINE_JAR);
    }


    private static byte[] handleManifestEditor(BuilderContext builderContext, BuildParamMeta buildParamMeta, byte[] manifestFileData, boolean enableDebug,
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

        if (NumberUtils.toInt(builderContext.infectApk.apkMeta.getTargetSdkVersion()) >= 29) {
            // android 10,无法访问内存卡，临时放开
            axmlVisitor = new RequestLegacyExternalStorage(axmlVisitor);
        }


        rd.accept(axmlVisitor);
        return wr.toByteArray();
    }
}
