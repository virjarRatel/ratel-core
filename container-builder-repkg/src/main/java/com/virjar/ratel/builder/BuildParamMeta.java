package com.virjar.ratel.builder;

import org.w3c.dom.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated 已经过期，我们讲这些参数逐步迁移到BuilderContext中
 */
@Deprecated
public class BuildParamMeta {
    public String packageName;
    public String appEntryClass;
    public String originApplicationClass;
    public String launcherActivityClass;
    public String serialNo;
    public String buildTimestamp;
    public Document androidManifestXml;
    public String androidAppComponentFactory;
    //zelda引擎特有的字段
    public String newPkgName;
    public Set<String> declaredComponentClassNames = new HashSet<>();
    public String sufferKey;
    public Set<String> permissionDeclare = new HashSet<>();
    public Set<String> authorities = new HashSet<>();
    public Set<String> childProcess = new HashSet<>();
}