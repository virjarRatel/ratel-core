package com.virjar.ratel.allcommon;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * 开源版没有将会逐步移除掉授权逻辑
 */
@Deprecated
public class StandardEncryptor {

    public static final String containerRsaPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCr0tGgys/Ih07NLPrWPWGPlNEWbsFqfDO50nDPeBB1fVM3qVLXKYM3Ybz/plL3NOpDdU3+SMYzWbhrpZsr8uNJsW1ZFKLgRkoeBUr13hTp7B0EcB/W4nMitKMlI85wQRZXnLuRacZkivdpLzb/CY2M84vfH0bZ5mZviG4EXnLuOwIDAQAB";

    /**
     * RSA密钥长度必须是64的倍数，在512~65536之间。默认是1024
     */
    public static final int KEY_SIZE = 1024;

    public static final String KEY_ALGORITHM = "RSA";

    public static byte[] cipherDoFinal(Cipher cipher, byte[] srcBytes, int segmentSize)
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

    /**
     * 标准解密函数，请注意这个Android和jvm环境下标准不同。这个只适合在jvm下使用
     *
     * @param data 密文
     * @return 明文
     */
    public static byte[] standardRSADecrypt(String data) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(StandardEncryptor.KEY_ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
                    Base64.decode(containerRsaPublicKey)

            );
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            return StandardEncryptor.cipherDoFinal(cipher, Base64.decode(data), StandardEncryptor.KEY_SIZE / 8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
