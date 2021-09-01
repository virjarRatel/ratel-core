package com.virjar.ratel.runtime.ipc;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.manager.bridge.IRatelManagerClientRegister;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

public class ClientHandlerServiceConnection implements ServiceConnection {

    private static IRatelManagerClientRegister ratelManagerClientRegister = null;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (ratelManagerClientRegister != null) {
            return;
        }
        ratelManagerClientRegister = IRatelManagerClientRegister.Stub.asInterface(service);
        if (ratelManagerClientRegister == null) {
            return;
        }
        try {
            ratelManagerClientRegister.registerRemoteControlHandler(new RatelRemoteControllerHandler());
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "register ipc client failed!!", e);
            IPCControlHandler.initRatelManagerIPCClient();
        }
        for (OnManagerReadyListener onManagerReadyListener : onManagerReadyListeners) {
            onManagerReadyListener.onManagerIPCReady(ratelManagerClientRegister);
            onManagerReadyListeners.remove(onManagerReadyListener);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        ratelManagerClientRegister = null;
        IPCControlHandler.initRatelManagerIPCClient();
    }

    public static IRatelManagerClientRegister getRatelManagerClientRegister() {
        return ratelManagerClientRegister;
    }

    public static void addOnManagerIPCListener(OnManagerReadyListener onManagerReadyListener) {
        if (onManagerReadyListener == null) {
            return;
        }
        IRatelManagerClientRegister ratelManagerClientRegisterCopy = ratelManagerClientRegister;
        if (ratelManagerClientRegisterCopy != null) {
            onManagerReadyListener.onManagerIPCReady(ratelManagerClientRegisterCopy);
            return;
        }
        onManagerReadyListeners.add(onManagerReadyListener);
    }

    private static CopyOnWriteArraySet<OnManagerReadyListener> onManagerReadyListeners = new CopyOnWriteArraySet<>();

    public interface OnManagerReadyListener {
        void onManagerIPCReady(IRatelManagerClientRegister iRatelManagerClientRegister);
    }
}
