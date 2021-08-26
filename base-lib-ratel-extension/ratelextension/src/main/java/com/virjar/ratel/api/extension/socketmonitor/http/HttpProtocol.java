package com.virjar.ratel.api.extension.socketmonitor.http;

import android.util.Log;

import com.virjar.ratel.api.extension.socketmonitor.MonitorMode;
import com.virjar.ratel.api.extension.socketmonitor.SocketMonitor;
import com.virjar.ratel.api.extension.socketmonitor.protocol.AbstractProtocol;

import java.io.IOException;
import java.io.InputStream;


public class HttpProtocol extends AbstractProtocol {

    private HttpBaseInfoDecoder requestDecoder;
    private HttpBaseInfoDecoder responseDecoder;

    private InputStream nowRequestInputStream = null;
    private InputStream nowResponseInputStream = null;

    public HttpProtocol(MonitorMode monitorMode) {
        super(monitorMode);
        requestDecoder = new HttpBaseInfoDecoder(monitorMode.getOutputSteamData(), true);
        responseDecoder = new HttpBaseInfoDecoder(monitorMode.getInputStreamData(), false);
    }

    @Override
    public AbstractProtocol process() {
        try {
            nowRequestInputStream = requestDecoder.parse();
            nowResponseInputStream = responseDecoder.parse();
        } catch (IOException e) {
            //not happen
            Log.e(SocketMonitor.TAG, "http decode error", e);
        }
        return super.process();
    }

    @Override
    public boolean needPrint() {
        return nowRequestInputStream != null || nowResponseInputStream != null || monitorMode.isClosed();
    }

    @Override
    public InputStream finalOutputStream() {
        if (nowRequestInputStream != null) {
            return nowRequestInputStream;
        }
        if (monitorMode.isClosed()) {
            return super.finalOutputStream();
        }
        return null;
    }

    @Override
    public InputStream finalInputStream() {
        if (nowResponseInputStream != null) {
            return nowResponseInputStream;
        }
        if (monitorMode.isClosed()) {
            return super.finalInputStream();
        }
        return null;
    }
}
