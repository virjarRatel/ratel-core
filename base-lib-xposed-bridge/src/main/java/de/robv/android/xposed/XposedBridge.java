package de.robv.android.xposed;

import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;
import com.virjar.ratel.api.rposed.RC_MethodHook;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * This class contains most of Xposed's central logic, such as initialization and callbacks used by
 * the native side. It also includes methods to add new hooks.
 */
@SuppressWarnings("JniMissingFunction")
public final class XposedBridge {
    /**
     * The system class loader which can be used to locate Android framework classes.
     * Application classes cannot be retrieved from it.
     */
    public static final ClassLoader BOOTCLASSLOADER = XposedBridge.class.getClassLoader();

    /**
     * @hide
     */
    public static final String TAG = "RATEL-Xposed-Bridge";

    /**
     * @deprecated Use {@link #getXposedVersion()} instead.
     */
    @Deprecated
    public static int XPOSED_BRIDGE_VERSION;

    /*package*/ static boolean isZygote = true; // ed: RuntimeInit.main() tool process not supported yet

    private static int runtime = 2; // ed: only support art
    private static final int RUNTIME_DALVIK = 1;
    private static final int RUNTIME_ART = 2;

    public static boolean disableHooks = false;

    // This field is set "magically" on MIUI.
    /*package*/ static long BOOT_START_TIME;

    private static final Object[] EMPTY_ARRAY = new Object[0];


    private XposedBridge() {
    }

    /**
     * Called when native methods and other things are initialized, but before preloading classes etc.
     *
     * @hide
     */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        // ed: moved
    }

    /**
     * @hide
     */
