package com.virjar.ratel.api.extension.socketmonitor;

import java.lang.reflect.Method;

public class MethodUtils {
    public static boolean isMethodSame(Method method1, Method method2) {
        if (!method1.getName().equals(method2.getName())) {
            return false;
        }
        if (!method1.getReturnType().equals(method2.getReturnType())) {
            return false;
        }
        return getParametersString(method1.getParameterTypes()).equals(getParametersString(method2.getParameterTypes()));
    }

    private static String getParametersString(Class<?>... clazzes) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (Class<?> clazz : clazzes) {
            if (first)
                first = false;
            else
                sb.append(",");

            if (clazz != null)
                sb.append(clazz.getCanonicalName());
            else
                sb.append("null");
        }
        sb.append(")");
        return sb.toString();
    }
}
