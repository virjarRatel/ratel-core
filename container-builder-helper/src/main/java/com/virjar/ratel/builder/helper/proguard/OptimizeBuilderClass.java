package com.virjar.ratel.builder.helper.proguard;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 优化builder的java代码，减少无用的class资源，给jar包瘦身，也让转化为出的dex文件变得跟小
 */
public class OptimizeBuilderClass {
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(new Option("", "package-output", true, "the maven app assembler output directory"));
        options.addOption(new Option("", "proguard-jar", true, "proguard.jar is proguard main tool,we call proguard.jar to execute proguard task"));
        options.addOption(new Option("", "addition-libs", true, "some compile only dependencies, we pass this jar lib to help proguard.jar known more classes define"));
        options.addOption(new Option("h", "help", false, "show help message"));

        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, false);
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("OPTIMIZE_BUILDER_CLASS", options);
            return;
        }
    }
}
