package com.virjar.ratel.builder.manifesthandler;

import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.NodeVisitor;

public class ReplaceApplication extends AxmlVisitor {
    private String targetApplication;

    public ReplaceApplication(AxmlVisitor av, String targetApplication) {
        super(av);
        this.targetApplication = targetApplication;
    }

    @Override
    public NodeVisitor visitFirst(String namespace, String name) {
        return new NodeVisitor(super.visitFirst(namespace, name)) {

            @Override
            public NodeVisitor visitChild(String ns, String name) {// application
                if (!"application".equals(name)) {
                    return super.visitChild(ns, name);
                }

                return new NodeVisitor(super.visitChild(ns, name)) {

                    @Override
                    public void visitEnd() {
                        //替换 Application Name
                        super.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID,
                                AxmlVisitor.TYPE_STRING, targetApplication);
                        super.visitEnd();
                    }
                };
            }
        };
    }
}
