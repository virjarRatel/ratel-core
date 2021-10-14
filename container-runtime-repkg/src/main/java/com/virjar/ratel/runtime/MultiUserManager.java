package com.virjar.ratel.runtime;

import android.text.TextUtils;
import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.VirtualEnv;
import com.virjar.ratel.utils.FileUtils;
import com.virjar.ratel.utils.ProcessUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import external.org.apache.commons.lang3.StringUtils;

import static com.virjar.ratel.envmock.EnvMockController.getVirtualEnvModel;

public class MultiUserManager {
    private static String mockUserId = "";

    static String getMockUserId() {
        return mockUserId;
    }

    /**
     * 如果处于mutli模式下，这个函数必由ratel框架调用触发。此时如果没有mockUserId定义，那么使用默认值：default_0
     */
    public static void setMockUserId(String mockUserId) {
        mockUserId = addUserIfNotExist(mockUserId);
        if (!RatelRuntime.isStartCompleted()) {
            MultiUserManager.mockUserId = mockUserId;
        }
        RatelConfig.setConfig(Constants.multiUserIdKey, mockUserId);
    }


    public static String addUserIfNotExist(String mockUserId) {
        String trimMockUserId = VirtualEnv.defaultMultiUserId;
        if (!TextUtils.isEmpty(mockUserId)) {
            StringBuilder stringBuilder = new StringBuilder(mockUserId.length());
            for (char aChar : mockUserId.toCharArray()) {
                if (Character.isJavaIdentifierPart(aChar)) {
                    stringBuilder.append(aChar);
                }
            }
            trimMockUserId = stringBuilder.toString();
        }

        if (!trimMockUserId.equals(mockUserId)) {
            Log.w(Constants.TAG, "trim userId from: " + mockUserId + " to: " + trimMockUserId);
        }

        Set<String> userSet = multiUserSet();
        if (!userSet.contains(trimMockUserId)) {
            userSet.add(trimMockUserId);
            RatelConfig.setConfig(Constants.multiUserListKey, StringUtils.join(userSet, ","));
        }

        // if this id exited in clean task,we should remove
        String deletedUser = RatelConfig.getConfig(Constants.needRemoveUserListKey);
        if (!TextUtils.isEmpty(deletedUser)) {
            String[] userList = deletedUser.split(",");
            Set<String> newUserSet = new HashSet<>();
            for (String userId : userList) {
                if (!userId.equals(mockUserId)) {
                    newUserSet.add(userId);
                }
            }
            if (newUserSet.size() != userList.length) {
                RatelConfig.setConfig(Constants.needRemoveUserListKey, StringUtils.join(newUserSet, ","));
            }
        }


        return trimMockUserId;
    }

    public static Set<String> multiUserSet() {
        String config = RatelConfig.getConfig(Constants.multiUserListKey);
        if (StringUtils.isEmpty(config)) {
            return new HashSet<>();
        }
        Set<String> ret = new HashSet<>();
        for (String str : config.split(",")) {
            str = StringUtils.trim(str);
            if (!StringUtils.isEmpty(str)) {
                ret.add(str);
            }
        }
        return ret;
    }


    public static String nowUser() {
        if (getVirtualEnvModel() != VirtualEnv.VirtualEnvModel.MULTI) {
            Log.w(Constants.TAG, "app not running on multi user model");
            return null;
        }
        return RatelConfig.getConfig(Constants.multiUserIdKey);
    }

    public static boolean removeUser(String userId) {
        if (getVirtualEnvModel() != VirtualEnv.VirtualEnvModel.MULTI) {
            Log.w(Constants.TAG, "app not running on multi user model");
            return false;
        }
        if (StringUtils.equals(userId, nowUser())) {
            Log.w(Constants.TAG, "can not remove user:" + userId + "   ,app running on this user now");
            return false;
        }
//        if (RatelRuntime.isStartCompleted()) {
//            //当ratel框架启动完成之后，io重定向将会生效。此时其他设备文件无法访问，也就无法删除其他文件了
//            Log.w(Constants.TAG, "can not call remove user after ratel component startup!!");
//            return false;
//        }
        Set<String> userSet = multiUserSet();

        boolean remove = userSet.remove(userId);

        if (!remove) {
            Log.w(Constants.TAG, "can not find user:" + userId);
            return false;
        }
        RatelConfig.setConfig(Constants.multiUserListKey, StringUtils.join(userSet, ","));

        if (RatelRuntime.isStartCompleted()) {
            String deletedUser = RatelConfig.getConfig(Constants.needRemoveUserListKey);
            if (deletedUser != null) {
                deletedUser = deletedUser + "," + userId;
            } else {
                deletedUser = userId;
            }
            RatelConfig.setConfig(Constants.needRemoveUserListKey, deletedUser);
            return true;
        } else {
            File userDir = new File(RatelEnvironment.envMockBaseDir(), userId);
            return FileUtils.deleteDir(userDir);
        }
    }

    public static void cleanRemovedUser() {
        String deletedUser = RatelConfig.getConfig(Constants.needRemoveUserListKey);
        if (TextUtils.isEmpty(deletedUser)) {
            return;
        }
        String[] userList = deletedUser.split(",");
        for (String userId : userList) {
            File userDir = new File(RatelEnvironment.envMockBaseDir(), userId);
            FileUtils.deleteDir(userDir);
        }
        RatelConfig.setConfig(Constants.needRemoveUserListKey, null);
    }

    public static void switchEnv(String userId, boolean fromAPICall) {
        if (fromAPICall) {
            String flag = RatelConfig.getConfig(Constants.disableSwitchEnvForAPI);
            if (!TextUtils.isEmpty(flag)) {
                //禁用api切换
                return;
            }
        }

        switch (getVirtualEnvModel()) {
            case MULTI:
                MultiUserManager.setMockUserId(userId);
                if (!RatelRuntime.isStartCompleted()) {
                    //multi 模式下，如果在系统ratel启动前切换，那么不需要重启app，因为这个时候app的逻辑还没有执行
                    return;
                }
                break;
            case DISABLE:
                return;
            case INSTALL:
                throw new IllegalStateException("can not call env switch for VirtualEnvModel: " + getVirtualEnvModel());
            case START_UP:
                break;
            default:
                throw new IllegalStateException("unknown VirtualEnvModel: " + getVirtualEnvModel());
        }

        ProcessUtil.killMe();

    }
}
