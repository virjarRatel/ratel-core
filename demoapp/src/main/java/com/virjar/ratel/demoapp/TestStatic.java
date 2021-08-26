package com.virjar.ratel.demoapp;

public class TestStatic {

    public static boolean flag;

    static {
        flag = TestStatic2.flag;
    }

    public static void checkFlag() {
        if (!flag) {
            throw new IllegalStateException("call sequence error");
        }
    }


}
