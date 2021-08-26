//
// Created by 邓维佳 on 2019-08-11.
//

#include <malloc.h>
#include <cstring>
#include <VAJni.h>
#include <Log.h>
#include "RatelLicence.h"

#define KEY_SIZE 1024
char rsa_public_key[] = "wZYxOmvlud#b9NaPnBHFAoG+fc530e06couQVdTZOaaq+bhrof1qtSLwz3IR0alykOlNN#lFO1GRe9E#sZoPzUvCdamtAdHoVhbt#YONYdJWyvPsW#q8Ly1GO6WzRTJWYnvAxiHrNaU9vzycEejWhyfaWEHdsLh908r0QLXnqT6seG3fSRmHx6eMImKX2KDtgTcHxuL5P8uzvJD4r1i#FFq8wpx1BJ8xvCHXLaWinIANQoZnm0Jca4zD";

/*
 * public static byte[] decrypt(String input) {
        //  return RatelBase64.decode(input);
        byte[] rsaInput = RatelBase64.decode(input);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
                    // Base64.decodeBase64(rsaPublicKey)
                    RatelBase64.decode(rsaPublicKey)
            );
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            byte[] rsaOutput = cipherDoFinal(cipher, rsaInput, KEY_SIZE / 8);
            return DES.ratelDecrypt(rsaOutput, desKey);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
*/

//signed char *RatelRSADecrypt(const char *cipher_text, size_t cipher_text_length,
//                             size_t &output_length) {
//    JNIEnv *jniEnv = getEnv();
//    //    char *rsa_input = static_cast<char *>(malloc(output_length + 1));
////    memcpy(rsa_input, output, output_length);
//
//    size_t rsa_public_key_binary_length;
//    char *rsa_public_key_binary = RatelBase64Decode(rsa_public_key, strlen(rsa_public_key),
//                                                    rsa_public_key_binary_length);
//    jbyteArray rsa_public_key_byte_array = jniEnv->NewByteArray(
//            static_cast<jsize>(rsa_public_key_binary_length));
//    jniEnv->SetByteArrayRegion(rsa_public_key_byte_array, 0,
//                               static_cast<jsize>(rsa_public_key_binary_length),
//                               reinterpret_cast<const jbyte *>(rsa_public_key_binary));
//    free(rsa_public_key_binary);
//
//
//
//
//
//    jbyteArray input = jniEnv->NewByteArray(
//            static_cast<jsize>(cipher_text_length));
//
//    jniEnv->SetByteArrayRegion(input, 0,
//                               static_cast<jsize>(cipher_text_length),
//                               reinterpret_cast<const jbyte *>(cipher_text));
//
//    jclass rsaClass = jniEnv->FindClass(ratelRSAClass);
//    jmethodID rsaDecryptMethodId = jniEnv->GetStaticMethodID(rsaClass, "rsaDecrypt", "([B[B)[B");
//    jbyteArray output = static_cast<jbyteArray>(jniEnv->CallStaticObjectMethod(rsaClass,
//                                                                               rsaDecryptMethodId,
//                                                                               input,
//                                                                               rsa_public_key_byte_array));
//
//    jbyte *c_array = jniEnv->GetByteArrayElements(output, 0);
//    int len_arr = jniEnv->GetArrayLength(output);
//    char *ret = static_cast<char *>(malloc(static_cast<size_t>(len_arr + 1)));
//    memcpy(ret, c_array, len_arr);
//    ret[len_arr] = '\0';
//
//    jniEnv->ReleaseByteArrayElements(output, c_array, 0);
//    output_length = len_arr;
//    return reinterpret_cast<signed char *>(ret);
//}

