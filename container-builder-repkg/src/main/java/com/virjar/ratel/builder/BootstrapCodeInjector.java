package com.virjar.ratel.builder;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.virjar.ratel.allcommon.ClassNames;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.builder.ratelentry.BuilderContext;
import com.virjar.ratel.builder.utils.SDK_VERSION_CODES;
import com.virjar.ratel.builder.utils.SmaliBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.InlineMethodResolver;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import external.com.android.dex.Dex;
import external.com.android.dx.command.dexer.DxContext;
import external.com.android.dx.merge.CollisionPolicy;
import external.com.android.dx.merge.DexMerger;

public class BootstrapCodeInjector {

    public static File injectCInit(File dexImage,
                                   File workDir,
                                   File bootstrapAPKDecodeDir,
                                   String className, boolean decodeAllSmali) throws IOException {
        File runtimeSmaliDir = makeSureRuntimeSmaliDir(workDir);
        doStaticLink(className, smaliFile(runtimeSmaliDir, Util.descriptorToDot(className)), bootstrapAPKDecodeDir, runtimeSmaliDir, dexImage, decodeAllSmali, true);
        File rebuildDex = new File(workDir, "injected.dex");
        System.out.println("create inject entry dex info :" + rebuildDex.getAbsolutePath());
        try {
            SmaliBuilder.build(runtimeSmaliDir, rebuildDex, Opcodes.getDefault().api, !decodeAllSmali);
        } catch (Exception e) {
            throw new SmaliRebuildFailedException(e);
        } finally {
            //这里可能build失败，上游会有失败复原机制，所以这里需要把工作空间清理干净
            //FileUtils.cleanDirectory(runtimeSmaliDir);
        }
        System.out.println("last dex time" + dexImage.lastModified());
        System.out.println("path " + dexImage.getAbsolutePath() + ",update last dex time" + dexImage.lastModified());
        // FileUtils.deleteQuietly(runtimeSmaliDir);

        DexMerger dexMerger = new DexMerger(new Dex[]{new Dex(rebuildDex), new Dex(dexImage)}, CollisionPolicy.KEEP_FIRST, new DxContext());
        File mergedDex = new File(workDir, "merged.dex");
        System.out.println("merge dex: " + rebuildDex.getAbsolutePath() + " and :" + dexImage.getAbsolutePath() + " into: " + mergedDex.getAbsolutePath());
        try {
            dexMerger.merge().writeTo(mergedDex);
        } catch (Throwable e) {
            throw new DexMergeFailedException(e);
        }
        return mergedDex;


    }

    public static File injectBootstrapCode(File dexImage,
                                           File workDir,
                                           File bootstrapAPKDecodeDir,
                                           BuildParamMeta buildParamMeta,
                                           BuilderContext context,
                                           boolean injectLogComponent, boolean decodeAllSmali
    ) throws IOException, DexMergeFailedException {
        if (context.infectApk.apkMeta.getPackageName().equals(Constants.RATEL_MANAGER_PACKAGE)) {
            //rm 进行代码注入
            File rebuildDex = new File(workDir, "not_inject_for_rm.dex");
            FileUtils.copyFile(dexImage, rebuildDex);
            return rebuildDex;
        }

        DexBackedDexFile dexBackedDexFile = DexFileFactory.loadDexFile(dexImage, Opcodes.getDefault());
        Map<String, DexBackedClassDef> classDefMap = Maps.newHashMap();
        for (DexBackedClassDef backedClassDef : dexBackedDexFile.getClasses()) {
            classDefMap.put(Util.descriptorToDot(backedClassDef.getType()), backedClassDef);
        }


        File runtimeSmaliDir = makeSureRuntimeSmaliDir(workDir);
        String bootStrapSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_REBUILD_BOOTSTRAP.getClassName());

