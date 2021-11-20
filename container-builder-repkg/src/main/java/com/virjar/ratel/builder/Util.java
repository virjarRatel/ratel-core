package com.virjar.ratel.builder;

import com.google.common.base.Defaults;
import com.google.common.base.Splitter;

import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

public class Util {
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

    public static String classNameToSmaliPath(String className) {
        String nativeName = className.replaceAll("\\.", "/");
        return nativeName + ".smali";
    }


    /**
     * Converts a type descriptor to human-readable "dotted" form.  For
     * example, "Ljava/lang/String;" becomes "java.lang.String", and
     * "[I" becomes "int[]".  Also converts '$' to '.', which means this
     * form can't be converted back to a descriptor.
     * 这段代码是虚拟机里面抠出来的，c++转java
     */
    public static String descriptorToDot(String str) {
        if (str == null) {
            return null;
        }
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


    public static void copyLibrary(ZipOutputStream zos, Set<String> supportArch, File apkFile) throws IOException {
        try (ZipFile zipFile = new ZipFile(apkFile)) {
            Enumeration<ZipEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().startsWith("lib/")) {
                    List<String> pathSegment = Splitter.on("/").splitToList(zipEntry.getName());
                    String arch = pathSegment.get(1);
                    if (supportArch.contains(arch) || supportArch.isEmpty()) {
                        ZipEntry soZipEntry = new ZipEntry(zipEntry);
                        soZipEntry.setMethod(ZipEntry.STORED);
                        //否则 Library '%s' is compressed - will not be able to open it directly from apk.\n
                        zos.putNextEntry(soZipEntry);
                        zos.write(IOUtils.toByteArray(zipFile.getInputStream(zipEntry)));
                    } else if (supportArch.contains("armeabi") && arch.equals("armeabi-v7a")) {
                        // 如果没有armeabi-v7a，那么转化到armeabi上面
                        ZipEntry soZipEntry = new ZipEntry("lib/armeabi/" + pathSegment.get(2));
                        zos.putNextEntry(soZipEntry);
                        soZipEntry.setMethod(ZipEntry.STORED);
                        zos.write(IOUtils.toByteArray(zipFile.getInputStream(zipEntry)));
                    }
                }
            }
        }
    }

    private static Set<String> ratelSupportArch = new HashSet<>();

    public static void setupRatelSupportArch(String settings) {
        ratelSupportArch.addAll(Arrays.asList(settings.split(",")));
        if (ratelSupportArch.contains("armeabi-v7a")) {
            ratelSupportArch.add("armeabi");
        }
    }

    /**
     * 永远为false，我们不能随便删除不支持的架构so文件，目前发现"全球骑士购"app，可能在读取某些so资源，然后导致app闪退
     */
    @Deprecated
    public static boolean isRatelUnSupportArch(String zipEntryName) {
        return false;
//        if (!zipEntryName.startsWith("lib/")) {
//            return false;
//        }
//        zipEntryName = zipEntryName.substring("lib/".length());
//        int index = zipEntryName.indexOf("/");
//        if (index <= 0) {
//            return false;
//        }
//        String nowArch = zipEntryName.substring(0, index);
//        return !ratelSupportArch.contains(nowArch);
    }

    public static void copyAssets(ZipOutputStream zos, File from, String name) throws IOException {
        zos.putNextEntry(new ZipEntry("assets/" + name));
        try (FileInputStream fileInputStream = new FileInputStream(from)) {
            IOUtils.copy(fileInputStream, zos);
        }
    }

    public static final Pattern classesIndexPattern = Pattern.compile("classes(\\d+)\\.dex");
    private static Random random = new Random();

    public static String randomChars(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(nextChar());
        }

        return stringBuilder.toString();
    }


    public static char nextChar() {
        int aAStart = random.nextInt(2) % 2 == 0 ? 65 : 97; //取得大写字母还是小写字母
        return (char) (aAStart + random.nextInt(26));
    }


    /**
     * Maps names of primitives to their corresponding primitive {@code Class}es.
     */
    private static final Map<String, Class<?>> namePrimitiveMap = new HashMap<>();

    static {
        namePrimitiveMap.put("boolean", Boolean.TYPE);
        namePrimitiveMap.put("byte", Byte.TYPE);
        namePrimitiveMap.put("char", Character.TYPE);
        namePrimitiveMap.put("short", Short.TYPE);
        namePrimitiveMap.put("int", Integer.TYPE);
        namePrimitiveMap.put("long", Long.TYPE);
        namePrimitiveMap.put("double", Double.TYPE);
        namePrimitiveMap.put("float", Float.TYPE);
        namePrimitiveMap.put("void", Void.TYPE);
    }

    /**
     * Maps primitive {@code Class}es to their corresponding wrapper {@code Class}.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    /**
     * Maps a primitive class name to its corresponding abbreviation used in array class names.
     */
    private static final Map<String, String> abbreviationMap;

    /**
     * Maps an abbreviation used in array class names to corresponding primitive class name.
     */
    private static final Map<String, String> reverseAbbreviationMap;

    /**
     * Feed abbreviation maps
     */
    static {
        final Map<String, String> m = new HashMap<>();
        m.put("int", "I");
        m.put("boolean", "Z");
        m.put("float", "F");
        m.put("long", "J");
        m.put("short", "S");
        m.put("byte", "B");
        m.put("double", "D");
        m.put("char", "C");
        final Map<String, String> r = new HashMap<>();
        for (final Map.Entry<String, String> e : m.entrySet()) {
            r.put(e.getValue(), e.getKey());
        }
        abbreviationMap = Collections.unmodifiableMap(m);
        reverseAbbreviationMap = Collections.unmodifiableMap(r);
    }

    /**
     * Maps wrapper {@code Class}es to their corresponding primitive types.
     */
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();

    static {
        for (final Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperMap.entrySet()) {
            final Class<?> primitiveClass = entry.getKey();
            final Class<?> wrapperClass = entry.getValue();
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        if (cls.isPrimitive()) {
            return cls;
        }
        return wrapperPrimitiveMap.get(cls);
    }

    @SuppressWarnings("unchecked")
    public static <T> T primitiveCast(String value, Class<T> type) {

        if (value == null) {
            if (type.isPrimitive()) {
                return Defaults.defaultValue(type);
            }
            if (wrapperPrimitiveMap.containsKey(type)) {
                return (T) Defaults.defaultValue(wrapperToPrimitive(type));
            }
            return null;
        }

        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        if (type.isAssignableFrom(String.class)) {
            return (T) value;
        }

        Class<?> primitiveType = wrapperToPrimitive(type);
        if (primitiveType == null) {
            return null;
        }
        if (type == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == char.class) {
            return (T) Character.valueOf(value.charAt(0));
        } else if (type == byte.class) {
            return (T) Byte.valueOf(value);
        } else if (type == short.class) {
            return (T) Short.valueOf(value);
        } else if (type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == long.class) {
            return (T) Long.valueOf(value);
        } else if (type == float.class) {
            return (T) Float.valueOf(value);
        } else if (type == double.class) {
            return (T) Double.valueOf(value);
        } else {
            return null;
        }
    }

    public static boolean isCertificateOrMANIFEST(String zipEntryName) {
        if (!zipEntryName.startsWith("META-INF/")) {
            return false;
        }
        // META-INF/MANIFEST.MF
        // META-INF/*.RSA
        // META-INF/*.DSA
        // 请注意，META-INF 不能暴力删除，根据java规范，spi配置也会存在于META-INF中

        if (zipEntryName.endsWith(".RSA") || zipEntryName.endsWith(".DSA")) {
            return true;
        }
        return zipEntryName.equals("META-INF/MANIFEST.MF");
    }
}
