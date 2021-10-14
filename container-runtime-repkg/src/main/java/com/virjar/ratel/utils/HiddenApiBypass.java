package com.virjar.ratel.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.virjar.ratel.core.runtime.BuildConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import dalvik.system.VMRuntime;
import sun.misc.Unsafe;

@RequiresApi(Build.VERSION_CODES.P)
public final class HiddenApiBypass {
    private static final String TAG = "HiddenApiBypass";
    private static final Unsafe unsafe;
    private static final long artOffset;
    private static final long infoOffset;
    private static final long methodsOffset;
    private static final long memberOffset;
    private static final long size;
    private static final long bias;
    private static final Set<String> signaturePrefixes = new HashSet<>();

    static {
        try {
            //noinspection JavaReflectionMemberAccess DiscouragedPrivateApi
            unsafe = (Unsafe) Unsafe.class.getDeclaredMethod("getUnsafe").invoke(null);
            assert unsafe != null;
            artOffset = unsafe.objectFieldOffset(Helper.MethodHandle.class.getDeclaredField("artFieldOrMethod"));
            infoOffset = unsafe.objectFieldOffset(Helper.MethodHandleImpl.class.getDeclaredField("info"));
            methodsOffset = unsafe.objectFieldOffset(Helper.Class.class.getDeclaredField("methods"));
            memberOffset = unsafe.objectFieldOffset(Helper.HandleInfo.class.getDeclaredField("member"));
            MethodHandle mhA = MethodHandles.lookup().unreflect(Helper.NeverCall.class.getDeclaredMethod("a"));
            MethodHandle mhB = MethodHandles.lookup().unreflect(Helper.NeverCall.class.getDeclaredMethod("b"));
            long aAddr = unsafe.getLong(mhA, artOffset);
            long bAddr = unsafe.getLong(mhB, artOffset);
            long aMethods = unsafe.getLong(Helper.NeverCall.class, methodsOffset);
            size = bAddr - aAddr;
            if (BuildConfig.DEBUG) {
                Log.v(TAG, size + " " +
                        Long.toString(aAddr, 16) + ", " +
                        Long.toString(bAddr, 16) + ", " +
                        Long.toString(aMethods, 16));
            }
            bias = aAddr - aMethods - size;
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * get declared methods of given class without hidden api restriction
     *
     * @param clazz the class to fetch declared methods
     * @return list of declared methods of {@code clazz}
     */
    public static List<Executable> getDeclaredMethods(Class<?> clazz) {
        ArrayList<Executable> list = new ArrayList<>();
        if (clazz.isPrimitive() || clazz.isArray()) return list;
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().unreflect(Helper.NeverCall.class.getDeclaredMethod("a"));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return list;
        }
        long methods = unsafe.getLong(clazz, methodsOffset);
        int numMethods = unsafe.getInt(methods);
        if (BuildConfig.DEBUG) Log.d(TAG, clazz + " has " + numMethods + " methods");
        boolean hasGetRuntime = false;
        boolean hasSetHiddenApiExemptions = false;
        for (int i = 0; i < numMethods; i++) {
            long method = methods + i * size + bias;
            unsafe.putLong(mh, artOffset, method);
            unsafe.putObject(mh, infoOffset, null);
            try {
                MethodHandles.lookup().revealDirect(mh);
            } catch (Throwable ignored) {
                continue;
            }
            MethodHandleInfo info = (MethodHandleInfo) unsafe.getObject(mh, infoOffset);
            Executable member = (Executable) unsafe.getObject(info, memberOffset);
            if (member.getName().contains("getRuntime")) {
                hasGetRuntime = true;
            } else if (member.getName().contains("setHiddenApiExemptions")) {
                hasSetHiddenApiExemptions = true;
            }
            if (BuildConfig.DEBUG) Log.v(TAG, "got " + clazz.getTypeName() + "." + member +
                    "(" + Arrays.stream(member.getTypeParameters()).map(Type::getTypeName).collect(Collectors.joining()) + ")");
            list.add(member);
            if (hasGetRuntime && hasSetHiddenApiExemptions) {
                break;
            }
        }
        return list;
    }

    /**
     * Sets the list of exemptions from hidden API access enforcement.
     *
     * @param signaturePrefixes A list of class signature prefixes. Each item in the list is a prefix match on the type
     *                          signature of a blacklisted API. All matching APIs are treated as if they were on
     *                          the whitelist: access permitted, and no logging..
     * @return whether the operation is successful
     */
    public static boolean setHiddenApiExemptions(String... signaturePrefixes) {
        List<Executable> methods = getDeclaredMethods(VMRuntime.class);
        Optional<Executable> getRuntime = methods.stream().filter(it -> it.getName().equals("getRuntime")).findFirst();
        Optional<Executable> setHiddenApiExemptions = methods.stream().filter(it -> it.getName().equals("setHiddenApiExemptions")).findFirst();
        if (getRuntime.isPresent() && setHiddenApiExemptions.isPresent()) {
            getRuntime.get().setAccessible(true);
            try {
                Object runtime = ((Method) getRuntime.get()).invoke(null);
                setHiddenApiExemptions.get().setAccessible(true);
                ((Method) setHiddenApiExemptions.get()).invoke(runtime, (Object) signaturePrefixes);
                return true;
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return false;
    }

    /**
     * Adds the list of exemptions from hidden API access enforcement.
     *
     * @param signaturePrefixes A list of class signature prefixes. Each item in the list is a prefix match on the type
     *                          signature of a blacklisted API. All matching APIs are treated as if they were on
     *                          the whitelist: access permitted, and no logging..
     * @return whether the operation is successful
     */
    public static boolean addHiddenApiExemptions(String... signaturePrefixes) {
        HiddenApiBypass.signaturePrefixes.addAll(Arrays.asList(signaturePrefixes));
        String[] strings = new String[HiddenApiBypass.signaturePrefixes.size()];
        HiddenApiBypass.signaturePrefixes.toArray(strings);
        return setHiddenApiExemptions(strings);
    }

    /**
     * Clear the list of exemptions from hidden API access enforcement.
     *
     * @return whether the operation is successful
     */
    public static boolean clearHiddenApiExemptions() {
        HiddenApiBypass.signaturePrefixes.clear();
        return setHiddenApiExemptions("");
    }


    public static class Helper {
        static public class MethodHandle {
            private final MethodType type = null;
            private MethodType nominalType;
            private MethodHandle cachedSpreadInvoker;
            protected final int handleKind = 0;

            // The ArtMethod* or ArtField* associated with this method handle (used by the runtime).
            protected final long artFieldOrMethod = 0;
        }

        static final public class MethodHandleImpl extends MethodHandle {
            private final MethodHandleInfo info = null;
        }

        static final public class HandleInfo {
            private final Member member = null;
            private final MethodHandle handle = null;
        }

        static final public class Class {
            private transient ClassLoader classLoader;
            private transient java.lang.Class<?> componentType;
            private transient Object dexCache;
            private transient Object extData;
            private transient Object[] ifTable;
            private transient String name;
            private transient java.lang.Class<?> superClass;
            private transient Object vtable;
            private transient long iFields;
            private transient long methods;
            private transient long sFields;
            private transient int accessFlags;
            private transient int classFlags;
            private transient int classSize;
            private transient int clinitThreadId;
            private transient int dexClassDefIndex;
            private transient volatile int dexTypeIndex;
            private transient int numReferenceInstanceFields;
            private transient int numReferenceStaticFields;
            private transient int objectSize;
            private transient int objectSizeAllocFastPath;
            private transient int primitiveType;
            private transient int referenceInstanceOffsets;
            private transient int status;
            private transient short copiedMethodsOffset;
            private transient short virtualMethodsOffset;
        }

        public static class NeverCall {
            static void a() {
            }

            static void b() {
            }
        }
    }
}