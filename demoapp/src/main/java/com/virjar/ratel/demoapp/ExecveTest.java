package com.virjar.ratel.demoapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExecveTest {
    static void cmdTest() {
        try {
            Process process = Runtime.getRuntime().exec("echo weijiatest > /sdcard/../../../sdcard/weijiatest.txt");
            InputStream errorStream = process.getErrorStream();
            InputStream inputStream = process.getInputStream();
            printStream("cmd-error", errorStream);
            printStream("cmd-stand", inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printStream(final String tName, final InputStream inputStream) {
        new Thread(tName) {
            @Override
            public void run() {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        Log.i(tName, line);
                    }
                    bufferedReader.close();
                    inputStreamReader.close();
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(tName, "read error", e);
                }
            }
        }.start();
    }
}
