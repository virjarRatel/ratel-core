package com.virjar.ratel.envmock.idfake;

public class LuhnSignUpdater implements SignUpdater {

    @Override
    public String updateSign(String input) {
        if (input == null) {
            return null;
        }
        char[] chars = input.toCharArray();
        int signIndex = chars.length - 2;
        int multiStartIndex = signIndex;
        int[] slots = new int[multiStartIndex + 1];
        boolean multi = true;
        while (multiStartIndex >= 0) {
            if (multi) {
                int doubleVal = (chars[multiStartIndex] - '0') * 2;
                slots[multiStartIndex] = doubleVal % 10 + (doubleVal / 10) % 10;
            } else {
                slots[multiStartIndex] = chars[multiStartIndex] - '0';
            }
            multi = !multi;
            multiStartIndex--;
        }
        int sum = 0;
        for (int slot : slots) {
            sum += slot;
        }
        char checkSum = (char) ('0' + (10 - sum % 10) % 10);
        chars[signIndex + 1] = checkSum;
        return new String(chars);
    }
}
