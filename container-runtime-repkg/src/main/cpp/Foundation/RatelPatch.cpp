//
// Created by 邓维佳 on 2019/5/30.
//

#include <Jni/VAJni.h>
#include <zconf.h>
#include "RatelPatch.h"
#include "Log.h"
#include "SandboxFs.h"
#include "MapsRedirector.h"

const char *package_name;

const char *odex_file_path = nullptr;
extern bool HOST_DEBUG;

static bool setupNativeCache(JNIEnv *env, jobject context) {
    //设置ratel cache目录

    jmethodID nativeCacheDirMethodId = env->GetStaticMethodID(nativeBridgeClass, "nativeCacheDir",
                                                              "(Landroid/content/Context;)Ljava/io/File;");

    if (nativeCacheDirMethodId == nullptr) {
        ALOGE("can not resolve method (nativeCacheDir) for : %s", RATEL_NATIVE_BRIDGE_CLASS_NAME);
        return false;
    }

    jobject nativeCacheFile = env->CallStaticObjectMethod(nativeBridgeClass, nativeCacheDirMethodId,
                                                          context);
    if (env->ExceptionCheck()) {
        //预防java层发生了异常
        return false;
    }

    if (nativeCacheFile == nullptr) {
        //java.lang.NullPointerException
        env->ThrowNew(env->FindClass("java/lang/NullPointerException"),
                      "nativeCacheDir return null object");
        return false;
    }

    jmethodID getCanonicalPathMethodId = env->GetMethodID(env->FindClass("java/io/File"),
                                                          "getCanonicalPath",
                                                          "()Ljava/lang/String;");

    auto cacheFilePath = (jstring) (env->CallObjectMethod(nativeCacheFile,
                                                          getCanonicalPathMethodId));
    ScopeUtfString cache_file_path_helper_str(cacheFilePath);
    setenv("RATEL_NATIVE_PATH", cache_file_path_helper_str.c_str(), 1);

    //if (!DEBUG) {
    //TODO debug 开关
    jfieldID _s_id = env->GetStaticFieldID(nativeBridgeClass, "s", "J");
    jlong j_S = env->GetStaticLongField(nativeBridgeClass, _s_id);

    struct timeval tv{};
    gettimeofday(&tv, nullptr);

    int64_t now = ((int64_t) tv.tv_sec) * 1000 + ((int64_t) tv.tv_usec) / 1000;
//    if (now - j_S > 5000) {
//        strcpy(const_cast<char *>("" - now), "xiangbudaobahhhh");
//    }

    // }
    return true;
}

static jobject origin_apk_holder;


