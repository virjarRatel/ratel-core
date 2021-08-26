#include <cstdint>
#include <cstdlib>
#include <cstring>
#include "RatelLicence.h"

char IP_TABLE[] = {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17, 9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
};

char FP_TABLE[] = {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41, 9, 49, 17, 57, 25
};


char E_TABLE[] = {
        1, 1, 2, 3, 4, 5,
        4, 5, 6, 7, 8, 9,
        29, 9, 10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        28, 25, 26, 27, 28, 8,
        24, 29, 30, 31, 32, 32
};

char S_TABLE[8][64] = {
        {
                14, 4,  13, 1,  2,  15, 11, 8,  3,  10, 6,  12, 5,  9,  0,  7,
                0,  15, 7,  4,  14, 2,  13, 1,  10, 6, 12, 11, 9,  5,  3,  8,
                4,  1,  14, 8,  13, 6,  2,  11, 15, 12, 9,  7,  3,  10, 5,  0,
                15, 12, 8,  2,  4,  9,  1,  7,  5,  11, 3,  14, 10, 0, 6,  13
        },
        {
                15, 1,  8,  14, 6,  11, 3,  4,  9,  7,  2,  13, 12, 0,  5,  10,
                3,  13, 4,  7,  15, 2,  8,  14, 12, 0, 1,  10, 6,  9,  11, 5,
                0,  14, 7,  11, 10, 15, 13, 1,  5,  8,  12, 6,  9,  3,  2,  15,
                13, 8,  10, 1,  3,  4,  4,  2,  11, 6,  7,  12, 0,  5, 14, 9
        },
        {
                10, 0,  9,  14, 6,  3,  15, 5,  1,  13, 12, 7,  11, 4,  2,  8,
                13, 7,  0,  9,  3,  4,  6,  10, 2,  8, 5,  14, 12, 11, 15, 1,
                13, 6,  4,  9,  8,  15, 3,  0,  11, 1,  2,  12, 5,  10, 14, 7,
                1,  10, 13, 0,  6,  9,  8,  7,  4,  14, 15, 11, 3,  5, 2,  12
        },
        {
                7,  13, 14, 3,  0,  6,  9,  10, 1,  2,  8,  5,  11, 12, 4,  15,
                13, 8,  11, 5,  6,  15, 0,  3,  4,  7, 2,  12, 1,  10, 14, 9,
                10, 6,  9,  0,  12, 11, 7,  13, 15, 1,  3,  14, 5,  2,  8,  4,
                3,  15, 0,  6,  10, 1,  13, 8,  9,  4,  5,  11, 12, 7, 2,  14
        },
        {
                2,  12, 4,  1,  7,  10, 11, 6,  8,  5,  3,  15, 13, 0,  14, 9,
                14, 11, 2,  12, 4,  7,  13, 1,  5,  0, 15, 10, 3,  9,  8,  6,
                4,  2,  1,  11, 10, 13, 7,  8,  15, 9,  12, 5,  6,  3,  0,  14,
                11, 8,  12, 7,  1,  14, 2,  13, 6,  15, 0,  9,  10, 4, 5,  3
        },
        {
                12, 1,  10, 15, 9,  2,  6,  8,  0,  13, 3,  4,  14, 7,  5,  11,
                10, 15, 4,  2,  7,  12, 9,  5,  6,  1, 13, 14, 0,  11, 3,  8,
                9,  14, 15, 5,  2,  8,  12, 3,  7,  0,  4,  10, 1,  13, 11, 6,
                4,  3,  2,  12, 9,  5,  15, 10, 11, 14, 1,  7,  6,  0, 8,  13
        },
        {
                4,  11, 2,  14, 15, 0,  8,  13, 3,  12, 9,  7,  5,  10, 6,  1,
                13, 0,  11, 7,  4,  9,  1,  10, 14, 3, 5,  12, 2,  15, 8,  6,
                1,  4,  11, 13, 12, 3,  7,  14, 10, 15, 6,  8,  0,  5,  9,  2,
                6,  11, 13, 8,  1,  4,  10, 7,  9,  5,  0,  15, 14, 2, 3,  12
        },
        {
                13, 2,  8,  4,  6,  15, 11, 1,  10, 9,  3,  14, 5,  0,  12, 7,
                1,  15, 13, 8,  10, 3,  7,  4,  12, 5, 6,  11, 0,  14, 9,  2,
                7,  11, 4,  1,  9,  12, 14, 2,  0,  6,  10, 13, 15, 3,  5,  8,
                2,  1,  14, 7,  4,  10, 8,  13, 15, 12, 9,  0,  3,  5, 6,  11
        }
};


