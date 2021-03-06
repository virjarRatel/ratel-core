package com.virjar.ratel.api.inspect;

import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;
import com.virjar.ratel.api.rposed.RposedBridge;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassLoadMonitor {
    public interface OnClassLoader {
        void onClassLoad(Class<?> clazz);
    }

    private static ConcurrentMap<String, Set<OnClassLoader>> callBacks = new ConcurrentHashMap<>();
    private static Set<OnClassLoader> onClassLoaders = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static Set<ClassLoader> hookedClassLoader = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

    static {
        hookedClassLoader.add(Thread.currentThread().getContextClassLoader());
        hookedClassLoader.add(ClassLoadMonitor.class.getClassLoader());
        hookedClassLoader.add(RatelToolKit.sContext.getClassLoader());
    }

    public static void notifyClassInit(Class clazz) {
        hookedClassLoader.add(clazz.getClassLoader());

        for (OnClassLoader onClassLoader : onClassLoaders) {
            try {
                onClassLoader.onClassLoad(clazz);
            } catch (Throwable throwable) {
                Log.e(RatelToolKit.TAG, "error when callback for class load monitor", throwable);
            }
        }

        Collection<OnClassLoader> onClassLoaders = callBacks.remove(clazz.getName());
        if (onClassLoaders != null) {
            for (OnClassLoader onClassLoader : onClassLoaders) {
                try {
                    onClassLoader.onClassLoad(clazz);
                } catch (Throwable throwable) {
                    Log.e(RatelToolKit.TAG, "error when callback for class load monitor", throwable);
                }
            }
        }
    }

    public static void addClassLoadMonitor(String className, OnClassLoader onClassLoader) {
        addClassLoadMonitor(className, onClassLoader, false);
    }

    /**
     * ????????????class????????????????????????????????????????????????????????????????????????????????????????????????????????????<br>
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param className       ???????????????className?????????????????????class name?????????????????????????????????classloader??????????????????????????????
     * @param onClassLoader   ???????????????
     * @param retryWithSystem ???????????????????????????classloader?????????????????????class????????????????????????class????????????duplicate load?????????
     */
    public static void addClassLoadMonitor(String className, OnClassLoader onClassLoader, boolean retryWithSystem) {
        if (retryWithSystem) {
            for (ClassLoader classLoader : hookedClassLoader) {
                Class<?> classIfExists = RposedHelpers.findClassIfExists(className, classLoader);
                if (classIfExists == null) {
                    continue;
                }
                if (ClassStatusUtils.isInitialized(classIfExists)) {
                    onClassLoader.onClassLoad(classIfExists);
                    return;
                }
            }

        }


        Set<OnClassLoader> onClassLoaders = callBacks.get(className);
        if (onClassLoaders == null) {
            onClassLoaders = Collections.newSetFromMap(new ConcurrentHashMap<>());
            //putIfAbsent maybe null
            callBacks.putIfAbsent(className, onClassLoaders);
            onClassLoaders = callBacks.get(className);
        }
        onClassLoaders.add(onClassLoader);
    }

    public static void addClassLoadMonitor(OnClassLoader onClassLoader) {
        onClassLoaders.add(onClassLoader);
    }

    /**
     * ??????????????????class???????????????classloader????????????
     *
     * @param className className
     * @return class????????????????????????????????????null
     */
    public static Class<?> tryLoadClass(String className) {
        Class<?> ret = classCache.get(className);
        if (ret != null) {
            return ret;
        }

        for (ClassLoader classLoader : hookedClassLoader) {
            try {
                Class<?> aClass = RposedHelpers.findClassIfExists(className, classLoader);
                if (aClass != null) {
                    classCache.put(className, aClass);
                    return aClass;
                }
            } catch (Throwable throwable) {
                // ???????????????????????????class?????????????????????????????????catch Throwable
                // ignore
            }
        }
        return RposedHelpers.findClassIfExists(className, null);
    }


    public static void findAndHookMethod(String className, final String methodName, final Object... parameterTypesAndCallback) {
        addClassLoadMonitor(className, clazz -> RposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback), true);
    }

    public static void findAndHookMethodWithSupper(String className, final String methodName, final Object... parameterTypesAndCallback) {
        addClassLoadMonitor(className, clazz -> {
            Throwable t = null;
            while (clazz != Object.class) {
                try {
                    RposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
                    return;
                } catch (Throwable throwable) {
                    if (t == null) {
                        t = throwable;
                    }
                    clazz = clazz.getSuperclass();
                }
            }
            throw new IllegalStateException(t);
        }, true);
    }

    public static void hookAllMethod(String className, RC_MethodHook callback) {
        hookAllMethod(className, null, callback);
    }

    public static void hookAllMethod(final String className, final String methodName, final RC_MethodHook callback) {
        addClassLoadMonitor(className, clazz -> {
            if (Modifier.isInterface(clazz.getModifiers())) {
                Log.e(RatelToolKit.TAG, "the class : {" + clazz.getName() + "} is interface can not hook any method!!");
                return;
            }
            for (Method method : clazz.getDeclaredMethods()) {
                if (methodName != null && !method.getName().equals(methodName)) {
                    continue;
                }
                if (Modifier.isAbstract(method.getModifiers())) {
                    continue;
                }
                RposedBridge.hookMethod(method, callback);
            }
        }, true);
    }

    public static void hookAllConstructor(String className, final RC_MethodHook callback) {
        addClassLoadMonitor(className, clazz -> RposedBridge.hookAllConstructors(clazz, callback), true);
    }
}
