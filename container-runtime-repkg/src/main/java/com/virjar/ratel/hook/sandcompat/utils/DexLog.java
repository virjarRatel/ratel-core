package com.virjar.ratel.hook.sandcompat.utils;

import android.util.Log;


import com.virjar.ratel.allcommon.Constants;

import java.lang.reflect.Member;


public class DexLog {

    public static void printMethodHookIn(Member member) {
//        if (RatelRuntime.isRatelDebugBuild && member != null) {
//            Log.d(Constants.TAG, "method <" + member.toString() + "> hook in");
//        }
        //这个日志不能打打太多，实际业务hook这里也会刷屏，当猜测这里有问题的时候，才打开这部分代码
    }

    public static void printCallOriginError(Member member) {
        Log.e(Constants.TAG, "method <" + member.toString() + "> call origin error!");
    }

}
