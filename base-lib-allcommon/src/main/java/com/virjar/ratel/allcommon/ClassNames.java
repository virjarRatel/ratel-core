package com.virjar.ratel.allcommon;

/**
 * 多个模块部件相互关联，通过多个部件无法在同一个classloader直接引用和检查所有的class<br>
 * 所以className在这里管理，如果未来ratel存在对抗，需要修改className，那么需要同步修改这张表
 */
public enum ClassNames {
    BUILDER_HELPER_MAIN("com.virjar.ratel.builder.helper.Main"),
    BUILDER_MAIN("com.virjar.ratel.builder.ratelentry.Main"),
    INJECT_REBUILD_BOOTSTRAP("com.virjar.ratel.inject.template.rebuild.BootStrap"),
    INJECT_REBUILD_BOOTSTRAP_CINT("com.virjar.ratel.inject.template.rebuild.BootStrapWithStaticInit"),


    INJECT_TOOL_SMALI_LOG("com.virjar.ratel.inject.template.RatelSmaliLog"),
    INJECT_TOOL_EVENT_NOTIFIER("com.virjar.ratel.inject.template.EventNotifier"),


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
