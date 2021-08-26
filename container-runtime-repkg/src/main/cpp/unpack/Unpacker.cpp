//
// Created by 邓维佳 on 2020/8/28.
//

#include "Unpacker.h"

#include <utility>
#include <Log.h>
#include "Substrate/SubstrateHook.h"
#include "dlfcn_nougat.h"
#include <fstream>
#include <ostream>
#include <zconf.h>
#include <Helper.h>
#include "dex_ratel.h"
#include "elf_image.h"
#include "process_map.h"
#include "native_api.h"

//ArtMethod.Invoke()
#define ART_METHOD_INVOKE_SYM "_ZN3art9ArtMethod6InvokeEPNS_6ThreadEPjjPNS_6JValueEPKc"

#define AER_METHOD_PRETTY_METHOD   "_ZN3art9ArtMethod12PrettyMethodEb"
#define AER_METHOD_PRETTY_METHOD_2 "_ZN3art12PrettyMethodEPNS_9ArtMethodEb"

#define SHOULD_USE_INTERPRETER "_ZN3art11ClassLinker30ShouldUseInterpreterEntrypointEPNS_9ArtMethodEPKv"

#define CLASS_LINKER_LOAD_METHOD "_ZN3art11ClassLinker10LoadMethodERKNS_7DexFileERKNS_21ClassDataItemIteratorENS_6HandleINS_6mirror5ClassEEEPNS_9ArtMethodE"

extern const char *art_lib_path;
extern const char *jit_lib_path;
extern int SDK_INT;
Unpacker UnpackerInstance;

void (*artMethod_Invoke_Origin)(
        art::mirror::ArtMethod *thiz,
        void *thread_self, uint32_t *args, uint32_t args_size, JValue *result,
        const char *shorty) = nullptr;

void artMethod_Invoke_fake(
        art::mirror::ArtMethod *art_method,
        void *thread_self, uint32_t *args, uint32_t args_size, JValue *result,
        const char *shorty) {
    artMethod_Invoke_Origin(art_method, thread_self, args, args_size, result, shorty);
    UnpackerInstance.handleMethodDump(art_method);
}

void (*class_linker_loadMethod_Origin)(
        void *classLinker, void *dexFile, void *classDataItemIterator, void *classHandle,
        art::mirror::ArtMethod *method
);

void class_linker_loadMethod(
        void *classLinker, void *dexFile, void *classDataItemIterator, void *classHandle,
        art::mirror::ArtMethod *method
) {
    UnpackerInstance.dumpRawDex(dexFile);
    class_linker_loadMethod_Origin(classLinker, dexFile, classDataItemIterator, classHandle,
                                   method);
}


void Unpacker::init(std::string dump_work_dir, bool need_dump_method) {
    this->dumpWorkDir = std::move(dump_work_dir);
    this->need_dump_method_code = need_dump_method;
    // resolve method invoke
    void *handle = getSymCompat(
            art_lib_path, ART_METHOD_INVOKE_SYM);
    if (handle == nullptr) {
        UNPACK_LOGW("can not find symbol: %s ratel unpack will not running", ART_METHOD_INVOKE_SYM);
        return;
    }

    void *handle_prettyMethod = getSymCompat(art_lib_path, AER_METHOD_PRETTY_METHOD);
    if (handle_prettyMethod == nullptr) {
        handle_prettyMethod = getSymCompat(art_lib_path, AER_METHOD_PRETTY_METHOD_2);
    }
    if (handle_prettyMethod == nullptr) {
        UNPACK_LOGW("can not find symbol: %s and %s ratel unpack will not running",
                    AER_METHOD_PRETTY_METHOD, AER_METHOD_PRETTY_METHOD_2);
        return;
    }

    this->prettyMethod_handler = (std::string (*)(art::mirror::ArtMethod *artMethod,
                                                  bool with_signature)) (handle_prettyMethod);

    MSHookFunctionOld(handle, (void *) artMethod_Invoke_fake, (void **) &artMethod_Invoke_Origin);

//    void *class_linker_load_method_handle = getSymCompat(art_lib_path, CLASS_LINKER_LOAD_METHOD);
//    if (class_linker_load_method_handle == nullptr) {
//        UNPACK_LOGW("can not find symbol:%s", CLASS_LINKER_LOAD_METHOD);
//    } else {
//        MSHookFunction(class_linker_load_method_handle,
//                       (void *) class_linker_loadMethod,
//                       (void **) &class_linker_loadMethod_Origin);
//    }

    UNPACK_LOGI("ratel unpack init finished!");


    auto range = whale::FindExecuteMemoryRange(art_lib_path);
    if (range->IsValid()) {
        auto *image = new whale::ElfImage();
        if (!image->Open(range->path_, range->base_)) {
            delete image;
        } else {
            void *getDexFileHandle = image->FindSymbol<void *>("_ZN3art9ArtMethod10GetDexFileEv");
            UNPACK_LOGI("getDexFileHandle: %p", getDexFileHandle);
            this->getDexFile_handle = (void *(*)(art::mirror::ArtMethod *)) (getDexFileHandle);
        }
    }


    this->open = true;
}

