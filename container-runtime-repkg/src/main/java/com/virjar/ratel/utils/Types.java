package com.virjar.ratel.utils;


import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by virjar on 2018/5/17.<br>移植自libcore.reflect.Types
 * 该类为Android内部API
 */
public class Types {
    private Types() {
    }

    // Holds a mapping from Java type names to native type codes.
    private static final Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE;

    static {
        PRIMITIVE_TO_SIGNATURE = new HashMap<Class<?>, String>(9);
        PRIMITIVE_TO_SIGNATURE.put(byte.class, "B");
        PRIMITIVE_TO_SIGNATURE.put(char.class, "C");
        PRIMITIVE_TO_SIGNATURE.put(short.class, "S");
        PRIMITIVE_TO_SIGNATURE.put(int.class, "I");
        PRIMITIVE_TO_SIGNATURE.put(long.class, "J");
        PRIMITIVE_TO_SIGNATURE.put(float.class, "F");
        PRIMITIVE_TO_SIGNATURE.put(double.class, "D");
        PRIMITIVE_TO_SIGNATURE.put(void.class, "V");
        PRIMITIVE_TO_SIGNATURE.put(boolean.class, "Z");
    }

    /**
     * Returns the internal name of {@code clazz} (also known as the descriptor).
     */
    public static String getSignature(Class<?> clazz) {
        String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        } else if (clazz.isArray()) {
            return "[" + getSignature(clazz.getComponentType());
        } else {
            return "L" + clazz.getName().replaceAll("\\.", "/") + ";";
        }
    }

    public static String getNativeClassName(Class<?> clazz) {
        String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        } else if (clazz.isArray()) {
            return "[" + getSignature(clazz.getComponentType());
        } else {
            return clazz.getName().replaceAll("\\.", "/");
        }
    }

    /**
     * Returns the names of {@code types} separated by commas.
     */
    public static String toString(Class<?>[] types) {
        if (types.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        appendTypeName(result, types[0]);
        for (int i = 1; i < types.length; i++) {
            result.append(',');
            appendTypeName(result, types[i]);
        }
        return result.toString();
    }

    public static void appendTypeName(StringBuilder out, Class<?> c) {
        int dimensions = 0;
        while (c.isArray()) {
            c = c.getComponentType();
            dimensions++;
        }
        out.append(c.getName());
        for (int d = 0; d < dimensions; d++) {
            out.append("[]");
        }
    }

    /**
     * Appends names of the {@code types} to {@code out} separated by commas.
     */
    public static void appendArrayGenericType(StringBuilder out, Type[] types) {
        if (types.length == 0) {
            return;
        }
        appendGenericType(out, types[0]);
        for (int i = 1; i < types.length; i++) {
            out.append(',');
            appendGenericType(out, types[i]);
        }
    }

    public static void appendGenericType(StringBuilder out, Type type) {
        if (type instanceof TypeVariable) {
            out.append(((TypeVariable) type).getName());
        } else if (type instanceof ParameterizedType) {
            out.append(type.toString());
        } else if (type instanceof GenericArrayType) {
            Type simplified = ((GenericArrayType) type).getGenericComponentType();
            appendGenericType(out, simplified);
            out.append("[]");
        } else if (type instanceof Class) {
            Class c = (Class<?>) type;
            if (c.isArray()) {
                String[] as = c.getName().split("\\[");
                int len = as.length - 1;
                if (as[len].length() > 1) {
                    out.append(as[len].substring(1, as[len].length() - 1));
                } else {
                    char ch = as[len].charAt(0);
                    if (ch == 'I') {
                        out.append("int");
                    } else if (ch == 'B') {
                        out.append("byte");
                    } else if (ch == 'J') {
                        out.append("long");
                    } else if (ch == 'F') {
                        out.append("float");
                    } else if (ch == 'D') {
                        out.append("double");
                    } else if (ch == 'S') {
                        out.append("short");
                    } else if (ch == 'C') {
                        out.append("char");
                    } else if (ch == 'Z') {
                        out.append("boolean");
                    } else if (ch == 'V') {
                        out.append("void");
                    }
                }
                for (int i = 0; i < len; i++) {
                    out.append("[]");
                }
            } else {
                out.append(c.getName());
            }
        }
    }
}
