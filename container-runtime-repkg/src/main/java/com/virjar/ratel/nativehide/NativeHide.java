package com.virjar.ratel.nativehide;

public class NativeHide {

    static {
        System.loadLibrary("ratelhide");
    }
    public static native void doHide();
}
