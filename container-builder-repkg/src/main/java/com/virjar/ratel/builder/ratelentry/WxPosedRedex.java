package com.virjar.ratel.builder.ratelentry;

import com.google.common.io.ByteStreams;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.builder.PackageTrie;
import com.virjar.ratel.builder.Util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.dexbacked.raw.HeaderItem;
import org.jf.dexlib2.dexbacked.raw.OdexHeaderItem;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.MethodReferenceRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.rewriter.TypeRewriter;
import org.jf.dexlib2.util.DexUtil;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;

public class WxPosedRedex {
    public static void main(String[] args) throws Exception {
        ZipFile input = new ZipFile("/Users/virjar/git/ratel/ratel2/com.fkzhang.wechatxposed_2.9_58_ratel.apk");
        File output = new File("/Users/virjar/git/ratel/ratel2/com.fkzhang.wechatxposed_2.9_58_ratel_redex.apk");
        if (output.exists()) {
            FileUtils.forceDelete(output);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(output);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        zipOutputStream.setEncoding("UTF8");

        Enumeration<? extends ZipEntry> entries = input.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry originEntry = entries.nextElement();
            String entryName = originEntry.getName();
            //remove sign data
            if (entryName.startsWith("META-INF/")) {
                continue;
            }

            //i will edit androidManifest.xml ,so skip it now
            if (entryName.equals("classes.dex")) {
                zipOutputStream.putNextEntry(new ZipEntry(originEntry));
                zipOutputStream.write(rewrite(IOUtils.toByteArray(input.getInputStream(originEntry))));
                continue;
            }
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            zipOutputStream.write(IOUtils.toByteArray(input.getInputStream(originEntry)));
        }
        input.close();

        zipOutputStream.close();
        fileOutputStream.close();

        File workDir = new File("/Users/virjar/Desktop/redex-work");
        if (workDir.exists()) {
            FileUtils.cleanDirectory(workDir);
        } else {
            FileUtils.forceMkdir(workDir);
        }
        Main.zipalign(output, workDir);

        File signatureKeyFile = new File(workDir, Constants.ratelDefaultApkSignatureKey);
        System.out.println("release ratel default apk signature key into : " + signatureKeyFile.getAbsolutePath());
        Main.copyAndClose(Main.class.getClassLoader().getResourceAsStream(Constants.ratelDefaultApkSignatureKey), new FileOutputStream(signatureKeyFile));

        Main.signatureApk(output, signatureKeyFile, null);
    }

    private static byte[] rewrite(byte[] input) throws IOException {
        DexBackedDexFile memoryDexFile = createMemoryDexFile(input);
        DexFile dexFile = new DexRewriter(new ClassNameRewriterModule()).rewriteDexFile(memoryDexFile);
        MemoryDataStore memoryDataStore = new MemoryDataStore();

        DexPool tempDexPool = new DexPool(Opcodes.getDefault());
        for (ClassDef classDef : dexFile.getClasses()) {
            tempDexPool.internClass(classDef);
        }
        tempDexPool.writeTo(memoryDataStore);

        return memoryDataStore.getData();
    }

    private static class ClassNameRewriterModule extends RewriterModule {


        /**
         * 修改方法名词
         *
         * @param rewriters
         * @return
         */
        @Nonnull
        @Override
        public Rewriter<MethodReference> getMethodReferenceRewriter(@Nonnull Rewriters rewriters) {
            return new MethodReferenceRewriter(rewriters) {
                @Nonnull
                @Override
                public MethodReference rewrite(@Nonnull MethodReference methodReference) {
                    return new RewrittenMethodReference(methodReference) {
                        @Nonnull
                        @Override
                        public String getName() {
                            return super.getName();
//                            String classTypeId = super.getDefiningClass();
//                            String methodName = super.getName();
//
//
//                            String now = handleMethodNameRewrite(classTypeId, methodName);
////                            if (Util.descriptorToDot(classTypeId).equals("com.fkzhang.xposed.hook.e")) {
////                                System.out.println("rewrite method: " + classTypeId + "->" + methodName + " to :" + classTypeId + "->" + now);
////                            }
//                            if (!now.equals(methodName)) {
//                                List<? extends CharSequence> parameterTypes = super.getParameterTypes();
//                                StringBuilder stringBuilder = new StringBuilder(now);
//                                for (CharSequence paramType : parameterTypes) {
//                                    if (!isAllCharsPrintable(paramType.toString())) {
//                                        continue;
//                                    }
//                                    paramType = "_" + paramType.toString().toLowerCase()
//                                            .replaceAll("\\.", "_")
//                                            .replaceAll("/", "_")
//                                            .replaceAll(";", "_")
//                                            .replaceAll("\\?", "")
//                                            .replaceAll(">", "")
//                                            .replaceAll("\\[", "")
//                                            .replaceAll("]", "")
//                                            .replaceAll("<", "");
//                                    stringBuilder.append(paramType);
//                                }
//                                now = now + stringBuilder.toString();
//                                System.out.println("rewrite method: " + classTypeId + "->" + methodName + " to :" + classTypeId + "->" + now);
//                            }
//                            return now;
                        }
                    };
                }
            };
        }


