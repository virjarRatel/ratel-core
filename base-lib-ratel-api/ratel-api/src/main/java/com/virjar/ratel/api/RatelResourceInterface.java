package com.virjar.ratel.api;

import android.content.Context;
import android.content.res.Resources;

import com.virjar.ratel.api.hint.RatelEngineHistory;
import com.virjar.ratel.api.hint.RatelEngineVersion;

/**
 * 提供访问模块资源的功能，包括皮肤，字体，图片，布局等<br>
 * 如果资源已经加载过，那么本接口返回的是缓存对象
 */
@RatelEngineVersion(RatelEngineHistory.V_1_3_8)
public interface RatelResourceInterface {
    /**
     * 以一个特定的apk创建一个Resource对象
     *
     * @param moduleApkPath     apk路径，一般为模块apk地址。Ratel框架下，可以在模块入口得到模块地址: com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage.LoadPackageParam#modulePath
     * @param moduleClassLoader 指向这个apk的classloader
     * @return resource对象
     */
    Resources createResource(String moduleApkPath, ClassLoader moduleClassLoader);

    /**
     * 以一个特定的apk创建一个Context对象
     *
     * @param moduleApkPath     apk路径，一般为模块apk地址。Ratel框架下，可以在模块入口得到模块地址: com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage.LoadPackageParam#modulePath
     * @param moduleClassLoader 指向这个apk的classloader，如果你不需要通过inflater创建view对象，classloader可以为空
     * @param baseContext       context需要有一个父context，创建的context仅仅替换资源相关环境
     * @return context对象
     */
    Context createContext(String moduleApkPath, ClassLoader moduleClassLoader, Context baseContext);
}
