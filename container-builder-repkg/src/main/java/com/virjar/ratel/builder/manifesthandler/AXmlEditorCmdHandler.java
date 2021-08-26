package com.virjar.ratel.builder.manifesthandler;

import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.NodeVisitor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AXmlEditorCmdHandler {
    public static AxmlVisitor handleCmd(AxmlVisitor axmlVisitor, List<String> cmdList) {
        if (cmdList.isEmpty()) {
            return axmlVisitor;
        }
        for (String cmd : cmdList) {
            axmlVisitor = edit(cmd, axmlVisitor);
        }

        return axmlVisitor;
    }

    private static final String TASK_ADD_PERMISSION = "add_permission_";

    private static AxmlVisitor edit(String cmd, AxmlVisitor axmlVisitor) {
        if (cmd.startsWith(TASK_ADD_PERMISSION)) {
            String permission = cmd.substring(TASK_ADD_PERMISSION.length());
            //<uses-permission android:name="android.permission.SEND_SMS" />
            return addPermission(axmlVisitor, permission);
        } else {
            return axmlVisitor;
        }
    }

    private static AxmlVisitor addPermission(AxmlVisitor axmlVisitor, String permission) {
        return new AxmlVisitor(axmlVisitor) {

            @Override
            public NodeVisitor visitFirst(String namespace, String name) {
                return new NodeVisitor(super.visitFirst(namespace, name)) {

                    private final Set<String> nowPermission = new HashSet<>();

                    @Override
                    public void visitEnd() {
                        if (!nowPermission.contains(permission)) {
                            NodeVisitor nodeVisitor = super.visitChild(AxmlVisitor.NS_ANDROID, "uses-permission");
                            nodeVisitor.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID,
                                    AxmlVisitor.TYPE_STRING, permission);
                        }
                        super.visitEnd();
                    }

                    @Override
                    public NodeVisitor visitChild(String ns, String name) {// application

                        if (!"uses-permission".equals(name)) {
                            return super.visitChild(ns, name);
                        }
                        return new NodeVisitor(super.visitChild(ns, name)) {

                            @Override
                            public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                super.visitContentAttr(ns, name, resourceId, type, obj);
                                if ("name".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)) {
                                    String permissionDeclare = obj.toString();
                                    nowPermission.add(permissionDeclare);
                                }
                            }

                        };
                    }
                };
            }
        };
    }
}
