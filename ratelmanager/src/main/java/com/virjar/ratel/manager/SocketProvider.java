package com.virjar.ratel.manager;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.virjar.ratel.allcommon.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketProvider {
    public static final int serverPort = 5975;

    private static void startServices() {

        new Thread("manager-socket-provider") {
            @Override
            public void run() {
                try {
                    startProviderService();
                } catch (Exception e) {
                    Log.e(Constants.TAG, "startProviderService failed", e);
                }
            }
        }.start();
    }

    /* For autonumbering anonymous threads. */
    private static int threadInitNumber;

    private static synchronized int nextThreadNum() {
        return threadInitNumber++;
    }

    private static void startProviderService() throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverPort);

        while (!Thread.currentThread().isInterrupted()) {
            final Socket socket = serverSocket.accept();

            new Thread("request-client-worker-" + nextThreadNum()) {
                @Override
                public void run() {
                    try {
                        handleClientSocket(socket);
                    } catch (Exception e) {
                        Log.w(Constants.TAG, "handle socket request failed", e);
                    }
                }
            }.start();
        }
    }


    private static void handleClientSocket(Socket socket) throws IOException {
        int dataLength = socket.getInputStream().read();
        if (dataLength <= 0) {
            Log.w(Constants.TAG, "error data length");
            return;
        }
        byte data[] = new byte[dataLength];
        JSONObject jsonObject = JSON.parseObject(new String(data, StandardCharsets.UTF_8));


    }
}
