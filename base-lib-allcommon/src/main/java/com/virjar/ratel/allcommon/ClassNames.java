package com.virjar.ratel.allcommon;

/**
 * 多个模块部件相互关联，通过多个部件无法在同一个classloader直接引用和检查所有的class<br>
 * 所以className在这里管理，如果未来ratel存在对抗，需要修改className，那么需要同步修改这张表
 */
public enum ClassNames {
    //构建工具链优化器入口
    BUILDER_HELPER_MAIN("com.virjar.ratel.builder.helper.Main"),
    //重打包构建工具入口
    BUILDER_MAIN("com.virjar.ratel.builder.ratelentry.Main"),

    // 平头哥配套smali重打包方案入口
    RDP_MAIN("com.virjar.ratel.rdp.RDPBuilder"),

    // dex重编Android代码植入入口
    INJECT_REBUILD_BOOTSTRAP("com.virjar.ratel.inject.template.rebuild.BootStrap"),
    INJECT_REBUILD_BOOTSTRAP_CINT("com.virjar.ratel.inject.template.rebuild.BootStrapWithStaticInit"),

    // AndroidManifest入口替换代码植入入口
    INJECT_APPEND_APPLICATION("com.virjar.ratel.inject.template.append.RatelMultiDexApplication"),

    // app包名rename模式功能入口
    INJECT_ZELDA_APPLICATION("com.virjar.ratel.inject.template.zelda.RatelZeldaApplication"),

    // magisk和aosp源码自定义模式代码入口
    INJECT_KRATOS_BOOTSTRAP("com.virjar.ratel.inject.template.kratos.KratosBootstrap"),

    // smali工具类集合
    INJECT_TOOL_SMALI_LOG("com.virjar.ratel.inject.template.RatelSmaliLog"),
    INJECT_TOOL_EVENT_NOTIFIER("com.virjar.ratel.inject.template.EventNotifier"),
    INJECT_TOOL_SMALI_Helper("com.virjar.ratel.inject.template.hidebypass.Helper"),
    INJECT_TOOL_SMALI_Helper_Class("com.virjar.ratel.inject.template.hidebypass.Helper$Class"),
    INJECT_TOOL_SMALI_Helper_HandleInfo("com.virjar.ratel.inject.template.hidebypass.Helper$HandleInfo"),
    INJECT_TOOL_SMALI_Helper_MethodHandle("com.virjar.ratel.inject.template.hidebypass.Helper$MethodHandle"),
    INJECT_TOOL_SMALI_Helper_MethodHandleImpl("com.virjar.ratel.inject.template.hidebypass.Helper$MethodHandleImpl"),
    INJECT_TOOL_SMALI_Helper_NeverCall("com.virjar.ratel.inject.template.hidebypass.Helper$NeverCall"),
    INJECT_TOOL_SMALI_HiddenApiBypass("com.virjar.ratel.inject.template.hidebypass.HiddenApiBypass"),
    ;

    ClassNames(String className) {
        this.className = className;
    }

    private final String className;

    public void check(Class<?> clazz) {
        if (!clazz.getName().equals(className)) {
            throw new IllegalStateException("class name define error->  expected: " + className + " actually: " + clazz.getName());
        }
    }

    public String getClassName() {
        return className;
    }
}
