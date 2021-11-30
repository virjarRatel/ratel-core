package com.virjar.ratel.runtime.ipc;

import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.VirtualEnv;
import com.virjar.ratel.envmock.EnvMockController;
import com.virjar.ratel.manager.bridge.ClientInfo;
import com.virjar.ratel.manager.bridge.IRatelRemoteControlHandler;
import com.virjar.ratel.manager.bridge.MultiUserBundle;
import com.virjar.ratel.runtime.MultiUserManager;
import com.virjar.ratel.runtime.RatelConfig;
import com.virjar.ratel.runtime.RatelRuntime;
import com.virjar.ratel.runtime.SchedulerTaskLoader;
import com.virjar.ratel.runtime.XposedModuleLoader;
import com.virjar.ratel.utils.ProcessUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import external.org.apache.commons.io.IOUtils;

public class RatelRemoteControllerHandler extends IRatelRemoteControlHandler.Stub {

    @Override
    public void killMe() throws RemoteException {
        ProcessUtil.killMe();
    }

    @Override
    public void updateHookStatus(boolean hookStatus) throws RemoteException {
        Log.w(Constants.TAG, "not implemented now!!");
    }

    @Override
    public String executeCmd(String cmd) throws RemoteException {
        try {
            java.lang.Process process = Runtime.getRuntime().exec(cmd);
            String out = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
            String error = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8);
            return out + "\n" + error;
        } catch (IOException e) {
            Log.w(Constants.TAG, "failed to execute cmd from ratel manage");
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public ClientInfo getClientInfo() throws RemoteException {
        return new ClientInfo(RatelRuntime.nowPackageName, RatelRuntime.processName, Process.myPid());
    }


    @Override
    public boolean switchEnv(String userId) throws RemoteException {
        if (EnvMockController.getVirtualEnvModel() != VirtualEnv.VirtualEnvModel.MULTI) {
            return false;
        }
        try {
            MultiUserManager.switchEnv(userId, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeUser(String userId) throws RemoteException {
        return RatelToolKit.virtualEnv.removeUser(userId);
    }

    /**
     * 暂时只考虑从其他模式切换为 MULTI 模式，后续可能支持多种模式相互转换
     */
    @Override
    public boolean switchEnvModel(String model) throws RemoteException {
        if (VirtualEnv.VirtualEnvModel.MULTI.name().equals(model)) {
            RatelConfig.setConfig(Constants.virtualEnvModel, VirtualEnv.VirtualEnvModel.MULTI.name());
        }
        return true;
    }

    @Override
    public boolean addUser(String userId) throws RemoteException {
        MultiUserManager.addUserIfNotExist(userId);
        return true;
    }

    @Override
    public void updateMultiEnvAPISwitchStatus(boolean enable) throws RemoteException {
        if (enable) {
            RatelConfig.setConfig(Constants.disableSwitchEnvForAPI, null);
        } else {
            RatelConfig.setConfig(Constants.disableSwitchEnvForAPI, "disable");
        }
    }

    @Override
    public MultiUserBundle multiUserStatus() throws RemoteException {
        return new MultiUserBundle(
                RatelToolKit.virtualEnv.nowUser(),
                new ArrayList<>(RatelToolKit.virtualEnv.availableUserSet()),
                EnvMockController.getVirtualEnvModel() == VirtualEnv.VirtualEnvModel.MULTI,
                !TextUtils.isEmpty(RatelConfig.getConfig(Constants.disableSwitchEnvForAPI))
        );
    }

    @Override
    public void callSchedulerTask(Map params) throws RemoteException {
        try {
            @SuppressWarnings("unchecked") Map<String, String> paramCopy = params;
            SchedulerTaskLoader.callSchedulerTask(paramCopy);
        } catch (Exception e) {
            Log.e(Constants.SCHEDULER_TAG, "call scheduler task failed", e);
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void updateHotmoduleStatus(boolean enable) throws RemoteException {
        XposedModuleLoader.setHotmoduleSwitchStatus(enable);
    }

    @Override
    public boolean getHotmoduleStatus() throws RemoteException {
        return XposedModuleLoader.hotmoduleEnabled();
    }

}
