package com.virjar.ratel.api.extension.socketmonitor;

import com.virjar.ratel.api.extension.socketmonitor.observer.SocketDataObserver;
import com.virjar.ratel.api.extension.socketmonitor.protocol.AbstractProtocol;
import com.virjar.ratel.api.extension.socketmonitor.protocol.UnknownProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import external.org.apache.commons.io.input.ClosedInputStream;
import external.org.apache.commons.io.output.ByteArrayOutputStream;

public class MonitorMode {
    //被监控的socket
    private Socket socket;

    private ByteArrayOutputStream inputStreamData = new ByteArrayOutputStream();

    private ByteArrayOutputStream outputSteamData = new ByteArrayOutputStream();

    private Throwable readStacktrace;

    private Throwable writeStacktrace;

    private boolean closed = false;

    private AbstractProtocol abstractProtocol = new UnknownProtocol(this);

    public MonitorMode(Socket socket) {
        this.socket = socket;
    }

    public ByteArrayOutputStream getInputStreamData() {
        return inputStreamData;
    }

    public ByteArrayOutputStream getOutputSteamData() {
        return outputSteamData;
    }

    public void makeSureReadTrace() {
        if (readStacktrace == null) {
            readStacktrace = new Throwable();
        }
    }

    public void makeSureWriteTrace() {
        if (writeStacktrace == null) {
            writeStacktrace = new Throwable();
        }
    }

    public void close() {

        MonitorModeRegistry.destroy(this);
        closed = true;
        check();
    }

    public Socket getSocket() {
        return socket;
    }

    public void outputWrite(int i) {
        makeSureWriteTrace();
        outputSteamData.write(i);
        check();
    }

    public void outputWrite(byte[] bytes) throws IOException {
        makeSureWriteTrace();
        outputSteamData.write(bytes);
        check();
    }

    public void outputWrite(byte[] bytes, int i, int i1) {
        makeSureWriteTrace();
        outputSteamData.write(bytes, i, i1);
        check();
    }

    public void inputWrite(int i) {
        makeSureReadTrace();
        inputStreamData.write(i);
        check();
    }

    public void inputWrite(byte[] bytes) throws IOException {
        makeSureReadTrace();
        inputStreamData.write(bytes);
        check();
    }

    public void inputWrite(byte[] bytes, int i, int i1) {
        makeSureReadTrace();
        inputStreamData.write(bytes, i, i1);
        check();
    }

    private void check() {
        if (!abstractProtocol.hasData()) {
            return;
        }
        abstractProtocol = abstractProtocol.process();
        if (abstractProtocol.needPrint()) {
            InputStream inputStream = abstractProtocol.finalInputStream();
            InputStream outputStream = abstractProtocol.finalOutputStream();

            if (outputStream != null && !(outputStream instanceof ClosedInputStream)) {
                SocketDataObserver.DataModel dataModel = new SocketDataObserver.DataModel();
                dataModel.data = outputStream;
                dataModel.in = false;
                dataModel.socket = socket;
                makeSureWriteTrace();
                dataModel.stackTrace = writeStacktrace;
                SocketMonitor.callDataObserver(dataModel);
            }

            if (inputStream != null && !(inputStream instanceof ClosedInputStream)) {
                SocketDataObserver.DataModel dataModel = new SocketDataObserver.DataModel();
                dataModel.data = inputStream;
                dataModel.in = true;
                dataModel.socket = socket;
                makeSureReadTrace();
                dataModel.stackTrace = readStacktrace;
                SocketMonitor.callDataObserver(dataModel);
            }

            //resetStatus();
        }
    }

    //连接复用场景下，报文业务变更，但是连接不中断。我们需要记录为下一个event
    public void resetStatus() {
        readStacktrace = null;
        writeStacktrace = null;
        //考虑tcp 粘包风险，这里让handler端负责buffer重置
        //inputStreamData = new ByteArrayOutputStream();
        //outputSteamData = new ByteArrayOutputStream();
        abstractProtocol = new UnknownProtocol(this);
    }

    public boolean isClosed() {
        return closed;
    }
}
