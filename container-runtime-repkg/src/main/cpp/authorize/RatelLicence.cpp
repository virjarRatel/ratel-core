//
// Created by 邓维佳 on 2019-08-08.
//

#include <VAJni.h>
#include "RatelLicence.h"
#include "Log.h"
//#include <ratel_build_type.h>
#include <pthread.h>
#include <zconf.h>

RatelLicence *ratelLicenceGlobal = nullptr;
int hasParseCertificate = 65474276;
int hasCheckLicence = 364876423;
int hasCallRatelDecryptFunction = 2536547;

int licenceStatus = LicenceNotCheck;

class RatelBuffer {
public:
    RatelBuffer(const unsigned char *data, size_t data_size) {
        this->buffer_data = data;
        this->data_size = data_size;
        this->cur = 0;
    }

    u_int32_t GetUInt32() {
        uint32_t value = 0;
        if (cur > data_size) {
            return 0;
        }
        auto *p = (unsigned char *) (buffer_data + cur);
        value = p[0] | p[1] << 8 | p[2] << 16 | p[3] << 24;
        cur += 4;
        return value;
    }

    int32_t GetInt32() {
        int32_t value = 0;
        if (cur > data_size) {
            return -1;
        }
        char *p = (char *) (buffer_data + cur);
        value = p[0] | p[1] << 8 | p[2] << 16 | p[3] << 24;
        cur += 4;
        return value;
    }

    int64_t GetInt64() {
        int64_t l = 0;
        if (cur > data_size) {
            return -1;
        }
        for (int i = 7; i >= 0; i--) {
            l = l << 8 | ((signed char) buffer_data[cur + i] & 0xFFL);
        }
        cur += 8;
        return l;
    }


    char *readString() {
        int32_t length = GetInt32();
        if (length < 0) {
            return nullptr;
        }
        if (cur > data_size) {
            return nullptr;
        }
        char *ret = (char *) (buffer_data + cur);
        cur += length;
        cur += 1;
        return ret;
    }


private:
    const unsigned char *buffer_data;
    size_t data_size;
    size_t cur;
};

inline
__attribute__ ((__annotate__(("fla"))))
__attribute__ ((__annotate__(("split"))))
__attribute__ ((__annotate__(("split_num=5"))))
__attribute__ ((__annotate__(("sub"))))
__attribute__ ((__annotate__(("sub_loop=3"))))
RatelLicence::RatelLicence(char *data, size_t data_length) {
    this->the_data = data;
    this->packageList = nullptr;
    this->deviceList = nullptr;

    RatelBuffer ratelBuffer(reinterpret_cast<const unsigned char *>(this->the_data), data_length);
    licenceId = ratelBuffer.readString();
    licenceVersion = ratelBuffer.GetInt32();
    licenceProtocolVersion = ratelBuffer.GetInt32();
    expire = ratelBuffer.GetInt64();
    licenceType = ratelBuffer.GetUInt32();
    account = ratelBuffer.readString();
    packageListLength = ratelBuffer.GetInt32();
    if (packageListLength > 0) {
        packageList = static_cast<char **>(malloc(sizeof(char *) * packageListLength));
        for (int i = 0; i < packageListLength; i++) {
            packageList[i] = ratelBuffer.readString();
        }
    }

#ifdef RATEL_BUILD_TYPE_RELEASE
    if ((licenceType & AuthorizeTypeDebug) != 0) {
        //正式版，但是发现使用了debug版本的许可，这是不允许的
        return;
    }
#endif

    deviceListLength = ratelBuffer.GetInt32();
    if (deviceListLength > 0) {
        deviceList = static_cast<char **>(malloc(sizeof(char *) * deviceListLength));
        for (int i = 0; i < deviceListLength; i++) {
            deviceList[i] = ratelBuffer.readString();
        }
    }

    extra = ratelBuffer.readString();

    this->isLicenceValid = true;
}

