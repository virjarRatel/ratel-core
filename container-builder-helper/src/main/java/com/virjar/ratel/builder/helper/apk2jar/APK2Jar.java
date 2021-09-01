package com.virjar.ratel.builder.helper.apk2jar;

import com.google.common.base.Splitter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.Properties;

public class APK2Jar {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(new Option("", "input", true, "input apk file"));
        options.addOption(new Option("", "output", true, "output jar file"));
        options.addOption(new Option("", "keepScene", true, "keep class config: apk_to_jar.keep.runtime | apk_to_jar.keep.xposedBridge | apk_to_jar.keep.injectTemplate"));
        options.addOption(new Option("h", "help", false, "show help message"));

        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, false);
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("APK_TO_JAR", options);
            return;
        }

        Properties properties = new Properties();
        properties.load(APK2Jar.class.getClassLoader().getResourceAsStream("config.properties"));

        Iterable<String> keepScene = Splitter.on(",").omitEmptyStrings().trimResults().split(properties.getProperty(cmd.getOptionValue("keepScene")));

        PackageTrie packageTrie = new PackageTrie();
        for (String str : keepScene) {
            packageTrie.addToTree(str);
        }
        AndroidJarUtil.transformApkToAndroidJar(new File(cmd.getOptionValue("input")),
                new File(cmd.getOptionValue("output")), packageTrie);
    }
}
