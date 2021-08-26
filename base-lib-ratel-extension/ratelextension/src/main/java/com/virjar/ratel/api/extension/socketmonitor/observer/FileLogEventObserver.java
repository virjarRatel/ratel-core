package com.virjar.ratel.api.extension.socketmonitor.observer;

import android.util.Log;

import com.virjar.ratel.api.extension.socketmonitor.SocketMonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import external.org.apache.commons.io.IOUtils;

public class FileLogEventObserver implements SocketDataObserver {
    private File dir;

    private Map<Socket, EventAppender> socketEventAppenderMap = new ConcurrentHashMap<>();

    public FileLogEventObserver(File dir) {
        this.dir = dir;
    }

    @Override
    public void onDecodeSocketData(DataModel dataModel) {
        EventAppender eventAppender = makeSureAppender(dataModel.socket);
        if (eventAppender == null) {
            //not happen
            return;
        }

        eventAppender.appendEvent(dataModel);
    }

    private EventAppender makeSureAppender(Socket socket) {
        EventAppender eventAppender = socketEventAppenderMap.get(socket);
        if (eventAppender != null) {
            return eventAppender;
        }
        synchronized (this) {
            eventAppender = socketEventAppenderMap.get(socket);
            if (eventAppender != null) {
                return eventAppender;
            }
            try {
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        throw new IllegalStateException("can not create directory: " + dir.getAbsolutePath());
                    }
                }
                eventAppender = new EventAppender(new File(dir, System.currentTimeMillis() + "_socket.txt"));
                socketEventAppenderMap.put(socket, eventAppender);
                return eventAppender;
            } catch (IOException e) {
                Log.e(SocketMonitor.TAG, "failed to write data", e);
                return null;
            }
        }
    }

    private class EventAppender {
        private FileOutputStream fileOutputStream;

        public EventAppender(File theLogFile) throws IOException {
            if (!theLogFile.exists()) {
                if (!theLogFile.createNewFile()) {
                    throw new IOException("can not create file: " + theLogFile.getAbsolutePath());
                }
            }
            fileOutputStream = new FileOutputStream(theLogFile, true);
        }

        public synchronized void appendEvent(DataModel socketPackEvent) {
            int localPort = socketPackEvent.socket.getLocalPort();
            int remotePort = socketPackEvent.socket.getPort();
            InetAddress inetAddress = socketPackEvent.socket.getInetAddress();

            String remoteAddress;
            if (inetAddress != null) {
                remoteAddress = inetAddress.getHostAddress();
            } else {
                remoteAddress = socketPackEvent.socket.toString();
            }

            StringBuilder headerBuilder = new StringBuilder();
            headerBuilder.append("\n\n\n");
            headerBuilder.append("Socket ");

            if (socketPackEvent.in) {
                headerBuilder.append("response");
            } else {
                headerBuilder.append("request");
            }
            headerBuilder.append(" local port:").append(localPort)
                    .append(" remote address:").append(remoteAddress).append(":").append(remotePort)
                    .append("\n").append("StackTrace:");

            try {
                //输出头部数据
                fileOutputStream.write(headerBuilder.toString().getBytes(StandardCharsets.UTF_8));

                //输出堆栈
                PrintStream printStream = new PrintStream(fileOutputStream);
                socketPackEvent.stackTrace.printStackTrace(printStream);
                printStream.flush();
                printStream.write(newLineBytes);

                //输出报文内容
                IOUtils.copy(socketPackEvent.data, fileOutputStream);

                fileOutputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final byte[] newLineBytes = "\n\n".getBytes(StandardCharsets.UTF_8);
}
