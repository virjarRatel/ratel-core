package com.virjar.ratel.builder.helper.utils;
import java.io.IOException;

public class Shell {
    public static void execute(String cmd) throws IOException, InterruptedException {
        System.out.println("execute cmd: " + cmd);
        Process process = Runtime.getRuntime().exec(cmd);
        new InputStreamPrintThread(process.getErrorStream());
        new InputStreamPrintThread(process.getInputStream());
        process.waitFor();
    }
}