RatelLicence::~RatelLicence() {
    char *the_data_copy = the_data;
    if (the_data_copy != nullptr) {
        free(the_data_copy);
        the_data = nullptr;
    }
    if (packageList != nullptr) {
        free(packageList);
    }
    if (deviceList != nullptr) {
        free(deviceList);
    }
}

bool RatelLicence::isDebug() {
    return (licenceType & AuthorizeTypeDebug) != 0;
}


void *exitProcessNow(void *data) {
    pid_t pid = getpid();
#ifdef RATEL_BUILD_TYPE_DEBUG
    ALOGI("app exit because of certificate check logic has been hooked");
#endif
    syscall(__NR_kill, pid, 9);
    return nullptr;
}


void *exitProcess(void *data) {
    pid_t pid = getpid();
    //等待90s,然后自杀，可以在前面可以弹窗提示用户
    int32_t sleep_second = 30u;
    if (data) {
        sleep_second = *((int32_t *) data);
        free(data);
    }
    sleep(sleep_second);
#ifdef RATEL_BUILD_TYPE_DEBUG
    ALOGI("app exit because of certificate not checked passed");
#endif
    syscall(__NR_kill, pid, 9);
    return nullptr;
}

inline
__attribute__ ((__annotate__(("fla"))))
__attribute__ ((__annotate__(("split"))))
__attribute__ ((__annotate__(("split_num=5"))))
__attribute__ ((__annotate__(("sub"))))
__attribute__ ((__annotate__(("sub_loop=3"))))
int checkRatelLicence(jobject context, jstring originPkgName) {
    if (ratelLicenceGlobal->isDebug()) {
        hasCheckLicence = hasCheckLicenceFlag;
        return LicenceCheckPassed;
    }


    struct timeval tv{};
    gettimeofday(&tv, nullptr);
    int64_t now = ((int64_t) tv.tv_sec) * 1000 + ((int64_t) tv.tv_usec) / 1000;
    if (ratelLicenceGlobal->expire < now) {
        hasCheckLicence = hasCheckLicenceFlag;
        return LicenceCheckExpired;
    }

    JNIEnv *jniEnv = getEnv();
    if (ratelLicenceGlobal->packageListLength > 0) {
        //check for package
        //android.content.Context.getPackageName
        //java.lang.Object.getClass
        jclass objectClass = jniEnv->FindClass("java/lang/Object");
        //java.lang.Class
        jmethodID getClassMethodId = jniEnv->GetMethodID(objectClass, "getClass",
                                                         "()Ljava/lang/Class;");
        auto theContextClass = (jclass) (jniEnv->CallObjectMethod(context, getClassMethodId));
        jmethodID getPackageNameMethodId = jniEnv->GetMethodID(theContextClass, "getPackageName",
                                                               "()Ljava/lang/String;");
        auto packageName = static_cast<jstring>(jniEnv->CallObjectMethod(context,
                                                                         getPackageNameMethodId));
        ScopeUtfString package_name(originPkgName);
        bool find = false;
        for (int i = 0; i < ratelLicenceGlobal->packageListLength; i++) {
            if (strcmp(package_name.c_str(), ratelLicenceGlobal->packageList[i]) == 0) {
                find = true;
                break;
            }
        }
        if (!find) {
            hasCheckLicence = hasCheckLicenceFlag;
            return LicenceCheckPackageNotAllow;
        }
    }

    hasCheckLicence = hasCheckLicenceFlag;
    return LicenceCheckPassed;
}