signed char *RatelRSADecrypt(const char *cipher_text, size_t cipher_text_length,
                             size_t &output_length) {

    size_t rsa_public_key_binary_length;
    char *rsa_public_key_binary = RatelBase64Decode(rsa_public_key, strlen(rsa_public_key),
                                                    rsa_public_key_binary_length);

    JNIEnv *jniEnv = getEnv();
    jclass keyFactoryClass = jniEnv->FindClass("java/security/KeyFactory");
    jmethodID getInstanceMethodId = jniEnv->GetStaticMethodID(keyFactoryClass, "getInstance",
                                                              "(Ljava/lang/String;)Ljava/security/KeyFactory;");
    //get keyFactory instance
    jobject rsaKeyFactoryObject = jniEnv->CallStaticObjectMethod(keyFactoryClass,
                                                                 getInstanceMethodId,
                                                                 jniEnv->NewStringUTF("RSA"));

    jclass x509KeySpecClass = jniEnv->FindClass(
            "java/security/spec/X509EncodedKeySpec");
    jmethodID x509KeySpecConstructorId = jniEnv->GetMethodID(x509KeySpecClass, "<init>", "([B)V");
    jbyteArray rsa_public_key_byte_array = jniEnv->NewByteArray(
            static_cast<jsize>(rsa_public_key_binary_length));
    jniEnv->SetByteArrayRegion(rsa_public_key_byte_array, 0,
                               static_cast<jsize>(rsa_public_key_binary_length),
                               reinterpret_cast<const jbyte *>(rsa_public_key_binary));


    jobject x509KeySpec = jniEnv->NewObject(x509KeySpecClass, x509KeySpecConstructorId,
                                            rsa_public_key_byte_array);

    //gen pubic key object
    jmethodID generatePublicMethodId = jniEnv->GetMethodID(keyFactoryClass, "generatePublic",
                                                           "(Ljava/security/spec/KeySpec;)Ljava/security/PublicKey;");
    jobject pubKey = jniEnv->CallObjectMethod(rsaKeyFactoryObject, generatePublicMethodId,
                                              x509KeySpec);

    //create cipher
    //jmethodID getAlgorithMethodId = jniEnv->GetMethodID(keyFactoryClass,"getAlgorithm","()")
    jclass cipherClass = jniEnv->FindClass("javax/crypto/Cipher");
    jmethodID getCipherInstanceMethodId = jniEnv->GetStaticMethodID(cipherClass, "getInstance",
                                                                    "(Ljava/lang/String;)Ljavax/crypto/Cipher;");
    jobject cipher = jniEnv->CallStaticObjectMethod(cipherClass, getCipherInstanceMethodId,
                                                    jniEnv->NewStringUTF("RSA/ECB/PKCS1Padding"));

    //init cipher
    jmethodID cipherInitMethodId = jniEnv->GetMethodID(cipherClass, "init",
                                                       "(ILjava/security/Key;)V");
    jniEnv->CallVoidMethod(cipher, cipherInitMethodId, 2, pubKey);

    // decrypt with cipher object
    int segmentSize = KEY_SIZE / 8;
    int inputLen = cipher_text_length;
    int offSet = 0;
    jbyteArray cache = nullptr;
    int i = 0;

    jclass byteArrayOutputStreamClass = jniEnv->FindClass("java/io/ByteArrayOutputStream");
    jmethodID byteArrayOutputStreamConstructor = jniEnv->GetMethodID(byteArrayOutputStreamClass,
                                                                     "<init>", "()V");

    jobject out = jniEnv->NewObject(byteArrayOutputStreamClass, byteArrayOutputStreamConstructor);


    jmethodID doFinalMethodId = jniEnv->GetMethodID(cipherClass, "doFinal", "([BII)[B");
    jmethodID writeMethodId = jniEnv->GetMethodID(byteArrayOutputStreamClass, "write", "([BII)V");

    jbyteArray srcBytes = jniEnv->NewByteArray(static_cast<jsize>(cipher_text_length));
    jniEnv->SetByteArrayRegion(srcBytes, 0, cipher_text_length,
                               reinterpret_cast<const jbyte *>(cipher_text));


    while (inputLen - offSet > 0) {
        if (inputLen - offSet > segmentSize) {
            cache = static_cast<jbyteArray>(jniEnv->CallObjectMethod(cipher,
                                                                     doFinalMethodId, srcBytes,
                                                                     offSet,
                                                                     segmentSize));//cipher.doFinal(srcBytes, offSet, segmentSize);
        } else {
            cache = static_cast<jbyteArray>(jniEnv->CallObjectMethod(cipher,
                                                                     doFinalMethodId, srcBytes,
                                                                     offSet,
                                                                     inputLen - offSet));
            //cipher.doFinal(srcBytes, offSet, inputLen - offSet);
        }
        jniEnv->CallVoidMethod(out, writeMethodId, cache, 0, jniEnv->GetArrayLength(cache));
        //out.write(cache, 0, cache.length);
        i++;
        offSet = i * segmentSize;
    }


    //close out stream & toByteArray
    //java.io.ByteArrayOutputStream#toByteArray
    jmethodID toByteArrayMethodId = jniEnv->GetMethodID(byteArrayOutputStreamClass, "toByteArray",
                                                        "()[B");
    auto data = static_cast<jbyteArray>(jniEnv->CallObjectMethod(out, toByteArrayMethodId));

    signed char *output = static_cast<signed char *>(malloc(
            static_cast<size_t>(jniEnv->GetArrayLength(data))));

    jniEnv->GetByteArrayRegion(data, 0, jniEnv->GetArrayLength(data),
                               output);
    output_length = static_cast<size_t>(jniEnv->GetArrayLength(data));

    free(rsa_public_key_binary);
    return output;
}
/*
 * private static byte[] cipherDoFinal(Cipher cipher, byte[] srcBytes, int segmentSize)
            throws IllegalBlockSizeException, BadPaddingException, IOException {
        if (segmentSize <= 0)
            throw new RuntimeException("分段大小必须大于0");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int inputLen = srcBytes.length;
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > segmentSize) {
                cache = cipher.doFinal(srcBytes, offSet, segmentSize);
            } else {
                cache = cipher.doFinal(srcBytes, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * segmentSize;
        }
        byte[] data = out.toByteArray();
        out.close();
        return data;
    }
 */