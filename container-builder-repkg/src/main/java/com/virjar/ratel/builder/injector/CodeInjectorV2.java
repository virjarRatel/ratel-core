package com.virjar.ratel.builder.injector;

import com.google.common.collect.Sets;
import com.virjar.ratel.allcommon.NewConstants;
import com.virjar.ratel.builder.BuildParamMeta;
import com.virjar.ratel.builder.Util;
import com.virjar.ratel.builder.ratelentry.BindingResourceManager;

import org.apache.commons.io.FileUtils;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import external.com.android.dex.Dex;
import external.com.android.dx.command.dexer.DxContext;
import external.com.android.dx.merge.CollisionPolicy;
import external.com.android.dx.merge.DexMerger;

/**
 * 核心思路来自于：
 * https://github.com/makeloveandroid/XpRoot/blob/main/src/main/kotlin/task/ModifyApplicationDexTask.kt
 */
public class CodeInjectorV2 {
    private static boolean hasCall = false;

    private final static Set<String> classImplementsByAndroid = Sets.newHashSet("android.app.Application", "android.app.AppComponentFactory");

    public static void doInject(DexFiles dexFiles, String entryClassName, BuildParamMeta buildParamMeta) throws IOException {
        DexFiles.DexFile dex = dexFiles.findClassInDex(entryClassName);
        if (dex == null) {
            throw new IllegalStateException("can not find class: " + entryClassName);
        }

        Set<String> mainClasses = Sets.newHashSet();
        mainClasses.add(entryClassName);
        // find supper class for entry
        // 父class的静态代码块将会于当前class先执行，故需要寻找到supperClass，
        while (true) {
            DexBackedClassDef classDef = dex.getClassDef(entryClassName);
            String supperClass = Util.descriptorToDot(classDef.getSuperclass());
            mainClasses.add(supperClass);
            if (supperClass != null && !classImplementsByAndroid.contains(supperClass)) {
                DexFiles.DexFile newDex = dexFiles.findClassInDex(supperClass);
                if (newDex == null) {
                    System.out.println("warning: can not find class: " + supperClass);
                } else {
                    entryClassName = supperClass;
                    dex = newDex;
                    continue;
                }
            }
            break;
        }
        dex.splitIfNeed(buildParamMeta, mainClasses);
        // dex 拆分之后，有一定的可能会导致我们的目标class跑到其他dex文件中去了，所以我们重新搜索一下dex
        dex = dexFiles.findClassInDex(entryClassName);

        System.out.println("handle inject for class: " + entryClassName);
        DexBackedClassDef classDef = dex.getClassDef(entryClassName);

        // 寻找到静态代码块
        DexBackedMethod cInitMethod = null;
        for (DexBackedMethod method : classDef.getDirectMethods()) {
            if (method.getName().equals("<clinit>")) {
                cInitMethod = method;
                break;
            }
        }

        ClassDefWrapper classDefWrapper = new ClassDefWrapper(classDef);
        List<Method> originDirectMethods = classDefWrapper.getOriginDirectMethods();
        if (cInitMethod == null) {
            originDirectMethods.add(InjectMethodBuilder.buildStaticContextMethod(classDef.getType()));
        } else {
            originDirectMethods.remove(cInitMethod);
            // 备份之前的cinit
            Method backupCInitMethod = InjectMethodBuilder.renameOriginCInitMethod(cInitMethod);
            originDirectMethods.add(backupCInitMethod);
            originDirectMethods.add(
                    InjectMethodBuilder.buildStaticContextMethod(classDef.getType(), backupCInitMethod)
            );
        }

        // 三个dex合并
        DexPool injectClassDex = new DexPool(Opcodes.getDefault());
        injectClassDex.internClass(classDefWrapper);
        byte[] injectClassDexData = DexPoolUtils.encodeDex(injectClassDex);

        Dex[] mergeDex;
        if (hasCall) {
            mergeDex = new Dex[]{
                    new Dex(injectClassDexData), // 被我们修改过的class，放在最前面
                    new Dex(dex.getRawData()),// 原始的dex数据
            };
        } else {
            File templateDexFile = BindingResourceManager.get(NewConstants.BUILDER_RESOURCE_LAYOUT.TEMPLATE_DEX_FILE);
            mergeDex = new Dex[]{
                    new Dex(injectClassDexData), // 被我们修改过的class，放在最前面
                    new Dex(FileUtils.readFileToByteArray(templateDexFile)), // 我们注入的class数据
                    new Dex(dex.getRawData()),// 原始的dex数据
            };
        }


        DexMerger dexMerger = new DexMerger(mergeDex, CollisionPolicy.KEEP_FIRST, new DxContext());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        dexMerger.merge().writeTo(byteArrayOutputStream);

        // 替换之前的dex文件
        dex.setRawData(byteArrayOutputStream.toByteArray());
        hasCall = true;
    }


}