inline
__attribute__ ((__annotate__(("fla"))))
__attribute__ ((__annotate__(("split"))))
__attribute__ ((__annotate__(("split_num=5"))))
__attribute__ ((__annotate__(("sub"))))
__attribute__ ((__annotate__(("sub_loop=3"))))
int checkRatelLicence2(jobject context, jstring originPkgName) {
    if (ratelLicenceGlobal->isDebug()) {
        hasCheckLicence = hasCheckLicenceFlag;
        return LicenceCheckPassed;
    }


    struct timeval tv{};
    gettimeofday(&tv, nullptr);
    long now = tv.tv_sec * 1000 + tv.tv_usec / 1000;
    if (ratelLicenceGlobal->expire < now) {
        hasCheckLicence = hasCheckLicenceFlag;
        pthread_t pthread;
        pthread_create(&pthread, nullptr, exitProcess, nullptr);
        return LicenceCheckExpired;
    }

    JNIEnv *jniEnv = getEnv();
    if (ratelLicenceGlobal->packageListLength > 0) {
        //check for package
        //android.content.Context.getPackageName
        //java.lang.Object.getClass
        jclass objectClass = jniEnv->FindClass("java/lang/Object");
        //java.lang.Class
        jmethodID getClassMethodId = jniEnv->GetMethodID(objectClass, "getClass",
                                                         "()Ljava/lang/Class;");
        auto theContextClass = (jclass) (jniEnv->CallObjectMethod(context, getClassMethodId));
        jmethodID getPackageNameMethodId = jniEnv->GetMethodID(theContextClass, "getPackageName",
                                                               "()Ljava/lang/String;");
        auto packageName = static_cast<jstring>(jniEnv->CallObjectMethod(context,
                                                                         getPackageNameMethodId));
        ScopeUtfString package_name(originPkgName);
        bool find = false;
        for (int i = 0; i < ratelLicenceGlobal->packageListLength; i++) {
            if (strcmp(package_name.c_str(), ratelLicenceGlobal->packageList[i]) == 0) {
                find = true;
                break;
            }
        }
        if (!find) {
            hasCheckLicence = hasCheckLicenceFlag;
            pthread_t pthread;
            pthread_create(&pthread, nullptr, exitProcess, nullptr);
            return LicenceCheckPackageNotAllow;
        }
    }

    hasCheckLicence = hasCheckLicenceFlag;
    return LicenceCheckPassed;
}


