package com.virjar.ratel.api.extension.socketmonitor.protocol;

import com.virjar.ratel.api.extension.socketmonitor.MonitorMode;

import java.io.InputStream;

public class AbstractProtocol {
    protected MonitorMode monitorMode;

    public AbstractProtocol(MonitorMode monitorMode) {
        this.monitorMode = monitorMode;
    }

    public AbstractProtocol process() {
        return this;
    }

    public InputStream finalOutputStream() {
        return monitorMode.getOutputSteamData().toInputStream();
    }

    public InputStream finalInputStream() {
        return monitorMode.getInputStreamData().toInputStream();
    }

    public boolean needPrint() {
        return monitorMode.isClosed();
    }

    public boolean hasData() {
        return monitorMode.getOutputSteamData().size() > 0 || monitorMode.getInputStreamData().size() > 0;
    }

}
