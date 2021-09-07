package com.virjar.ratel.builder.ratelentry;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import org.apache.commons.cli.CommandLine;
import org.apache.tools.zip.ZipFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            zipFile.close();
        }
    }
}
