package com.virjar.ratel.api.inspect;


import android.os.Bundle;

import com.virjar.ratel.api.rposed.FreeReflection;
import com.virjar.ratel.api.rposed.RposedHelpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import external.org.apache.commons.lang3.ClassUtils;

/**
 * Created by virjar on 2018/9/25.<br>
 * 强制的，将一个对象的内部属性dump出来，忽略java pojo的getter规范，请注意这个功能是为了抓取使用，因为该转换不是幂等的，无法还原
 */
public class ForceFiledViewer {
    /**
     * build a plain object from any flex object,the output is a view ,witch access all private and public field,ignore getter on input object
     *
     * @param input any object
     * @return a view,just contain string,number,list,map;or it`s combination
     */
    public static Object toView(Object input) {
        return toView(input, new HashSet<>(), false);
    }

    public static Object toView(Object input, boolean withObjType) {
        return toView(input, new HashSet<>(), withObjType);
    }

    private static Object trimView(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Map && ((Map) input).size() == 0) {
            return null;
        }
        if (input.getClass().isArray() && Array.getLength(input) == 0) {
            return null;
        }
        if (input instanceof Collection && ((Collection) input).size() == 0) {
            return null;
        }
        return input;
    }

    private static Object toView(Object input, Set<Object> accessedObjects, boolean withObjType) {
        try {
            if (input == null) {
                return Collections.emptyMap();
            }
            if (skipTranslatePackage.isSubPackage(input.getClass().getName())) {
                return input;
            }
            return toViewInternal(input, accessedObjects, withObjType);
        } catch (Throwable throwable) {
            return null;
        }
    }

    private static Object toViewInternal(Object input, Set<Object> accessedObjects, boolean withObjType) {
        if (input == null) {
            return null;
        }
        Class<?> inputClass = input.getClass();
        // primary type,
        if (inputClass.isPrimitive() || ClassUtils.wrapperToPrimitive(inputClass) != null) {
            return input;
        }
        if (input instanceof CharSequence || input instanceof Number) {
            return input;
        }
        if (input instanceof Date) {
            return ((Date) input).getTime();
        }
        if (input instanceof byte[]) {
            return input;
        }

        if (accessedObjects.contains(input)) {
            return null;
        }
        //create a copy ,we just avoid cycle reference,so a object can appear on different subtree
        accessedObjects = new HashSet<>(accessedObjects);
        accessedObjects.add(input);

        if (input instanceof Collection) {
            List<Object> subRet = Lists.newArrayListWithCapacity(((Collection) input).size());
            boolean hint = false;
            for (Object o1 : (Collection) input) {
                Object view = trimView(toView(o1, accessedObjects, withObjType));
                if (view != null) {
                    hint = true;
                }
                subRet.add(view);
            }
            if (!hint) {
                subRet.clear();
            }
            return subRet;
        }

        if (input.getClass().isArray()) {
            List<Object> subRet = Lists.newArrayListWithCapacity(Array.getLength(input));
            boolean hint = false;
            for (int i = 0; i < Array.getLength(input); i++) {
                Object o1 = Array.get(input, i);
                Object view = trimView(toView(o1, accessedObjects, withObjType));
                if (view != null) {
                    hint = true;
                }
                subRet.add(view);
            }
            if (!hint) {
                subRet.clear();
            }
            return subRet;
        }


        if (input instanceof Map) {
            Map map = (Map) input;
            Map<String, Object> subRet = new TreeMap<>();
            Set set = map.entrySet();
            for (Object entry : set) {
                if (!(entry instanceof Map.Entry)) {
                    continue;
                }
                Map.Entry entry1 = (Map.Entry) entry;
                Object key = entry1.getKey();
                if (key == null) {
                    continue;
                }
                Object value = entry1.getValue();
                Object view = trimView(toView(value, accessedObjects, withObjType));
                if (view != null) {
                    if (key instanceof CharSequence || key.getClass().isPrimitive() || ClassUtils.wrapperToPrimitive(key.getClass()) != null) {
                        subRet.put(key.toString(), view);
                    } else if (key instanceof Class) {
                        subRet.put(((Class) key).getName(), view);
                    } else {
                        Map<String, Object> container = new HashMap<>();
                        container.put("key", trimView(toView(value, accessedObjects, withObjType)));
                        container.put("value", value);
                        subRet.put(key.getClass() + "@" + key.hashCode(), container);
                    }


                }
            }
            return subRet;
        }
        String className = input.getClass().getName();

        Function function = specialViewer.get(className);
        if (function != null) {
            //某些特殊的dump规则，不如jodaTime，dump内容没有意义
            return function.apply(input);
        }

        if (skipDumpPackage.isSubPackage(className) && !forceDumpPackage.isSubPackage(className)) {
            //框架内部对象，不抽取，这可能导致递归过深，而且没有意义
            return null;
        }
        if (Bundle.class.isAssignableFrom(input.getClass())) {
            Bundle bundle = (Bundle) input;
            //触发反序列化
            bundle.get("test");
        }

        Map<String, Object> ret = new TreeMap<>();

        Map<String, Field> fields = classFileds(input.getClass());
        for (Map.Entry<String, Field> field : fields.entrySet()) {
            Object o = null;
            try {
                o = field.getValue().get(input);
            } catch (IllegalAccessException e) {
                //ignore
            }
            if (o == null) {
                if (withObjType) {
                    Map<String, Object> container = new HashMap<>();
                    container.put("type", field.getValue().getType());
                    container.put("value", null);
                    ret.put(field.getKey(), container);
                }
                continue;
            }
            Object view = trimView(toView(o, accessedObjects, withObjType));
            if (view != null) {
                if (withObjType) {
                    Map<String, Object> container = new HashMap<>();
                    container.put("type", o.getClass());
                    container.put("value", view);
                    container.put("hash", o.hashCode());
                    ret.put(field.getKey(), container);
                } else {
                    ret.put(field.getKey(), view);
                }
            } else if (withObjType) {
                Map<String, Object> container = new HashMap<>();
                container.put("type", field.getValue().getType());
                container.put("value", field.getValue().toString());
                container.put("hash", o.hashCode());
                ret.put(field.getKey(), container);
            }
        }
        return ret;
    }


    public static Map<String, Field> classFileds(Class clazz) {
        if (clazz == Object.class) {
            return Collections.emptyMap();
        }
        Map<String, Field> fields = fieldCache.get(clazz);
        if (fields != null) {
            return fields;
        }
        Class<? extends Annotation> gsonSerializeNameAnnotation = null;
        try {
            gsonSerializeNameAnnotation = (Class<? extends Annotation>) clazz.getClassLoader().loadClass("com.google.gson.annotations.SerializedName");
        } catch (Throwable throwable) {
            //ignore
        }
        synchronized (clazz) {
            fields = fieldCache.get(clazz);
            if (fields != null) {
                return fields;
            }

            fields = new HashMap<>();
            fields.putAll(classFileds(clazz.getSuperclass()));
            for (Field next : FreeReflection.getDeclaredField(clazz)) {
                if (Modifier.isStatic(next.getModifiers())) {
                    continue;
                }
                if (next.isSynthetic()) {
                    continue;
                }
                if (!next.isAccessible()) {
                    next.setAccessible(true);
                }
                String candidateName = next.getName();
                try {
                    if (gsonSerializeNameAnnotation != null && next.isAnnotationPresent(gsonSerializeNameAnnotation)) {
                        Annotation annotation = next.getAnnotation(gsonSerializeNameAnnotation);
                        String gsonName = (String) RposedHelpers.callMethod(annotation, "value");
                        if (gsonName != null) {
                            candidateName = gsonName;
                        }
                    }
                } catch (Throwable throwable) {
                    //ignore
                }

                if (fields.containsKey(candidateName)) {
                    candidateName = clazz.getName() + "." + candidateName;
                }
                int retryIndex = 0;
                while (fields.containsKey(candidateName)) {
                    retryIndex++;
                    candidateName = candidateName + retryIndex;
                }
                fields.put(candidateName, next);
            }

            fieldCache.put(clazz, fields);
        }
        return fields;
    }

    private static final ConcurrentMap<Class<?>, Map<String, Field>> fieldCache = new ConcurrentHashMap<>();
    private static PackageTrie forceDumpPackage = new PackageTrie();
    private static PackageTrie skipDumpPackage = new PackageTrie();
    private static PackageTrie skipTranslatePackage = new PackageTrie();
    private static Map<String, Function> specialViewer = new ConcurrentHashMap<>();

    public static void addForceViewConfig(String basePackage) {
        forceDumpPackage.addToTree(basePackage);
    }

    public static void addSkipViewConfig(String basePackage) {
        skipDumpPackage.addToTree(basePackage);
    }

    public static void addSKipTranslateConfig(String basePackage) {
        skipTranslatePackage.addToTree(basePackage);
    }

    public static void addSpecialViewer(String clazz, Function transformFunction) {
        specialViewer.put(clazz, transformFunction);
    }

    public interface Function {
        Object apply(Object o);
    }

    private static Function ToString = o -> {
        if (o == null) {
            return null;
        }
        return o.toString();
    };


    static {
        addSkipViewConfig("android");
        addSkipViewConfig("com.android");
        addSkipViewConfig("java");
        addSkipViewConfig("dalvik");

        addForceViewConfig("android.os.Bundle");
        addForceViewConfig("android.os.BaseBundle");
        addForceViewConfig("android.util.ArrayMap");
        addForceViewConfig("android.util.SparseArray");
        addForceViewConfig("android.util.ArraySet");
        addForceViewConfig("android.util.SparseBooleanArray");
        addForceViewConfig("android.util.SparseIntArray");
        addForceViewConfig("android.util.SparseLongArray");
        addForceViewConfig("android.util.StateSet");
        addForceViewConfig("android.content.Intent");
        addForceViewConfig("android.content.ClipData");
        addForceViewConfig("android.content.ClipData#Item");
        addForceViewConfig("android.content.ClipData.Item");
        addForceViewConfig("android.content.ComponentName");
        addForceViewConfig("android.content.Entity");
        addForceViewConfig("android.content.ContentValues");

        addSKipTranslateConfig("com.alibaba.fastjson");
        addSKipTranslateConfig("org.json");
        addSKipTranslateConfig("com.google.gson");

        addSpecialViewer("org.joda.time.DateTime", ToString);
    }


}
