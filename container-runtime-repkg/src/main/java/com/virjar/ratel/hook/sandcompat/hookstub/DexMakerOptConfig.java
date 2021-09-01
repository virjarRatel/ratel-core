package com.virjar.ratel.hook.sandcompat.hookstub;

import android.util.Log;

import com.virjar.ratel.allcommon.Constants;
import com.virjar.ratel.sandhook.wrapper.HookWrapper;
import com.virjar.ratel.api.rposed.RposedHelpers;
import com.virjar.ratel.hook.sandcompat.RposedAdditionalHookInfo;
import com.virjar.ratel.hook.sandcompat.XposedCompat;
import com.virjar.ratel.runtime.RatelEnvironment;
import com.virjar.ratel.runtime.RatelRuntime;

import java.io.File;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

import static com.virjar.ratel.hook.sandcompat.utils.DexMakerUtils.MD5;

public class DexMakerOptConfig {
    private static DexClassLoader dexClassLoader = null;
    private static Class<?> delegateDexMakerOptConfigClass = null;
    private static Method delegateHasMethod = null;

    private static final String METHOD_NAME_BACKUP = "backup";
    private static final String METHOD_NAME_HOOK = "hook";

    private static final String FIELD_NAME_HOOK_INFO = "additionalHookInfo";
    private static final String FIELD_NAME_METHOD = "method";
    private static final String FIELD_NAME_BACKUP_METHOD = "backupMethod";

    //
//    static {
//        optClassNames.add("test");
//    }
//
//
    public static boolean has(String methodSignatureHash) {
        if (delegateHasMethod == null) {
            return false;
        }
        try {
            return (Boolean) delegateHasMethod.invoke(null, methodSignatureHash);
        } catch (Throwable throwable) {
            Log.w(Constants.TAG, "failed to call dexmaker opt resource", throwable);
            return false;
        }
    }

    static {
        try {
            loadResource();
        } catch (Throwable throwable) {
            Log.w(Constants.TAG, "failed to load dexmaker opt resource", throwable);
        }
    }

    public static HookWrapper.HookEntity frameworkStub(Member member, RposedAdditionalHookInfo hookInfo) {
        String className = getClassName(member);
        if (!has(className)) {
            return null;
        }
        Class<?> stubClass;
        try {
            stubClass = dexClassLoader.loadClass(className);
        } catch (Throwable throwable) {
            Log.w(Constants.TAG, "failed to load dexmaker opt class: " + className, throwable);
            return null;
        }


        Method mHookMethod = null;
        Method mBackupMethod = null;

        Method[] declaredMethods = stubClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().equals(METHOD_NAME_HOOK)) {
                mHookMethod = method;
            } else if (method.getName().equals(METHOD_NAME_BACKUP)) {
                mBackupMethod = method;
            }
        }

        RposedHelpers.setStaticObjectField(stubClass, FIELD_NAME_METHOD, member);
        RposedHelpers.setStaticObjectField(stubClass, FIELD_NAME_BACKUP_METHOD, mBackupMethod);
        RposedHelpers.setStaticObjectField(stubClass, FIELD_NAME_HOOK_INFO, hookInfo);

        return new HookWrapper.HookEntity(member, mHookMethod, mBackupMethod, false);

    }

    private static void loadResource() throws Throwable {
        File file = RatelEnvironment.ratelDexMakerOptDir();
        if (!file.exists() || !file.canRead()) {
            return;
        }

        dexClassLoader = new DexClassLoader(
                file.getAbsolutePath(),
                RatelEnvironment.APKOptimizedDirectory().getAbsolutePath(),
                RatelRuntime.getOriginApplicationInfo().nativeLibraryDir,
                XposedCompat.getComposedClassLoader(RatelRuntime.getOriginContext().getClassLoader())
        );

        delegateDexMakerOptConfigClass = dexClassLoader.loadClass(descriptorToDot(Constants.dexMakerOptConfigClassId));
        delegateHasMethod = delegateDexMakerOptConfigClass.getMethod("has", String.class);
    }

    private static String primitiveTypeLabel(char typeChar) {
        switch (typeChar) {
            case 'B':
                return "byte";
            case 'C':
                return "char";
            case 'D':
                return "double";
            case 'F':
                return "float";
            case 'I':
                return "int";
            case 'J':
                return "long";
            case 'S':
                return "short";
            case 'V':
                return "void";
            case 'Z':
                return "boolean";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Converts a type descriptor to human-readable "dotted" form.  For
     * example, "Ljava/lang/String;" becomes "java.lang.String", and
     * "[I" becomes "int[]".  Also converts '$' to '.', which means this
     * form can't be converted back to a descriptor.
     * 这段代码是虚拟机里面抠出来的，c++转java
     */
    public static String descriptorToDot(String str) {
        int targetLen = str.length();
        int offset = 0;
        int arrayDepth = 0;


        /* strip leading [s; will be added to end */
        while (targetLen > 1 && str.charAt(offset) == '[') {
            offset++;
            targetLen--;
        }
        arrayDepth = offset;

        if (targetLen == 1) {
            /* primitive type */
            str = primitiveTypeLabel(str.charAt(offset));
            offset = 0;
            targetLen = str.length();
        } else {
            /* account for leading 'L' and trailing ';' */
            if (targetLen >= 2 && str.charAt(offset) == 'L' &&
                    str.charAt(offset + targetLen - 1) == ';') {
                targetLen -= 2;
                offset++;
            }
        }
        StringBuilder newStr = new StringBuilder(targetLen + arrayDepth * 2);
        /* copy class name over */
        int i;
        for (i = 0; i < targetLen; i++) {
            char ch = str.charAt(offset + i);
            newStr.append((ch == '/') ? '.' : ch);
            //do not convert "$" to ".", ClassLoader.loadClass use "$"
            //newStr.append((ch == '/' || ch == '$') ? '.' : ch);
        }
        /* add the appropriate number of brackets for arrays */
        //感觉源代码这里有bug？？？？，arrayDepth会被覆盖，之后的assert应该有问题
        int tempArrayDepth = arrayDepth;
        while (tempArrayDepth-- > 0) {
            newStr.append('[');
            newStr.append(']');
        }
        return new String(newStr);
    }

    private static final String CLASS_NAME_PREFIX = "SandHookerNew";

    private static String getClassName(Member originMethod) {
        return CLASS_NAME_PREFIX + "_" + MD5(originMethod.toString());
    }


}
