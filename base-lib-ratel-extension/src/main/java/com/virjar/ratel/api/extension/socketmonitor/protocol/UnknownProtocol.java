package com.virjar.ratel.api.extension.socketmonitor.protocol;

import android.util.Log;

import com.virjar.ratel.api.extension.socketmonitor.MonitorMode;
import com.virjar.ratel.api.extension.socketmonitor.SocketMonitor;
import com.virjar.ratel.api.extension.socketmonitor.http.HttpProtocol;
import com.virjar.ratel.api.extension.socketmonitor.http.HttpProtocolUtil;
import com.virjar.ratel.api.extension.socketmonitor.http.HttpStreamUtil;

import java.io.IOException;
import java.io.InputStream;

import external.org.apache.commons.io.output.ByteArrayOutputStream;

public class UnknownProtocol extends AbstractProtocol {
    public UnknownProtocol(MonitorMode monitorMode) {
        super(monitorMode);
    }

    @Override
    public AbstractProtocol process() {
        if (isHttp()) {
            return new HttpProtocol(monitorMode);
        }
        return super.process();
    }

    private boolean isHttp() {
        if (monitorMode.getInputStreamData().size() < 10 && monitorMode.getOutputSteamData().size() < 10) {
            return false;
        }

        //检查响应报文是否为http
        ByteArrayOutputStream inputStreamData = monitorMode.getInputStreamData();
        //test if the input data may be a http response
        byte[] buf = new byte[10];
        if (inputStreamData.size() >= 10) {
            //这个inputStream是一个view，不会消耗额外空间资源，所以我们使用了就丢弃
            InputStream inputStream = inputStreamData.toInputStream();
            try {
                long read = HttpStreamUtil.readFully(inputStream, buf, buf.length);
                if (read < 10) {
                    return false;
                }
                if (HttpProtocolUtil.maybeHttpResponse(buf, read)) {
                    return true;
                }
            } catch (IOException e) {
                //the exception not happened
                Log.e(SocketMonitor.TAG, "http response magic detect failed", e);
            }
        }


        ByteArrayOutputStream outputSteamData = monitorMode.getOutputSteamData();
        if (outputSteamData.size() >= 10) {
            InputStream inputStream = outputSteamData.toInputStream();
            try {
                long read = HttpStreamUtil.readFully(inputStream, buf, buf.length);
                if (read < 10) {
                    return false;
                }
                if (HttpProtocolUtil.maybeHttpRequest(buf)) {
                    return true;
                }
            } catch (IOException e) {
                //the exception not happened
                Log.e(SocketMonitor.TAG, "http request magic detect failed", e);
            }
        }
        return false;

    }

}