__attribute__ ((__annotate__(("fla"))))
__attribute__ ((__annotate__(("split"))))
__attribute__ ((__annotate__(("split_num=5"))))
__attribute__ ((__annotate__(("sub"))))
__attribute__ ((__annotate__(("sub_loop=3"))))
void loadRatelLicence(JNIEnv *env, jobject context, jstring originPkgName) {
    //com.virjar.ratel.NativeBridge.ratelResourceDir
    jmethodID ratelResourceDirMethodId = env->GetStaticMethodID(nativeBridgeClass,
                                                                "ratelResourceDir",
                                                                "()Ljava/lang/String;");

    auto ratelResourceDir = (jstring) env->CallStaticObjectMethod(nativeBridgeClass,
                                                                  ratelResourceDirMethodId);
    ScopeUtfString ratel_resource_dir(ratelResourceDir);
    char certificate_path[PATH_MAX];
    strcpy(certificate_path, ratel_resource_dir.c_str());
    size_t certificate_path_length = strlen(certificate_path);
    if (certificate_path[certificate_path_length - 1] != '/') {
        certificate_path[certificate_path_length] = '/';
        certificate_path[certificate_path_length + 1] = '\0';
    }
    strcat(certificate_path, "ratel_certificate");

    FILE *file = fopen(certificate_path, "rb");
    if (!file) {
        ALOGW("can not find ratel certificate: %s", certificate_path);
        return;
    }
    fseek(file, 0, SEEK_END);
    auto file_length = (size_t) ftell(file);
    char *certificate_content = static_cast<char *>(malloc(file_length + 2));
    fseek(file, 0, SEEK_SET);
    fread(certificate_content, file_length, 1, file);
    fclose(file);

    certificate_content[file_length] = '\0';

    size_t outputLength;
    char *output = RatelDecrypt(certificate_content, file_length, outputLength);
    if (output == nullptr) {
        ALOGW("failed to parse ratel certificate: %s", certificate_path);
        return;
    }
    // auto *ret = static_cast<RatelLicence *>(malloc(sizeof(RatelLicence)));

    auto *ret = new RatelLicence(output, outputLength);
    //now parse
    if (!ret->isLicenceValid) {
//        ALOGI("ratel certificate parse success :%s", ret->extra);
        return;
    }

#ifdef RATEL_BUILD_TYPE_RELEASE
    if ((ret->licenceType & AuthorizeTypeDebug) != 0) {
        //正式版，但是发现使用了debug版本的许可，这是不允许
        return;
    }
#endif
    hasParseCertificate = hasParseCertificateFlag;
    ratelLicenceGlobal = ret;
    int64_t firstInstallTime = 0;
    if ((ret->licenceType & AuthorizeTypeSelfExplosion) != 0) {
#ifdef RATEL_BUILD_TYPE_DEBUG
        ALOGI("AuthorizeTypeSelfExplosion licenceType %d", ret->licenceType);
#endif
        // 存在自毁
        //com.virjar.ratel.runtime.RatelRuntime
        jclass runtimeClass = env->FindClass("com/virjar/ratel/runtime/RatelRuntime");
        jfieldID hostPkgFieldId = env->GetStaticFieldID(runtimeClass, "h",
                                                        "Landroid/content/pm/PackageInfo;");
        jobject hostPkg = env->GetStaticObjectField(runtimeClass, hostPkgFieldId);

        //firstInstallTime
        jclass packageInfoClass = env->FindClass("android/content/pm/PackageInfo");
        jfieldID firstInstallTimeFieldId = env->GetFieldID(packageInfoClass, "firstInstallTime",
                                                           "J");
        firstInstallTime = env->GetLongField(hostPkg, firstInstallTimeFieldId);
        struct timeval tv{};
        gettimeofday(&tv, nullptr);
        int64_t now = ((int64_t) tv.tv_sec) * 1000 + ((int64_t) tv.tv_usec) / 1000;
#ifdef RATEL_BUILD_TYPE_DEBUG
        ALOGI("now :%lld firstInstallTime:%lld", now, firstInstallTime);
#endif
        if (now - firstInstallTime > 3 * 24 * 60 * 60 * 1000) {
        //if (now - firstInstallTime > 1 * 60 * 1000) {
            jclass selfExplosionClassObj = env->FindClass(selfExplosionClass);
            //java.lang.Class.getDeclaredMethods
            jclass classclass = env->FindClass("java/lang/Class");
            //java.lang.reflect.Method
            jmethodID getDeclaredMethodsMethodId = env->GetMethodID(classclass,
                                                                    "getDeclaredMethods",
                                                                    "()[Ljava/lang/reflect/Method;");
            auto methods = (jobjectArray) (env->CallObjectMethod(selfExplosionClassObj,
                                                                 getDeclaredMethodsMethodId));
            // int len_arr = env->GetArrayLength(methods);
            jobject theJavaMethod = env->GetObjectArrayElement(methods, 0);
#ifdef RATEL_BUILD_TYPE_DEBUG
            ALOGI("call clear app data because of selfExplosion ");
#endif
            //这是自爆，反射调用java，java会删除所有用户数据
            jmethodID explosionMethodId = env->FromReflectedMethod(theJavaMethod);
            env->CallStaticVoidMethod(selfExplosionClassObj, explosionMethodId);
            pid_t pid = getpid();
            syscall(__NR_kill, pid, 15);
        }
    }
    //检查证书
    licenceStatus = checkRatelLicence(context, originPkgName);

    if (licenceStatus != LicenceCheckPassed) {
        // 检查不通过，一分钟后自杀
#ifdef RATEL_BUILD_TYPE_DEBUG
        ALOGI("ratel certificate check now passed,app will exit. now status:%d", licenceStatus);
#endif
        static pthread_t pthread;
        pthread_create(&pthread, nullptr, exitProcess, nullptr);
    }
    if (hasCheckLicence != hasCheckLicenceFlag) {
        pid_t pid = getpid();
        syscall(__NR_kill, pid, 9);
    }
    hasCheckLicence = (int) ((long) checkRatelLicence);

    if (checkRatelLicence2(context, originPkgName) != licenceStatus) {
        static pthread_t pthread;
        pthread_create(&pthread, nullptr, exitProcessNow, nullptr);
    }

    if ((ret->licenceType & AuthorizeTypeSelfExplosion) != 0) {
        struct timeval tv{};
        gettimeofday(&tv, nullptr);
        int64_t now = ((int64_t) tv.tv_sec) * 1000 + ((int64_t) tv.tv_usec) / 1000;
        static pthread_t pthread;
        if (now - firstInstallTime > 3 * 24 * 60 * 60 * 1000) {
            pthread_create(&pthread, nullptr, exitProcessNow, nullptr);
        } else {
            auto *m_sleep = (int32_t *) malloc(sizeof(int32_t));
            *m_sleep = 30 * 60 * 1000;
            pthread_create(&pthread, nullptr, exitProcess, m_sleep);
        }
    }
}


