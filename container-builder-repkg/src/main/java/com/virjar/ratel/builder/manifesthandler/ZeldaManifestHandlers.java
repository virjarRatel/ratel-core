package com.virjar.ratel.builder.manifesthandler;


import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.ratelentry.BuilderContext;

import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.NodeVisitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ZeldaManifestHandlers {
    private static final Set<String> androidComponents = new HashSet<>(Arrays.asList("activity", "receiver", "service", "provider"));

    public static class AndroidManifestDeclareCollector extends AxmlVisitor {
        private BuildParamMeta buildParamMeta;

        public AndroidManifestDeclareCollector(BuildParamMeta buildParamMeta) {

            this.buildParamMeta = buildParamMeta;
        }

        @Override
        public NodeVisitor visitFirst(String namespace, String name) {

            return new NodeVisitor(super.visitFirst(namespace, name)) {


                @Override
                public NodeVisitor visitChild(String ns, String name) {// application

                    if ("permission".equals(name)) {
                        return new NodeVisitor(super.visitChild(ns, name)) {

                            @Override
                            public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                if ("name".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)) {
                                    String permissionDeclare = obj.toString();
                                    if (!permissionDeclare.startsWith("android.permission.")) {
                                        buildParamMeta.permissionDeclare.add(permissionDeclare);
                                    }
                                }
                                super.visitContentAttr(ns, name, resourceId, type, obj);
                            }
                        };
                    } else if ("application".equals(name)) {
                        return new NodeVisitor(super.visitChild(ns, name)) {

                            @Override
                            public NodeVisitor visitChild(String ns, String name) {
                                //activity receiver service provider
                                if (!androidComponents.contains(name)) {
                                    return super.visitChild(ns, name);
                                }
                                return new NodeVisitor(super.visitChild(ns, name)) {
                                    @Override
                                    public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                        if ("process".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)) {
                                            String process = obj.toString();
                                            buildParamMeta.childProcess.add(process);
                                        }
                                        super.visitContentAttr(ns, name, resourceId, type, obj);
                                    }
                                };
                            }
                        };
                    }
                    return super.visitChild(ns, name);

                }
            };
        }
    }


    /**
     * [INSTALL_FAILED_DUPLICATE_PERMISSION: Package virjar.zelda.comssandroidugca.xrvzNi attempting to redeclare permission com.ss.android.ugc.aweme.permission.MIPUSH_RECEIVE already owned by com.ss.android.ugc.aweme]
     */
    public static class ReNamePermissionDeclare extends AxmlVisitor {
        private BuildParamMeta zeldaBuildContext;

        public ReNamePermissionDeclare(AxmlVisitor av, BuildParamMeta zeldaBuildContext) {
            super(av);
            this.zeldaBuildContext = zeldaBuildContext;
        }

        @Override
        public NodeVisitor visitFirst(String namespace, String name) {

            return new NodeVisitor(super.visitFirst(namespace, name)) {


                @Override
                public NodeVisitor visitChild(String ns, String name) {// application

                    if ("permission".equals(name)) {
                        return new NodeVisitor(super.visitChild(ns, name)) {

                            @Override
                            public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                if ("name".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)) {
                                    String permissionDeclare = obj.toString();
                                    //这里看起来是重复逻辑 TODO
                                    if (zeldaBuildContext.permissionDeclare.contains(permissionDeclare)) {
                                        obj = permissionDeclare + "." + zeldaBuildContext.sufferKey;
                                    }
                                }

                                super.visitContentAttr(ns, name, resourceId, type, obj);
                            }
                        };
                    } else if ("uses-permission".equals(name)) {
                        return new NodeVisitor(super.visitChild(ns, name)) {

                            @Override
                            public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                if ("name".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)) {
                                    String usesPermission = obj.toString();
                                    if (zeldaBuildContext.permissionDeclare.contains(usesPermission)) {
                                        obj = usesPermission + "." + zeldaBuildContext.sufferKey;
                                    }
                                }

                                super.visitContentAttr(ns, name, resourceId, type, obj);
                            }
                        };
                    } else if ("application".equals(name)) {
                        return new NodeVisitor(super.visitChild(ns, name)) {

                            @Override
                            public NodeVisitor visitChild(String ns, String name) {
                                //activity receiver service provider
                                if (!androidComponents.contains(name)) {
                                    return super.visitChild(ns, name);
                                }
                                return new NodeVisitor(super.visitChild(ns, name)) {
                                    @Override
                                    public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                        if ("permission".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)) {
                                            String permission = obj.toString();
                                            if (zeldaBuildContext.permissionDeclare.contains(permission)) {
                                                obj = permission + "." + zeldaBuildContext.sufferKey;
                                            }
                                        }
                                        super.visitContentAttr(ns, name, resourceId, type, obj);
                                    }
                                };
                            }
                        };
                    } else {
                        return super.visitChild(ns, name);
                    }
                }
            };
        }
    }

    public static class ReNameProviderAuthorities extends AxmlVisitor {
        private BuildParamMeta zeldaBuildContext;

        public ReNameProviderAuthorities(AxmlVisitor av, BuildParamMeta zeldaBuildContext) {
            super(av);
            this.zeldaBuildContext = zeldaBuildContext;
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
                        public NodeVisitor visitChild(String ns, String name) {
                            if (!"provider".equals(name)) {
                                return super.visitChild(ns, name);
                            }
                            return new NodeVisitor(super.visitChild(ns, name)) {
                                @Override
                                public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                    if ("authorities".equals(name) && obj instanceof String && AxmlVisitor.NS_ANDROID.equals(ns)) {
                                        String authorities = obj.toString();
                                        zeldaBuildContext.authorities.add(authorities);
                                        obj = authorities + "." + zeldaBuildContext.sufferKey;
                                    }
                                    super.visitContentAttr(ns, name, resourceId, type, obj);
                                }
                            };
                        }
                    };
                }
            };
        }
    }

    public static class FixRelativeClassName extends AxmlVisitor {

        private BuildParamMeta zeldaBuildContext;
        private BuilderContext builderContext;

        public FixRelativeClassName(AxmlVisitor av, BuildParamMeta zeldaBuildContext,BuilderContext builderContext) {
            super(av);
            this.zeldaBuildContext = zeldaBuildContext;
            this.builderContext = builderContext;
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
                        public NodeVisitor visitChild(String ns, String name) {
                            //activity receiver service provider
                            if (!androidComponents.contains(name)) {
                                return super.visitChild(ns, name);
                            }
                            return new NodeVisitor(super.visitChild(ns, name)) {
                                @Override
                                public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                    if ("name".equals(name) && obj instanceof String) {
                                        String componentName = obj.toString();
                                        if (componentName.startsWith(".")) {
                                            obj = builderContext.infectApk.apkMeta.getPackageName() + componentName;
                                        }
                                        zeldaBuildContext.declaredComponentClassNames.add(obj.toString());
                                    }
                                    super.visitContentAttr(ns, name, resourceId, type, obj);
                                }
                            };
                        }
                    };
                }
            };
        }

    }


}