        FileUtils.copyFile(new File(bootstrapAPKDecodeDir, bootStrapSmaliFilePath),
                new File(runtimeSmaliDir, bootStrapSmaliFilePath)
        );
        if (injectLogComponent) {
            System.out.println("inject ratel SmaliLog component!!");
            String logSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_LOG.getClassName());
            FileUtils.copyFile(
                    new File(bootstrapAPKDecodeDir, logSmaliFilePath),
                    new File(runtimeSmaliDir, logSmaliFilePath)
            );
        }

        String helperSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_Helper.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, helperSmaliFilePath),
                new File(runtimeSmaliDir, helperSmaliFilePath)
        );

        String helperClassSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_Helper_Class.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, helperClassSmaliFilePath),
                new File(runtimeSmaliDir, helperClassSmaliFilePath)
        );
        String helperHandleInfoSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_Helper_HandleInfo.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, helperHandleInfoSmaliFilePath),
                new File(runtimeSmaliDir, helperHandleInfoSmaliFilePath)
        );
        String helperMethodHandleSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_Helper_MethodHandle.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, helperMethodHandleSmaliFilePath),
                new File(runtimeSmaliDir, helperMethodHandleSmaliFilePath)
        );
        String helperMethodHandleImplSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_Helper_MethodHandleImpl.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, helperMethodHandleImplSmaliFilePath),
                new File(runtimeSmaliDir, helperMethodHandleImplSmaliFilePath)
        );
        String helperNeverCallSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_Helper_NeverCall.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, helperNeverCallSmaliFilePath),
                new File(runtimeSmaliDir, helperNeverCallSmaliFilePath)
        );

        String hiddenApiBypassSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_HiddenApiBypass.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, hiddenApiBypassSmaliFilePath),
                new File(runtimeSmaliDir, hiddenApiBypassSmaliFilePath)
        );

        String logSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_TOOL_SMALI_LOG.getClassName());
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, logSmaliFilePath),
                new File(runtimeSmaliDir, logSmaliFilePath)
        );

        injectIntoContextEntry(runtimeSmaliDir, bootstrapAPKDecodeDir, buildParamMeta.appEntryClass, classDefMap, dexImage, decodeAllSmali);

        File rebuildDex = new File(workDir, "injected.dex");
        System.out.println("create inject entry dex info :" + rebuildDex.getAbsolutePath());
        try {
            SmaliBuilder.build(runtimeSmaliDir, rebuildDex, Opcodes.getDefault().api, !decodeAllSmali);
        } catch (Exception e) {
            throw new SmaliRebuildFailedException(e);
        } finally {
            //这里可能build失败，上游会有失败复原机制，所以这里需要把工作空间清理干净
            FileUtils.cleanDirectory(runtimeSmaliDir);
        }
        System.out.println("last dex time" + dexImage.lastModified());
        System.out.println("path " + dexImage.getAbsolutePath() + ",update last dex time" + dexImage.lastModified());
        // FileUtils.deleteQuietly(runtimeSmaliDir);

        DexMerger dexMerger = new DexMerger(new Dex[]{new Dex(rebuildDex), new Dex(dexImage)}, CollisionPolicy.KEEP_FIRST, new DxContext());
        File mergedDex = new File(workDir, "merged.dex");
        System.out.println("merge dex: " + rebuildDex.getAbsolutePath() + " and :" + dexImage.getAbsolutePath() + " into: " + mergedDex.getAbsolutePath());
        try {
            dexMerger.merge().writeTo(mergedDex);
        } catch (Throwable e) {
            throw new DexMergeFailedException(e);
        }
        return mergedDex;
    }



    private static int mapSdkShorthandToVersion(String sdkVersion) {
        switch (sdkVersion.toUpperCase()) {
            case "M":
                return SDK_VERSION_CODES.M;
            case "N":
                return SDK_VERSION_CODES.N;
            case "O":
                return SDK_VERSION_CODES.O;
            case "P":
                return SDK_VERSION_CODES.P;
            default:
                return Integer.parseInt(sdkVersion);
        }
    }


    private static File makeSureRuntimeSmaliDir(File originAPKDecodeDir) throws IOException {
        File smaliDir = new File(originAPKDecodeDir, "runtimeSmali");
        FileUtils.deleteDirectory(smaliDir);
        smaliDir.mkdirs();
        return smaliDir;
    }


    private static void injectIntoContextEntry(File runtimeSmaliDir, File bootstrapAPKDecodeDir
            , String applicationClass
            , Map<String, DexBackedClassDef> applicationDexMap,
                                               File dexImage, boolean decodeAllSmali
    ) throws IOException {


        //find out attachBaseContext || .method protected attachBaseContext(Landroid/content/Context;)V
        DexBackedClassDef dexClass = applicationDexMap.get(applicationClass);
        if (dexClass == null) {
            throw new RuntimeException("unable to find application define for class: " + applicationClass);
        }
        //TODO 如果app继承关系跨dex，那么此时方案会有问题

        String staticInjectCodeEntry = findStaticInjectCodeEntry(dexClass, applicationDexMap);
        if (staticInjectCodeEntry != null && doStaticLink(staticInjectCodeEntry,
                smaliFile(runtimeSmaliDir, Util.descriptorToDot(staticInjectCodeEntry)), bootstrapAPKDecodeDir, runtimeSmaliDir, dexImage, decodeAllSmali, false)
        ) {
            return;
        }
        String attachBaseContextInjectCodeEntry = findAttachBaseContextInjectCodeEntry(dexClass, applicationDexMap);
        dexClass = applicationDexMap.get(Util.descriptorToDot(attachBaseContextInjectCodeEntry));
        handleAttachBaseContext(attachBaseContextInjectCodeEntry, dexClass, smaliFile(runtimeSmaliDir, Util.descriptorToDot(attachBaseContextInjectCodeEntry)), dexImage, runtimeSmaliDir, decodeAllSmali);
    }


    private static File smaliFile(File runtimeSmaliDir, String applicationClass) {
        // String applicationSmaliFilePath = applicationClass.replaceAll("\\.", File.separator) + ".smali";
        // File.separator -> character to be escaped is mission
        String applicationSmaliFilePath = applicationClass.replaceAll("\\.", "/") + ".smali";
        return new File(runtimeSmaliDir, applicationSmaliFilePath);
    }

    /**
     * 寻找代码置入点
     *
     * @param codeLines   代码内容
     * @param methodStart 搜索开始索引，一般为方法起始索引
     * @return index的下一行开始插入代码
     */
    private static int findInjectIndex(List<String> codeLines, int methodStart) {
        for (int i = methodStart + 1; i < codeLines.size(); i++) {
            String line = StringUtils.trimToEmpty(codeLines.get(i));
            if (StringUtils.isBlank(line)) {
                continue;
            }
            if (StringUtils.startsWith(line, ".locals ")) {
                continue;
            }
            if (StringUtils.startsWith(line, ".param ")) {
                continue;
            }
            if (StringUtils.startsWith(line, ".line ")) {
                continue;
            }

            if (StringUtils.startsWith(line, ".prologue")) {
                continue;
            }

            if (StringUtils.startsWith(line, ".registers")) {
                continue;
            }

            if (StringUtils.startsWith(line, ".annotation")) {
                /**
                 *  .locals 2
                 *  38     .annotation build Landroid/annotation/SuppressLint;
                 *  39         value = {
                 *  40             "ObsoleteSdkInt"
                 *  41         }
                 *  42     .end annotation
                 */

                while (i < codeLines.size()) {
                    i++;
                    line = StringUtils.trimToEmpty(codeLines.get(i));
                    if (StringUtils.isBlank(line)) {
                        continue;
                    }
                    if (StringUtils.startsWith(line, ".end annotation")) {
                        break;
                    }
                }
                continue;
            }

            //backtrace to a blank line
            for (int j = i; j > 0; j--) {
                if (StringUtils.isBlank(codeLines.get(j))) {
                    return j;
                }
            }
            break;
        }
        return -1;
    }

    private static boolean needShiftRegister(List<String> codeLines, int methodStart) {
        for (int i = methodStart + 1; i < codeLines.size(); i++) {
            String line = StringUtils.trimToEmpty(codeLines.get(i));
            if (StringUtils.isBlank(line)) {
                continue;
            }
            if (StringUtils.startsWith(line, ".locals ")) {
                int registerSize = NumberUtils.toInt(line.substring(".locals ".length()));
                //当寄存器数量超过15，那么无法直接访问p0,p1，需要使用 move-object/from16 指令迁移到v0-v15
                return registerSize > 15;
            } else if (StringUtils.startsWith(line, ".registers ")) {
                int registerSize = NumberUtils.toInt(line.substring(".registers ".length()));
                //当寄存器数量超过15，那么无法直接访问p0,p1，需要使用 move-object/from16 指令迁移到v0-v15
                return registerSize > 15;
            }

            if (StringUtils.startsWith(line, ".end method")) {
                return false;
            }

        }
        return false;
    }

    private static boolean doStaticLink(String applicationClassName, File applicationSmaliFile,
                                        File bootstrapAPKDecodeDir, File runtimeSmaliDir, File dexImage,
                                        boolean decodeAllSmali, boolean force
    ) throws IOException {
        String bootStrapCintSmaliFilePath = Util.classNameToSmaliPath(ClassNames.INJECT_REBUILD_BOOTSTRAP_CINT.getClassName());
        String bootStrapCintNativeName = ClassNames.INJECT_REBUILD_BOOTSTRAP_CINT.getClassName().replaceAll("\\.", "/");
        FileUtils.copyFile(
                new File(bootstrapAPKDecodeDir, bootStrapCintSmaliFilePath),
                new File(runtimeSmaliDir, bootStrapCintSmaliFilePath)
        );

        if (decodeAllSmali) {
            baksmali(null, DexFileFactory.loadDexFile(dexImage, Opcodes.getDefault()), runtimeSmaliDir);
        } else {
            baksmali(applicationClassName, DexFileFactory.loadDexFile(dexImage, Opcodes.getDefault()), runtimeSmaliDir);
        }

        if (!applicationSmaliFile.exists() || !applicationSmaliFile.isFile()) {
            throw new IllegalStateException("can not read application class: " + applicationSmaliFile.getCanonicalPath());
        }


        List<String> codeLines = FileUtils.readLines(applicationSmaliFile, Charsets.UTF_8);
        //find attachBaseContext start
        int cinitMethodIndex = -1;
        for (int i = 0; i < codeLines.size(); i++) {
            String line = StringUtils.trimToEmpty(codeLines.get(i));
            if (line.startsWith(".method static constructor <clinit>()V")
                    || line.startsWith(".method public static constructor <clinit>()V")) {
                cinitMethodIndex = i;
                break;
            }
        }

        List<String> newCodeLines = Lists.newArrayListWithExpectedSize(codeLines.size() + 4);

        int injectIndex = codeLines.size() - 1;

        if (cinitMethodIndex < 0) {
            if (!force) {
                return false;
            }
            //copy before
            for (int i = 0; i <= injectIndex; i++) {
                newCodeLines.add(codeLines.get(i));
            }
            boolean hasLineDeclared = FileUtils.readFileToString(applicationSmaliFile, Charsets.UTF_8).contains(".line ");
            //inject instruction
            newCodeLines.add("# direct methods");
            newCodeLines.add(".method static constructor <clinit>()V");
            if (hasLineDeclared) {
                newCodeLines.add("    .line 0");
            }
            newCodeLines.add(".locals 0");
            newCodeLines.add("    invoke-static {}, L" + bootStrapCintNativeName + ";->startup()V");
            newCodeLines.add("    ");
            newCodeLines.add("    return-void");
            newCodeLines.add(".end method");
        } else {
            injectIndex = findInjectIndex(codeLines, cinitMethodIndex);
            if (injectIndex < 0) {
                throw new IllegalStateException("error to locate method instruction for <clinit> ");
            }
            //copy before
            for (int i = 0; i <= injectIndex; i++) {
                newCodeLines.add(codeLines.get(i));
            }
            boolean hasLineDeclared = FileUtils.readFileToString(applicationSmaliFile, Charsets.UTF_8).contains(".line ");
            //inject instruction
            if (hasLineDeclared) {
                newCodeLines.add("    .line 0");
            }
            newCodeLines.add("    invoke-static {}, L" + bootStrapCintNativeName + ";->startup()V");
            newCodeLines.add("    ");

            //copy after
            for (int i = injectIndex + 1; i < codeLines.size(); i++) {
                newCodeLines.add(codeLines.get(i));
            }
        }


        FileUtils.writeLines(applicationSmaliFile, Charsets.UTF_8.name(), newCodeLines);
        return true;
    }


    private static String findStaticInjectCodeEntry(DexBackedClassDef dexClass, Map<String, DexBackedClassDef> applicationDexMap) {
        String superclass = dexClass.getSuperclass();
        if (superclass != null) {
            String supperClassDot = Util.descriptorToDot(superclass);
            DexBackedClassDef supperClassDef = applicationDexMap.get(supperClassDot);
            if (supperClassDef != null) {
                return findStaticInjectCodeEntry(supperClassDef, applicationDexMap);
            }
        }
        for (DexBackedMethod dexBackedMethod : dexClass.getMethods()) {
            if (StringUtils.equals(dexBackedMethod.getName(), "<clinit>")
                    && Modifier.isStatic(dexBackedMethod.accessFlags)
                    && "V".equalsIgnoreCase(dexBackedMethod.getReturnType())
                    && dexBackedMethod.getParameterTypes().size() == 0
            ) {

                return dexClass.getType();
            }
        }
        return null;
    }


    private static String findAttachBaseContextInjectCodeEntry(DexBackedClassDef dexClass, Map<String, DexBackedClassDef> applicationDexMap) {

        for (DexBackedMethod dexBackedMethod : dexClass.getMethods()) {
            if (!dexBackedMethod.getName().equals("attachBaseContext")) {
                continue;
            }
            List<String> parameterTypes = dexBackedMethod.getParameterTypes();
            if (parameterTypes.size() != 1 || !"Landroid/content/Context;".equals(parameterTypes.get(0))) {
                continue;
            }
            //如果自身找到了，那么不需要在supper寻找
            return dexClass.getType();
        }


        String superclass = dexClass.getSuperclass();
        if (superclass == null) {
            return dexClass.getType();
        }
        String supperClassDot = Util.descriptorToDot(superclass);
        DexBackedClassDef supperClassDef = applicationDexMap.get(supperClassDot);
        if (supperClassDef == null) {
            return dexClass.getType();
        }


        String supperApplicationClass = findAttachBaseContextInjectCodeEntry(supperClassDef, applicationDexMap);
        DexBackedClassDef dexBackedClassDef = applicationDexMap.get(Util.descriptorToDot(supperApplicationClass));
        if (dexBackedClassDef == null) {
            return dexClass.getType();
        }

        for (DexBackedMethod dexBackedMethod : dexBackedClassDef.getMethods()) {
            if (!dexBackedMethod.getName().equals("attachBaseContext")) {
                continue;
            }
            List<String> parameterTypes = dexBackedMethod.getParameterTypes();
            if (parameterTypes.size() != 1 || !"Landroid/content/Context;".equals(parameterTypes.get(0))) {
                continue;
            }
            if (AccessFlags.FINAL.isSet(dexBackedMethod.getAccessFlags())) {
                return dexBackedClassDef.getType();
            }
        }
        return dexClass.getType();
    }


    public static void baksmali(String targetClass, DexFile dexFile, File outputDirectoryFile) {

        final BaksmaliOptions options = new BaksmaliOptions();

        // options
        options.deodex = false;
        options.implicitReferences = false;
        options.parameterRegisters = true;
        options.localsDirective = true;
        options.sequentialLabels = true;
        options.debugInfo = true;
        options.codeOffsets = false;
        options.accessorComments = false;
        options.registerInfo = 0;
        options.inlineResolver = null;

        // set jobs automatically
        int jobs = Runtime.getRuntime().availableProcessors();
        if (jobs > 6) {
            jobs = 6;
        }


        if (dexFile instanceof DexBackedOdexFile) {
            options.inlineResolver =
                    InlineMethodResolver.createInlineMethodResolver(((DexBackedOdexFile) dexFile).getOdexVersion());
        }

        List<String> needDissembleClass = null;
        if (targetClass != null) {
            needDissembleClass = new ArrayList<>();
            needDissembleClass.add(targetClass);
        }


        Baksmali.disassembleDexFile(dexFile, outputDirectoryFile, jobs, options, needDissembleClass);
    }

    private static void handleAttachBaseContext(String applicationClassName, DexBackedClassDef dexClass, File applicationSmaliFile
            , File dexImage, File runtimeSmaliDir, boolean decodeAllSmali) throws IOException {
        //org.jf.baksmali.Main.main(new String[]{"d", "--classes", applicationClassName, dexImage.getAbsolutePath(), "-o", runtimeSmaliDir.getAbsolutePath()});
        if (decodeAllSmali) {
            baksmali(null, DexFileFactory.loadDexFile(dexImage, Opcodes.getDefault()), runtimeSmaliDir);
        } else {
            baksmali(applicationClassName, DexFileFactory.loadDexFile(dexImage, Opcodes.getDefault()), runtimeSmaliDir);
        }
        String bootStrapNativeName = ClassNames.INJECT_REBUILD_BOOTSTRAP.getClassName().replaceAll("\\.", "/");

        boolean findAttacheBaseContextMethod = false;
        for (DexBackedMethod dexBackedMethod : dexClass.getMethods()) {
            String name = dexBackedMethod.getName();
            if (!StringUtils.equals(name, "attachBaseContext")) {
                continue;
            }

            List<String> parameterTypes = dexBackedMethod.getParameterTypes();
            if (parameterTypes.size() == 1 && "Landroid/content/Context;".equals(parameterTypes.get(0))) {
                findAttacheBaseContextMethod = true;
                break;
            }

        }

        if (findAttacheBaseContextMethod) {
            // inject code info method
            List<String> codeLines = FileUtils.readLines(applicationSmaliFile, Charsets.UTF_8);
            //find attachBaseContext start
            int attachBaseContextMethodStartIndex = -1;
            for (int i = 0; i < codeLines.size(); i++) {
                //.method public final attachBaseContext(Landroid/content/Context;)V
                //.method protected attachBaseContext(Landroid/content/Context;)V
                //.method public attachBaseContext(Landroid/content/Context;)V
                String codeLine = StringUtils.trimToEmpty(codeLines.get(i));
                if (codeLine.startsWith(".method ") && codeLine.endsWith("attachBaseContext(Landroid/content/Context;)V")) {
                    attachBaseContextMethodStartIndex = i;
                    break;
                }
            }
            if (attachBaseContextMethodStartIndex < 0) {
                throw new IllegalStateException("can not find attachBaseContext method in the smali file: " + applicationSmaliFile.getAbsolutePath());
            }

            int injectIndex = findInjectIndex(codeLines, attachBaseContextMethodStartIndex);
            if (injectIndex < 0) {
                throw new IllegalStateException("error to locate method instruction for attachBaseContext");
            }
            boolean needShiftRegister = needShiftRegister(codeLines, attachBaseContextMethodStartIndex);

            List<String> newCodeLines = Lists.newArrayListWithExpectedSize(codeLines.size() + 4);
            //copy before
            for (int i = 0; i <= injectIndex; i++) {
                newCodeLines.add(codeLines.get(i));
            }

            boolean hasLineDeclared = FileUtils.readFileToString(applicationSmaliFile, Charsets.UTF_8).contains(".line ");

            if (needShiftRegister) {
                //inject instruction
                if (hasLineDeclared) {
                    newCodeLines.add("    .line 0");
                }
                newCodeLines.add("    move-object/from16 v2, p0");
                newCodeLines.add("    ");


                if (hasLineDeclared) {
                    newCodeLines.add("    .line 1");
                }
                newCodeLines.add("    move-object/from16 v8, p1");
                newCodeLines.add("    ");

                if (hasLineDeclared) {
                    newCodeLines.add("    .line 2");
                }
                newCodeLines.add("    invoke-static {v8, v2}, L" + bootStrapNativeName + ";->startUp(Landroid/content/Context;Landroid/content/Context;)V");
                newCodeLines.add("    ");

            } else {
                if (hasLineDeclared) {
                    newCodeLines.add("    .line 0");
                }
                newCodeLines.add("    invoke-static {p1, p0}, L" + bootStrapNativeName + ";->startUp(Landroid/content/Context;Landroid/content/Context;)V");
                newCodeLines.add("    ");

            }
            //inject instruction

            //copy after
            for (int i = injectIndex + 1; i < codeLines.size(); i++) {
                newCodeLines.add(codeLines.get(i));
            }

            FileUtils.writeLines(applicationSmaliFile, Charsets.UTF_8.name(), newCodeLines);

        } else {
            String superclass = dexClass.getSuperclass();

            //gen a method
            String smaliCode = FileUtils.readFileToString(applicationSmaliFile, Charsets.UTF_8);
            smaliCode = smaliCode + "\n.method protected attachBaseContext(Landroid/content/Context;)V\n" +
                    "    .locals 0\n" +
                    "    .param p1, \"base\"    # Landroid/content/Context;\n" +
                    "\n" +
                    "    .line 12\n" +
                    "    invoke-static {p1,p0}, L" + bootStrapNativeName + ";->startUp(Landroid/content/Context;Landroid/content/Context;)V\n" +
                    "\n" +
                    "    .line 13\n" +
                    "    invoke-super {p0, p1}, " + superclass + "->attachBaseContext(Landroid/content/Context;)V\n" +
                    "\n" +
                    "    .line 14\n" +
                    "    return-void\n" +
                    ".end method\n" +
                    "\n\n";
            FileUtils.write(applicationSmaliFile, smaliCode, Charsets.UTF_8);
        }
    }


}
