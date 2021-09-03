package com.virjar.ratel.builder.helper.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamPrintThread extends Thread {
    private final InputStream inputStream;

    public InputStreamPrintThread(InputStream inputStream) {
        this.inputStream = inputStream;
        start();
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
