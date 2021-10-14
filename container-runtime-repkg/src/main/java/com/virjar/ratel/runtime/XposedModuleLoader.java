package com.virjar.ratel.runtime;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.rposed.IRposedHookLoadPackage;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage;
import com.virjar.ratel.api.xposed.IRXposedHookInitPackageResources;
import com.virjar.ratel.api.xposed.IRXposedHookLoadPackage;
import com.virjar.ratel.api.xposed.IRXposedHookZygoteInit;
import com.virjar.ratel.core.runtime.BuildConfig;
import com.virjar.ratel.hook.ContextAttachCollector;
import com.virjar.ratel.utils.StringEtacsufbo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import external.org.apache.commons.io.FileUtils;
import external.org.apache.commons.io.IOUtils;
import external.org.apache.commons.lang3.StringUtils;

public class XposedModuleLoader {

    private static Set<String> externalModulePackage = new HashSet<>();
    private static Set<PackageInfo> externalModuleSourceDirs = new HashSet<>();
    private static PackageInfo ratelManagerPackageInfo = null;

    public static boolean isRatelManagerInstalled() {
        if (ratelManagerPackageInfo != null) {
            return true;
        }
        try {
            ratelManagerPackageInfo = RatelRuntime.getOriginContext().getPackageManager().getPackageInfo(Constants.ratelManagerPackage, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static long getRatelManagerVersionCode() {
        if (ratelManagerPackageInfo == null) {
            return -1;
        }
        return ratelManagerPackageInfo.versionCode;
    }

    private static Cursor getRatelRepositoryConfigCursor() {
        try {
            return RatelRuntime.getOriginContext().getContentResolver().query(
                    Uri.parse("content://com.virjar.ratel.manager/module_list"),
                    null,
                    "mPackage=?",
                    new String[]{RatelRuntime.originPackageName},
                    null
            );
        } catch (Exception e) {
            Log.w(Constants.TAG, "failed to get external module config cursor!!", e);
            return null;
        }
    }

    private static Boolean hasSDCardReadPermission = null;
    private static Boolean hasSDCardWritePermission = null;

    public static boolean testHasSDCardWritePermission() {
        if (hasSDCardWritePermission != null) {
            return hasSDCardWritePermission;
        }
        String[] requestedPermissions = RatelRuntime.getH().requestedPermissions;

        boolean hinted = false;
        if (requestedPermissions != null) {
            for (String str : requestedPermissions) {
                if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(str)) {
                    hinted = true;
                    break;
                }
            }
        }
        if (!hinted) {
            hasSDCardWritePermission = false;
            return hasSDCardWritePermission;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int permissionGrantStatus = RatelRuntime.getOriginContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return (hasSDCardWritePermission = permissionGrantStatus == PackageManager.PERMISSION_GRANTED);
        }
        hasSDCardWritePermission = true;
        return hasSDCardWritePermission;
    }


    public static boolean testHasSDCardReadPermission() {
        if (hasSDCardReadPermission != null) {
            return hasSDCardReadPermission;
        }
        String[] requestedPermissions = RatelRuntime.getH().requestedPermissions;

        boolean hinted = false;
        if (requestedPermissions != null) {
            for (String str : requestedPermissions) {
                if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(str)) {
                    hinted = true;
                    break;
                }
            }
        }
        if (!hinted) {
            hasSDCardReadPermission = false;
            return hasSDCardReadPermission;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int permissionGrantStatus = RatelRuntime.getOriginContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return (hasSDCardReadPermission = permissionGrantStatus == PackageManager.PERMISSION_GRANTED);
        }
        hasSDCardReadPermission = true;
        return hasSDCardReadPermission;
    }

    private static boolean getModuleFromRatelManager() {
        if (!isRatelManagerInstalled()) {
            if (RatelRuntime.isRatelDebugBuild) {
                Log.i(Constants.TAG, "RatelManager not installed!!");
            }
            // think ratelManger handle success if RatelManger not installed
            return true;
        }
        Cursor cursor = getRatelRepositoryConfigCursor();
        if (cursor == null) {

            Log.w(Constants.TAG, "RatelManager config repository cursor is null");

//            //maybe ratel manager not running,so launch ratel manager app,and retry again
////            Intent ratelLaunchIntent = RatelRuntime.getOriginContext().getPackageManager().getLaunchIntentForPackage(Constants.ratelManagerPackage);
////            // notice this is an asynchronized call,we can not wait for ratel manager process started
////            RatelRuntime.getOriginContext().startActivity(ratelLaunchIntent);
////            //TODO it`s necessary??
////            cursor = getRatelRepositoryConfigCursor();
            return false;
        }

        //安装在系统的外部插件
        try {
            int targetPackageIndex = -1;
            while (cursor.moveToNext()) {
                if (targetPackageIndex < 0) {
                    targetPackageIndex = cursor.getColumnIndex("modulePackage");
                }
                String modulePackage = cursor.getString(targetPackageIndex);
                if (RatelRuntime.isRatelDebugBuild) {
                    Log.i(Constants.TAG, "get a module app package : " + modulePackage + "  from ratel manager content provider");
                }
                externalModulePackage.add(modulePackage);
            }
            cursor.close();
            return true;
        } catch (Exception e) {
            Log.w(Constants.RATEL_WARNING_TAG, "query external module list failed", e);
        }
        return false;
    }

    private static void getModuleFromSdcard() {
        // call ratelManger failed sometimes,use sdcard file system  as  a fail over plan
        File ratelConfigRoot = new File(Environment.getExternalStorageDirectory(), Constants.ratelSDCardRoot);
        File globalConfigFile = new File(ratelConfigRoot, Constants.modulePackageGlobal);
        if (globalConfigFile.exists() && globalConfigFile.isFile() && globalConfigFile.canRead()) {
            try {
                for (String line : FileUtils.readLines(globalConfigFile, StandardCharsets.UTF_8)) {
                    line = StringUtils.trim(line);
                    if (StringUtils.isNotEmpty(line)) {
                        externalModulePackage.add(line);
                    }
                }
            } catch (IOException e) {
                // the exception will not happened
                Log.w(Constants.RATEL_WARNING_TAG, "can not read ratel config file: " + globalConfigFile.getAbsolutePath(), e);
            }
        }

        // config item,we need filter
        File packageItemConfigFile = new File(ratelConfigRoot, Constants.modulePackageItem);
        if (packageItemConfigFile.exists() && packageItemConfigFile.isFile() && packageItemConfigFile.canRead()) {
            try {
                for (String line : FileUtils.readLines(packageItemConfigFile, StandardCharsets.UTF_8)) {
                    line = StringUtils.trim(line);
                    //format(modulePackage:targetPackage) I`m targetPackage
                    String[] split = line.split(":");
                    if (split.length <= 1) {
                        continue;
                    }
                    if (!RatelRuntime.originPackageName.equalsIgnoreCase(StringUtils.trim(split[1]))) {
                        continue;
                    }
                    externalModulePackage.add(StringUtils.trim(split[0]));
                }
            } catch (IOException e) {
                // the exception will not happened
                Log.w(Constants.RATEL_WARNING_TAG, "can not read ratel config file: " + packageItemConfigFile.getAbsolutePath(), e);
            }
        }
    }

    private static void getModuleFromBackDoor() {
        // a black door,for some special needs
        // 一个后门配置，为了避免我们批量化业务过程中，需要单独管理ratelManager，以及RatelManager本身的不稳定。所以通过这个文件做特殊配置
        // 当然，如果app没有sdcard权限，那么这个后门无法起作用
        File ratelConfigRoot = new File(Environment.getExternalStorageDirectory(), Constants.ratelSDCardRoot);
        File forceEnableFile = new File(ratelConfigRoot, Constants.backDoorPackageConfigFile);
        if (forceEnableFile.exists() && forceEnableFile.isFile() && forceEnableFile.canRead()) {
            try {
                for (String line : FileUtils.readLines(forceEnableFile, StandardCharsets.UTF_8)) {
                    line = StringUtils.trim(line);
                    //format(modulePackage:targetPackage) I`m targetPackage
                    String[] split = line.split(":");
                    if (split.length <= 1) {
                        continue;
                    }
                    if (!RatelRuntime.originPackageName.equalsIgnoreCase(StringUtils.trim(split[1]))) {
                        continue;
                    }
                    externalModulePackage.add(StringUtils.trim(split[0]));
                }
            } catch (IOException e) {
                // the exception will not happened
                Log.w(Constants.RATEL_WARNING_TAG, "can not read ratel config file: " + forceEnableFile.getAbsolutePath(), e);
            }
        } else if (RatelRuntime.isRatelDebugBuild) {
            Log.i(Constants.TAG, "can not read global module config file: " + forceEnableFile.getAbsolutePath() + " exits: " + forceEnableFile.exists() + "  isFile: " + forceEnableFile.isFile() + " can read: " + forceEnableFile.canRead());
        }
    }

    public static void init() {
        boolean canSdcardRead = testHasSDCardReadPermission();
        boolean handleWithRatelManager = getModuleFromRatelManager();
        if (!handleWithRatelManager && canSdcardRead) {
            if (RatelRuntime.isRatelDebugBuild) {
                Log.i(Constants.TAG, "load module from sdcard");
            }
            getModuleFromSdcard();
        }

        if (canSdcardRead) {
            // scan for back door anyway
            if (RatelRuntime.isRatelDebugBuild) {
                Log.i(Constants.TAG, "now read module config for global config file");
            }
            getModuleFromBackDoor();
        }

        for (String packageName : externalModulePackage) {
            try {
                PackageInfo packageInfo = RatelRuntime.getOriginContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
                RatelConfig.loadConfig(packageInfo.applicationInfo);
                externalModuleSourceDirs.add(packageInfo);
                if (RatelRuntime.isRatelDebugBuild) {
                    Log.i(Constants.TAG, "get external xposed module path: " + packageInfo.applicationInfo.sourceDir);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(Constants.TAG, "can not get module source path for xposed module: " + packageName + " module apk not installed");
            }
        }
        //anti debug
        if (!BuildConfig.DEBUG && System.currentTimeMillis() - RatelRuntime.runtimeStartupTimestamp > 5000) {
            externalModulePackage.clear();
            externalModuleSourceDirs.clear();
        }
    }

    /**
     * 当xposed模块被感染之后，模块签名将会变化，所以需要将原包抽取，然后重定向
     *
     * @param packageInfo 模块应用信息
     */
    private static void relocateForRatelInfectedXposedModule(PackageInfo packageInfo) throws IOException {
        if (!packageInfo.applicationInfo.metaData.containsKey("xposedmodule")) {
            return;
        }
        if (packageInfo.applicationInfo.metaData.containsKey("for_ratel_apps")) {
            return;
        }
        try (ZipFile zipFile = new ZipFile(packageInfo.applicationInfo.sourceDir)) {
            ZipEntry entry = zipFile.getEntry("assets/" + Constants.originAPKFileName);
            if (entry == null) {
                return;
            }
            String fileName;
            if (Build.VERSION.SDK_INT > 28) {
                fileName = packageInfo.applicationInfo.packageName + "_" + packageInfo.getLongVersionCode() + ".apk";
            } else {
                fileName = packageInfo.applicationInfo.packageName + "_" + packageInfo.versionCode + ".apk";
            }

            File releaseDir = new File(RatelEnvironment.ratelResourceDir(), fileName);
            if (!releaseDir.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(releaseDir);
                IOUtils.copy(zipFile.getInputStream(entry), fileOutputStream);
                fileOutputStream.close();
            }
            RatelNative.redirectFile(packageInfo.applicationInfo.sourceDir, releaseDir.getAbsolutePath());
        }

    }

    static void relocateForRatelInfectedXposedModules() throws IOException {
        for (PackageInfo externalModuleSource : externalModuleSourceDirs) {
            relocateForRatelInfectedXposedModule(externalModuleSource);
        }
    }

    static void loadXposedModuleClasses() throws IOException {

        Set<String> loadedPackage = new HashSet<>();
        //热发
        if (hotmoduleEnabled()) {
            List<HotModuleManager.HotModule> enableHotModules = HotModuleManager.getEnableHotModules();
            for (HotModuleManager.HotModule file : enableHotModules) {
                loadedPackage.add(file.modulePkgName);
                Log.i(Constants.TAG, "load HotModules: " + file.localFile.getAbsolutePath());
                if (!loadXposedModuleFile(file.localFile.getAbsolutePath(), false, RatelRuntime.getOriginApplicationInfo())) {
                    // 加载失败，所以重新来一波
                    loadedPackage.remove(file.modulePkgName);

                }
            }
        } else {
            Log.i(Constants.TAG, "hotmodule disabled!");
        }

        //load all external modules
        for (PackageInfo externalModuleSource : externalModuleSourceDirs) {
            if (loadedPackage.contains(externalModuleSource.packageName)) {
                Log.i(Constants.TAG, "the module: " + externalModuleSource.packageName + " has load from hotmodule skipt it");
                continue;
            }
            loadedPackage.add(externalModuleSource.packageName);
            if (externalModuleSource.applicationInfo.metaData.containsKey(Constants.moduleHasNativeCode)) {
                //如果插件具有lib库，那么需要使用DexClassLoader，PathClassLoader无法自定义librarySearchPath
                //TODO 是否可以绕过还需要研究
                loadXposedModuleFile(externalModuleSource.applicationInfo.sourceDir, false, externalModuleSource.applicationInfo);
            } else {
                loadXposedModuleFile(externalModuleSource.applicationInfo.sourceDir, true, RatelRuntime.getOriginApplicationInfo());
            }
        }

        //集成的内置xposed插件
        if (!RatelRuntime.isKratosEngine()) {
            String moduleApkPath = RatelEnvironment.xposedModulePath().getCanonicalPath();
            if (new File(moduleApkPath).exists()) {
                String embedXposedModuleApkPackage = RatelConfig.getConfig(Constants.xposedModuleApkPackageNameKey);
                if (externalModulePackage.contains(embedXposedModuleApkPackage) &&
                        //有可能外置插件被卸载了，这个时候以内置插件为主
                        loadedPackage.contains(embedXposedModuleApkPackage)) {
                    Log.w(Constants.TAG, "the embed xposed module :{" + embedXposedModuleApkPackage + "} has load from external already ,skip it");
                } else {
                    loadXposedModuleFile(moduleApkPath, false, RatelRuntime.getOriginApplicationInfo());
                }
            }
        }
    }


    private static boolean loadXposedModuleFile(String filePath, boolean isInstalledOnSystem, ApplicationInfo applicationInfo) throws IOException {
        Log.i(Constants.TAG, "Loading modules from " + filePath);

        if (!new File(filePath).exists()) {
            Log.w(Constants.TAG, filePath + " does not exist");
            return false;
        }

        ClassLoader hostClassLoader = RposedBridge.class.getClassLoader();
        //ClassLoader appClassLoaderWithXposed = XposedBridge.getAppClassLoaderWithXposed(appClassLoader);


        ClassLoader mcl;

        ClassLoader existed = moduleClassLoaderCache.get(filePath);
        if (existed != null) {
            mcl = existed;
        } else if (isInstalledOnSystem) {
            mcl = new PathClassLoader(filePath, hostClassLoader);
        } else {
            File moduleOdexDir = RatelEnvironment.APKOptimizedDirectory();
            mcl = new DexClassLoader(filePath, moduleOdexDir.getCanonicalPath(), applicationInfo.nativeLibraryDir, hostClassLoader);
        }


        InputStream is = mcl.getResourceAsStream("assets/xposed_init");
        if (is == null) {
            Log.w(Constants.TAG, "assets/xposed_init not found in the APK");
            return false;
        }


        Set<String> xposedClassSet = new HashSet<>();


        BufferedReader moduleClassesReader = new BufferedReader(new InputStreamReader(is));
        try {
            String moduleClassName;
            while ((moduleClassName = moduleClassesReader.readLine()) != null) {
                moduleClassName = moduleClassName.trim();
                if (moduleClassName.isEmpty() || moduleClassName.startsWith("#"))
                    continue;
                xposedClassSet.add(moduleClassName);
            }
        } catch (IOException e) {
            Log.w(Constants.TAG, e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        Set<Class<?>> exposedModuleClasses = new HashSet<>();

        boolean canLoad = true;
        for (String className : xposedClassSet) {
            try {
                Log.i(Constants.TAG, "  Loading class " + className);
                Class<?> moduleClass = mcl.loadClass(className);
                exposedModuleClasses.add(moduleClass);
            } catch (Throwable t) {
                canLoad = false;
                break;
            }
        }

        if (!canLoad) {
            exposedModuleClasses.clear();
            Log.i(Constants.TAG, "origin xposed module,refresh classloader");
            //refresh classloader
            hostClassLoader = new DexClassLoader(
                    RatelEnvironment.xposedApiBridgeJar().getAbsolutePath(),
                    RatelEnvironment.APKOptimizedDirectory().getCanonicalPath(),
                    applicationInfo.nativeLibraryDir,
                    hostClassLoader);

            if (isInstalledOnSystem) {
                mcl = new PathClassLoader(filePath, hostClassLoader);
            } else {
                File moduleOdexDir = RatelEnvironment.APKOptimizedDirectory();
                mcl = new DexClassLoader(filePath, moduleOdexDir.getCanonicalPath(), applicationInfo.nativeLibraryDir, hostClassLoader);
            }

            for (String className : xposedClassSet) {
                try {
                    Log.i(Constants.TAG, "  Loading class " + className);
                    Class<?> moduleClass = mcl.loadClass(className);
                    Log.i(Constants.TAG, "origin xposed module, load class with xposed bridge, strongly recommend use Rposed to replace Xposed if you are the author of this module");
                    exposedModuleClasses.add(moduleClass);
                } catch (Throwable t) {
                    Log.w(Constants.TAG, "load module failed", t);
                }
            }
        }
        if (exposedModuleClasses.size() > 0) {
            ClassLoader classLoader = exposedModuleClasses.iterator().next().getClassLoader();
            if (classLoader != null) {
                moduleClassLoaderCache.put(filePath, classLoader);
            }

        }

        for (Class<?> clazz : exposedModuleClasses) {
            addMoudleClass(filePath, clazz);
        }
        return true;
        //  loadXposedModuleClasses(exposedModuleClasses, filePath);
    }

    public static ClassLoader queryModuleClassLoaderCache(String apkPath) {
        return moduleClassLoaderCache.get(apkPath);
    }

    public static void cacheClassLoader(String path, ClassLoader classLoader) {
        moduleClassLoaderCache.put(path, classLoader);
    }

    public static Map<String, ClassLoader> moduleClassLoaderCache = new ConcurrentHashMap<>();


    private static Map<String, Set<Class<?>>> moduleClasses = new HashMap<>();

    private static void addMoudleClass(String moduleApkPath, Class<?> clazz) {
        Set<Class<?>> classSet = moduleClasses.get(moduleApkPath);
        if (classSet == null) {
            classSet = new HashSet<>();
            moduleClasses.put(moduleApkPath, classSet);
        }
        classSet.add(clazz);
    }

    /**
     * 平头哥的模块，提前加载
     */
    static void loadRatelModules() {
        for (Map.Entry<String, Set<Class<?>>> entry : moduleClasses.entrySet()) {
            String path = entry.getKey();
            Set<Class<?>> classes = entry.getValue();
            if (classes.isEmpty()) {
                continue;
            }
            if (classes.iterator().next().getClassLoader().getParent() == RposedBridge.class.getClassLoader()) {
                loadXposedModuleClasses(classes, path);
            }
        }
    }

    /**
     * xposed原生模块，RatelToolkit无法使用，使用RatelToolKit切换设备会导致数据紊乱
     */
    static void loadXposedModules() {
        for (Map.Entry<String, Set<Class<?>>> entry : moduleClasses.entrySet()) {
            String path = entry.getKey();
            Set<Class<?>> classes = entry.getValue();
            if (classes.isEmpty()) {
                continue;
            }
            if (classes.iterator().next().getClassLoader().getParent() != RposedBridge.class.getClassLoader()) {
                loadXposedModuleClasses(classes, path);
            }
        }
        //fire ContextWrapper attach(like) method hook callback
        ContextAttachCollector.fireContextAttach();
    }

    private static void loadXposedModuleClasses(Set<Class<?>> exposedModuleClasses, String moduleApkPath) {
        for (Class<?> moduleClass : exposedModuleClasses) {
            try {
                boolean hint = false;

                final Object moduleInstance = moduleClass.newInstance();
                if (moduleInstance instanceof IRXposedHookZygoteInit) {
                    //ExposedHelper.callInitZygote(moduleApkPath, moduleInstance);
                    Class<?> startupParamClass = RposedHelpers.findClass(moduleInstance.getClass().getName() + "$StartupParam", moduleInstance.getClass().getClassLoader());
                    //  IRXposedHookZygoteInit.StartupParam param = new IRXposedHookZygoteInit.StartupParam();
                    IRXposedHookZygoteInit.StartupParam param = (IRXposedHookZygoteInit.StartupParam) RposedHelpers.newInstance(startupParamClass);


                    param.modulePath = moduleApkPath;
                    param.startsSystemServer = false;
                    ((IRXposedHookZygoteInit) moduleInstance).initZygote(param);
                    hint = true;
                }

                if (moduleInstance instanceof IRXposedHookLoadPackage) {
                    if (moduleInstance instanceof IRposedHookLoadPackage) {
                        //rposed
                        // hookLoadPackage(new IXposedHookLoadPackage.Wrapper((IXposedHookLoadPackage) moduleInstance));
                        IRposedHookLoadPackage.Wrapper wrapper = new IRposedHookLoadPackage.Wrapper((IRposedHookLoadPackage) moduleInstance);

                        RposedBridge.CopyOnWriteSortedSet<IRposedHookLoadPackage.Wrapper> xc_loadPackageCopyOnWriteSortedSet = new RposedBridge.CopyOnWriteSortedSet<>();
                        if (!BuildConfig.DEBUG || com.virjar.ratel.hook.sandcompat.methodgen.HookerDexMaker.class.getName().equals(StringEtacsufbo.decodeToString(
                                "4K#p#VkA1duAZikdG4J5zY3qwGCPNt7tlJdnVm#ZKDSTlV8xM9+O9Cs3xpT5sg6W9Jtkp0v+wDO9", 98274
                        ))) {
                            // 被强行修改为debug版本了,所以要制造一些麻烦给他
                            xc_loadPackageCopyOnWriteSortedSet.add(wrapper);
                        }

                        RC_LoadPackage.LoadPackageParam lpparam = (RC_LoadPackage.LoadPackageParam) RposedHelpers.newInstance(RC_LoadPackage.LoadPackageParam.class, xc_loadPackageCopyOnWriteSortedSet);
                        lpparam.packageName = RatelRuntime.originPackageName;
                        RatelRuntime.getOriginApplicationInfo().processName = RatelRuntime.processName;
                        lpparam.processName = RatelRuntime.getOriginApplicationInfo().processName;
                        //lpparam.classLoader = appClassLoaderWithXposed;
                        lpparam.classLoader = RatelRuntime.getOriginContext().getClassLoader();
                        lpparam.appInfo = RatelRuntime.getOriginApplicationInfo();
                        lpparam.isFirstApplication = true;
                        lpparam.modulePath = moduleApkPath;
                        RC_LoadPackage.callAll(lpparam);
                        hint = true;
                    } else {
                        //xposed分支，这里只能通过反射

                        Class<?> xposedHookLoadPackageClass = moduleInstance.getClass();
                        Object wrapper = RposedHelpers.newInstance(RposedHelpers.findClass("de.robv.android.xposed.IXposedHookLoadPackage$Wrapper", xposedHookLoadPackageClass.getClassLoader()), moduleInstance);

                        Class xposedBridgeCopyWriteSortedSetClass = RposedHelpers.findClass("de.robv.android.xposed.XposedBridge$CopyOnWriteSortedSet", xposedHookLoadPackageClass.getClassLoader());
                        Object xc_loadPackageCopyOnWriteSortedSet = RposedHelpers.newInstance(xposedBridgeCopyWriteSortedSetClass);


                        if (!BuildConfig.DEBUG || com.virjar.ratel.hook.sandcompat.methodgen.HookerDexMaker.class.getName().equals(StringEtacsufbo.decodeToString(
                                "4K#p#VkA1duAZikdG4J5zY3qwGCPNt7tlJdnVm#ZKDSTlV8xM9+O9Cs3xpT5sg6W9Jtkp0v+wDO9", 98274
                        ))) {
                            // 被强行修改为debug版本了,所以要制造一些麻烦给他
                            RposedHelpers.callMethod(xc_loadPackageCopyOnWriteSortedSet, "add", wrapper);
                        }

                        Class XC_LoadPackageLoadPackageParamClass = RposedHelpers.findClass("de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam", xposedHookLoadPackageClass.getClassLoader());

                        Object lpparam = RposedHelpers.newInstance(XC_LoadPackageLoadPackageParamClass, xc_loadPackageCopyOnWriteSortedSet);

                        RposedHelpers.setObjectField(lpparam, "packageName", RatelRuntime.originPackageName);

                        // lpparam.packageName = RatelRuntime.getOriginApplicationInfo().packageName;
                        RatelRuntime.getOriginApplicationInfo().processName = RatelRuntime.processName;

                        RposedHelpers.setObjectField(lpparam, "processName", RatelRuntime.processName);
                        //  lpparam.processName = RatelRuntime.getOriginApplicationInfo().processName;

                        RposedHelpers.setObjectField(lpparam, "classLoader", RatelRuntime.getOriginContext().getClassLoader());
                        // lpparam.classLoader = RatelRuntime.getOriginContext().getClassLoader();

                        RposedHelpers.setObjectField(lpparam, "appInfo", RatelRuntime.getOriginApplicationInfo());
                        // lpparam.appInfo = RatelRuntime.getOriginApplicationInfo();

                        RposedHelpers.setBooleanField(lpparam, "isFirstApplication", true);
                        //lpparam.isFirstApplication = true;

                        Class XC_LoadPackageClass = RposedHelpers.findClass("de.robv.android.xposed.callbacks.XC_LoadPackage", xposedHookLoadPackageClass.getClassLoader());
                        RposedHelpers.callStaticMethod(XC_LoadPackageClass, "callAll", lpparam);
                        // XC_LoadPackage.callAll(lpparam);
                        hint = true;
                    }
                }


                if (moduleInstance instanceof IRXposedHookInitPackageResources) {
                    // hookInitPackageResources(new IXposedHookInitPackageResources.Wrapper((IXposedHookInitPackageResources) moduleInstance));
                    // TODO: 17/12/1 Support Resource hook
                    Log.d(Constants.TAG, "not support hook resource,the hook" + moduleInstance.getClass() + " will be ignore");
                    hint = true;
                }
                if (!hint) {
                    Log.w(Constants.TAG, "can not recognize handler type: " + moduleInstance.getClass().getName() + ",the class must be instance of xposed/rposed framework interface");
                }
            } catch (Throwable throwable) {
                Log.e(Constants.TAG, "load xposed module failed", throwable);
            }

        }
    }

    private static final String hotmoduleSwitch = "ratel_hotmodule_switch";

    public static boolean hotmoduleEnabled() {
        String value = RatelConfig.getConfig(hotmoduleSwitch);
        if (TextUtils.isEmpty(value)) {
            //默认为true
            return true;
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(value);
    }

    public static void setHotmoduleSwitchStatus(boolean openStatus) {
        RatelConfig.setConfig(hotmoduleSwitch, String.valueOf(openStatus));
    }
}
