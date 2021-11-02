package com.virjar.ratel.builder.helper.proguard;

import com.virjar.ratel.allcommon.BuildEnv;
import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.builder.helper.buildenv.BuildInfoEditor;
import com.virjar.ratel.builder.helper.utils.Shell;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 优化builder的java代码，减少无用的class资源，给jar包瘦身，也让转化为出的dex文件变得跟小
 */
public class OptimizeBuilderClass {
    public static void main(String[] args) throws ParseException, IOException, InterruptedException {
        Options options = new Options();
        options.addOption(new Option("i", "input", true, "the builder jar file"));
        options.addOption(new Option("t", "template", true, "the builder jar file"));
        options.addOption(new Option("h", "help", false, "show help message"));

        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, false);
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("OPTIMIZE_BUILDER_CLASS", options);
            return;
        }

        File inputJarFile = new File(cmd.getOptionValue('i'));
        insertBuildInfoClass(inputJarFile);
        String templateKey = "builder";
        if (cmd.hasOption('t')) {
            templateKey = cmd.getOptionValue('t');
        }
        doOptimize(templateKey, inputJarFile);
    }

    public static void doOptimize(String templateKey, File inputJarFile) throws IOException, InterruptedException {
        File proguardJarFile = releaseProguardJar();

        File buildOptimizeOutputJar = File.createTempFile("builder-opt", ".jar");
        File proguardRules = buildProguardRules(buildOptimizeOutputJar, inputJarFile, templateKey);

        // execute command
        String command = "java -jar " +
                proguardJarFile.getAbsolutePath() +
                " " +
                "@" + proguardRules.getAbsolutePath();
        Shell.execute(command);

        // 对于jar包，如果创建了目录索引，那么在class 扫描的时候将会更快，特别是spring这类class扫描框架
        // 但是这个特性在Android中将不复存在
        // 另外，jar包还可以通过在jar中建立index文件加快扫描，这是另一种目录索引方案
        byte[] bytes = fixZipDirectoryLayout(buildOptimizeOutputJar);

        FileUtils.writeByteArrayToFile(inputJarFile, bytes);

        FileUtils.deleteQuietly(buildOptimizeOutputJar);
        FileUtils.deleteQuietly(proguardJarFile);
    }

    private static void insertBuildInfoClass(File inputJarFile) throws IOException {
        BuildEnv.DEBUG = false;
        BuildEnv.ANDROID_ENV = false;
        BuildInfoEditor.editBuildInfoInBuilderJar(inputJarFile);
    }

    private static byte[] fixZipDirectoryLayout(File buildOptimizeOutputJar) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        Set<String> allEntries = new HashSet<>();
        try (ZipFile zipFile = new ZipFile(buildOptimizeOutputJar)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                allEntries.add(zipEntry.getName());
                IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
            }

            Set<String> duplicateEntries = new HashSet<>(allEntries);
            for (String entry : allEntries) {
                if (entry.endsWith("/")) {
                    continue;
                }

                if (!entry.contains("/")) {
                    continue;
                }

                String dir = entry.substring(0, entry.lastIndexOf("/") + 1);
                if (duplicateEntries.contains(dir)) {
                    continue;
                }
                duplicateEntries.add(dir);

                // System.out.println("create directory entry for :" + dir);
                ZipEntry zipEntry = new ZipEntry(dir);
                zipOutputStream.putNextEntry(zipEntry);
            }
        }
        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private static File buildProguardRules(File buildOptimizeOutputJar, File inputBuilderJarFile, String templateKey) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("# input and output").append(System.lineSeparator());
        File javaHome = new File(System.getProperty("java.home"));
        // jdk的依赖
        if (new File(javaHome, "lib/rt.jar").exists()) {
            // java8以下
            sb.append("-libraryjars ").append(new File(javaHome, "lib/rt.jar")).append(System.lineSeparator());
            sb.append("-libraryjars ").append(new File(javaHome, "lib/jce.jar")).append(System.lineSeparator());
        } else {
            // java9以上
            sb.append("-libraryjars ").append(new File(javaHome, "jmods/java.base.jmod(!**.jar;!module-info.class)")).append(System.lineSeparator());
        }
        sb.append("-injars ").append(inputBuilderJarFile.getAbsolutePath()).append(System.lineSeparator());
        sb.append("-outjars ").append(buildOptimizeOutputJar.getAbsolutePath()).append(System.lineSeparator());

        // 映射表
        sb.append("-printmapping  ").append(new File(inputBuilderJarFile.getParent(), "builder-proguard.map").getAbsolutePath()).append(System.lineSeparator());

        InputStream inputStream = OptimizeBuilderClass.class.getClassLoader().getResourceAsStream(templateKey + ".pro.template");
        if (inputStream == null) {
            throw new IOException("can not find binding resource: builder.pro.template");
        }
        String proguardTemplate = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        for (ClassNames classNames : ClassNames.values()) {
            String template = "\\$\\{\\{" + classNames.name() + "}}";
            proguardTemplate = proguardTemplate.replaceAll(template, classNames.getClassName());
        }

        sb.append(proguardTemplate);

        File proguardRulesFile = File.createTempFile("proguard", ".pro");
        FileUtils.writeStringToFile(proguardRulesFile, sb.toString(), StandardCharsets.UTF_8);
        System.out.println(sb.toString());
        return proguardRulesFile;
    }

    private static File releaseProguardJar() throws IOException {
        // 释放proguard.jar
        File proguardFile = File.createTempFile("proguard", ".jar");
        InputStream inputStream = OptimizeBuilderClass.class.getClassLoader().getResourceAsStream("proguard.jar.bin");
        if (inputStream == null) {
            throw new IOException("can not find binding resource: proguard.jar.bin");
        }
        FileUtils.copyInputStreamToFile(inputStream, proguardFile);
        return proguardFile;
    }
}
