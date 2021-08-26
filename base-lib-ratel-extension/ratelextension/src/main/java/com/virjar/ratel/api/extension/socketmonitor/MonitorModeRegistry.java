package com.virjar.ratel.api.extension.socketmonitor;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MonitorModeRegistry {
    private static Map<Socket, MonitorMode> allMonitor = new ConcurrentHashMap<>();

    static MonitorMode createOrGet(Socket socket) {
        MonitorMode monitorMode = allMonitor.get(socket);
        if (monitorMode != null) {
            return monitorMode;
        }
        synchronized (MonitorModeRegistry.class) {
            monitorMode = allMonitor.get(socket);
            if (monitorMode != null) {
                return monitorMode;
            }
            allMonitor.put(socket, new MonitorMode(socket));

        }
        return allMonitor.get(socket);
    }

    static void destroy(MonitorMode monitorMode) {
        allMonitor.remove(monitorMode.getSocket());
    }
}
