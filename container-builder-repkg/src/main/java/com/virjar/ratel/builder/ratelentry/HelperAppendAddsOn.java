package com.virjar.ratel.builder.ratelentry;

import com.virjar.ratel.builder.DexSplitter;
import com.virjar.ratel.builder.ReNameEntry;
import com.virjar.ratel.builder.Util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * addsOn 是一个特殊场景，主要给基于RDP有合并其他apk的功能，
 * 如果对一个融合过其他apk的apk进行再次打包，那么需要将addsOn资源重新抽取出来，附加在apk中
 * 但是本功能理论上很少使用，毕竟RDP都是少部分场景才会使用了
 */
public class HelperAppendAddsOn {
    static void mergeAddsOnFiles(File rawOriginApk, File nowOriginApk, File outputApk) throws IOException {
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
}
