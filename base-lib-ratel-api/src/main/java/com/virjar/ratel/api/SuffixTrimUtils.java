package com.virjar.ratel.api;

import java.util.Random;

public class SuffixTrimUtils {
    private static Random random = new Random();

    public interface ValueUpdater {
        String modify(String value);
    }

    public static class SuffixModifier implements ValueUpdater {
        private int with;

        public SuffixModifier(int with) {
            this.with = with;
        }

        public String modify(String input) {
            return mockSuffix(input, with);
        }
    }

    public static SuffixModifier SuffixModifier5 = new SuffixModifier(5);
    public static SuffixModifier SuffixModifier4 = new SuffixModifier(4);
    public static ValueUpdater replaceToEmpty = value -> "";

    public static String mockSuffix(String input, int width) {
        if (input == null) {
            return null;
        }
        int fakeWith = Math.min(width, input.length() - 1);
        if (fakeWith < 1) {
            return input;
        }
        char[] needModify = input.substring(input.length() - fakeWith).toCharArray();
        for (int i = 0; i < needModify.length; i++) {
            if (Character.isDigit(needModify[i])) {
                needModify[i] = nextDigit();
            } else if (isHex(needModify[i])) {
                needModify[i] = nextHex();
            } else if (Character.isLetter(needModify[i])) {
                needModify[i] = nextChar();
            }
            //其他字符串，不是数字或者字母。下划线，连接线，其他特殊字符保留原来的模样
        }
        return input.substring(0, input.length() - fakeWith) + new String(needModify);
    }

    private static boolean isHex(char ch) {
//        if (ch >= '0' && ch <= '9') {
//            return true;
//        }
        if (ch >= 'a' && ch <= 'f') {
            return true;
        }
        return false;
    }

    private static char nextHex() {
        return (char) ('a' + random.nextInt('f' - 'a'));
    }

    public static char nextChar() {
        int aAStart = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母
        return (char) (aAStart + random.nextInt(26));
    }

    public static int randomMockDiff(int size) {
        return random.nextInt(size * 2) - size;
    }

    private static char nextDigit() {
        return (char) ('0' + random.nextInt(10));
    }


    public static Random getRandom() {
        return random;
    }

    public static void main(String[] args) {
        System.out.println(mockSuffix("ac:37:43:a1:0b:88", 7));
        System.out.println(mockSuffix("hello-world123a", 3));
        System.out.println(mockSuffix("hello-world", 11));
        System.out.println(mockSuffix("hello-world", 12));
        System.out.println(mockSuffix("abc", 1));
        System.out.println(mockSuffix("abc", 2));
        System.out.println(mockSuffix("abc", 3));
    }
}
