package com.virjar.ratel.builder.helper.bro;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.tools.zip.ZipFile;

import java.io.File;

public class OptimizeBuilderResource {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(new Option("", "input", true, "input apk file"));
        options.addOption(new Option("", "output", true, "output jar file"));
        options.addOption(new Option("h", "help", false, "show help message"));

        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args, false);
        if (cmd.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.setWidth(110);
            hf.printHelp("OPTIMIZE_BUILDER_RESOURCE", options);
            return;
        }

        File builderJarInputFile = new File(cmd.getOptionValue("input"));
        try (ZipFile zipFile = new ZipFile(builderJarInputFile)) {

        }


    }
}