char P_TABLE[] = {
        16, 7, 20, 21,
        29, 12, 28, 17,
        1, 15, 23, 26,
        5, 18, 31, 10,
        2, 8, 24, 14,
        32, 27, 3, 9,
        19, 13, 30, 6,
        22, 11, 4, 25
};

char PC1_TABLE[] = {
        57, 49, 41, 33, 25, 17, 9,
        1, 58, 50, 42, 34, 26, 18,
        10, 2, 59, 51, 43, 35, 27,
        19, 11, 3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15,
        7, 62, 54, 46, 38, 30, 22,
        14, 6, 61, 53, 45, 37, 29,
        21, 13, 5, 28, 20, 12, 4
};

char PC2_TABLE[] = {
        14, 17, 11, 24, 1, 5,
        3, 28, 15, 6, 21, 10,
        23, 19, 12, 4, 26, 8,
        16, 7, 27, 20, 13, 2,
        41, 52, 31, 37, 47, 55,
        30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53,
        46, 42, 50, 36, 29, 32
};

char rotations_TABLE[] = {
        1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 1
};


inline int64_t permute(const char *table, size_t table_length, int srcWidth, int64_t src) __attribute((always_inline)){
    int64_t dst = 0;
    for (int i = 0; i < table_length; i++) {
        int srcPos = srcWidth - table[i];
        dst = (dst << 1) | (src >> srcPos & 0x01);
    }
    return dst;
}

#define IP(src) \
    permute(IP_TABLE, sizeof(IP_TABLE), 64, src)


#define FP(src)\
    permute(FP_TABLE, sizeof(FP_TABLE), 64, src)


#define E(src)\
    permute(E_TABLE, sizeof(E_TABLE), 32, (src) & 0xFFFFFFFFL)


#define P(src)\
    (int32_t) permute(P_TABLE, sizeof(P_TABLE), 32, (src) & 0xFFFFFFFFL)


#define PC1(src) \
    permute(PC1_TABLE, sizeof(PC1_TABLE), 64, src)


#define PC2(src) \
     permute(PC2_TABLE, sizeof(PC2_TABLE), 56, src)


#define S(box_number, src) \
    S_TABLE[(box_number) - 1][(signed char) ((src) & 0x20 | (((src) & 0x01) << 4) | (((src) & 0x1E) >> 1))]

inline int64_t getLongFromBytes(signed char *ba, size_t ba_length, int offset) __attribute((always_inline)){
    int64_t l = 0;
    for (int i = 0; i < 8; i++) {
        signed char value;
        if ((offset + i) < ba_length) {
            // and last bits determine which 16-value row to
            // reference, so we transform the 6-bit input into an
            // absolute
            value = ba[offset + i];
        } else {
            value = 0;
        }
        l = l << 8 | (value & 0xFFL);
    }
    return l;
}

inline void getBytesFromLong(signed char *ba, size_t ba_length, int offset, int64_t l) __attribute((always_inline)) {
    for (int i = 7; i > -1; i--) {
        if ((offset + i) < ba_length) {
            ba[offset + i] = (signed char) (l & 0xFF);
            l = l >> 8;
        } else {
            break;
        }
    }
}


