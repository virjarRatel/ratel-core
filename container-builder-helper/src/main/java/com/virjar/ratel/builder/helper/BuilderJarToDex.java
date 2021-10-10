package com.virjar.ratel.builder.helper;

import com.android.tools.r8.D8;
import com.virjar.ratel.allcommon.BuildEnv;
import com.virjar.ratel.allcommon.NewConstants;
import com.virjar.ratel.builder.helper.buildenv.BuildInfoEditor;
import com.virjar.ratel.builder.helper.proguard.OptimizeBuilderClass;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;


public class BuilderJarToDex {
    //java -jar /Users/virjar/git/ratel/ratel2/container-builder-transformer/build/libs/EngineBinTransformer-1.0.jar -s /Users/virjar/git/ratel/ratel2/script/dist/res/container-builder-repkg-1.2.9-SNAPSHOT.jar
    public static void main(String[] args) throws Exception {
        final Options options = new Options();
        options.addOption(new Option("s", "source", true, "path to container-builder-repkg-versionCode.jar"));
        options.addOption(new Option("d", "destination", true, "path to output jar"));
        options.addOption(new Option("h", "help", false, "path to output jar"));


        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, false);
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("EngineBinTransformer", options);
            return;
        }

        if (!cmd.hasOption('s')) {
            System.out.println("need pass builderJar");
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("EngineBinTransformer", options);
            return;
        }
        File sourcePath = new File(cmd.getOptionValue('s'));
        if (!sourcePath.exists() || !sourcePath.canRead() || !sourcePath.isFile()) {
            System.out.println("can not read builderJar");
            return;
        }

        File tmpFile = File.createTempFile("builder-dex", ".jar");
        FileUtils.copyFile(sourcePath, tmpFile, true);

        insertBuildInfoClass(tmpFile);
        // 使用Android的环境进行代码优化
        OptimizeBuilderClass.doOptimize("builder", tmpFile);

        File destinationFile;
        if (cmd.hasOption('d')) {
            destinationFile = new File(cmd.getOptionValue('d'));
        } else {
            String name = sourcePath.getName();
            int splitIndex = name.lastIndexOf(".");
            String newFileName;
            if (splitIndex < 0) {
                newFileName = name + "-dex.jar";
            } else {
                newFileName = name.substring(0, splitIndex) + "-dex" + name.substring(splitIndex);
            }

            destinationFile = new File(sourcePath.getParentFile(), newFileName);
        }


        //todo
        // 确定有一些功能在Android环境下肯定不会执行，如rdp模块
        // 所以我们这里可以对jar包的class进行修正，如删除rdp的代码
        // 是的最终输出到的dex更小一些

        // File tempFile = cleanJavaXClass(sourcePath);
        D8.main(new String[]{
                "--release",
                "--min-api", "21",
                "--output", destinationFile.getAbsolutePath(),
                "--lib", androidLibJar().getAbsolutePath(),
                tmpFile.getAbsolutePath()});

        // FileUtils.forceDelete(tempFile);

        migrateResourceFromJar(tmpFile, destinationFile);
    }

    private static void insertBuildInfoClass(File inputJarFile) throws IOException {
        BuildEnv.DEBUG = false;
        BuildEnv.ANDROID_ENV = true;
        BuildInfoEditor.editBuildInfoInBuilderJar(inputJarFile);
    }

    private static final Set<String> notMigrateFilter = new HashSet<String>() {{
        add("AndroidManifest.xml");
        add("resources.arsc");
        // dex模式运行在Android的，所以可以删除mac和windows的资源
        add(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_MAC.getNAME());
        add(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_WINDOWS.getNAME());
        add(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_LINUX.getNAME());
        add(NewConstants.BUILDER_RESOURCE_LAYOUT.ZIP_ALIGN_LINUX_LIB_CPP.getNAME());
        // 排除整个rdp模块
        for (NewConstants.BUILDER_RESOURCE_LAYOUT layout : NewConstants.BUILDER_RESOURCE_LAYOUT.values()) {
            if (layout.getNAME().startsWith(NewConstants.BUILDER_RESOURCE_LAYOUT.RDP_BASE.getNAME())) {
                add(layout.getNAME());
            }
        }
    }};

    private static void migrateResourceFromJar(File sourcePath, File destinationFile) throws IOException {
        ZipFile sourceZipFile = new ZipFile(sourcePath);
        File tempFile = File.createTempFile("temp-dex", ".jar");
        tempFile.deleteOnExit();
        ZipOutputStream zipOutputStream = new ZipOutputStream(tempFile);

        //java.util.zip.ZipException: invalid CEN header (duplicate entry)
        Set<String> addedEntry = new HashSet<>();

        Enumeration<ZipEntry> entries = sourceZipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().endsWith(".class")) {
                continue;
            }
            if (zipEntry.getName().startsWith("META-INF/")) {
                continue;
            }
            if (zipEntry.getName().endsWith(".java")) {
                continue;
            }
            if (notMigrateFilter.contains(zipEntry.getName())) {
                continue;
            }
            if (addedEntry.contains(zipEntry.getName())) {
                System.out.println("duplicate zip entry: " + zipEntry.getName());
                continue;
            }
            if (zipEntry.getName().toLowerCase().equals("RDP-1.0.jar".toLowerCase())) {
                //ignore rdp component
                continue;
            }
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(IOUtils.toByteArray(sourceZipFile.getInputStream(zipEntry)));
            addedEntry.add(zipEntry.getName());
        }
        sourceZipFile.close();

        ZipFile destinationZipFile = new ZipFile(destinationFile);
        Enumeration<ZipEntry> destinationZipFileEntries = destinationZipFile.getEntries();
        while (destinationZipFileEntries.hasMoreElements()) {
            ZipEntry zipEntry = destinationZipFileEntries.nextElement();
            if (addedEntry.contains(zipEntry.getName())) {
                System.out.println("duplicate zip entry: " + zipEntry.getName());
                continue;
            }
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(IOUtils.toByteArray(destinationZipFile.getInputStream(zipEntry)));
            addedEntry.add(zipEntry.getName());
        }
        destinationZipFile.close();

        zipOutputStream.close();
        FileUtils.forceDelete(destinationFile);
        FileUtils.moveFile(tempFile, destinationFile);
    }

    private static File androidLibJar() throws IOException {
        File tempFile = File.createTempFile("android", ".jar");
        tempFile.deleteOnExit();
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        InputStream resourceAsStream = BuilderJarToDex.class.getClassLoader().getResourceAsStream("android-21-jar.bin");
        assert resourceAsStream != null;
        IOUtils.copy(resourceAsStream, fileOutputStream);
        fileOutputStream.close();
        return tempFile;
    }
}
