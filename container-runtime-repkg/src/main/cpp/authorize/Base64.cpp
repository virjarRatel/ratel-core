//
// Created by 邓维佳 on 2019-08-10.
//

#include <jni.h>
#include <VAJni.h>
#include <Log.h>
#include "RatelLicence.h"

char base64std[] = "9+#FGHlmBuvAwxNOPYZabnod5678pqrcstefghijCDEWXyz01IJKLM234kQRSTUV";
int base64res[128];
int maskArray[24];

bool hasInit = false;

class RatelBase64Random {

public:
    explicit RatelBase64Random(jstring seed) {
        //java.lang.Integer#parseUnsignedInt(java.lang.String, int)
        //java.util.Random
        jmethodID parseUnsignedIntMethodId = getEnv()->GetStaticMethodID(nativeBridgeClass,
                                                                         "newRandom",
                                                                         "(Ljava/lang/String;)Ljava/util/Random;");
        random = getEnv()->CallStaticObjectMethod(nativeBridgeClass, parseUnsignedIntMethodId,
                                                  seed);
        randomClass = getEnv()->FindClass("java/util/Random");
        nextIntMethodId = getEnv()->GetMethodID(randomClass, "nextInt", "()I");
    }

    int nextInt() {
        return getEnv()->CallIntMethod(random, nextIntMethodId);
    }

private:
    jobject random = nullptr;
    jclass randomClass = nullptr;
    jmethodID nextIntMethodId = nullptr;
};

//注意，这里只提供解密算法，不提供加密算法，避免加密算法被破译
char *
__attribute__ ((__annotate__(("fla"))))
__attribute__ ((__annotate__(("split"))))
__attribute__ ((__annotate__(("split_num=5"))))
__attribute__ ((__annotate__(("sub"))))
__attribute__ ((__annotate__(("sub_loop=3"))))
RatelBase64Decode(const char *in_data, size_t in_data_length, size_t &outputLength) {
    if (!hasInit) {
        for (int i = 0; i < 64; i++) {
            base64res[base64std[i]] = i;
        }
        for (unsigned int i = 0; i < 24; i++) {
            maskArray[i] = 1u << i;
        }
        hasInit = true;
    }

    if (in_data_length < 32) {
        //不合法数据
        return nullptr;
    }

    unsigned int matchArray[24];
    //还原maskarray
    for (int i = 0; i < 24; i++) {
        unsigned int j = 0;
        for (; j < 24; j++) {
            if (base64std[j] == in_data[i]) {
                matchArray[i] = j;
                break;
            }
        }
        if (j == 24) {
            return nullptr;
        }
    }

    int i, j, n;
    n = in_data_length - 4;
    int temp;
    char *output = static_cast<char *>(malloc((in_data_length - 32) * 3 / 4 + 8));
    for (i = 32, j = 0; i < n; i += 4) {
        //这里的警告不重要，因为数据最多24位，不会溢出
        temp = (base64res[in_data[i]] << 18) + (base64res[in_data[i + 1]] << 12)
               + (base64res[in_data[i + 2]] << 6) + (base64res[in_data[i + 3]]);
        unsigned int substitution = 0;
        for (int k = 23; k >= 0; k--) {
            substitution |= (temp >> k & 0x01u) << matchArray[23 - k];
        }
        output[j++] = (char) (substitution >> 16u);
        output[j++] = (char) ((substitution >> 8u) & 0xffu);
        output[j++] = (char) (substitution & 0xffu);
    }

    if (in_data[i + 2] == '=') {
        output[j] = (char) ((base64res[in_data[i]] << 2) +
                            (base64res[in_data[i + 1]] >> 4));
    } else if (in_data[i + 3] == '=') {
        temp = (base64res[in_data[i]] << 10) + (base64res[in_data[i + 1]] << 4)
               + (base64res[in_data[i + 2]] >> 2);
        output[j++] = (char) (temp >> 8u);
        output[j] = (char) (temp & 0xffu);
    } else {
        temp = (base64res[in_data[i]] << 18) + (base64res[in_data[i + 1]] << 12)
               + (base64res[in_data[i + 2]] << 6) + (base64res[in_data[i + 3]]);
        unsigned int substitution = 0;
        for (int k = 23; k >= 0; k--) {
            substitution |= (temp >> k & 0x01u) << matchArray[23 - k];
        }
        output[j++] = (char) (substitution >> 16u);
        output[j++] = (char) ((substitution >> 8u) & 0xffu);
        output[j] = (char) (substitution & 0xffu);
    }

    outputLength = (j + 1);
    // 异或序列
    char key_string[9];
    for (int k = 24; k < 32; k++) {
        key_string[k - 24] = in_data[k];
    }
    key_string[8] = '\0';
    jstring key_string_jni = getEnv()->NewStringUTF(key_string);
    RatelBase64Random random(key_string_jni);
    for (int k = 0; k <= j; k++) {
        output[k] = output[k] ^ (char) random.nextInt();
    }
    return output;
}