package de.robv.android.xposed;

import com.virjar.ratel.api.xposed.IRXposedHookZygoteInit;

/**
 * Hook the initialization of Zygote process(es), from which all the apps are forked.
 *
 * <p>Implement this interface in your module's main class in order to be notified when Android is
 * starting up. In {@link IXposedHookZygoteInit}, you can modify objects and place hooks that should
 * be applied for every app. Only the Android framework/system classes are available at that point
 * in time. Use {@code null} as class loader for {@link XposedHelpers#findAndHookMethod(String, ClassLoader, String, Object...)}
 * and its variants.
 *
 * <p>If you want to hook one/multiple specific apps, use {@link IXposedHookLoadPackage} instead.
 */
public interface IXposedHookZygoteInit extends IXposedMod, IRXposedHookZygoteInit {
    /**
     * Data holder for {@link #initZygote}.
     */
    class StartupParam extends IRXposedHookZygoteInit.StartupParam {
        public StartupParam() {
        }
    }

}