//	protected static final class ToolEntryPoint {
//		protected static void main(String[] args) {
//			isZygote = false;
//			XposedBridge.main(args);
//		}
//	}
    private static void initXResources() throws IOException {
        // ed: no support for now
    }


    /**
     * Returns the currently installed version of the Xposed framework.
     */
    public static int getXposedVersion() {
        // ed: fixed value for now
        return 90;
    }

    /**
     * Writes a message to the Xposed error log.
     *
     * <p class="warning"><b>DON'T FLOOD THE LOG!!!</b> This is only meant for error logging.
     * If you want to write information/debug messages, use logcat.
     *
     * @param text The log message.
     */
    public synchronized static void log(String text) {
        Log.i(TAG, text);
    }

    /**
     * Logs a stack trace to the Xposed error log.
     *
     * <p class="warning"><b>DON'T FLOOD THE LOG!!!</b> This is only meant for error logging.
     * If you want to write information/debug messages, use logcat.
     *
     * @param t The Throwable object for the stack trace.
     */
    public synchronized static void log(Throwable t) {
        Log.e(TAG, Log.getStackTraceString(t));
    }

    /**
     * Hook any method (or constructor) with the specified callback. See below for some wrappers
     * that make it easier to find a method/constructor in one step.
     *
     * @param hookMethod The method to be hooked.
     * @param callback   The callback to be executed when the hooked method is called.
     * @return An object that can be used to remove the hook.
     * @see XposedHelpers#findAndHookMethod(java.lang.String, ClassLoader, String, Object...)
     * @see XposedHelpers#findAndHookMethod(Class, String, Object...)
     * @see #hookAllMethods
     * @see XposedHelpers#findAndHookConstructor(String, ClassLoader, Object...)
     * @see XposedHelpers#findAndHookConstructor(Class, Object...)
     * @see #hookAllConstructors
     */
    public static XC_MethodHook.Unhook hookMethod(Member hookMethod, XC_MethodHook callback) {

        XC2RC_MethodHook xc2RC_methodHook = new XC2RC_MethodHook(callback);
        unhookMap.put(callback, xc2RC_methodHook);
        RC_MethodHook.Unhook unhook = RatelToolKit.usedHookProvider.hookMethod(hookMethod, xc2RC_methodHook);
        return callback.new Unhook(hookMethod, unhook);
    }

    private static Map<XC_MethodHook, XC2RC_MethodHook> unhookMap = new ConcurrentHashMap<>();

    /**
     * Removes the callback for a hooked method/constructor.
     *
     * @param hookMethod The method for which the callback should be removed.
     * @param callback   The reference to the callback as specified in {@link #hookMethod}.
     * @deprecated Use {@link XC_MethodHook.Unhook#unhook} instead. An instance of the {@code Unhook}
     * class is returned when you hook the method.
     */
    @Deprecated
    public static void unhookMethod(Member hookMethod, XC_MethodHook callback) {
        XC2RC_MethodHook xc2RC_methodHook = unhookMap.get(callback);
        if (xc2RC_methodHook == null) {
            return;
        }
        RatelToolKit.usedHookProvider.unhookMethod(hookMethod, xc2RC_methodHook);
    }

    /**
     * Hooks all methods with a certain name that were declared in the specified class. Inherited
     * methods and constructors are not considered. For constructors, use
     * {@link #hookAllConstructors} instead.
     *
     * @param hookClass  The class to check for declared methods.
     * @param methodName The name of the method(s) to hook.
     * @param callback   The callback to be executed when the hooked methods are called.
     * @return A set containing one object for each found method which can be used to unhook it.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Member method : hookClass.getDeclaredMethods())
            if (method.getName().equals(methodName))
                unhooks.add(hookMethod(method, callback));
        return unhooks;
    }

    /**
     * Hook all constructors of the specified class.
     *
     * @param hookClass The class to check for constructors.
     * @param callback  The callback to be executed when the hooked constructors are called.
     * @return A set containing one object for each found constructor which can be used to unhook it.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Member constructor : hookClass.getDeclaredConstructors())
            unhooks.add(hookMethod(constructor, callback));
        return unhooks;
    }

    /**
     * This method is called as a replacement for hooked methods.
     */
    public static Object handleHookedMethod(Member method, long originalMethodId, Object additionalInfoObj,
                                            Object thisObject, Object[] args) throws Throwable {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a callback to be executed when an app ("Android package") is loaded.
     *
     * <p class="note">You probably don't need to call this. Simply implement {@link IXposedHookLoadPackage}
     * in your module class and Xposed will take care of registering it as a callback.
     *
     * @param callback The callback to be executed.
     * @hide
     */
    public static void hookLoadPackage(XC_LoadPackage callback) {
        Log.w(RatelToolKit.TAG, "now support for hookLoadPackage");
    }

    public static void clearLoadedPackages() {
        Log.w(RatelToolKit.TAG, "now support for clearLoadedPackages");
    }

    /**
     * Adds a callback to be executed when the resources for an app are initialized.
     *
     * <p class="note">You probably don't need to call this. Simply implement {@link IXposedHookInitPackageResources}
     * in your module class and Xposed will take care of registering it as a callback.
     *
     * @param callback The callback to be executed.
     * @hide
     */
    public static void hookInitPackageResources(XC_InitPackageResources callback) {
        Log.w(RatelToolKit.TAG, "now support for hookInitPackageResources");
        // TODO not supported yet
//		synchronized (sInitPackageResourcesCallbacks) {
//			sInitPackageResourcesCallbacks.add(callback);
//		}
    }

    public static void clearInitPackageResources() {
        Log.w(RatelToolKit.TAG, "now support for clearInitPackageResources");
    }

    /**
     * Intercept every call to the specified method and call a handler function instead.
     *
     * @param method The method to intercept
     */
    private synchronized static void hookMethodNative(final Member method, Class<?> declaringClass,
                                                      int slot, final Object additionalInfoObj) {
        throw new UnsupportedOperationException("not support");
    }

    private static Object invokeOriginalMethodNative(Member method,
                                                     Object thisObject, Object[] args)
            throws Throwable {
        return RatelToolKit.usedHookProvider.invokeOriginalMethod(method, thisObject, args);
    }

    /**
     * Basically the same as {@link Method#invoke}, but calls the original method
     * as it was before the interception by Xposed. Also, access permissions are not checked.
     *
     * <p class="caution">There are very few cases where this method is needed. A common mistake is
     * to replace a method and then invoke the original one based on dynamic conditions. This
     * creates overhead and skips further hooks by other modules. Instead, just hook (don't replace)
     * the method and call {@code param.setResult(null)} in {@link XC_MethodHook#beforeHookedMethod}
     * if the original method should be skipped.
     *
     * @param method     The method to be called.
     * @param thisObject For non-static calls, the "this" pointer, otherwise {@code null}.
     * @param args       Arguments for the method call as Object[] array.
     * @return The result returned from the invoked method.
     * @throws NullPointerException      if {@code receiver == null} for a non-static method
     * @throws IllegalAccessException    if this method is not accessible (see {@link AccessibleObject})
     * @throws IllegalArgumentException  if the number of arguments doesn't match the number of parameters, the receiver
     *                                   is incompatible with the declaring class, or an argument could not be unboxed
     *                                   or converted by a widening conversion to the corresponding parameter type
     * @throws InvocationTargetException if an exception was thrown by the invoked method
     */
    public static Object invokeOriginalMethod(Member method, Object thisObject, Object[] args)
            throws Throwable {
        if (args == null) {
            args = EMPTY_ARRAY;
        }
        return invokeOriginalMethodNative(method, thisObject, args);
    }


    /**
     * @hide
     */
    public static final class CopyOnWriteSortedSet<E> {
        private transient volatile Object[] elements = EMPTY_ARRAY;

        @SuppressWarnings("UnusedReturnValue")
        public synchronized boolean add(E e) {
            int index = indexOf(e);
            if (index >= 0)
                return false;

            Object[] newElements = new Object[elements.length + 1];
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            newElements[elements.length] = e;
            Arrays.sort(newElements);
            elements = newElements;
            return true;
        }

        @SuppressWarnings("UnusedReturnValue")
        public synchronized boolean remove(E e) {
            int index = indexOf(e);
            if (index == -1)
                return false;

            Object[] newElements = new Object[elements.length - 1];
            System.arraycopy(elements, 0, newElements, 0, index);
            System.arraycopy(elements, index + 1, newElements, index, elements.length - index - 1);
            elements = newElements;
            return true;
        }

        private int indexOf(Object o) {
            for (int i = 0; i < elements.length; i++) {
                if (o.equals(elements[i]))
                    return i;
            }
            return -1;
        }

        public Object[] getSnapshot() {
            return elements;
        }

        public synchronized void clear() {
            elements = EMPTY_ARRAY;
        }
    }

//    public static class AdditionalHookInfo {
//        public final CopyOnWriteSortedSet<RC_MethodHook> callbacks;
//        //TODO 貌似parameterTypes和returnType都没有意义，再观察一下
//        public final Class<?>[] parameterTypes;
//        public final Class<?> returnType;
//
//        public AdditionalHookInfo(CopyOnWriteSortedSet<RC_MethodHook> callbacks, Class<?>[] parameterTypes, Class<?> returnType) {
//            this.callbacks = callbacks;
//            this.parameterTypes = parameterTypes;
//            this.returnType = returnType;
//        }
//    }
}
