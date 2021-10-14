package com.virjar.ratel.builder.mode;


import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.ReNameEntry;
import com.virjar.ratel.builder.Util;
import com.virjar.ratel.builder.manifesthandler.AXmlEditorCmdHandler;
import com.virjar.ratel.builder.manifesthandler.EnableDebug;
import com.virjar.ratel.builder.manifesthandler.ReplaceApplication;
import com.virjar.ratel.builder.manifesthandler.ReplacePackage;
import com.virjar.ratel.builder.manifesthandler.ZeldaManifestHandlers;
import com.virjar.ratel.builder.ratelentry.BuilderContext;

import net.dongliu.apk.parser.struct.ChunkType;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.pxb.android.axml.AxmlReader;
import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.AxmlWriter;
import org.jf.pxb.googlecode.dex2jar.reader.io.ArrayDataIn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.regex.Matcher;

public class RatelPackageBuilderZelda {
    public static void handleTask(File workDir, BuilderContext context, BuildParamMeta buildParamMeta,
                                  CommandLine cmd, ZipOutputStream zos
    ) throws IOException {

        genMeta(buildParamMeta, context);

        ZipFile originAPKZip = context.infectApk.zipFile;
        Enumeration<ZipEntry> entries = originAPKZip.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry originEntry = entries.nextElement();
            String entryName = originEntry.getName();
            //remove sign data
            if (entryName.startsWith("META-INF/")) {
                continue;
            }
            if (Util.isRatelUnSupportArch(entryName)) {
                //过滤掉不支持的架构
                continue;
            }
            //i will edit androidManifest.xml ,so skip it now
            if (entryName.equals(Constants.manifestFileName)) {
                zos.putNextEntry(new ZipEntry(originEntry));
                zos.write(editManifestWithAXmlEditor(IOUtils.toByteArray(originAPKZip.getInputStream(originEntry)), cmd, buildParamMeta, context));
                continue;
            }
            if (entryName.equals(Constants.CONSTANTS_RESOURCES_ARSC)) {
                zos.putNextEntry(new ZipEntry(originEntry));
                zos.write(editARSCPackageName(IOUtils.toByteArray(originAPKZip.getInputStream(originEntry)), buildParamMeta));
                continue;
            }
            // 可能用户排除一些arch
            boolean needCopy = false;
            if (!entryName.startsWith("lib") || context.arch.isEmpty()) {
                needCopy = true;
            } else {
                for (String str : context.arch) {
                    if (entryName.startsWith("lib/" + str)) {
                        needCopy = true;
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


        storeDeclaredComponentClassNames(zos, buildParamMeta);
        storeZeldaProperties(buildParamMeta, context);

        Util.copyAssets(zos, new File(workDir, Constants.RATEL_ENGINE_JAR), Constants.RATEL_ENGINE_JAR);
        System.out.println("copy ratel engine dex resources");
        appendDex(zos, context.infectApk.file, new File(workDir, Constants.ratelZeldaBootstrapJarPath));
    }

    private static void storeZeldaProperties(BuildParamMeta buildParamMeta, BuilderContext builderContext) {
        builderContext.ratelBuildProperties.setProperty(Constants.KEY_ORIGIN_PKG_NAME, builderContext.infectApk.apkMeta.getPackageName());
        builderContext.ratelBuildProperties.setProperty(Constants.KEY_ORIGIN_APPLICATION_NAME, buildParamMeta.originApplicationClass);
        builderContext.ratelBuildProperties.setProperty(Constants.KEY_NEW_PKG_NAME, buildParamMeta.newPkgName);
        builderContext.ratelBuildProperties.setProperty(Constants.kEY_SUFFER_KEY, buildParamMeta.sufferKey);
    }


    private static void genMeta(BuildParamMeta buildParamMeta, BuilderContext builderContext) {
        if (builderContext.infectApk.apkMeta.getPackageName().equals(buildParamMeta.newPkgName)) {
            throw new IllegalStateException("you should set a new package name,not: " + builderContext.infectApk.apkMeta.getPackageName());
        }
        if (Constants.devBuild) {
            buildParamMeta.sufferKey = "debug" + builderContext.infectApk.apkMeta.getVersionCode();
        } else {
            buildParamMeta.sufferKey = Util.randomChars(8);
        }
        buildParamMeta.sufferKey = "zElDa." + buildParamMeta.sufferKey;
        if (StringUtils.isBlank(buildParamMeta.newPkgName)) {
            buildParamMeta.newPkgName = "virjar.zelda."
                    + StringUtils.substring(builderContext.infectApk.apkMeta.getPackageName().replaceAll("\\.", ""), 0, 20)
                    + "."
                    + buildParamMeta.sufferKey;

        }
    }

    private static void storeDeclaredComponentClassNames(ZipOutputStream zos, BuildParamMeta buildParamMeta) throws IOException {
        zos.putNextEntry(new ZipEntry("assets/" + Constants.declaredComponentListConfig));
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(zos);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        for (String str : buildParamMeta.declaredComponentClassNames) {
            bufferedWriter.write(Constants.KEY_KEEP_CLASS_NAMES);
            bufferedWriter.write(str);
            bufferedWriter.newLine();
        }
        for (String authorities : buildParamMeta.authorities) {
            bufferedWriter.write(Constants.KEY_REPLACE_AUTHORITIES);
            bufferedWriter.write(authorities);
            bufferedWriter.newLine();
        }
        for (String childProcess : buildParamMeta.childProcess) {
            bufferedWriter.write(Constants.KEY_CHILD_PROCESS);
            bufferedWriter.write(childProcess);
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
    }


    private static void appendDex(ZipOutputStream zos, File originApk, File engineAPk) throws IOException {

        // classes.dex classes2.dex classes3.dex 没有 classes1.dex
        ZipFile orignApkZipFile = new ZipFile(originApk);
        Enumeration<ZipEntry> originEntries = orignApkZipFile.getEntries();
        int maxIndex = 1;
        while (originEntries.hasMoreElements()) {
            Matcher matcher = Util.classesIndexPattern.matcher(originEntries.nextElement().getName());
            if (!matcher.matches()) {
                continue;
            }
            int nowIndex = NumberUtils.toInt(matcher.group(1));
            if (nowIndex > maxIndex) {
                maxIndex = nowIndex;
            }
        }
        maxIndex++;


        // copy dex
        ZipFile driverApkZipFile = new ZipFile(engineAPk);
        Enumeration<ZipEntry> entries = driverApkZipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry originEntry = entries.nextElement();
            String entryName = originEntry.getName();
            if (entryName.contains("/")) {
                continue;
            }
            if (!entryName.endsWith(".dex")) {
                continue;
            }
            if (!entryName.startsWith("classes")) {
                continue;
            }

            zos.putNextEntry(new ReNameEntry(originEntry, "classes" + maxIndex + ".dex"));
            zos.write(IOUtils.toByteArray(driverApkZipFile.getInputStream(originEntry)));
            maxIndex++;

        }
        driverApkZipFile.close();
    }

    private static final int RES_TABLE_PACKAGE_TYPE = 0x0200;

    private static byte[] editARSCPackageName(byte[] arscData, BuildParamMeta buildParamMeta) {
        ArrayDataIn dataIn = ArrayDataIn.le(arscData);

        while (true) {
            int currentPosition = dataIn.getCurrentPosition();

            int type = dataIn.readUShortx();
            int headerSize = dataIn.readUShortx();
            int chunkSize = dataIn.readUIntx();

            if (type == ChunkType.TABLE) {
                //对于table类型的chunk来说，chunkSize代表了整个文件大小，也就是说ChunkType.TABLE类型的chunk是一个chunk容器
                dataIn.move(currentPosition + headerSize);
                continue;
            }

            if (type != RES_TABLE_PACKAGE_TYPE) {
                dataIn.move(currentPosition + chunkSize);
                continue;
            }


            //TODO 如果有多个package
            //一般为 127
            int packageId = dataIn.readUIntx();

            int packageBeginOffset = dataIn.getCurrentPosition();
            byte[] newPackageData = buildParamMeta.newPkgName.getBytes(StandardCharsets.UTF_16LE);
            if (newPackageData.length > 254) {
                throw new IllegalStateException("package too long: " + buildParamMeta.newPkgName);
            }

            System.arraycopy(newPackageData, 0, arscData, packageBeginOffset, newPackageData.length);

            for (int i = newPackageData.length; i < 256; i++) {
                if (arscData[packageBeginOffset + i] != 0) {
                    arscData[packageBeginOffset + i] = 0;
                } else {
                    break;
                }
            }
            return arscData;
        }
    }

    private static byte[] editManifestWithAXmlEditor(byte[] manifestFileData, CommandLine cmd, BuildParamMeta buildParamMeta, BuilderContext context) throws IOException {

        AxmlReader rd = new AxmlReader(manifestFileData);

        rd.accept(new ZeldaManifestHandlers.AndroidManifestDeclareCollector(buildParamMeta));

        //只能使用一次就不能复用
        rd = new AxmlReader(manifestFileData);
        AxmlWriter wr = new AxmlWriter();

        AxmlVisitor axmlVisitor = wr;

        if (context.cmd.hasOption('d')) {
            axmlVisitor = new EnableDebug(axmlVisitor);
        }
        axmlVisitor = AXmlEditorCmdHandler.handleCmd(axmlVisitor, context.axmlEditorCommand);

        axmlVisitor = new ReplacePackage(axmlVisitor, buildParamMeta.newPkgName);
        axmlVisitor = new ReplaceApplication(axmlVisitor, ClassNames.INJECT_ZELDA_APPLICATION.getClassName());
        //axmlVisitor = new ManifestHandlers.ReplaceApplication(axmlVisitor, buildParamMeta);
        axmlVisitor = new ZeldaManifestHandlers.FixRelativeClassName(axmlVisitor, buildParamMeta, context);
        axmlVisitor = new ZeldaManifestHandlers.ReNameProviderAuthorities(axmlVisitor, buildParamMeta);
        axmlVisitor = new ZeldaManifestHandlers.ReNamePermissionDeclare(axmlVisitor, buildParamMeta);

        rd.accept(axmlVisitor);
        return wr.toByteArray();
    }
}