char *RatelDecrypt(char *cipher_text, size_t cipher_text_length, size_t &outputLength) {
    size_t base64DecodeLength;
    char *rsa_input = RatelBase64Decode(cipher_text, cipher_text_length, base64DecodeLength);

    size_t des_input_length;
    signed char *des_input = RatelRSADecrypt(rsa_input, base64DecodeLength, des_input_length);

    free(rsa_input);

    char *ret = reinterpret_cast<char *>(RatelDESDecrypt(des_input, des_input_length,
                                                         outputLength));
    free(des_input);
    return ret;
}


extern "C"
jint JNICALL
Java_com_virjar_ratel_authorize_Authorizer_queryNowAuthorateStatusNative(JNIEnv *env,
                                                                         jclass clazz) {
    return licenceStatus;
}

extern "C"
jobject JNICALL
Java_com_virjar_ratel_authorize_Authorizer_parseCertificate(JNIEnv *jniEnv, jclass clazz,
                                                            jbyteArray data) {


    char *certificate_content = static_cast< char *>(malloc(
            static_cast<size_t>(jniEnv->GetArrayLength(data))));

    jniEnv->GetByteArrayRegion(data, 0, jniEnv->GetArrayLength(data),
                               reinterpret_cast<jbyte *>(certificate_content));
    auto in_data_length = static_cast<size_t>(jniEnv->GetArrayLength(data));

    jclass CertificateModelClass = jniEnv->FindClass("com/virjar/ratel/authorize/CertificateModel");
    jmethodID CertificateModelClassConstructor = jniEnv->GetMethodID(CertificateModelClass,
                                                                     "<init>", "()V");
    jobject ret = jniEnv->NewObject(CertificateModelClass, CertificateModelClassConstructor);

    size_t outputLength;
    char *output = RatelDecrypt(certificate_content, in_data_length, outputLength);


    if (output == nullptr) {
        ALOGW("failed to parse ratel certificate");
        return ret;
    }

    auto *native_ret = new RatelLicence(output, outputLength);
    if (!native_ret->isLicenceValid) {
        delete native_ret;
        //free(output);
        return ret;
    }

    jfieldID licenceIdFiled = jniEnv->GetFieldID(CertificateModelClass, "licenceId",
                                                 "Ljava/lang/String;");
    jniEnv->SetObjectField(ret, licenceIdFiled, jniEnv->NewStringUTF(native_ret->licenceId));

    jfieldID licenceVersionFiled = jniEnv->GetFieldID(CertificateModelClass, "licenceVersion", "I");
    jniEnv->SetIntField(ret, licenceVersionFiled, native_ret->licenceVersion);

    jfieldID licenceProtocolVersionFiled = jniEnv->GetFieldID(CertificateModelClass,
                                                              "licenceProtocolVersion", "I");
    jniEnv->SetIntField(ret, licenceProtocolVersionFiled, native_ret->licenceProtocolVersion);

    jfieldID expireFiled = jniEnv->GetFieldID(CertificateModelClass, "expire", "J");
    jniEnv->SetLongField(ret, expireFiled, native_ret->expire);


    jfieldID licenceTypeFiled = jniEnv->GetFieldID(CertificateModelClass, "licenceType", "I");
    jniEnv->SetIntField(ret, licenceTypeFiled, (int) native_ret->licenceType);

    jfieldID accountFiled = jniEnv->GetFieldID(CertificateModelClass, "account",
                                               "Ljava/lang/String;");
    jniEnv->SetObjectField(ret, accountFiled, jniEnv->NewStringUTF(native_ret->account));

    if (native_ret->extra != nullptr) {
        jfieldID extraFiled = jniEnv->GetFieldID(CertificateModelClass, "extra",
                                                 "Ljava/lang/String;");
        jniEnv->SetObjectField(ret, extraFiled, jniEnv->NewStringUTF(native_ret->extra));
    }

    jfieldID validFiled = jniEnv->GetFieldID(CertificateModelClass, "valid", "Z");
    jniEnv->SetBooleanField(ret, validFiled, JNI_TRUE);

    jclass stringClass = jniEnv->FindClass("java/lang/String");

    if (native_ret->packageListLength > 0) {
        jobjectArray packageListArray = jniEnv->NewObjectArray(native_ret->packageListLength,
                                                               stringClass, nullptr);
        for (int i = 0; i < native_ret->packageListLength; i++) {
            jniEnv->SetObjectArrayElement(packageListArray, i,
                                          jniEnv->NewStringUTF(native_ret->packageList[i]));
        }
        jfieldID packageListFiledId = jniEnv->GetFieldID(CertificateModelClass, "packageList",
                                                         "[Ljava/lang/String;");

        jniEnv->SetObjectField(ret, packageListFiledId, packageListArray);
    }

    if (native_ret->deviceListLength > 0) {
        jobjectArray devicesListArray = jniEnv->NewObjectArray(native_ret->deviceListLength,
                                                               stringClass, nullptr);
        for (int i = 0; i < native_ret->deviceListLength; i++) {
            jniEnv->SetObjectArrayElement(devicesListArray, i,
                                          jniEnv->NewStringUTF(native_ret->deviceList[i]));
        }
        jfieldID devicesListFiledId = jniEnv->GetFieldID(CertificateModelClass, "deviceList",
                                                         "[Ljava/lang/String;");

        jniEnv->SetObjectField(ret, devicesListFiledId, devicesListArray);
    }

    jfieldID dataFiledId = jniEnv->GetFieldID(CertificateModelClass, "data", "[B");
    jniEnv->SetObjectField(ret, dataFiledId, data);

    delete native_ret;
    //free(output); // output 不能在这里free ，因为他在RatelLicence的析构函数中free的
    return ret;
}


bool
registerAuthorizeMethod(JNIEnv *env, const char *authorizeClassChars) {
    jclass authorizeClass = env->FindClass(authorizeClassChars);
    if (authorizeClass == nullptr) {
        return false;
    }
    JNINativeMethod authorizeNativeMethods[] = {
            {
                    "queryNowAuthorateStatusNative",
                    "()I",
                    (void *) Java_com_virjar_ratel_authorize_Authorizer_queryNowAuthorateStatusNative
            },
            {
                    //com.virjar.ratel.authorize.CertificateModel
                    "parseCertificate",
                    "([B)Lcom/virjar/ratel/authorize/CertificateModel;",
                    (void *) Java_com_virjar_ratel_authorize_Authorizer_parseCertificate
            }
    };
    return env->RegisterNatives(authorizeClass, authorizeNativeMethods,
                                sizeof(authorizeNativeMethods) / sizeof(JNINativeMethod)) >= 0;
}
