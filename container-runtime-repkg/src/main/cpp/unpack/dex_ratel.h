//
// Created by 邓维佳 on 2020/8/30.
//

#ifndef RATEL2_DEX_RATEL_H
#define RATEL2_DEX_RATEL_H

#define TAG_UNPACK "unpack"

#include "primitive_types.h"
#include "cxx_helper.h"

#define UNPACK_LOGI(...) __android_log_print(ANDROID_LOG_ERROR, TAG_UNPACK ,__VA_ARGS__)
#define UNPACK_LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG_UNPACK ,__VA_ARGS__)
#define UNPACK_LOGW(...) __android_log_print(ANDROID_LOG_ERROR, TAG_UNPACK ,__VA_ARGS__)
#define UNPACK_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG_UNPACK ,__VA_ARGS__)

static constexpr size_t kSha1DigestSize = 20;
// Raw header_item.
struct Header {
    uint8_t magic_[8];
    uint32_t checksum_;  // See also location_checksum_
    uint8_t signature_[kSha1DigestSize];
    uint32_t file_size_;  // size of entire file
    uint32_t header_size_;  // offset to start of next section
    uint32_t endian_tag_;
    uint32_t link_size_;  // unused
    uint32_t link_off_;  // unused
    uint32_t map_off_;  // unused
    uint32_t string_ids_size_;  // number of StringIds
    uint32_t string_ids_off_;  // file offset of StringIds array
    uint32_t type_ids_size_;  // number of TypeIds, we don't support more than 65535
    uint32_t type_ids_off_;  // file offset of TypeIds array
    uint32_t proto_ids_size_;  // number of ProtoIds, we don't support more than 65535
    uint32_t proto_ids_off_;  // file offset of ProtoIds array
    uint32_t field_ids_size_;  // number of FieldIds
    uint32_t field_ids_off_;  // file offset of FieldIds array
    uint32_t method_ids_size_;  // number of MethodIds
    uint32_t method_ids_off_;  // file offset of MethodIds array
    uint32_t class_defs_size_;  // number of ClassDefs
    uint32_t class_defs_off_;  // file offset of ClassDef array
    uint32_t data_size_;  // size of data section
    uint32_t data_off_;  // file offset of data section
};

// Raw code_item.
struct CodeItem {
    uint16_t registers_size_;            // the number of registers used by this code
    //   (locals + parameters)
    uint16_t ins_size_;                  // the number of words of incoming arguments to the method
    //   that this code is for
    uint16_t outs_size_;                 // the number of words of outgoing argument space required
    //   by this code for method invocation
    uint16_t tries_size_;                // the number of try_items for this instance. If non-zero,
    //   then these appear as the tries array just after the
    //   insns in this instance.
    uint32_t debug_info_off_;            // file offset to debug info stream
    uint32_t insns_size_in_code_units_;  // size of the insns array, in 2 byte code units
    uint16_t insns_[1];                  // actual array of bytecode.
};

//https://www.androidos.net.cn/android/8.0.0_r4/xref/art/runtime/dex_file.h
class DexFileO {
public:
    const void *stub;
    // The base address of the memory mapping.
    const uint8_t *const begin_;

    // The size of the underlying memory allocation in bytes.
    const size_t size_;

};

//https
// ://www.androidos.net.cn/android/9.0.0_r8/xref/art/libdexfile/dex/dex_file.h
class DexFileP {
public:
    const void *stub;
    // The base address of the memory mapping.
    const uint8_t *const begin_;

    // The size of the underlying memory allocation in bytes.
    const size_t size_;

    // The base address of the data section (same as Begin() for standard dex).
    const uint8_t *const data_begin_;

    // The size of the data section.
    const size_t data_size_;
};



// Raw try_item.
struct TryItem {
    uint32_t start_addr_;
    uint16_t insn_count_;
    uint16_t handler_off_;
};



template<typename T>
constexpr T RoundDown(T x, typename Identity<T>::type n) {
    // DCHECK(IsPowerOfTwo(n));
    return (x & -n);
}

template<typename T>
constexpr T RoundUp(T x, typename std::remove_reference<T>::type n) {
    return RoundDown(x + n - 1, n);
}

const TryItem *GetTryItems(const CodeItem &code_item, uint32_t offset) {
    const uint16_t *insns_end_ = &code_item.insns_[code_item.insns_size_in_code_units_];
    return reinterpret_cast<const TryItem *>
           (RoundUp(reinterpret_cast<uintptr_t>(insns_end_), 4)) + offset;
}

/*
 * Reads an unsigned LEB128 value, updating the given pointer to point
 * just past the end of the read value. This function tolerates
 * non-zero high-order bits in the fifth encoded byte.
 */
int readUnsignedLeb128(const u1 **pStream) {
    const u1 *ptr = *pStream;
    int result = *(ptr++);

    if (result > 0x7f) {
        int cur = *(ptr++);
        result = (result & 0x7f) | ((cur & 0x7f) << 7);
        if (cur > 0x7f) {
            cur = *(ptr++);
            result |= (cur & 0x7f) << 14;
            if (cur > 0x7f) {
                cur = *(ptr++);
                result |= (cur & 0x7f) << 21;
                if (cur > 0x7f) {
                    /*
                     * Note: We don't check to see if cur is out of
                     * range here, meaning we tolerate garbage in the
                     * high four-order bits.
                     */
                    cur = *(ptr++);
                    result |= cur << 28;
                }
            }
        }
    }

    *pStream = ptr;
    return result;
}

/*
 * Reads a signed LEB128 value, updating the given pointer to point
 * just past the end of the read value. This function tolerates
 * non-zero high-order bits in the fifth encoded byte.
 */
int readSignedLeb128(const u1 **pStream) {
    const u1 *ptr = *pStream;
    int result = *(ptr++);

    if (result <= 0x7f) {
        result = (result << 25) >> 25;
    } else {
        int cur = *(ptr++);
        result = (result & 0x7f) | ((cur & 0x7f) << 7);
        if (cur <= 0x7f) {
            result = (result << 18) >> 18;
        } else {
            cur = *(ptr++);
            result |= (cur & 0x7f) << 14;
            if (cur <= 0x7f) {
                result = (result << 11) >> 11;
            } else {
                cur = *(ptr++);
                result |= (cur & 0x7f) << 21;
                if (cur <= 0x7f) {
                    result = (result << 4) >> 4;
                } else {
                    /*
                     * Note: We don't check to see if cur is out of
                     * range here, meaning we tolerate garbage in the
                     * high four-order bits.
                     */
                    cur = *(ptr++);
                    result |= cur << 28;
                }
            }
        }
    }

    *pStream = ptr;
    return result;
}

uint8_t *codeitem_end(const u1 **pData) {
    uint32_t num_of_list = readUnsignedLeb128(pData);
    for (; num_of_list > 0; num_of_list--) {
        int32_t num_of_handlers = readSignedLeb128(pData);
        int num = num_of_handlers;
        if (num_of_handlers <= 0) {
            num = -num_of_handlers;
        }
        for (; num > 0; num--) {
            readUnsignedLeb128(pData);
            readUnsignedLeb128(pData);
        }
        if (num_of_handlers <= 0) {
            readUnsignedLeb128(pData);
        }
    }
    return (uint8_t *) (*pData);
}

#endif //RATEL2_DEX_RATEL_H