static bool check_dex_magic(const uint8_t *dex_data) {
    if (dex_data == nullptr) {
        return false;
    }

    if ((unsigned long) dex_data % (sizeof(uint8_t *)) != 0) {
        //不是指针
        return false;
    }

    // check if this is dex file "dex\n035\0"
    // vdex的内容，其magic是 "dex\n037\0"
    // 所以检查到 "dex\n03" 就够了
    static uint8_t magic[] = {0x64, 0x65, 0x78, 0x0a, 0x30, 0x33};

    for (int i = 0; i < sizeof(magic); i++) {
        if (magic[i] != dex_data[i]) {
            return false;
        }
    }
    return true;
}

void Unpacker::handleMethodDump(art::mirror::ArtMethod *artMethod) {
    if (!this->open) {
        return;
    }
    //std::string method_sign = UnpackerInstance.prettyMethod(artMethod);
    // UNPACK_LOGI("dump method: %s", method_sign.c_str());
    // 当方法执行完成之后，执行dump
    void *dexFile = getDexFile(artMethod);
    //UNPACK_LOGI("dex file:%p", dexFile);
    if (dexFile == nullptr) {
        UNPACK_LOGW("can not get dexFile from artMethod");
        return;
    }

    DexFileHolder *dexFileHolder = this->get_or_create_dex_image(dexFile, artMethod);
    //UNPACK_LOGI("dex dexFileHolder :%p", dexFileHolder);
    if (dexFileHolder->valid() && this->need_dump_method_code) {
        dexFileHolder->dumpMethod(artMethod);
    } else {
        // UNPACK_LOGW("can not locate dex image for method:%s", method_sign.c_str());
    }
}

DexFileHolder *
Unpacker::create_dex_image(void *dex_file_handle, art::mirror::ArtMethod *artMethod) {
    //std::string method_sign = prettyMethod(artMethod);
    //UNPACK_LOGI("dump dexFile: %p", dex_file_handle);
    // 扫描 dexFile对象
    // 当android版本小于9.0的时候，第一个字段为dex开始，第二个字段为dex大小
    // 当Android版本大于等于9.0的时候，前面加一组VDex映射，之后是标准dex开始和标准dex大小
    const uint8_t *begin_;
    size_t size_;
    //const Header *header_;
    if (SDK_INT >= ANDROID_P) {
        auto *dexFileP = static_cast<DexFileP *>(dex_file_handle);
        // The base address of the memory mapping.
        begin_ = dexFileP->data_begin_;
        // The size of the underlying memory allocation in bytes.
        size_ = dexFileP->data_size_;

    } else {
        auto *dexFileO = (DexFileO *) (dex_file_handle);
        // The base address of the memory mapping.
        begin_ = dexFileO->begin_;

        // The size of the underlying memory allocation in bytes.
        size_ = dexFileO->size_;
    }

    auto *dexFileHolder = new DexFileHolder(this->dumpWorkDir);
    // 当前位置
    dexFileHolder->size_ = size_;
    dexFileHolder->begin_ = begin_;

    if (check_dex_magic(begin_)) {
        dexFileHolder->is_valid = true;
        dumpRawDex(dexFileHolder);
        return dexFileHolder;
    }
    std::string method_sign = std::string("unknown");
    if (artMethod != nullptr) {
        method_sign = prettyMethod(artMethod);
    }

    char print_buff[65];
    int ret = 0;
    for (int i = 0; i < 32; i++) {
        ret += sprintf(print_buff + ret, "%02x", begin_[i]);
    }
    print_buff[ret] = '\0';
    UNPACK_LOGW("can not auto find  dex image from dexFile for data:%s method:%s size:%d",
                print_buff, method_sign.c_str(), size_);
    return dexFileHolder;
}

