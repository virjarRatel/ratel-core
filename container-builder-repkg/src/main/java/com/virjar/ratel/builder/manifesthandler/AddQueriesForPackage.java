package com.virjar.ratel.builder.manifesthandler;

import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.NodeVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * 添加queries标签，在targetSdkVersion为安卓11之后包的可见性需要由该标签保证
 */
public class AddQueriesForPackage extends AxmlVisitor {

    private final Set<String> needAddPackages = new HashSet<>();

    public AddQueriesForPackage(AxmlVisitor av, Set<String> needAddPackages) {
        super(av);
        if (needAddPackages != null) {
            needAddPackages.addAll(needAddPackages);
        }
    }

    public AddQueriesForPackage(AxmlVisitor av, String needAddPackage) {
        super(av);
        if (needAddPackages != null) {
            needAddPackages.add(needAddPackage);
        }
    }

    @Override
    public NodeVisitor visitFirst(String namespace, String name) {
        return new NodeVisitor(super.visitFirst(namespace, name)){

            private final Set<String> nowPackages = new HashSet<>();

            @Override
            public NodeVisitor visitChild(String ns, String name) {
                if(!"queries".equals(name)){
                    return super.visitChild(ns, name);
                }
                return new NodeVisitor(super.visitChild(ns,name)){
                    @Override
                    public NodeVisitor visitChild(String ns, String name) {
                        if(!"package".equals(name)){
                            return super.visitChild(ns, name);
                        }
                        return new NodeVisitor(super.visitChild(ns, name)){
                            @Override
                            public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                super.visitContentAttr(ns, name, resourceId, type, obj);
                                if("name".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)){
                                    nowPackages.add(obj.toString());
                                    System.out.println("find the existed query package:" + obj);
                                }
                            }
                        };
                    }

                    @Override
                    public void visitEnd() {
                        for(String packageName : needAddPackages){
                            if(nowPackages.contains(packageName)){
                               continue;
                            }
                            NodeVisitor nodeVisitor = super.visitChild(AxmlVisitor.NS_ANDROID, "package");
                            nodeVisitor.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID,
                                    AxmlVisitor.TYPE_STRING, packageName);
                            System.out.println("query tag exists, add the packageName:" + packageName);
                        }
                        super.visitEnd();
                    }
                };
            }

            @Override
            public void visitEnd() {
                // 如果没有queries标签
                if(nowPackages.isEmpty()){
                    NodeVisitor nodeVisitor = super.visitChild(null, "queries");
                    for(String packageName : needAddPackages){
                        NodeVisitor packageVisitor = nodeVisitor.visitChild(AxmlVisitor.NS_ANDROID, "package");
                        packageVisitor.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID,
                                AxmlVisitor.TYPE_STRING, packageName);
                        System.out.println("query tag is not exist, add the packageName:" + packageName);
                    }
                }
                super.visitEnd();
            }
        };
    }
}
