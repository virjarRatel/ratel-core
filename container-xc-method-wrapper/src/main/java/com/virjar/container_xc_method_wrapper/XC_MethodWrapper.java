package com.virjar.container_xc_method_wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * 千万小心，这里不要使用XposedHelpers，小心又小心
 */
public class XC_MethodWrapper extends XC_MethodHook {
    //ratel 环境中的xposed对象
    private Object xposedLikeXCMethoObject;

    private Class<?> xposedLikeMethodHookParamClass = null;
    private Method beforeMethodHookMethod = null;
    private Method afterMethodHookMethod = null;

    private static Map<Class, Class> methodHookParamMap = new ConcurrentHashMap<>();
    private static final HashMap<Class, Method> beforeMethodHookMethodMap = new HashMap<>();
    private static final HashMap<Class, Method> afterMethodHookMethodMap = new HashMap<>();

    private static final ConcurrentHashMap<ClassLoader, HashMap<String, Field>> fieldCache = new ConcurrentHashMap<>();

    public XC_MethodWrapper(Object xcMethodLikeObject) {
        this.xposedLikeXCMethoObject = xcMethodLikeObject;
        int priority = getIntField(xcMethodLikeObject, "priority");
        setIntField(this, "priority", priority);

        initMethodHookParamClass(xcMethodLikeObject.getClass());

    }

    private void initMethodHookParamClass(Class xposedLikeXC_MethodHookClass) {
        Class ret = methodHookParamMap.get(xposedLikeXC_MethodHookClass);
        if (ret != null) {
            xposedLikeMethodHookParamClass = ret;
            beforeMethodHookMethod = beforeMethodHookMethodMap.get(xposedLikeXC_MethodHookClass);
            afterMethodHookMethod = afterMethodHookMethodMap.get(xposedLikeXC_MethodHookClass);
            return;
        }

        while (xposedLikeXC_MethodHookClass != null && xposedLikeXC_MethodHookClass != Object.class) {
            for (Method method : xposedLikeXC_MethodHookClass.getDeclaredMethods()) {
                if (method.getParameterTypes().length != 1) {
                    continue;
                }

                if (method.getName().equals("beforeHookedMethod")) {
                    xposedLikeMethodHookParamClass = method.getParameterTypes()[0];
                    methodHookParamMap.put(xposedLikeXC_MethodHookClass, xposedLikeMethodHookParamClass);
                    beforeMethodHookMethod = method;
                    if (!beforeMethodHookMethod.isAccessible()) {
                        beforeMethodHookMethod.setAccessible(true);
                    }
                    beforeMethodHookMethodMap.put(xposedLikeXC_MethodHookClass, beforeMethodHookMethod);
                } else if (method.getName().equals("afterHookedMethod")) {
                    afterMethodHookMethod = method;
                    if (!afterMethodHookMethod.isAccessible()) {
                        afterMethodHookMethod.setAccessible(true);
                    }
                    afterMethodHookMethodMap.put(xposedLikeXC_MethodHookClass, afterMethodHookMethod);
                }
            }
            if (xposedLikeMethodHookParamClass != null && beforeMethodHookMethod != null && afterMethodHookMethod != null) {
                break;
            }
            xposedLikeXC_MethodHookClass = xposedLikeXC_MethodHookClass.getSuperclass();
        }

    }


    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);

        Object xposedLikeMethodHookParam = xposedLikeMethodHookParamClass.newInstance();

        setObjectField(xposedLikeMethodHookParam, "method", param.method);
        setObjectField(xposedLikeMethodHookParam, "thisObject", param.thisObject);
        setObjectField(xposedLikeMethodHookParam, "args", param.args);
        setObjectField(xposedLikeMethodHookParam, "result", param.getResult());
        setObjectField(xposedLikeMethodHookParam, "throwable", param.getThrowable());
        setBooleanField(xposedLikeMethodHookParam, "returnEarly", getBooleanField(param, "returnEarly"));

        beforeMethodHookMethod.invoke(xposedLikeXCMethoObject, xposedLikeMethodHookParam);

        param.args = (Object[]) getObjectField(xposedLikeMethodHookParam, "args");
        setObjectField(param, "result", getObjectField(xposedLikeMethodHookParam, "result"));
        setObjectField(param, "throwable", getObjectField(xposedLikeMethodHookParam, "throwable"));
        setBooleanField(param, "returnEarly", getBooleanField(xposedLikeMethodHookParam, "returnEarly"));

    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);


        Object xposedLikeMethodHookParam = xposedLikeMethodHookParamClass.newInstance();

        setObjectField(xposedLikeMethodHookParam, "method", param.method);
        setObjectField(xposedLikeMethodHookParam, "thisObject", param.thisObject);
        setObjectField(xposedLikeMethodHookParam, "args", param.args);
        setObjectField(xposedLikeMethodHookParam, "result", param.getResult());
        setObjectField(xposedLikeMethodHookParam, "throwable", param.getThrowable());
        setBooleanField(xposedLikeMethodHookParam, "returnEarly", getBooleanField(param, "returnEarly"));

        afterMethodHookMethod.invoke(xposedLikeXCMethoObject, xposedLikeMethodHookParam);

        param.args = (Object[]) getObjectField(xposedLikeMethodHookParam, "args");
        setObjectField(param, "result", getObjectField(xposedLikeMethodHookParam, "result"));
        setObjectField(param, "throwable", getObjectField(xposedLikeMethodHookParam, "throwable"));
        setBooleanField(param, "returnEarly", getBooleanField(xposedLikeMethodHookParam, "returnEarly"));
    }

    public static boolean getBooleanField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getBoolean(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedBridge.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * Sets the value of a {@code boolean} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setBooleanField(Object obj, String fieldName, boolean value) {
        try {
            findField(obj.getClass(), fieldName).setBoolean(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedBridge.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    /**
     * Sets the value of an {@code int} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setIntField(Object obj, String fieldName, int value) {
        try {
            findField(obj.getClass(), fieldName).setInt(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedBridge.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public static int getIntField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getInt(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedBridge.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public static Object getObjectField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).get(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedBridge.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    private static void setObjectField(Object obj, String fieldName, Object value) {
        try {
            findField(obj.getClass(), fieldName).set(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedBridge.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }


    /**
     * Look up a field in a class and set it to accessible.
     *
     * @param clazz     The class which either declares or inherits the field.
     * @param fieldName The field name.
     * @return A reference to the field.
     * @throws NoSuchFieldError In case the field was not found.
     */
    public synchronized static Field findField(Class<?> clazz, String fieldName) {
        String fullFieldName = clazz.getName() + '#' + fieldName;

        HashMap<String, Field> classLoaderFiledCache = fieldCache.get(clazz.getClassLoader());
        if (classLoaderFiledCache == null) {
            classLoaderFiledCache = new HashMap<>();
            fieldCache.put(clazz.getClassLoader(), classLoaderFiledCache);
        }

        if (classLoaderFiledCache.containsKey(fullFieldName)) {
            Field field = classLoaderFiledCache.get(fullFieldName);
            if (field == null)
                throw new NoSuchFieldError(fullFieldName);
            return field;
        }

        try {
            Field field = findFieldRecursiveImpl(clazz, fieldName);
            field.setAccessible(true);
            classLoaderFiledCache.put(fullFieldName, field);
            return field;
        } catch (NoSuchFieldException e) {
            classLoaderFiledCache.put(fullFieldName, null);
            throw new NoSuchFieldError(fullFieldName);
        }
    }

    private static Field findFieldRecursiveImpl(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class))
                    break;

                try {
                    return clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }
}
