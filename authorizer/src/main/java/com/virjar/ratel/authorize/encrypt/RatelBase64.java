package com.virjar.ratel.authorize.encrypt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RatelBase64 {
    private static char[] base64std;
    private static final int[] base64res = new int[128];
    private static final String codeMapping = "9+#FGHlmBuvAwxNOPYZabnod5678pqrcstefghijCDEWXyz01IJKLM234kQRSTUV";
    //private static final String codeMapping = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static int[] maskArray = new int[24];

    static {
        boolean[] bitmap = new boolean[128];
        for (int i = 0; i < bitmap.length; i++) {
            bitmap[i] = false;
        }
        StringBuilder stringBuilder = new StringBuilder(64);
        for (char ch : codeMapping.toCharArray()) {
            if (ch > 128 || bitmap[ch]) {
                continue;
            }
            bitmap[ch] = true;
            stringBuilder.append(ch);
        }

        char[] chars = stringBuilder.toString().toCharArray();
        if (chars.length != 64) {
            throw new IllegalStateException();
        }

        base64std = chars;
        for (int i = 0; i < 64; i++) {
            base64res[base64std[i]] = i;
        }

        for (int i = 0; i < 24; i++) {
            maskArray[i] = 1 << i;
        }
    }

    // base64编码
    public static String encode(byte[] data) {
        //需要有一个copy，不鞥修改原数据
        data = Arrays.copyOf(data, data.length);
        int randomKey = ThreadLocalRandom.current().nextInt();

        Random random = new Random(randomKey + 32942);
        for (int k = 0; k < data.length; k++) {
            data[k] = (byte) (data[k] ^ (byte) random.nextInt());
        }

        //生成24为随机码
        List<Integer> collection = new ArrayList<>();
        for (int i = 23; i >= 0; i--) {
            collection.add(i);
        }
        //随机顺序
        List<Integer> collection2 = new ArrayList<>();
        random = new Random();
        while (collection.size() > 1) {
            collection2.add(collection.remove(random.nextInt(collection.size())));
        }
        collection2.add(collection.remove(0));

        //  List<Integer> collection2 = collection;
        int[] matchArray = new int[24];
        for (int i = 0; i < collection2.size(); i++) {
            matchArray[i] = collection2.get(i);
        }


        int indata = 0;
        char[] dest = new char[(data.length + 2) / 3 * 4 + 32];
        int n = 3 * (data.length / 3);
        int i, j;
        int[] a = new int[3];
        for (i = 0, j = 0; i < n; i += 3) {
            a[0] = data[i] & 0xff;
            a[1] = data[i + 1] & 0xff;
            a[2] = data[i + 2] & 0xff;
            indata = (a[0] << 16) + (a[1] << 8) + a[2];
            dest[j++ + 32] = base64std[maskIndex(matchArray, indata, 0)];
            dest[j++ + 32] = base64std[maskIndex(matchArray, indata, 1)];
            dest[j++ + 32] = base64std[maskIndex(matchArray, indata, 2)];
            dest[j++ + 32] = base64std[maskIndex(matchArray, indata, 3)];
        }
        if (data.length % 3 == 1) {
            indata = data[i] & 0xff;
            dest[j++ + 32] = base64std[indata >> 2];
            dest[j++ + 32] = base64std[(indata << 4) & 0x3f];
            dest[j++ + 32] = '=';
            dest[j + 32] = '=';
        } else if (data.length % 3 == 2) {
            indata = ((data[i] & 0xff) << 8) + (data[i + 1] & 0xff);
            dest[j++ + 32] = base64std[indata >> 10];
            dest[j++ + 32] = base64std[(indata >> 4) & 0x3f];
            dest[j++ + 32] = base64std[(indata << 2) & 0x3f];
            dest[j + 32] = '=';
        }
        for (int k = 0; k < 24; k++) {
            dest[k] = codeMapping.charAt(matchArray[k]);
        }

        char[] keyString = Integer.toHexString(randomKey).toCharArray();
        char[] keyStringExt = new char[8];
        for (int k = 0; k < 8; k++) {
            keyStringExt[k] = '0';
        }
        System.arraycopy(keyString, 0, keyStringExt, keyStringExt.length - keyString.length, keyString.length);
        System.arraycopy(keyStringExt, 0, dest, 24, 8);

        return String.valueOf(dest);
    }


    private static int maskIndex(int[] matchArray, int data, int batch4) {
        int start = batch4 * 6;
        int end = start + 6;
        int ret = 0;
        for (int i = start; i < end; i++) {
            ret |= ((data & maskArray[matchArray[i]]) >> (matchArray[i])) << (end - i - 1);
        }
        return ret;
    }

    // base64解码
    public static byte[] decode(String indata) {
        byte[] out;
        if (indata.contains("=")) {
            int x = indata.length() - indata.indexOf('=');
            out = new byte[(indata.length() - 32) * 3 / 4 - x];
        } else {
            out = new byte[(indata.length() - 32) * 3 / 4];
        }

        //还原maskarray
        int[] matchArray = new int[24];
        for (int i = 0; i < 24; i++) {
            matchArray[i] = codeMapping.indexOf(indata.charAt(i));
        }

        int i, j, n;
        int temp;
        n = indata.length() - 4;
        for (i = 32, j = 0; i < n; i += 4) {
            temp = (base64res[indata.charAt(i)] << 18) + (base64res[indata.charAt(i + 1)] << 12)
                    + (base64res[indata.charAt(i + 2)] << 6) + (base64res[indata.charAt(i + 3)]);
            int substitution = 0;
            for (int k = 23; k >= 0; k--) {
                substitution |= (temp >> k & 0x01) << matchArray[23 - k];
            }
            out[j++] = (byte) (substitution >> 16);
            out[j++] = (byte) ((substitution >> 8) & 0xff);
            out[j++] = (byte) (substitution & 0xff);
        }
        if (indata.charAt(i + 2) == '=') {
            out[j] = (byte) ((base64res[indata.charAt(i)] << 2) + (base64res[indata.charAt(i + 1)] >>> 4));
        } else if (indata.charAt(i + 3) == '=') {
            temp = (base64res[indata.charAt(i)] << 10) + (base64res[indata.charAt(i + 1)] << 4)
                    + (base64res[indata.charAt(i + 2)] >>> 2);
            out[j++] = (byte) (temp >> 8);
            out[j] = (byte) (temp & 0xff);
        } else {
            temp = (base64res[indata.charAt(i)] << 18) + (base64res[indata.charAt(i + 1)] << 12)
                    + (base64res[indata.charAt(i + 2)] << 6) + (base64res[indata.charAt(i + 3)]);
            int substitution = 0;
            for (int k = 23; k >= 0; k--) {
                substitution |= (temp >> k & 0x01) << matchArray[23 - k];
            }
            out[j++] = (byte) (substitution >> 16);
            out[j++] = (byte) ((substitution >> 8) & 0xff);
            out[j] = (byte) (substitution & 0xff);
        }

        char[] keyString = new char[8];
        for (int k = 24; k < 32; k++) {
            keyString[k - 24] = indata.charAt(k);
        }

        int key = Integer.parseUnsignedInt(new String(keyString), 16);

        Random random = new Random(key + 32942);
        for (int k = 0; k < out.length; k++) {
            out[k] = (byte) (out[k] ^ (byte) random.nextInt());
        }
        return out;
    }

    public static void main(String[] args) {
        //YWJjZGVmZw==
        System.out.println(encode("年美国 IBM 公司研制的对称密码体制加密算法。".getBytes(StandardCharsets.UTF_8)));
        //FMLKRSDTVBCAGXIPWHQNOEUJGI9mBC92ZA==
        System.out.println(new String(decode("AFl9u#vwYboOZxnNHBda+GPm4c52fa09yLsTTK1I9uQQC6N0yq67sIT52kL+SS3CxTnnotnqewq22kvSq6eF4F5y2lvXkKEtqZV3Ucq8ORN6FdNQLg4="), StandardCharsets.UTF_8));
    }
}
