package com.virjar.ratel.runtime;

import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

import com.virjar.ratel.RatelNative;
import com.virjar.ratel.RatelVersion;
import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.api.DexMakerProxyBuilder;
import com.virjar.ratel.api.DexMakerProxyBuilderHelper;
import com.virjar.ratel.api.FingerPrintModel;
import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.envmock.SdcardFake;
import com.virjar.ratel.manager.bridge.IRatelManagerClientRegister;
import com.virjar.ratel.runtime.apibridge.ContentProviderFakeRegisterImpl;
import com.virjar.ratel.runtime.apibridge.DexMakerProxyBuilderImpl;
import com.virjar.ratel.runtime.apibridge.HookProviderImpl;
import com.virjar.ratel.runtime.apibridge.IORelocatorImpl;
import com.virjar.ratel.runtime.apibridge.NativeHelperImpl;
import com.virjar.ratel.runtime.apibridge.RatelResourceInterfaceImpl;
import com.virjar.ratel.runtime.apibridge.RatelUnpackImpl;
import com.virjar.ratel.runtime.apibridge.VirtualEnvImpl;
import com.virjar.ratel.runtime.ipc.ClientHandlerServiceConnection;
import com.virjar.ratel.utils.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;

import external.com.android.dx.stock.ProxyBuilder;
import external.org.apache.commons.io.FileUtils;

class APIInitializer {
    static void initAPIConstants() {
        RatelToolKit.usedHookProvider = new HookProviderImpl();


        RatelToolKit.ratelConfig = RatelConfig::getConfig;

        RatelToolKit.sContext = RatelRuntime.getOriginContext();

        RatelToolKit.hostClassLoader = RatelToolKit.sContext.getClassLoader();

        RatelToolKit.TAG = Constants.TAG;

        RatelToolKit.ioRelocator = new IORelocatorImpl();

        RatelToolKit.processName = RatelRuntime.processName;

        RatelToolKit.packageName = RatelRuntime.originPackageName;

        RatelToolKit.virtualEnv = new VirtualEnvImpl();

        RatelToolKit.processUtils = ProcessUtil::killMe;
        ProcessUtil.collectSupperAppiumClass();

        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState())) {
            File directory = Environment.getExternalStorageDirectory();
            HashSet<String> strings = SdcardFake.allSdcardPoint();
            for (String base : strings) {
                // /storage/emulated/0/
                // /storage/emulated/legacy/
                // /storage/usba/    这个在root下面
                // /storage/sdcard1/ 这个在root下面
                //不是所有都有权限
                File sdcardRoot = new File(base);
                if (!sdcardRoot.canRead()) {
                    Log.i(Constants.TAG, "can not read sdcard root: " + sdcardRoot);
                    continue;
                }
                try {
                    File whiteDir = new File(base, "ratel_white_dir");
                    String ratelWhiteDir = new File(whiteDir, RatelToolKit.packageName).getCanonicalPath();
                    try {
                        FileUtils.forceMkdir(new File(ratelWhiteDir));
                    } catch (IOException e) {
                        // 请注意，Android11之后，内存卡机制发生了改变，内存卡根据文件夹路径进行授权，并且默认都具有权限
                        // 路径为： /storage/emulated/0/Android/data/pkg/files/Download/ratel_white_dir/pkg
                        // 此时不再需要检测内存卡的权限，所以这统一直接进行文件夹创建操作
                        Log.i(Constants.TAG, "create white dir failed " + ratelWhiteDir, e);
                    }
                    if (new File(ratelWhiteDir).canRead() && new File(ratelWhiteDir).canWrite()) {
                        RatelToolKit.whiteSdcardDirPath = ratelWhiteDir;
                        RatelNative.whitelist(whiteDir.getCanonicalPath());
                        Log.i(Constants.TAG, "ratel white sdcard dir path:" + RatelToolKit.whiteSdcardDirPath);
                    } else {
                        Log.i(Constants.TAG, "ratel white sdcard dir path is no permission");
                    }
                } catch (IOException e) {
                    Log.e(Constants.TAG, "create ratel white dir failed", e);
                }
            }
            //这也是一个白名单，用来和ratelManager交换数据
            //TODO ratelManager也需要适配安卓10和11的分区存储
            RatelNative.whitelist(new File(directory, Constants.ratelSDCardRoot).getAbsolutePath());
        }

        RatelToolKit.ratelVersionCode = RatelVersion.versionCode;
        RatelToolKit.ratelVersionName = RatelVersion.versionName;

        RatelToolKit.fingerPrintModel = new FingerPrintModel();

        RatelToolKit.dexMakerProxyBuilderHelper = new DexMakerProxyBuilderHelper() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> DexMakerProxyBuilder<T> forClass(Class<T> clazz) {
                return DexMakerProxyBuilderImpl.forClass(clazz);
            }

            @Override
            public boolean isProxyClass(Class<?> c) {
                return ProxyBuilder.isProxyClass(c);
            }

            @Override
            public Object callSuper(Object proxy, Method method, Object... args) throws Throwable {
                return ProxyBuilder.callSuper(proxy, method, args);
            }

            @Override
            public InvocationHandler getInvocationHandler(Object instance) {
                return ProxyBuilder.getInvocationHandler(instance);
            }

            @Override
            public void setInvocationHandler(Object instance, InvocationHandler handler) {
                ProxyBuilder.setInvocationHandler(instance, handler);
            }
        };

        RatelToolKit.contentProviderFakeRegister = new ContentProviderFakeRegisterImpl();

        RatelToolKit.schedulerTaskBeanHandler = SchedulerTaskLoader::finishTask;

        RatelToolKit.ratelResourceInterface = new RatelResourceInterfaceImpl();

        RatelToolKit.ratelEngine = RatelRuntime.ratelEngine;

        RatelToolKit.ratelUnpack = new RatelUnpackImpl();

        RatelToolKit.nativeHelper = new NativeHelperImpl();

        ClientHandlerServiceConnection.addOnManagerIPCListener(new RatelPhoneIdFetcher());

    }

    private static class RatelPhoneIdFetcher implements ClientHandlerServiceConnection.OnManagerReadyListener {

        @Override
        public void onManagerIPCReady(IRatelManagerClientRegister iRatelManagerClientRegister) {
            if (XposedModuleLoader.getRatelManagerVersionCode() < 13) {
                //1.1.2之前不支持查询
                return;
            }

            try {
                @SuppressWarnings("unchecked")
                Map<String, String> map = iRatelManagerClientRegister.queryProperties(RatelRuntime.nowPackageName);
                RatelToolKit.ratelPhoneIdentifier = map.get("phone_identifier");
            } catch (RemoteException r) {
                Log.w(Constants.TAG, "query phone_identifier failed", r);
            }
//            if (TextUtils.isEmpty(RatelToolKit.ratelPhoneIdentifier)) {
//                ClientHandlerServiceConnection.addOnManagerIPCListener(this);
//            }

        }
    }

    static void postInitAPIConstants() {
        RatelToolKit.userIdentifier = RatelEnvironment.userIdentifier();
        RatelToolKit.userIdentifierSeed = RatelEnvironment.userIdentifierSeed();
        RatelToolKit.userIdentifierSeedInt = (int) RatelEnvironment.userIdentifierSeed();
    }
}
