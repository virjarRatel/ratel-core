package com.virjar.ratel.authorize.encrypt;


import com.virjar.ratel.buildsrc.Base64;
import com.virjar.ratel.buildsrc.StandardEncryptor;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RatelLicenceEncryptor {


    private static final byte[] desKey = "oAuBv+aFxH".getBytes(StandardCharsets.UTF_8);

    private static final String rsaPrivateKey = "wBHNol9A#+ZmOaGnPYFudvxbc8a367c8bXJgXbAuquFXaPKIllTl1tRTnu6cQfbDLtA92G9tZBAvrAxWzh9m4R2OSk9ysIvDUuGNAPWfBSmqUpmuoiOdvHUmGPEnntYB6#TcUpVPsbGgQRk1OOaMtWCAlNg1JZLEJz5WMBmZAR8hwC+rI2aCsWxxa3k1yEwxiMSqsb5UgZNDXz1USwtK6C73qWPo0oQNNigbs5eModQqvRgCR1rO4jUbBGGjISpMyh4Ayrwk3g0tjm4zSrnCt3+SWOS00huE4J7va4ZxJfDpWNlnj#Mh7didiHwC5qNg7fwSVTa+0ERUK3BNAwMwsC6YHapvnOuWVbrI#GqDeeN4iMdPeY5bn3I+Ob#yfbLaGjbytrUqGNcQHXFkT2jzfzfiIXZ3JmW2lqbpwyiB91SlN5gj2ZmxHXfFcwOpEyMjNWTVUuLM2FsKmEM3O6VmqRXmMgkFJHQb58WzpYNAon9pnM5gsJGlmbd#4blRTRUMJGOwEkvCSM30kEpNX94nKJVfiL7ZrbnILJzbnoPYfejsx86tmdCs92zK11+2ZDXG1lcJigIBv0U8knmdpb4XwCXusbvEzK#Cut#tphla2gcrBtmhaS59rOec0or18DSLgMpGZfFIH5UsUhSI4PmDDjTme3XG8zyMIEZUucCGjnGQBGm21bz76OL60#A0HGAUzcY628Ij#IpTdc6bClJxHZ0wTvu4HfIyDu0SJfrCNyyY9MMplvrXHnepvyeESgjhzPJ90vDhyPnv1559M82nWjRYUnhk7z4b4TLXrqZzmrpdJWJtXTV2NuVufwLQ415nx9jL8LTAjnGKYvIIKfh0om2eSmG6x+RR2Ydji6gveJlE4aB=";

    private static final String rsaPublicKey = "wZYxOmvlud#b9NaPnBHFAoG+fc530e06couQVdTZOaaq+bhrof1qtSLwz3IR0alykOlNN#lFO1GRe9E#sZoPzUvCdamtAdHoVhbt#YONYdJWyvPsW#q8Ly1GO6WzRTJWYnvAxiHrNaU9vzycEejWhyfaWEHdsLh908r0QLXnqT6seG3fSRmHx6eMImKX2KDtgTcHxuL5P8uzvJD4r1i#FFq8wpx1BJ8xvCHXLaWinIANQoZnm0Jca4zD";

    private static final String containerRsaPrivateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKvS0aDKz8iHTs0s+tY9YY+U0RZuwWp8M7nScM94EHV9UzepUtcpgzdhvP+mUvc06kN1Tf5IxjNZuGulmyvy40mxbVkUouBGSh4FSvXeFOnsHQRwH9bicyK0oyUjznBBFlecu5FpxmSK92kvNv8JjYzzi98fRtnmZm+IbgRecu47AgMBAAECgYBd1V2hn+BlNG92YRADG7ZBwRWGWKnLy1Q8MI5m3ryuTOi7Rv70fbko73lVa7F3SzB78n3LVlbXCDJMpz3cfGJeoQhj/Ax9I4xVXBNMT7CD/MBUvmuKdYWjYBiet9NOIR6nX1AicfP6roZCXMPW6xiyEJ2CMDOkt+gnJmd0Sq2csQJBANnGoARfbBTFoTB1YuTPBNrVwZxcejgQ9JytW09EMmby21KYWvgaunmkoDc7UkGwqiKd1oNBZkKiIDgGES2xizcCQQDJ+2e7jmSOm7pUG20K1aBOLOKPGAfs6yLZ6pwe1EYW0VMTQA38qVP1Tl9fBoGvciD3A7rGPRDKbcgUHh0ImZ8dAkAhcgjHQyRlOEjeGVkbzNNxDF0Ut3spuyjmGxWn4dBf0TJvx+hIrEoxPmBAu0KRxiEK+fSk6dlbqGyMTho7S0YXAkAQ2JEMSZIFeuONhEQR9UNLgd7bhGuUzP+5ISIoSBgYaxj8sAj6m7zO5tx5dnd2hJRPRdZcFlfCKmnXk7NkNjvZAkAn7AaZUUOabf112EWCZ00yJf15ZRfaoP2YnJaSDKXgxvyzWAVT30gbMNNIssEeiG2EbB2Ahet9ETUS3/OtEs+p";


    public static String encrypt(byte[] input) {

        byte[] desOutput = DES.ratelEncrypt(input, desKey);

        //然后rsa，rsa是非对称，算法可以直接破译，但是客户端拿不到私钥无法加密
        try {
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
                    //Base64.decodeBase64(rsaPrivateKey)
                    RatelBase64.decode(rsaPrivateKey)
            );
            KeyFactory keyFactory = KeyFactory.getInstance(StandardEncryptor.KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            return RatelBase64.encode(
                    StandardEncryptor.cipherDoFinal(cipher, desOutput, StandardEncryptor.KEY_SIZE / 8 - 11)
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        //return RatelBase64.encode(input);
    }

    public static byte[] decrypt(String input) {
        //  return RatelBase64.decode(input);
        byte[] rsaInput = RatelBase64.decode(input);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(StandardEncryptor.KEY_ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
                    // Base64.decodeBase64(rsaPublicKey)
                    RatelBase64.decode(rsaPublicKey)
            );
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            byte[] rsaOutput = StandardEncryptor.cipherDoFinal(cipher, rsaInput, StandardEncryptor.KEY_SIZE / 8);
            return DES.ratelDecrypt(rsaOutput, desKey);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public static String standardRSAEncrypt(byte[] data) {
        //然后rsa，rsa是非对称，算法可以直接破译，但是客户端拿不到私钥无法加密
        try {
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
                    Base64.decode(containerRsaPrivateKey)

            );
            KeyFactory keyFactory = KeyFactory.getInstance(StandardEncryptor.KEY_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            return Base64.encode(
                    StandardEncryptor.cipherDoFinal(cipher, data, StandardEncryptor.KEY_SIZE / 8 - 11)
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public static byte[] standardRSADecrypt(String data) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(StandardEncryptor.KEY_ALGORITHM);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
                    Base64.decode(StandardEncryptor.containerRsaPublicKey)

            );
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            return StandardEncryptor.cipherDoFinal(cipher, Base64.decode(data), StandardEncryptor.KEY_SIZE / 8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    private static void genKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(StandardEncryptor.KEY_SIZE, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        String publicKeyString = RatelBase64.encode(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyString = RatelBase64.encode(privateKey.getEncoded());

        System.out.println(publicKeyString);
        System.out.println(privateKeyString);
        System.out.println("standard public:" + Base64.encode(publicKey.getEncoded()));
        System.out.println("standard private:" + Base64.encode(privateKey.getEncoded()));
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        genKeyPair();
    }
}