inline void
__attribute__ ((__annotate__(("fla"))))
__attribute__ ((__annotate__(("split"))))
__attribute__ ((__annotate__(("split_num=5"))))
__attribute__ ((__annotate__(("sub"))))
__attribute__ ((__annotate__(("sub_loop=3"))))
decryptCBC(const signed char *cipher_text, size_t cipher_text_length, signed char *des_key,
           size_t key_length,
           signed char *output, size_t &output_length) {
    //我们手动让代码inline，目的是增加单个函数的复杂度，所有逻辑糅合在单个函数之中后，可以借用ollvm混淆的能力，使得控制流混淆更加深层
    //目前知晓的是，ollvm只能在单个函数内执行流程混淆，如果单个函数流程比较简单，那么流程的模块化依然相对清晰
    int64_t IV = 5597;

    int64_t key = getLongFromBytes(des_key, key_length, 0);
    int64_t previousCipherBlock = IV;

    auto *cipher_text_copy = (signed char *) malloc(cipher_text_length + 1);
    memcpy(cipher_text_copy, cipher_text, cipher_text_length);

    int64_t subkeys[17];
    // createSubkeys(subkeys, key);
    ///////////////////////////////////////////////////////////////////createSubkeys start
    // perform the PC1 permutation
    key = PC1(key);

    // split into 28-bit left and right (c and d) pairs.
    int32_t c = (int32_t) (key >> 28);
    int32_t d = (int32_t) (key & 0x0FFFFFFF);

    // for each of the 16 needed subkeys, perform a bit
    // rotation on each 28-bit keystuff half, then join
    // the halves together and permute to generate the
    // subkey.
    for (int i = 0; i < 17; i++) {
        hasCallRatelDecryptFunction = hasCallRatelDecryptFunctionFlag;
        // rotate the 28-bit values

        c = ((c << rotations_TABLE[i]) & 0x0FFFFFFF) | (c >> (28 - rotations_TABLE[i]));
        d = ((d << rotations_TABLE[i]) & 0x0FFFFFFF) | (d >> (28 - rotations_TABLE[i]));
        // join the two keystuff halves together.
        int64_t cd = (((int64_t) c) & 0xFFFFFFFFL) << 28 | (d & 0xFFFFFFFFL);

        cd = cd ^ 3543654654623L;

        // perform the PC2 permutation
        subkeys[i] = PC2(cd);
    }

    ///////////////////////////////////////////////////////////////////createSubkeys end

    for (int i = 0; i < cipher_text_length; i += 8) {
        // get the cipher block to be decrypted (8bytes = 64bits)
        int64_t cipherBlock = getLongFromBytes(cipher_text_copy, cipher_text_length, i);


        ///////////////////////////////////////////////////////////////////decryptBlock start
        // perform the initial permutation
        int64_t ip = IP(cipherBlock);

        // split the 32-bit value into 16-bit left and right halves.
        int32_t l = (int32_t) (ip >> 32);
        int32_t r = (int32_t) (ip & 0xFFFFFFFFL);

        // perform 16 rounds
        // NOTE: reverse order of subkeys used!
        for (int j = 16; j > -1; j--) {
            int32_t previous_l = l;
            // the right half becomes the new left half.
            l = r;
            // the Feistel function is applied to the old left half
            // and the resulting value is stored in the right half.
            /////////////////////////////////////////////////////////////////////feistel start
            // 1. expansion
            int64_t e = E(r);
            // 2. key mixing
            int64_t x = e ^subkeys[j];
            // 3. substitution
            int32_t dst = 0;
            for (int k = 0; k < 8; k++) {
                //dst >> >= 4;
                //TODO 这里是否合理
                dst = ((unsigned int) dst) >> 4u;
                int32_t s = S(8 - k, (signed char) (x & 0x3F));
                dst |= s << 28;
                x >>= 6;
            }
            // 4. permutation
            int32_t feistel_ret = P(dst) ^3436547;

            /////////////////////////////////////////////////////////////////////feistel end
            //r = previous_l ^ feistel(r, subkeys[j]);
            r = previous_l ^ feistel_ret;
        }

        // reverse the two 32-bit segments (left to right; right to left)
        int64_t rl = (((int64_t) r) & 0xFFFFFFFFL) << 32 | (l & 0xFFFFFFFFL);

        // apply the final permutation
        int64_t messageBlock = FP(rl);

        /////////////////////////////////////////////////////////////////////decryptBlock end

        // Decrypt the cipher block and XOR with previousCipherBlock
        // First previousCiphertext = Initial Vector (IV)
        //int64_t messageBlock = decryptBlock(cipherBlock, key);
        messageBlock = messageBlock ^ previousCipherBlock;

        // Store the messageBlock in the correct position in message
        getBytesFromLong(output, cipher_text_length, i, messageBlock);

        // Update previousCipherBlock
        previousCipherBlock = cipherBlock;
    }

    output_length = cipher_text_length;
    free(cipher_text_copy);

}

char *desKey = const_cast<char *>("ABu9#b+vPHwxYaFlZNmdoGnOefebc3b7YSzuq2D9tKrsd9==");

signed char *
RatelDESDecrypt(const signed char *cipher_text, size_t cipher_text_length,

                size_t &output_length) {

    int last_length = cipher_text[cipher_text_length - 1];

    size_t decryptedDesKeyOutputLength;

    signed char *output = static_cast<signed char *>(malloc(cipher_text_length + 8));

    char *decryptedDesKey = RatelBase64Decode(desKey, strlen(desKey), decryptedDesKeyOutputLength);
    decryptedDesKey[decryptedDesKeyOutputLength] = '\0';


    decryptCBC(cipher_text, cipher_text_length - 1,
               reinterpret_cast<signed char *>(decryptedDesKey),
               decryptedDesKeyOutputLength, output,
               output_length);

    size_t final_output_length = cipher_text_length - 1 - (8 - last_length);
    if (last_length == 0) {
        final_output_length = output_length;
    }

    output_length = final_output_length;

    free(decryptedDesKey);
    return output;
}