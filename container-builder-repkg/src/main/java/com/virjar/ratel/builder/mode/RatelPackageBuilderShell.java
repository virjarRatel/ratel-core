package com.virjar.ratel.builder.mode;


import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.Util;
import com.virjar.ratel.builder.ratelentry.BuilderContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.pxb.android.axml.AxmlReader;
import org.jf.pxb.android.axml.AxmlVisitor;
import org.jf.pxb.android.axml.AxmlWriter;
import org.jf.pxb.android.axml.NodeVisitor;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class RatelPackageBuilderShell {
    public static void handleTask(File workDir, BuilderContext context, BuildParamMeta buildParamMeta,
                                  CommandLine cmd, ZipOutputStream zos
    ) throws IOException {

        ZipFile originAPKZip = context.infectApk.zipFile;
        Enumeration<ZipEntry> entries = originAPKZip.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry originEntry = entries.nextElement();
            String entryName = originEntry.getName();
            //remove sign data
            if (entryName.startsWith("META-INF/")) {
                continue;
            }
            if (Util.isRatelUnSupportArch(entryName)) {
                //过滤掉不支持的架构
                continue;
            }
            //remove code dex
            if (!entryName.contains("/") && entryName.endsWith(".dex") && entryName.startsWith("classes")) {
                continue;
            }
            if (entryName.startsWith("assets/")) {
                // we do not use original apk assets resources
                continue;
            }

            //i will edit androidManifest.xml ,so skip it now
            if (entryName.equals(Constants.manifestFileName)) {

                // edit manifest file
                System.out.println("edit androidManifest.xml entry");
                byte[] bytes = editManifestWithAXmlEditor(IOUtils.toByteArray(originAPKZip.getInputStream(originEntry)), cmd, buildParamMeta);
                zos.putNextEntry(new ZipEntry(originEntry));
                zos.write(bytes);
                continue;
            }
            // 可能用户排除一些arch
            boolean needCopy = false;
            if (!entryName.startsWith("lib") || context.arch.isEmpty()) {
                needCopy = true;
            } else {
                for (String str : context.arch) {
                    if (entryName.startsWith("lib/" + str)) {
                        needCopy = true;
                    }
                }
            }
            if (needCopy) {
                zos.putNextEntry(new ZipEntry(originEntry));
                zos.write(IOUtils.toByteArray(originAPKZip.getInputStream(originEntry)));
            }
        }
        //close
        originAPKZip.close();


        System.out.println("copy ratel engine dex resources");
        // copy dex
        ZipFile driverApkZipFile = new ZipFile(new File(workDir, Constants.RATEL_ENGINE_APK));
        entries = driverApkZipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry originEntry = entries.nextElement();
            String entryName = originEntry.getName();
            if (!entryName.contains("/") && entryName.endsWith(".dex") && entryName.startsWith("classes")) {
                zos.putNextEntry(new ZipEntry(originEntry));
                zos.write(IOUtils.toByteArray(driverApkZipFile.getInputStream(originEntry)));
            }
        }
        driverApkZipFile.close();


        System.out.println("apk build success!!");

    }

    private static byte[] editManifestWithAXmlEditor(byte[] manifestFileData, CommandLine cmd, BuildParamMeta buildParamMeta) throws IOException {


        AxmlReader rd = new AxmlReader(manifestFileData);
        AxmlWriter wr = new AxmlWriter();
        rd.accept(new AxmlVisitor(wr) {

            @Override
            public NodeVisitor visitFirst(String ns, String name) {// manifest
                return new NodeVisitor(super.visitFirst(ns, name)) {

                    @Override
                    public NodeVisitor visitChild(String ns, String name) {// application
                        if ("application".equals(name)) {
                            return new NodeVisitor(super.visitChild(ns, name)) {

                                //这里主要是为了remove原生的启动入口
                                @Override
                                public NodeVisitor visitChild(String ns, String name) {

                                    if (!"activity".equals(name)) {
                                        return super.visitChild(ns, name);
                                    }
                                    if (ns != null && !ns.equals(AxmlVisitor.NS_ANDROID)) {
                                        return super.visitChild(ns, name);
                                    }

                                    return new NodeVisitor(super.visitChild(ns, name)) {

                                        private String nowActivityName = null;

                                        @Override
                                        public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                            super.visitContentAttr(ns, name, resourceId, type, obj);
                                            if (name.equals("name") && ns.equals(AxmlVisitor.NS_ANDROID)) {
                                                nowActivityName = obj.toString();
                                            }
                                        }

                                        @Override
                                        public NodeVisitor visitChild(String ns, String name) {
                                            if (!nowActivityName.equals(buildParamMeta.launcherActivityClass)) {
                                                //过滤，只处理主activity
                                                return super.visitChild(ns, name);
                                            }

                                            return new NodeVisitor(super.visitChild(ns, name)) {
                                                @Override
                                                public NodeVisitor visitChild(String ns, String name) {
                                                    if (!"intent-filter".equals(name)) {
                                                        return super.visitChild(ns, name);
                                                    }
                                                    if (ns != null && !AxmlVisitor.NS_ANDROID.equals(ns)) {
                                                        return super.visitChild(ns, name);
                                                    }
                                                    return new NodeVisitor(super.visitChild(ns, name)) {
                                                        @Override
                                                        public NodeVisitor visitChild(String ns, String name) {

                                                            if (ns != null && !AxmlVisitor.NS_ANDROID.equals(ns)) {
                                                                return super.visitChild(ns, name);
                                                            }
                                                            if (name.equals("action")) {
                                                                return new NodeVisitor(super.visitChild(ns, name)) {

                                                                    @Override
                                                                    public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                                                        if (!"name".equals(name)) {
                                                                            super.visitContentAttr(ns, name, resourceId, type, obj);
                                                                            return;
                                                                        }
                                                                        if (ns != null && !AxmlVisitor.NS_ANDROID.equals(ns)) {
                                                                            super.visitContentAttr(ns, name, resourceId, type, obj);
                                                                            return;
                                                                        }
                                                                        if ("android.intent.action.MAIN".equals(obj)
                                                                        ) {
                                                                            obj = "ratel.disable.android.intent.action.MAIN";
                                                                        }
                                                                        super.visitContentAttr(ns, name, resourceId, type, obj);

                                                                    }
                                                                };
                                                            }
                                                            if (name.equals("category")) {
                                                                return new NodeVisitor(super.visitChild(ns, name)) {

                                                                    @Override
                                                                    public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                                                        if (!"name".equals(name)) {
                                                                            super.visitContentAttr(ns, name, resourceId, type, obj);
                                                                            return;
                                                                        }
                                                                        if (ns != null && !AxmlVisitor.NS_ANDROID.equals(ns)) {
                                                                            super.visitContentAttr(ns, name, resourceId, type, obj);
                                                                            return;
                                                                        }
                                                                        if ("android.intent.category.LAUNCHER".equals(obj)) {
                                                                            obj = "ratel.disable.android.intent.category.LAUNCHER";
                                                                        }
                                                                        super.visitContentAttr(ns, name, resourceId, type, obj);

                                                                    }
                                                                };
                                                            }
                                                            return super.visitChild(ns, name);
                                                        }

                                                    };

                                                }
                                            };
                                        }
                                    };
                                }

                                @Override
                                public void visitContentAttr(String ns, String name, int resourceId, int type, Object obj) {
                                    if (cmd.hasOption('d')) {
                                        if (AxmlVisitor.NS_ANDROID.equals(ns)
                                                && "debuggable".equals(name)) {
                                            return;
                                        }
                                    }

                                    super.visitContentAttr(ns, name, resourceId, type, obj);
                                }

                                @Override
                                public void visitEnd() {
                                    if (cmd.hasOption('d')) {
                                        // android:debuggable(0x0101000f)=(type 0x12)0xffffffff
                                        super.visitContentAttr(AxmlVisitor.NS_ANDROID, "debuggable", 0x0101000f,
                                                TYPE_INT_BOOLEAN, Boolean.TRUE);
                                    }
                                    super.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID, AxmlVisitor.TYPE_STRING, Constants.containerShellEngineBootstrapClass);

                                    //保存原来的application
                                    if (buildParamMeta.originApplicationClass != null) {
                                        NodeVisitor originApplicationMeta = super.visitChild(AxmlVisitor.NS_ANDROID, "meta-data");
                                        originApplicationMeta.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID, AxmlVisitor.TYPE_STRING, Constants.APPLICATION_CLASS_NAME);
                                        originApplicationMeta.visitContentAttr(AxmlVisitor.NS_ANDROID, "value", AxmlVisitor.VALUE_RESOURCE_ID, AxmlVisitor.TYPE_STRING, buildParamMeta.originApplicationClass);
                                    }

                                    //保存原来的activity
                                    NodeVisitor originActivityMeta = super.visitChild(AxmlVisitor.NS_ANDROID, "meta-data");
                                    originActivityMeta.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID, AxmlVisitor.TYPE_STRING, Constants.ratelLaunchActivityName);
                                    originActivityMeta.visitContentAttr(AxmlVisitor.NS_ANDROID, "value", AxmlVisitor.VALUE_RESOURCE_ID, AxmlVisitor.TYPE_STRING, buildParamMeta.launcherActivityClass);

                                    //插入新的activity
                                    NodeVisitor jumpActivity = super.visitChild(AxmlVisitor.NS_ANDROID, "activity");
                                    jumpActivity.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID, AxmlVisitor.TYPE_STRING, Constants.containerShellEngineJumpActivityClass);
                                    NodeVisitor newMainActivityIntentFilter = jumpActivity.visitChild(AxmlVisitor.NS_ANDROID, "intent-filter");

                                    NodeVisitor action = newMainActivityIntentFilter.visitChild(AxmlVisitor.NS_ANDROID, "action");
                                    action.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID, AxmlVisitor.TYPE_STRING, "android.intent.action.MAIN");

                                    NodeVisitor category = newMainActivityIntentFilter.visitChild(AxmlVisitor.NS_ANDROID, "category");
                                    category.visitContentAttr(AxmlVisitor.NS_ANDROID, "name", AxmlVisitor.NAME_RESOURCE_ID, AxmlVisitor.TYPE_STRING, "android.intent.category.LAUNCHER");


                                    super.visitEnd();
                                }
                            };
                        }
                        return super.visitChild(ns, name);
                    }


                };
            }

        });
        return wr.toByteArray();

