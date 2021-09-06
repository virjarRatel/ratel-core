package com.virjar.ratel.builder.ratelentry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.ArrayList;

public class BuilderContextParser {
    public static BuilderContext parse(String[] args) throws ParseException {
        BuilderContext builderContext = new BuilderContext();
        // 命令行参数解析
        builderContext.cmd = cmdParse(args);

        resolveWorkDir(builderContext);

        return builderContext;
    }

    private static void resolveWorkDir(BuilderContext builderContext) {
        if (builderContext.cmd.hasOption('w')) {
            builderContext.theWorkDir = new File(builderContext.cmd.getOptionValue('w'));
        } else {
            builderContext.theWorkDir = new File("ratel_work_dir_repkg");
        }
    }

    private static CommandLine cmdParse(String[] args) throws ParseException {
        Options options = setupOptions();
        DefaultParser parser = new DefaultParser();
        ArrayList<String> originParam = new ArrayList<>();
        for (String str : args) {
            // 不允许-Pxxx参数传递进来，目前我们认为这个可能是Gradle脚本传进来
            // 由于存在通过脚本debug模式进行改包，所以调试模式下可能存在-P参数
            if (!str.startsWith("-P") || str.length() <= 2) {
                originParam.add(str);
            }
        }
        return parser.parse(options, originParam.toArray(new String[]{}), false);
    }

    private static Options setupOptions() {
        Options options = new Options();
        options.addOption(new Option("t", "tell", false, "tell me the output apk file path"));
        options.addOption(new Option("o", "output", true, "the output apk path or output project path"));
        options.addOption(new Option("d", "debug", false, "add debuggable flag on androidManifest.xml "));
        options.addOption(new Option("w", "workdir", true, "set a ratel working dir"));
        options.addOption(new Option("p", "hookProvider", true, "no use , a deprecate flag,which ratel framework integrate sandhook"));
        options.addOption(new Option("e", "engine", true, "the ratel engine,now support appendDex|rebuildDex|shell"));
        options.addOption(new Option("h", "help", false, "print help message"));
        options.addOption(new Option("c", "certificate", true, "authorizer certificate file"));
        options.addOption(new Option("x", "extract", false, "extract origin apk from a ratel output apks"));
        options.addOption(new Option("l", "log", false, "add log component,we can call it from apktool baksmali"));
        options.addOption(new Option("s", "signature", false, "signature apk with ratel default KeyStore"));
        options.addOption(new Option("D", "decompile", false, "create a RDP(ratel decompile project) like \"apktool output project\"," +
                "this project only contains smali files and with capability to bypass signature check with ratel framework"));

        return options;
    }
}