DexFileHolder *
Unpacker::get_or_create_dex_image(void *dex_file_handle, art::mirror::ArtMethod *artMethod) {
    auto iter = this->dex_file_holder_map.find(
            dex_file_handle);
    if (iter != this->dex_file_holder_map.end()) {
        return iter->second;
    }
    DexFileHolder *holder = create_dex_image(dex_file_handle, artMethod);
    this->dex_file_holder_map.insert(
            std::pair<void *, DexFileHolder *>(dex_file_handle, holder)
    );

    return holder;
}

void Unpacker::dumpRawDex(DexFileHolder *dexFileHolder) {
    auto *header = (Header *) dexFileHolder->begin_;
    uint8_t *signature_ = header->signature_;

    char sig_chars[kSha1DigestSize * 2 + 1];

    static char char_map[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
                              'E', 'F'};

    for (unsigned int i = 0; i < kSha1DigestSize; i++) {
        sig_chars[i * 2] = char_map[(signature_[i] >> 4u) & 0x0fu];
        sig_chars[i * 2 + 1] = char_map[signature_[i] & 0x0fu];
    }
    sig_chars[kSha1DigestSize * 2] = '\0';


    char dex_path[PATH_MAX];
    sprintf(dex_path, "%s%s_method_dump/", this->dumpWorkDir.c_str(), sig_chars);
    dexFileHolder->method_dump_dirs = std::string(dex_path);
    if (access(dex_path, F_OK) != 0) {
        // 创建文件夹
        mkdir(dex_path, S_IRUSR + S_IWUSR + S_IXUSR);
    }

    sprintf(dex_path, "%s%s.dex", this->dumpWorkDir.c_str(), sig_chars);
    UNPACK_LOGI("dump dex filed into:%s", dex_path);
    if (access(dex_path, F_OK) == 0) {
        // exist
        //UNPACK_LOGE("can not write file:%s", dex_path);
        return;
    }
    std::ofstream out_dex_file;
    out_dex_file.open(dex_path, std::ios::out | std::ios::binary);

    out_dex_file.write(reinterpret_cast<const char *>(dexFileHolder->begin_), dexFileHolder->size_);
    out_dex_file.close();

    //UNPACK_LOGI("dump dex filed finished");
}

void *Unpacker::getDexFile(art::mirror::ArtMethod *pMethod) {
    if (this->getDexFile_handle != nullptr) {
        return this->getDexFile_handle(pMethod);
    }
    if ((pMethod->getAccessFlags() & 0x40000u)) {
        // obsoleteDex
        auto *pDexCache = static_cast<u_int32_t *>(getObsoleteDexCache_handle(pMethod));
        //return reinterpret_cast<void *>(*(pDexCache + 4));
        return reinterpret_cast<uint8_t *>(*((u_int32_t *) pDexCache + 4));
    }
//    u_int32_t declareClassGCRoot = pMethod->getDeclaringClass();
//    auto *mirrorClass = reinterpret_cast<u_int32_t *>(*(((u_int32_t *) declareClassGCRoot) +
//                                                        16));
//
//    return reinterpret_cast<void *>(*(mirrorClass + 16));

#ifdef __aarch64__
    return reinterpret_cast<uint8_t *>(*(int64_t *) (
            *(unsigned int *) (*(unsigned int *) pMethod + 0x10LL) + 0x10LL));
#else
    return reinterpret_cast<uint8_t *>(*(u_int32_t *) (*(u_int32_t *) (*(u_int32_t *) pMethod + 16) +
                                                    16));
#endif
}

