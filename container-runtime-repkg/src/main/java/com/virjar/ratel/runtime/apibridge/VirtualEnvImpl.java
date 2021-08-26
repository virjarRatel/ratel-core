package com.virjar.ratel.runtime.apibridge;

import android.os.RemoteException;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.VirtualEnv;
import com.virjar.ratel.envmock.EnvMockController;
import com.virjar.ratel.envmock.LocationFake;
import com.virjar.ratel.manager.bridge.FingerData;
import com.virjar.ratel.manager.bridge.IRatelManagerClientRegister;
import com.virjar.ratel.runtime.MultiUserManager;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.XposedModuleLoader;
import com.virjar.ratel.runtime.ipc.ClientHandlerServiceConnection;

import java.util.Set;

public class VirtualEnvImpl implements VirtualEnv {
    @Override
    public VirtualEnvModel getVirtualEnvModel() {
        return EnvMockController.getVirtualEnvModel();
    }

    @Override
    public void switchEnv(String userId) {
        MultiUserManager.switchEnv(userId, true);
    }

    @Override
    public Set<String> availableUserSet() {
        return MultiUserManager.multiUserSet();
    }

    @Override
    public boolean removeUser(String userId) {
        return MultiUserManager.removeUser(userId);
    }

    public void keepEnvForManualModel(boolean keep) {
        //do nothing
    }

    @Override
    public String nowUser() {
        return MultiUserManager.nowUser();
    }


    @Override
    public String originIMEI() {
        return null;
    }

    @Override
    public String originAndroidId() {
        return null;
    }

    @Override
    public String originLine1Number() {
        return null;
    }

    @Override
    public String originMeid() {
        return null;
    }

    @Override
    public String originIccSerialNumber() {
        return null;
    }

    @Override
    public void cleanPkgData() {
        try {
            EnvMockController.handleSingleUserEnv(RatelRuntime.getOriginContext());
        } catch (Exception e) {
            Log.e(RatelToolKit.TAG, "cleanPkgData failed: ", e);
        }
    }

    @Override
    public String originSerialNumber() {
        return null;
    }

    @Override
    public RawFingerData rawFingerData() {
        RawFingerData ret = null;
        if (XposedModuleLoader.isRatelManagerInstalled() && XposedModuleLoader.getRatelManagerVersionCode() >= 4) {
            //ratelManager 1.0.4 之后支持
            IRatelManagerClientRegister ratelManagerClientRegister = ClientHandlerServiceConnection.getRatelManagerClientRegister();
            if (ratelManagerClientRegister != null) {
                try {
                    FingerData fingerData = ratelManagerClientRegister.getFingerData();
                    ret = new RawFingerData(
                            fingerData.getImei(),
                            fingerData.getSerial(),
                            fingerData.getLatitude(),
                            fingerData.getLongitude());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ret == null) {
            ret = new RawFingerData();
            ret.latitude = RatelToolKit.fingerPrintModel.latitude.getOrigin();
            ret.longitude = RatelToolKit.fingerPrintModel.longitude.getOrigin();
            ret.imei = RatelToolKit.fingerPrintModel.imei.getOrigin();
            ret.serial = RatelToolKit.fingerPrintModel.serial.getOrigin();
        }

        return ret;
    }

    @Override
    public void disableLocalMock() {
        LocationFake.setEnable(false);
    }
}
