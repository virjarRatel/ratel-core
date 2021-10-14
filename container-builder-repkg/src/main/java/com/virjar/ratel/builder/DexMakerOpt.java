package com.virjar.ratel.builder;



import com.virjar.ratel.allcommon.Constants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import external.com.android.dx.Code;
import external.com.android.dx.DexMaker;
import external.com.android.dx.FieldId;
import external.com.android.dx.Local;
import external.com.android.dx.MethodId;
import external.com.android.dx.TypeId;

public class DexMakerOpt {
    public static File genDexMakerOptFile(File workDir) throws IOException {
        File dexMakerOptWorkDir = releaseDexJars(workDir);
        if (dexMakerOptWorkDir == null) {
            return null;
        }
        if (!dexMakerOptWorkDir.exists() || !dexMakerOptWorkDir.isDirectory()) {
            return null;
        }

        File[] files = dexMakerOptWorkDir.listFiles();
        if (files == null) {
            return null;
        }
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        Set<String> dexMakerOptClassNames = new HashSet<>();
        for (File file : files) {

            try {
                DexBackedDexFile dexBackedDexFile = DexFileFactory.loadDexFile(file, Opcodes.getDefault());
                Set<? extends DexBackedClassDef> classes = dexBackedDexFile.getClasses();
                for (ClassDef classDef : classes) {
                    System.out.println("merge dexmaker opt class:" + Util.descriptorToDot(classDef.getType()));
                    dexMakerOptClassNames.add(Util.descriptorToDot(classDef.getType()));
                    dexPool.internClass(classDef);
                }
            } catch (DexFileFactory.UnsupportedFileTypeException e1) {
                //如果是坏文件，直接删除
                e1.printStackTrace();
            } catch (Exception e) {
                System.out.println("error when load dexmaker opt jar file");
                e.printStackTrace();
            }
        }

        byte[] configClassDex = genConfigClass(dexMakerOptClassNames);
        DexBackedDexFile configClassDexFile = new DexBackedDexFile(Opcodes.getDefault(), configClassDex);
        for (ClassDef classDef : configClassDexFile.getClasses()) {
            System.out.println("add config generate class: " + Util.descriptorToDot(classDef.getType()));
            dexPool.internClass(classDef);
        }

        MemoryDataStore memoryDataStore = new MemoryDataStore();
        dexPool.writeTo(memoryDataStore);
        memoryDataStore.close();


        File file = new File(workDir, "dex_makder_opt.jar");
        ZipOutputStream zipOutputStream = new ZipOutputStream(file);
        ZipEntry zipEntry = new ZipEntry("classes.dex");
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(memoryDataStore.getData());
        zipOutputStream.close();
        //FileUtils.copyFile(file, new File("/Users/virjar/Desktop/dex_makder_opt.jar"));
        return file;
    }

    private static byte[] genConfigClass(Set<String> dexMakerOptClassNames) {
        DexMaker dexMaker = new DexMaker();
        TypeId<?> dexMakerConfigClassId = TypeId.get(Constants.dexMakerOptConfigClassId);
        dexMaker.declare(dexMakerConfigClassId, "DexMakerOptConfig.generated", Modifier.PUBLIC, TypeId.OBJECT);

        /**
         *    private static HashSet<String> optClassNames = new HashSet<>();
         *
         *     static {
         *         optClassNames.add("test");
         *     }
         *
         *
         *     public static boolean has(String methodSignatureHash) {
         *         return optClassNames.contains(methodSignatureHash);
         *     }
         */

        TypeId<HashSet> hashSetTypeId = TypeId.get(HashSet.class);

        FieldId<?, HashSet> optClassNames = dexMakerConfigClassId.getField(hashSetTypeId, "optClassNames");
        dexMaker.declare(optClassNames, Modifier.PRIVATE | Modifier.STATIC, null);

        //静态代码块
        MethodId<?, Void> staticInitializer = dexMakerConfigClassId.getStaticInitializer();
        Code staticInitCode = dexMaker.declare(staticInitializer, Modifier.STATIC);

        Local<HashSet> hashSet = staticInitCode.newLocal(hashSetTypeId);
        Local<String> stringLocal = staticInitCode.newLocal(TypeId.STRING);

        MethodId<HashSet, Void> hashSetConstructor = hashSetTypeId.getConstructor();
        staticInitCode.newInstance(hashSet, hashSetConstructor);

        //boolean add(E var1);
        MethodId<HashSet, Boolean> addMethod = hashSetTypeId.getMethod(TypeId.get(boolean.class), "add", TypeId.OBJECT);


        for (String item : dexMakerOptClassNames) {
            staticInitCode.loadConstant(stringLocal, item);
            staticInitCode.invokeVirtual(addMethod, null, hashSet, stringLocal);
        }
        staticInitCode.sput(optClassNames, hashSet);
        staticInitCode.returnVoid();

        //添加has方法

        MethodId<?, Boolean> hasMethod = dexMakerConfigClassId.getMethod(TypeId.get(boolean.class), "has", TypeId.STRING);
        Code hasMethodCode = dexMaker.declare(hasMethod, Modifier.PUBLIC | Modifier.STATIC);

        //Local<String> paramLocal = hasMethodCode.newLocal(TypeId.STRING);
        Local<String> methodSignatureHashLocal = hasMethodCode.getParameter(0, TypeId.STRING);

        hasMethodCode.sget(optClassNames, hashSet);
        // boolean contains(Object var1);
        MethodId<HashSet, Boolean> containsMethod = hashSetTypeId.getMethod(TypeId.get(boolean.class), "contains", TypeId.OBJECT);

        Local<Boolean> booleanLocal = hasMethodCode.newLocal(TypeId.get(boolean.class));
        hasMethodCode.invokeVirtual(containsMethod, booleanLocal, hashSet, methodSignatureHashLocal);
        hasMethodCode.returnValue(booleanLocal);
        return dexMaker.generate();
    }

    private static File releaseDexJars(File workDir) throws IOException {
        InputStream optResources = DexMakerOpt.class.getClassLoader().getResourceAsStream(Constants.dexmakerOptResource);
        if (optResources == null) {
            return null;
        }

        File dexmakerOptResourceZipFile = new File(workDir, Constants.dexmakerOptResource);
        IOUtils.copy(optResources, new FileOutputStream(dexmakerOptResourceZipFile));

        File dexMakerOptWorkDir = new File(workDir, "_dexmaker_opt");
        FileUtils.forceMkdir(dexMakerOptWorkDir);


        //unzip
        ZipFile zipFile = new ZipFile(dexmakerOptResourceZipFile);
        Enumeration<ZipEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(dexMakerOptWorkDir, zipEntry.getName()));
            IOUtils.copy(zipFile.getInputStream(zipEntry), fileOutputStream);
            fileOutputStream.close();
        }
        zipFile.close();

        return dexMakerOptWorkDir;

    }
}
