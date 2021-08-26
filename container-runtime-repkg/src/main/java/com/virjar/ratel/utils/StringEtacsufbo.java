package com.virjar.ratel.utils;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import external.org.apache.commons.lang3.StringUtils;

public class StringEtacsufbo {
    private char[] base64std;
    private final int[] base64res = new int[128];

    private StringEtacsufbo(String codeMapping) {

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
    }

    private static StringEtacsufbo instance;

    private static StringEtacsufbo getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new StringEtacsufbo(
                new String(new StringEtacsufbo("jEdJ3nmHFsTPxSTfViqrY6W#XAZo5vpIRGJq8hL1kkWlZL9wt2lWwkERpGUzfde7NMyytRcObK+kXuCAOcIbuaO40hPDkgnJBBDQ")
                        .decode("ku133MPbc2GLE3ygIrGMA4gGEFGZBfijurbTMn1kNAOaKFQQ5TOz1##5xLhr1XD9inaVWi8k9BeC72PdiT95TG0M4cLnYlQ7LWqXS4gMn0CnhjSdRRoCyRA#0tDHRQoxnxqjbj==", 15), StandardCharsets.UTF_8)
        );
        return instance;
    }

    //    private static String genFirstMappingCode() {
//        String base64Chars = "9+#FGHlmBuvAwxNOPYZabnod5678pqrcstefghijCDEWXyz01IJKLM234kQRSTUV";
//        List<Character> characters = new ArrayList<>();
//        for (char ch : base64Chars.toCharArray()) {
//            characters.add(ch);
//        }
//
//        List<Character> outCharacters = new ArrayList<>();
//        Random random = new Random();
//        while (characters.size() > 0) {
//            outCharacters.add(characters.remove(random.nextInt(characters.size())));
//        }
//
//        for (int i = 0; i < 36; i++) {
//            char randomCh = SuffixTrimUtils.nextChar();
//            int firstIndex = 0;
//            for (int j = 0; j < outCharacters.size(); j++) {
//                if (outCharacters.get(j) == randomCh) {
//                    firstIndex = j;
//                    break;
//                }
//            }
//
//            if (firstIndex == outCharacters.size() - 1) {
//                outCharacters.add(randomCh);
//            } else {
//                outCharacters.add(firstIndex + random.nextInt(outCharacters.size() - firstIndex), randomCh);
//            }
//        }
//        StringBuilder stringBuilder = new StringBuilder(outCharacters.size());
//        for (Character character : outCharacters) {
//            stringBuilder.append(character);
//        }
//        return stringBuilder.toString();
//    }
//
//
//    public static void main(String[] args) {
//        System.out.println(encodeFromString("com.virjar.ratel.runtime.fixer.pm.PackageParserEx", 3852));
//        System.out.println(decodeToString("5P3BfRw129X4+R0LsfeODO#q4nlmMNwxAuyJdlX+dza8FWpDoCXeEz6BCMgJxsg5T2==", 3852));
//        System.out.println(encodeFromString("com.virjar.ratel.hook.sandcompat.methodgen.HookerDexMaker", 98274));
//        System.out.println(decodeToString("4K#p#VkA1duAZikdG4J5zY3qwGCPNt7tlJdnVm#ZKDSTlV8xM9+O9Cs3xpT5sg6W9Jtkp0v+wDO9", 98274));
//    }

    private static String encodeFromString(String input, int key) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        return getInstance().encode(input.getBytes(StandardCharsets.UTF_8), key);
        // return null;
    }

    public static String decodeToString(String input, int key) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        return new String(getInstance().decode(input, key), StandardCharsets.UTF_8);
        // return null;
    }

    // base64编码
    public String encode(byte[] data, int key) {
        Random random = new Random(key + 32942);
        for (int k = 0; k < data.length; k++) {
            data[k] = (byte) (data[k] ^ (byte) random.nextInt());
        }


        int indata = 0;
        char[] dest = new char[(data.length + 2) / 3 * 4];
        int n = 3 * (data.length / 3);
        int i, j;
        int[] a = new int[3];
        for (i = 0, j = 0; i < n; i += 3) {
            a[0] = data[i] & 0xff;
            a[1] = data[i + 1] & 0xff;
            a[2] = data[i + 2] & 0xff;
            indata = (a[0] << 16) + (a[1] << 8) + a[2];
            dest[j++] = base64std[(indata >> 18)];
            dest[j++] = base64std[((indata >> 12) & 0x3F)];
            dest[j++] = base64std[((indata >> 6) & 0x3F)];
            dest[j++] = base64std[(indata & 0x3F)];
        }
        if (data.length % 3 == 1) {
            indata = data[i] & 0xff;
            dest[j++] = base64std[indata >> 2];
            dest[j++] = base64std[(indata << 4) & 0x3f];
            dest[j++] = '=';
            dest[j] = '=';
        } else if (data.length % 3 == 2) {
            indata = ((data[i] & 0xff) << 8) + (data[i + 1] & 0xff);
            dest[j++] = base64std[indata >> 10];
            dest[j++] = base64std[(indata >> 4) & 0x3f];
            dest[j++] = base64std[(indata << 2) & 0x3f];
            dest[j] = '=';
        }


        return String.valueOf(dest);
    }

    // base64解码
    public byte[] decode(String indata, int key) {
        byte[] out;
        if (indata.contains("=")) {
            int x = indata.length() - indata.indexOf('=');
            out = new byte[indata.length() * 3 / 4 - x];
        } else {
            out = new byte[indata.length() * 3 / 4];
        }

        int i, j, n;
        int temp;
        n = indata.length() - 4;
        for (i = 0, j = 0; i < n; i += 4) {
            temp = (base64res[indata.charAt(i)] << 18) + (base64res[indata.charAt(i + 1)] << 12)
                    + (base64res[indata.charAt(i + 2)] << 6) + (base64res[indata.charAt(i + 3)]);
            out[j++] = (byte) (temp >> 16);
            out[j++] = (byte) ((temp >> 8) & 0xff);
            out[j++] = (byte) (temp & 0xff);
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
            out[j++] = (byte) (temp >> 16);
            out[j++] = (byte) ((temp >> 8) & 0xff);
            out[j] = (byte) (temp & 0xff);
        }

        Random random = new Random(key + 32942);
        for (int k = 0; k < out.length; k++) {
            out[k] = (byte) (out[k] ^ (byte) random.nextInt());
        }
        return out;
    }
}