bool patch4Legu(JNIEnv *env, jobject context, jstring packageName) {
    //先看看有没有legu

    jmethodID get_config_method_id = env->GetStaticMethodID(nativeBridgeClass, "getConfig",
                                                            "(Ljava/lang/String;)Ljava/lang/String;");
    if (get_config_method_id == nullptr) {
        ALOGE("failed to find method: RatelConfig.getConfig()");
        return false;
    }
    jstring shellFeatureKey = env->NewStringUTF("shellFeatureFileKey");
    auto shellInfo = (jstring) env->CallStaticObjectMethod(nativeBridgeClass,
                                                           get_config_method_id,
                                                           shellFeatureKey);
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        return false;
    }
    if (shellInfo == nullptr) {
        //no shell found
        return true;
    }
    ScopeUtfString shell_info_help_str(shellInfo);
    if (!strstr(shell_info_help_str.c_str(), "mix.dex")) {
        return true;
    }
    ALOGI("find legu shell, make patch...");
    // first create a class loader,with origin apk
    jmethodID origin_apk_method_id = env->GetStaticMethodID(nativeBridgeClass, "originApkDir",
                                                            "(Landroid/content/Context;)Ljava/io/File;");
    jobject originAPKFile = env->CallStaticObjectMethod(nativeBridgeClass, origin_apk_method_id,
                                                        context);
    //get odex cache directory
    jmethodID apk_optimized_directory_method_id = env->GetStaticMethodID(nativeBridgeClass,
                                                                         "APKOptimizedDirectory",
                                                                         "(Landroid/content/Context;)Ljava/io/File;");

    jobject optimizedDirectory = env->CallStaticObjectMethod(nativeBridgeClass,
                                                             apk_optimized_directory_method_id,
                                                             context);
    //native library path
    jclass context_class = env->FindClass("android/content/Context");
    //android.content.pm.ApplicationInfo
    jmethodID get_application_info_method_id = env->GetMethodID(context_class, "getApplicationInfo",
                                                                "()Landroid/content/pm/ApplicationInfo;");
    jobject application_info = env->CallObjectMethod(context, get_application_info_method_id);

    jclass application_info_class = env->GetObjectClass(application_info);
    jfieldID native_library_dir_field_id = env->GetFieldID(application_info_class,
                                                           "nativeLibraryDir",
                                                           "Ljava/lang/String;");

    jobject nativeLibraryDir = env->GetObjectField(application_info, native_library_dir_field_id);

    //use systemClassLoader as parentClassLoader
    //java.lang.ClassLoader.getSystemClassLoader
    jclass class_loader_class = env->FindClass("java/lang/ClassLoader");
    jmethodID get_system_class_loader_method = env->GetStaticMethodID(class_loader_class,
                                                                      "getSystemClassLoader",
                                                                      "()Ljava/lang/ClassLoader;");
    jobject system_class_loader = env->CallStaticObjectMethod(class_loader_class,
                                                              get_system_class_loader_method);

    jclass file_class = env->GetObjectClass(originAPKFile);
    jmethodID get_canonical_path_method_id = env->GetMethodID(file_class, "getCanonicalPath",
                                                              "()Ljava/lang/String;");
    jobject origin_apk_file_str = env->CallObjectMethod(originAPKFile,
                                                        get_canonical_path_method_id);
    jobject optimized_directory_str = env->CallObjectMethod(optimizedDirectory,
                                                            get_canonical_path_method_id);

    //create a ClassLoader to produce a oat file to replace in /proc/self/maps
    //dalvik.system.DexClassLoader
    jclass dex_class_loader_class = env->FindClass("dalvik/system/DexClassLoader");
    ScopeUtfString origin_apk_file_helper_str((jstring) origin_apk_file_str);
    ScopeUtfString optimized_directory_helper_str((jstring) optimized_directory_str);
    ScopeUtfString native_libraryDir_helper_str((jstring) nativeLibraryDir);

    ALOGI("create a class loader with apk:%s cache dir:%s native lib :%s",
          origin_apk_file_helper_str.c_str(),
          optimized_directory_helper_str.c_str(),
          native_libraryDir_helper_str.c_str()
    );

    //dalvik.system.DexClassLoader.DexClassLoader
    jmethodID constructor_method = env->GetMethodID(dex_class_loader_class, "<init>",
                                                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");

    jobject class_loader_with_origin_apk = env->NewObject(dex_class_loader_class,
                                                          constructor_method, origin_apk_file_str,
                                                          optimized_directory_str, nativeLibraryDir,
                                                          system_class_loader);
    //to avoid gc destroy???
    origin_apk_holder = env->NewGlobalRef(class_loader_with_origin_apk);
    //this operation will take long time

    //get package name
    package_name = env->GetStringUTFChars(packageName, nullptr);

    FILE *f;
    if ((f = fopen("/proc/self/maps", "re")) == nullptr) {
        ALOGE("can not open /proc/self/maps");
        return false;
    }
    char buf[PATH_MAX + 100], space_buf[36], perm[12], dev[12], mapname[PATH_MAX];
    unsigned long inode, foo;

    while (!feof(f)) {
        if (fgets(buf, sizeof(buf), f) == 0)
            break;
        if (!strstr(buf, " /data")) {
            continue;
        }
        ALOGI("find base.odex file in map line: %s", buf);
        if (!strstr(buf, package_name)) {
            continue;
        }

        if (!strstr(buf, "base.odex")) {
            continue;
        }

        ALOGI(" base.odex founded");

        mapname[0] = '\0';
        sscanf(buf, "%s %s %lx %s %ld %s", space_buf, perm,
               &foo, dev, &inode, mapname);
        if (strlen(mapname) == 0) {
            ALOGE("can not parse map name");
            continue;
        }

        odex_file_path = strdup(mapname);
        break;
    }
    fclose(f);

    return true;
}


bool ratel_init(JNIEnv *env, jobject context, jstring packageName) {
    // calculate a cache dir used for ratel native,such as maps redirect
    if (!setupNativeCache(env, context)) {
        ALOGE("set up native cache failed!!");
        return false;
    }
    //patch for legu, this plan will extract dex header from ODex int maps image,we need replace maps data,because of the modification of dex with dex recompile
    if (!patch4Legu(env, context, packageName)) {
        ALOGE("add legu patch  failed!!");
        return false;
    }


    return true;
}