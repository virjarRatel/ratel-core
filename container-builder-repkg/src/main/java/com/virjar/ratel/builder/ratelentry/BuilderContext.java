package com.virjar.ratel.builder.ratelentry;

import net.dongliu.apk.parser.bean.ApkMeta;

import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 上下文，包含一个任务执行的所有数据
 */
public class BuilderContext {

    public ApkFile infectApk;
    public ApkFile xpModuleApk;

    public CommandLine cmd;

    public File theWorkDir = null;

    public List<String> propertiesConfig = new ArrayList<>();
    public List<String> axmlEditorCommand = new ArrayList<>();

    private static class ApkFile {
        public File apkFile;
        public ApkMeta apkMeta;
        public boolean isXpModule;
    }
}
