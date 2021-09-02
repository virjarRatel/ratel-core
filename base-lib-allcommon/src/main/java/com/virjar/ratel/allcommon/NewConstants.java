package com.virjar.ratel.allcommon;

/**
 * 新版本的常量定义，之前的太乱了
 */
public interface NewConstants {

    /**
     * builder的资源文件名称定义
     */
    enum BUILDER_RESOURCE_LAYOUT {
        LAYOUT_BASE("build-asset", true, true),
        BUILDER_HELPER_NAME(LAYOUT_BASE.NAME + "/builder-helper.jar.bin", true),
       // BUILDER_HELPER_NAME2(LAYOUT_BASE.NAME + "/builder-helper.jar.bin", true),

        RUNTIME_APK_FILE(LAYOUT_BASE.NAME + "/runtime.apk", true),
        RUNTIME_JAR_FILE(LAYOUT_BASE.NAME + "/runtime.jar", false),

        XPOSED_BRIDGE_APK_FILE(LAYOUT_BASE.NAME + "/xpBridge.apk", true),
        XPOSED_BRIDGE_JAR_FILE(LAYOUT_BASE.NAME + "/xpBridge.jar", false),

        /**
         * 入口代码定义，包括一些smali模版
         */
        TEMPLATE_APK_FILE(LAYOUT_BASE.NAME + "/template.apk", true),

        /**
         * appendDex模式下，直接使用dex文件
         */
        TEMPLATE_DEX_FILE(LAYOUT_BASE.NAME + "/template.dex", false),

        /**
         * template需要解成smali
         */
        TEMPLATE_SMALI_ZIP_FILE(LAYOUT_BASE.NAME + "/template-smali.zip.bin", false),
        ;

        private final String NAME;
        private final boolean raw;
        private final boolean dir;

        BUILDER_RESOURCE_LAYOUT(String NAME, boolean raw, boolean dir) {
            this.NAME = NAME;
            this.raw = raw;
            this.dir = dir;
        }

        BUILDER_RESOURCE_LAYOUT(String NAME, boolean raw) {
            this(NAME, raw, false);
        }

        public String getNAME() {
            return NAME;
        }

        public boolean isRaw() {
            return raw;
        }

        public boolean isDir() {
            return dir;
        }
    }


}