        /**
         * 修改类名
         *
         * @param rewriters
         * @return
         */
        @Nonnull
        @Override
        public Rewriter<String> getTypeRewriter(@Nonnull Rewriters rewriters) {
            //return super.getTypeRewriter(rewriters);

            PackageTrie packageTrie = new PackageTrie()
                    .addToTree("java")
                    .addToTree("android")
                    .addToTree("androidx")
                    .addToTree("javax")
                    .addToTree("org")
                    .addToTree("dalvik")
                    .addToTree("de");
            addMapping("Lcom/fkzhang/xposed/hook/WxCoreLoader;");
            addMapping("Lcom/fkzhang/wechatxposed/MainActivity;");
            addMapping("Lcom/fkzhang/wechatxposed/ModuleActiveCheck;");
            addMapping("Lcom/fkzhang/wechatxposed/XposedInit;");
            addMapping("Lcom/fkzhang/wechatxposed/R;");
            addMapping("Ljava");
            addMapping("Ljavax");
            addMapping("Landroid");
            addMapping("Landroidx");
            addMapping("Lorg");
            addMapping("Ldalvik");
            addMapping("Lde");
            // Set<String> hasPrinted = new HashSet<>();
            return new TypeRewriter() {
                @Nonnull
                @Override
                public String rewrite(@Nonnull String value) {
                    if (packageTrie.isSubPackage(Util.descriptorToDot(value))) {
                        //系统的定义，还有没有混淆数据，不重写
                        return super.rewrite(value);
                    }
                    if (!value.startsWith("L")) {
                        //基本类型数据，不重写
                        return super.rewrite(value);
                    }
                    String ret = handleRewrite(value);
                    //System.out.println("rewrite: " + value + " -> " + ret);
                    return ret;
                }
            };
        }
    }


    private static Map<String, String> renameMapping = new TreeMap<>();

    private static Map<String, Map<String, String>> methodMapping = new TreeMap<>();

    private static String handleMethodNameRewrite(String classTypeId, String methodName) {
        if (isAllCharsPrintable(methodName)) {
            return methodName;
        }
        Map<String, String> inMethodMapping = methodMapping.get(classTypeId);
        if (inMethodMapping == null) {
            inMethodMapping = new TreeMap<>();
            methodMapping.put(classTypeId, inMethodMapping);
        }
        if (inMethodMapping.containsKey(methodName)) {
            return inMethodMapping.get(methodName);
        }
        int retry = 0;
        String now = toPrintableSeq(retry);
        while (inMethodMapping.containsValue(now)) {
            now = toPrintableSeq(++retry);
        }
        inMethodMapping.put(methodName, now);
        return now;
    }


