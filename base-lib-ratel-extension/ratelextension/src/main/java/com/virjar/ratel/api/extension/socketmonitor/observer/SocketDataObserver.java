package com.virjar.ratel.api.extension.socketmonitor.observer;

import java.io.InputStream;
import java.net.Socket;

public interface SocketDataObserver {
    class DataModel {
        public Socket socket;
        public boolean in = false;
        public InputStream data;
        public Throwable stackTrace;
    }

    void onDecodeSocketData(DataModel dataModel);
}
