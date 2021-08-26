// IRatelManagerClientRegister.aidl
package com.virjar.ratel.manager.bridge;

import com.virjar.ratel.manager.bridge.IRatelRemoteControlHandler;
import java.util.Map;
import com.virjar.ratel.manager.bridge.FingerData;
// Declare any non-default types here with import statements

interface IRatelManagerClientRegister {
  void registerRemoteControlHandler(IRatelRemoteControlHandler ratelRemoteControlHandler);
  Map queryProperties(String mPackage);
  FingerData getFingerData();
  void saveMSignature(String mPackage,String mSignature);
  void addDelayDeamonTask(String mPackage,long delay);
}
