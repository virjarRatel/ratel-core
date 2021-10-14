package com.virjar.ratel.builder.helper.buildenv;

import com.virjar.ratel.allcommon.BuildEnv;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class BuildInfoEditor {
    public static void editBuildInfoInBuilderJar(File inputJarFile) throws IOException {
        byte[] bytes = ReCompileClass.recompile(BuildEnv.class);
        File tempFile = File.createTempFile("builder", ".jar");
        String classFileName = BuildEnv.class.getName().replaceAll("\\.", "/") + ".class";
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
                try (ZipFile zipFile = new ZipFile(inputJarFile)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                        if (zipEntry.getName().equals(classFileName)) {
                            System.out.println("replace BuildEnv.class");
                            zipOutputStream.write(bytes);
                        } else {
                            IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
                        }
                    }
                }
            }
        }
        FileUtils.forceDelete(inputJarFile);
        FileUtils.moveFile(tempFile, inputJarFile);
    }

}
