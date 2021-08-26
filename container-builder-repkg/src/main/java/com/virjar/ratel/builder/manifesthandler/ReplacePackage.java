package com.virjar.ratel.builder.manifesthandler;

import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.NodeVisitor;

public class ReplacePackage extends AxmlVisitor {
    private String targetPackage;

    public ReplacePackage(AxmlVisitor av, String targetPackage) {
        super(av);
        this.targetPackage = targetPackage;
    }

    @Override
    public NodeVisitor visitFirst(String namespace, String name) {
        return new NodeVisitor(super.visitFirst(namespace, name)) {
            @Override
            public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                if ("package".equals(name)) {
                    obj = targetPackage;
                }
                super.visitContentAttr(ns, name, resourceId, type, obj);
            }

        };
    }
}
