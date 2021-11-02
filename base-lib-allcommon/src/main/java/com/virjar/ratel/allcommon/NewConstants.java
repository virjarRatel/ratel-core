package com.virjar.ratel.allcommon;

/**
 * 新版本的常量定义，之前的太乱了
 */
public interface NewConstants {


    String DEX_BUILDER_ASSETS_NAME = "container-builder-repkg-dex.jar";

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
        TEMPLATE_DEX_FILE(LAYOUT_BASE.NAME + "/template.dex.bin", false),

        /**
         * template需要解成smali
         */
        TEMPLATE_SMALI_ZIP_FILE(LAYOUT_BASE.NAME + "/template-smali.zip.bin", false),


        /**
         * zip align对应的资源数据
         */
        ZIP_ALIGN_BASE(LAYOUT_BASE.NAME + "/zipalign", true, true),
        ZIP_ALIGN_LINUX(ZIP_ALIGN_BASE.NAME + "/linux/zipalign", false),
        // Linux上面，如果是64位，则需要手动添加c++的函数库
        ZIP_ALIGN_LINUX_LIB_CPP(ZIP_ALIGN_BASE.NAME + "/linux/lib64/libc++.so", false),
        ZIP_ALIGN_WINDOWS(ZIP_ALIGN_BASE.NAME + "/windows/zipalign.exe", false),
        ZIP_ALIGN_MAC(ZIP_ALIGN_BASE.NAME + "/mac/zipalign", false),
        ZIP_ALIGN_ANDROID(ZIP_ALIGN_BASE.NAME + "/android/zipalign.so", false),

        // 签名使用的默认证书
        DEFAULT_SIGN_KEY(LAYOUT_BASE.NAME + "/hermes_key", false),
        DEFAULT_SIGN_KEY_ANDROID(LAYOUT_BASE.NAME + "/hermes_bksv1_key", false),

        // RDP模块工具
        RDP_BASE(LAYOUT_BASE.NAME + "/rdp", true, true),

        RDP_GIT_IGNORE_1(RDP_BASE.NAME + "/gitignore", true),
        RDP_GIT_IGNORE(RDP_BASE.NAME + "/.gitignore", false),

        RDP_RESOURCE_DIR(RDP_BASE.NAME + "/ratel_resource", true, true),
        RDP_SH(RDP_RESOURCE_DIR.NAME + "/rdp.sh", false),
        RDP_BAT(RDP_RESOURCE_DIR.NAME + "/rdp.bat", false),
        RDP_JAR_FILE(RDP_RESOURCE_DIR.NAME + "/rdp.jar.bin", false),
        ;

        private final String NAME;
        private final boolean onlyDev;
        private final boolean dir;

        BUILDER_RESOURCE_LAYOUT(String NAME, boolean onlyDev, boolean dir) {
            this.NAME = NAME;
            this.onlyDev = onlyDev;
            this.dir = dir;
        }

        BUILDER_RESOURCE_LAYOUT(String NAME, boolean onlyDev) {
            this(NAME, onlyDev, false);
        }

        public String getNAME() {
            return NAME;
        }

        public boolean isOnlyDev() {
            return onlyDev;
        }

        public boolean isDir() {
            return dir;
        }
    }


}
