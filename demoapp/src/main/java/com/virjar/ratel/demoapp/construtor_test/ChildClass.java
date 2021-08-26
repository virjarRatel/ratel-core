package com.virjar.ratel.demoapp.construtor_test;

import android.util.Log;

import com.virjar.ratel.demoapp.MainActivity;

public class ChildClass extends ParentClass {
    public ChildClass() {
        Log.i(MainActivity.LogTag, "child Class constructor");
    }
}
