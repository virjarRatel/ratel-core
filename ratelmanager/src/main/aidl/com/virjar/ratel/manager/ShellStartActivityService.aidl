// ShellStartActivityService.aidl
package com.virjar.ratel.manager;

// Declare any non-default types here with import statements

interface ShellStartActivityService {

   //  启动Activity，但是使用adb shell的身份
   void startActivity(in Intent intent);
}