package com.virjar.ratel.api.rposed;

import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains most of Xposed's central logic, such as initialization and callbacks used by
 * the native side. It also includes methods to add new hooks.
 */
@SuppressWarnings("JniMissingFunction")
public final class RposedBridge {
    /**
     * The system class loader which can be used to locate Android framework classes.
     * Application classes cannot be retrieved from it.
     */
    public static final ClassLoader BOOTCLASSLOADER = RposedBridge.class.getClassLoader();

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


    private RposedBridge() {
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
     * @see RposedHelpers#findAndHookMethod(String, ClassLoader, String, Object...)
     * @see RposedHelpers#findAndHookMethod(Class, String, Object...)
     * @see #hookAllMethods
     * @see RposedHelpers#findAndHookConstructor(String, ClassLoader, Object...)
     * @see RposedHelpers#findAndHookConstructor(Class, Object...)
     * @see #hookAllConstructors
     */
    public static RC_MethodHook.Unhook hookMethod(Member hookMethod, RC_MethodHook callback) {
        return RatelToolKit.usedHookProvider.hookMethod(hookMethod, callback);
        // return callback.new Unhook(hookMethod);
    }

    /**
     * Removes the callback for a hooked method/constructor.
     *
     * @param hookMethod The method for which the callback should be removed.
     * @param callback   The reference to the callback as specified in {@link #hookMethod}.
     * @deprecated Use {@link RC_MethodHook.Unhook#unhook} instead. An instance of the {@code Unhook}
     * class is returned when you hook the method.<br>
     * 请注意，不要通过这个方法调用unhook，底层并没有实现他，请使用
     */
    @Deprecated
    public static void unhookMethod(Member hookMethod, RC_MethodHook callback) {
        RatelToolKit.usedHookProvider.unhookMethod(hookMethod, callback);
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
    public static Set<RC_MethodHook.Unhook> hookAllMethods(Class<?> hookClass, String methodName, RC_MethodHook callback) {
        Set<RC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Member method : hookClass.getDeclaredMethods())
            if (method.getName().equals(methodName))
                unhooks.add(hookMethod(method, callback));
        return unhooks;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Set<RC_MethodHook.Unhook> hookAllMethodsSafe(Class<?> hookClass, String methodName, RC_MethodHook callback) {
        try {
            return hookAllMethods(hookClass, methodName, callback);
        } catch (Throwable throwable) {
            Log.w("RATEL", "hookAllMethodsSafe exception", throwable);
            //TODO
            return null;
        }
    }


    /**
     * Hook all constructors of the specified class.
     *
     * @param hookClass The class to check for constructors.
     * @param callback  The callback to be executed when the hooked constructors are called.
     * @return A set containing one object for each found constructor which can be used to unhook it.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Set<RC_MethodHook.Unhook> hookAllConstructors(Class<?> hookClass, RC_MethodHook callback) {
        Set<RC_MethodHook.Unhook> unhooks = new HashSet<>();
        for (Member constructor : hookClass.getDeclaredConstructors())
            unhooks.add(hookMethod(constructor, callback));
        return unhooks;
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
     * the method and call {@code param.setResult(null)} in {@link RC_MethodHook#beforeHookedMethod}
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

}
