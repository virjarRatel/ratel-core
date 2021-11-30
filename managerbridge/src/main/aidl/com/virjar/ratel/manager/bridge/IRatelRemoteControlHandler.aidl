// IRatelRemoteControlHandler.aidl
package com.virjar.ratel.manager.bridge;

import com.virjar.ratel.manager.bridge.ClientInfo;
import com.virjar.ratel.manager.bridge.MultiUserBundle;

// Declare any non-default types here with import statements

interface IRatelRemoteControlHandler {
    void killMe();
    void updateHookStatus(boolean hookStatus);
    String executeCmd(String cmd);
    ClientInfo getClientInfo();

    //多用户操作接口
    boolean switchEnv(String userId);
    boolean removeUser(String userId);
    boolean addUser(String userId);
    void updateMultiEnvAPISwitchStatus(boolean enable);
    //获取多用户状态数据
    MultiUserBundle multiUserStatus();
    //app没有重启的状态下，调用ratel定时任务实现
    void callSchedulerTask(in Map params);

    // 设置热发插件开关
    void updateHotmoduleStatus(boolean enable);
    // 获取热发插件开关状态
    boolean getHotmoduleStatus();

    // 切换模式
    boolean switchEnvModel(String model);
}