//        ParserChunkUtils.xmlStruct.byteSrc = FileUtils.readFileToByteArray(manifestFile);
//        if (cmd.hasOption('d')) {
//            // add debuggable flag to debug the output apk
//            // XmlEditor.modifyAttr("application", "application", "debuggable", "true");
//            System.out.println("not support debug now");
//        }
//
//        //这里需要非常小心，因为applicationName是内置属性，其字符串偏移需要是固定的
//        //TODO 如果原始apk没有定义Application，那么这里可能出问题
//        //修改入口为我们定制的class
//        XmlEditor.modifyAttrNew("application", null, "name", Constants.containerShellEngineBootstrapClass);
//
//        //保存之前的入口
//        //TODO
//        XmlEditor.addTag("<meta-data android:name=\"" + Constants.ratelLaunchActivityName + "\" android:value=\"" + buildParamMeta.launcherActivityClass +
//                "\"/>");
//
//        if (buildParamMeta.originApplicationClass != null) {
//            XmlEditor.addTag("<meta-data android:name=\"" + Constants.APPLICATION_CLASS_NAME + "\" android:value=\"" +
//                    buildParamMeta.originApplicationClass + "\"/>");
//        }
//
//        //删除原来的主入口标签
//        XmlEditor.removeTag("action", "android.intent.action.MAIN");
//        XmlEditor.removeTag("category", "android.intent.category.LAUNCHER");
//
//        //插入跳转class
//        XmlEditor.addTag("<activity android:name=\"" + Constants.containerShellEngineJumpActivityClass + "\">\n" +
//                "\t\t\t<intent-filter>\n" +
//                "\t\t\t\t<action android:name=\"android.intent.action.MAIN\"/>\n" +
//                "\t\t\t\t<category android:name=\"android.intent.category.LAUNCHER\"/>\n" +
//                "\t\t\t</intent-filter>\n" +
//                "\t\t</activity>");
//
//        //删除所有provider 不删除看看，理论上不会出问题
////        for (String str : buildParamMeta.providerClasses) {
////            XmlEditor.removeTag("provider", str);
////        }
//
//        //保存文件
//        FileUtils.writeByteArrayToFile(manifestFile, ParserChunkUtils.xmlStruct.byteSrc);
    }

}