    private static String handleRewrite(String input) {
        if (input.length() < 3) {
            return input;
        }
        if (renameMapping.containsKey(input)) {
            //cache hinted
            return renameMapping.get(input);
        }
//        if (input.contains("Lcom/fkzhang/wechatxposed/MainActivity")) {
//            System.out.println("hinted");
//        }
        int start = 1, end = 2;
        while (end < input.length() - 1) {
            char nowCh = input.charAt(end);
            if (nowCh == '/' || nowCh == '$') {
                String prefix = input.substring(0, end);
                if (!renameMapping.containsKey(prefix)) {
                    String preNode = renameMapping.get(input.substring(0, start));
                    int retry = 0;
                    String now = preNode + toPrintableSeq(retry);
                    while (renameMapping.containsValue(now)) {
                        now = preNode + toPrintableSeq(++retry);
                    }
                    renameMapping.put(prefix, now);
                    renameMapping.put(prefix + "/", now + "/");
                    renameMapping.put(prefix + "$", now + "$");
                    renameMapping.put(prefix + ";", now + ";");
                    //System.out.println("add mapping : " + prefix + " ->" + now);
                }
                start = end + 1;
            }
            end++;
        }
        //最后是以分号结尾的
        String prefix = input.substring(0, end);
        if (!renameMapping.containsKey(prefix)) {
            String preNode = renameMapping.get(input.substring(0, start));
            int retry = 0;
            String now = preNode + toPrintableSeq(retry);
            while (renameMapping.containsValue(now)) {
                now = preNode + toPrintableSeq(++retry);
            }
            renameMapping.put(prefix, now);
            renameMapping.put(prefix + "/", now + "/");
            renameMapping.put(prefix + "$", now + "$");
            renameMapping.put(prefix + ";", now + ";");
            //System.out.println("add mapping : " + prefix + " ->" + now);
        }
        return renameMapping.get(input);
    }

    public static void addMapping(String typeId) {
        //Lcom/fkzhang/xposed/hook/
        for (int i = 1; i < typeId.length(); i++) {
            char nowCh = typeId.charAt(i);
            if (nowCh == '/' || nowCh == '$' || nowCh == ';') {
                String prefix = typeId.substring(0, i);
                renameMapping.put(prefix, prefix);
                renameMapping.put(prefix + "/", prefix + "/");
                renameMapping.put(prefix + ";", prefix + ";");
                renameMapping.put(prefix + "$", prefix + "$");
            }
        }
        renameMapping.put(typeId, typeId);
        renameMapping.put(typeId + "/", typeId + "/");
        renameMapping.put(typeId + ";", typeId + ";");
        renameMapping.put(typeId + "$", typeId + "$");
    }

    private static String toPrintableSeq(int num) {
        if (num < printSeqMapping.length) {
            return String.valueOf(printSeqMapping[num]);
        }
        return toPrintableSeq(num / printSeqMapping.length) + printSeqMapping[num % printSeqMapping.length];
    }

    private static char[] printSeqMapping = new char[('z' - 'a') + 1 + ('Z' - 'A') + 1];

    static {
        int index = 0;
        for (char i = 'a'; i <= 'z'; i++) {
            printSeqMapping[index++] = i;
        }
        for (char i = 'A'; i <= 'Z'; i++) {
            printSeqMapping[index++] = i;
        }
        renameMapping.put("L", "L");
    }

    private static DexBackedDexFile createMemoryDexFile(byte[] buffer) {

        if (HeaderItem.verifyMagic(buffer, 0)) {
            return new DexBackedDexFile(Opcodes.getDefault(), buffer);
            //a normal dex file
        }
        if (OdexHeaderItem.verifyMagic(buffer, 0)) {
            //this is a odex file
            try {
                ByteArrayInputStream is = new ByteArrayInputStream(buffer);
                DexUtil.verifyOdexHeader(is);
                is.reset();
                byte[] odexBuf = new byte[OdexHeaderItem.ITEM_SIZE];
                ByteStreams.readFully(is, odexBuf);
                int dexOffset = OdexHeaderItem.getDexOffset(odexBuf);
                if (dexOffset > OdexHeaderItem.ITEM_SIZE) {
                    ByteStreams.skipFully(is, dexOffset - OdexHeaderItem.ITEM_SIZE);
                }
                return new DexBackedOdexFile(Opcodes.getDefault(), odexBuf, ByteStreams.toByteArray(is));
            } catch (IOException e) {
                //while not happen
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("can not find out dex image in vm memory");
    }


    public static boolean isPrintableChar(int c) {
        return 32 <= c && c <= 126;
    }

    public static boolean isAllCharsPrintable(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!isPrintableChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
