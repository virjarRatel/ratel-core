package com.virjar.ratel.builder.helper;

import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.builder.helper.apk2jar.APK2Jar;
import com.virjar.ratel.builder.helper.bro.OptimizeBuilderResource;
import com.virjar.ratel.builder.helper.proguard.OptimizeBuilderClass;

public class Main {
    /***
     * 这是一系列的预处理逻辑，他的主要职责是处理ratel改包工具链中无法使用gradle本身的构建函数处理的逻辑。
     * 这些任务包括：
     *  1. 将构建工具从jar包格式转变为dex格式(给ratelManager使用)
     *  2. 优化构建工具jar包中的资源文件：如apk格式文件转换为jar格式（消除冗余的资源文件和冗余的class,我们的框架运行的时候只会使用到java代码，但是Android的构建工具将会产生apk文件，包含很多Android自带的class和自带的资源文件等）
     *  3. 产生smali模版文件，Ratel在改包过程，将会进行代码注入，这些注入代码是使用java书写，然后使用smali工具转化为smali文件，再注入到app中。这个转换过程只需要执行一次。（开发环境则需要每次执行）
     *  4. 优化jar的代码，进一步减少文件大小
     * 本模块干的都是脏活儿累活儿，他不需要考虑资源大小问题，因为他最终不会出现在发布版本的构建工具中
     */
    public static void main(String[] args) throws Exception {
        ClassNames.BUILDER_HELPER_MAIN.check(Main.class);
        if (args.length == 0) {
            showHelpMessage();
            return;
        }

        String action = args[0].trim().toUpperCase();

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0,
                subArgs.length);

        switch (action) {
            case "APK_TO_JAR":
                APK2Jar.main(subArgs);
                break;
            case "TRANSFORM_BUILDER_JAR":
                BuilderJarToDex.main(subArgs);
                break;
            case "OPTIMIZE_BUILDER_RESOURCE":
                OptimizeBuilderResource.main(subArgs);
                break;
            case "OPTIMIZE_BUILDER_CLASS":
                OptimizeBuilderClass.main(subArgs);
                break;
            case "-H":
            case "-h":
            default:
                showHelpMessage();
        }
    }

    private static void showHelpMessage() {
        System.out.println("BuilderHelper APK_TO_JAR ...option : transform apk to jar and clean unused class");
        System.out.println("BuilderHelper TRANSFORM_BUILDER_JAR ...option : transform ratel builder jar to dex file,so we can load it from ratel manager");
        System.out.println("BuilderHelper OPTIMIZE_BUILDER_RESOURCE ...option : optimize builder binding resource,like apk->dex,class clean,generate smali template");
        System.out.println("BuilderHelper -h : to show this message");
    }
}