void Unpacker::dumpRawDex(void *dex_file_handle) {
    get_or_create_dex_image(dex_file_handle, nullptr);
}

jobject Unpacker::methodDex(JNIEnv *env, jmethodID methodId) {
    if (!open) {
        UNPACK_LOGW(
                "component not enable,please call com.virjar.ratel.api.RatelUnpack.enableUnPack first!!");
        return nullptr;
    }
    art::mirror::ArtMethod *artMethod = getArtMethod(methodId);
    void *dexFileHandle = getDexFile(artMethod);
    if (dexFileHandle == nullptr) {
        std::string method_sign = UnpackerInstance.prettyMethod(artMethod);
        UNPACK_LOGW("can not find dex file for method:%s", method_sign.c_str());
        return nullptr;
    }

    DexFileHolder *dexFileHolder = get_or_create_dex_image(dexFileHandle, artMethod);
    dexFileHolder->forceValid();
    return env->NewDirectByteBuffer((void *) dexFileHolder->begin_, dexFileHolder->size_);
}

DexFileHolder::DexFileHolder(std::string dumpWorkDir) : dumpWorkDir(std::move(dumpWorkDir)) {}


void DexFileHolder::dumpMethod(art::mirror::ArtMethod *artMethod) {
    if (artMethod->isAbstract()) {
        return;
    }
    if (artMethod->isNative()) {
        return;
    }


    // Offset to the CodeItem.
    uint32_t dex_code_item_offset_ = artMethod->getDexCodeItemIndex();
    // Index into method_ids of the dex file associated with this method.
    uint32_t dex_method_index_ = artMethod->getDexMethodIndex();


    auto *codeItem = (CodeItem *) (this->begin_ + dex_code_item_offset_);

    auto inter = this->dumped_method.find(dex_method_index_);
    if (inter != this->dumped_method.end()) {
        //dumped already
        return;
    }
    std::string method_sign = UnpackerInstance.prettyMethod(artMethod);
    UNPACK_LOGI("dump method: %s  dex_code_item_offset_: %u dex_method_index_: %u",
                method_sign.c_str(), dex_code_item_offset_, dex_method_index_);
    this->dumped_method.insert(std::pair<uint32_t, bool>(dex_method_index_, true));

    auto *item = (uint8_t *) codeItem;
    int code_item_len;
    if (codeItem->tries_size_) {
        const u1 *handler_data = reinterpret_cast<const uint8_t *>(GetTryItems(*codeItem,
                                                                               codeItem->tries_size_));
        uint8_t *tail = codeitem_end(&handler_data);
        code_item_len = (int) (tail - item);
    } else {
        //正确的DexCode的大小
        code_item_len = 16 + codeItem->ins_size_ * 2;
    }

    if (code_item_len == 0) {
        //空函数body
        return;
    }

    char method_dump_file[PATH_MAX] = {'\0'};
    sprintf(method_dump_file, "%s%d_%d.bin", this->method_dump_dirs.c_str(), dex_method_index_,
            code_item_len);

    if (access(method_dump_file, F_OK) == 0) {
        // 已经写过了
        return;
    }
    UNPACK_LOGI("dump method into file:%s", method_dump_file);
    std::ofstream out_method_file;
    out_method_file.open(method_dump_file, std::ios::out | std::ios::binary);
    if (!out_method_file.is_open()) {
        UNPACK_LOGE("can not open file: %s", method_dump_file);
        return;
    }
    out_method_file.write((const char *) (item), code_item_len);
    out_method_file.flush();
    out_method_file.close();
}

void DexFileHolder::forceValid() {
    if (valid()) {
        return;
    }
    is_valid = true;
    UnpackerInstance.dumpRawDex(this);
}

