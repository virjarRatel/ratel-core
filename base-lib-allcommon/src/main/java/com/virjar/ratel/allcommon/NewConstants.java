package com.virjar.ratel.allcommon;

/**
 * 新版本的常量定义，之前的太乱了
 */
public interface NewConstants {
    /**
     * builder的资源文件名称定义
     */
    interface BUILDER_RESOURCE_LAYOUT {
        String LAYOUT_BASE = "build-asset";
        String BUILDER_HELPER_NAME = LAYOUT_BASE + "/builder-helper.jar";

        String RUNTIME_APK_FILE = LAYOUT_BASE + "/runtime.apk";
        String RUNTIME_JAR_FILE = LAYOUT_BASE + "/runtime.jar";

        String XPOSED_BRIDGE_APK_FILE = LAYOUT_BASE + "/xpBridge.apk";
        String XPOSED_BRIDGE_JAR_FILE = LAYOUT_BASE + "/xpBridge.jar";

        /**
         * 入口代码定义，包括一些smali模版
         */
        String TEMPLATE_APK_FILE = LAYOUT_BASE + "/template.apk";

        /**
         * appendDex模式下，直接使用dex文件
         */
        String TEMPLATE_DEX_FILE = LAYOUT_BASE + "/template.dex";

        /**
         * template需要解成smali
         */
        String TEMPLATE_SMALI_ZIP_FILE = LAYOUT_BASE + "/template-smali.zip.bin";
    }


}
