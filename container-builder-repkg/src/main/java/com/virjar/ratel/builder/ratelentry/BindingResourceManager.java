package com.virjar.ratel.builder.ratelentry;

import com.virjar.ratel.allcommon.BuildEnv;
import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.allcommon.NewConstants;
import com.virjar.ratel.allcommon.ReflectUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 处理打包到rebuilder中的附属资源逻辑，包括资源数据索引，资源优化，解压等
 */
public class BindingResourceManager {

    private static File workDir;

    public static File get(NewConstants.BUILDER_RESOURCE_LAYOUT layout) {
        return new File(workDir, layout.getNAME());
    }

    public static void extract(File workDir) throws IOException {
        BindingResourceManager.workDir = workDir;
        // todo 这部分数据可以直接缓存，理论上每一个引擎版本都可以有一份缓存文件，extract流程只需要处理一次
        URL runtimeUrl = BindingResourceManager.class.getClassLoader().getResource(NewConstants.BUILDER_RESOURCE_LAYOUT.RUNTIME_JAR_FILE.getNAME());
        if (runtimeUrl == null) {
            // 请注意，这个分支只有AndroidStudio调试的时候才会走
            if (BuildEnv.DEBUG) {
                // 生产环境这个代码分支会被优化掉
                doOptimizeExtract(workDir);
            } else {
                throw new IllegalStateException("can not run doOptimizeExtract on production env");
            }
            return;
        }
        directExtract(workDir);
    }

    private static void directExtract(File baseDir) throws IOException {
        ClassLoader classLoader = BindingResourceManager.class.getClassLoader();
        for (NewConstants.BUILDER_RESOURCE_LAYOUT layout : NewConstants.BUILDER_RESOURCE_LAYOUT.values()) {
            if (!layout.isDir()) {
                InputStream inputStream = classLoader.getResourceAsStream(layout.getNAME());
                if (inputStream == null) {
                    continue;
                }
                File file = new File(baseDir, layout.getNAME());
                FileUtils.forceMkdirParent(file);
                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    IOUtils.copy(inputStream, fileOutputStream);
                }
            }
        }
    }


    private static void doOptimizeExtract(File workDir) throws IOException {
        ClassLoader classLoader = BindingResourceManager.class.getClassLoader();
        // 伪造一个jar包
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (NewConstants.BUILDER_RESOURCE_LAYOUT layout : NewConstants.BUILDER_RESOURCE_LAYOUT.values()) {
                if (!layout.isDir()) {
                    zipOutputStream.putNextEntry(new ZipEntry(layout.getNAME()));
                    InputStream inputStream = classLoader.getResourceAsStream(layout.getNAME());
                    if (inputStream == null) {
                        if (!layout.isOnlyDev()) {
                            continue;
                        }
                        throw new IOException("can not find resource: " + layout.getNAME());
                    }
                    IOUtils.copy(inputStream, zipOutputStream);
                }
            }
        }
        File tempFile = File.createTempFile("fake-binding-resource-builder", ".jar");
        FileUtils.writeByteArrayToFile(tempFile, byteArrayOutputStream.toByteArray());
        File helperJar = new File(workDir, NewConstants.BUILDER_RESOURCE_LAYOUT.BUILDER_HELPER_NAME.getNAME());
        FileUtils.forceMkdirParent(helperJar);

        try (final FileOutputStream fileOutputStream = new FileOutputStream(helperJar)) {
            IOUtils.copy(classLoader.getResourceAsStream(NewConstants.BUILDER_RESOURCE_LAYOUT.BUILDER_HELPER_NAME.getNAME()),
                    fileOutputStream);
        }

        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{helperJar.toURL()});

        Class<?> helperMainClass = ReflectUtil.findClass(ClassNames.BUILDER_HELPER_MAIN.getClassName(), urlClassLoader);

        ReflectUtil.callStaticMethod(helperMainClass, "main",
                (Object) new String[]{
                        "OPTIMIZE_BUILDER_RESOURCE",
                        "-i",
                        tempFile.getAbsolutePath(),
                        "-o",
                        workDir.getAbsolutePath() + "/"
                });
        FileUtils.deleteQuietly(tempFile);
    }
}
