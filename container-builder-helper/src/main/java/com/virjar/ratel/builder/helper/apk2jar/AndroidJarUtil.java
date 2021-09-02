package com.virjar.ratel.builder.helper.apk2jar;

import com.virjar.ratel.builder.helper.utils.Util;

import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;

import external.com.android.dx.BinaryOp;
import external.com.android.dx.Code;
import external.com.android.dx.DexMaker;
import external.com.android.dx.FieldId;
import external.com.android.dx.Local;
import external.com.android.dx.MethodId;
import external.com.android.dx.TypeId;

public class AndroidJarUtil {

    public static void transformApkToAndroidJar(File apkFile, File outJarFile) {
        transformApkToAndroidJar(apkFile, outJarFile, null);
    }

    public static void transformApkToAndroidJar(File apkFile, File outJarFile, PackageTrie packageTrie) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outJarFile)) {
            try (ZipFile zipFile = new ZipFile(apkFile)) {
                Pattern classesPattern = Pattern.compile("classes(\\d*)\\.dex");
                Enumeration<ZipEntry> entries = zipFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();

                    if (classesPattern.matcher(zipEntry.getName()).matches()) {
                        zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                        if (packageTrie == null) {
                            zipOutputStream.write(IOUtils.toByteArray(zipFile.getInputStream(zipEntry)));
                        } else {
                            //清除无意义的class，因为我们使用了Android的打包工具链，存在一些android本身的class，这些class对于我们来说没有意义
                            zipOutputStream.write(cleanClasses(zipFile.getInputStream(zipEntry), packageTrie));
                        }
                    } else if (zipEntry.getName().startsWith("lib/")
                        // || zipEntry.getName().startsWith("assets/")
                    ) {
                        zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                        // lib资源需要迁移过去，因为我们构建的时候需要lib资源
                        zipOutputStream.write(IOUtils.toByteArray(zipFile.getInputStream(zipEntry)));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] cleanClasses(InputStream inputStream, PackageTrie packageTrie) throws IOException {
        DexBackedDexFile dexBackedDexFile = new DexBackedDexFile(Opcodes.getDefault(), IOUtils.toByteArray(inputStream));// DexFileFactory.loadDexFile(originDex, Opcodes.getDefault());
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        boolean hasClass = false;
        for (DexBackedClassDef dexBackedClassDef : dexBackedDexFile.getClasses()) {
            String className = Util.descriptorToDot(dexBackedClassDef.getType());
            if (packageTrie.isSubPackage(className)) {
                dexPool.internClass(dexBackedClassDef);
                hasClass = true;
            }
        }

        if (!hasClass) {
            System.out.println("class empty fill with random classes");
            return genRandomHelloWorldClass();
        }

        MemoryDataStore memoryDataStore = new MemoryDataStore();
        dexPool.writeTo(memoryDataStore);
        memoryDataStore.close();
        return memoryDataStore.getData();
    }

    private static byte[] genRandomHelloWorldClass() {
        return genRandomHelloWorldClass("HelloWorld_");
    }

    public static byte[] genRandomHelloWorldClass(String preffix) {
        DexMaker dexMaker = new DexMaker();


        // Generate a HelloWorld class.

        TypeId<?> helloWorld = TypeId.get("L" + preffix.replaceAll("\\.", "/") + "_" + MD5(UUID.randomUUID().toString()) + ";");
        dexMaker.declare(helloWorld, preffix + ".generated", Modifier.PUBLIC, TypeId.OBJECT);

        TypeId<System> systemType = TypeId.get(System.class);
        TypeId<PrintStream> printStreamType = TypeId.get(PrintStream.class);

        MethodId hello = helloWorld.getMethod(TypeId.VOID, "hello");
        Code code = dexMaker.declare(hello, Modifier.STATIC | Modifier.PUBLIC);
        Local<Integer> a = code.newLocal(TypeId.INT);
        Local<Integer> b = code.newLocal(TypeId.INT);
        Local<Integer> c = code.newLocal(TypeId.INT);
        Local<String> s = code.newLocal(TypeId.STRING);
        Local<PrintStream> localSystemOut = code.newLocal(printStreamType);

        // int a = 0xabcd;
        code.loadConstant(a, 0xabcd);

        // int b = 0xaaaa;
        code.loadConstant(b, 0xaaaa);

        // int c = a - b;
        code.op(BinaryOp.SUBTRACT, c, a, b);

        // String s = Integer.toHexString(c);
        MethodId<Integer, String> toHexString
                = TypeId.get(Integer.class).getMethod(TypeId.STRING, "toHexString", TypeId.INT);
        code.invokeStatic(toHexString, s, c);

        // System.out.println(s);
        FieldId<System, PrintStream> systemOutField = systemType.getField(printStreamType, "out");
        code.sget(systemOutField, localSystemOut);
        MethodId<PrintStream, Void> printlnMethod = printStreamType.getMethod(
                TypeId.VOID, "println", TypeId.STRING);
        code.invokeVirtual(printlnMethod, null, localSystemOut, s);

        // return;
        code.returnVoid();

        return dexMaker.generate();
    }

    public static String MD5(String source) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(source.getBytes());
            return new BigInteger(1, messageDigest.digest()).toString(32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return source;
    }

}
