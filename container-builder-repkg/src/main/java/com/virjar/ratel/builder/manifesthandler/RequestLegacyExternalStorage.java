package com.virjar.ratel.builder.manifesthandler;

import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.NodeVisitor;

public class RequestLegacyExternalStorage extends AxmlVisitor {
    public RequestLegacyExternalStorage(AxmlVisitor nv) {
        super(nv);
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
                        // android:debuggable(0x0101000f)=(type 0x12)0xffffffff
                        super.visitContentAttr(AxmlVisitor.NS_ANDROID, "requestLegacyExternalStorage", AxmlVisitor.requestLegacyExternalStorageID,
                                TYPE_INT_BOOLEAN, Boolean.TRUE);
                        super.visitEnd();
                    }
                };
            }
        };
    }
}