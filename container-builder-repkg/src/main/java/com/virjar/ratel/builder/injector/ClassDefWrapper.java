package com.virjar.ratel.builder.injector;

import org.apache.commons.lang3.StringUtils;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ljh102 on 2017/1/29.
 */
public class ClassDefWrapper implements ClassDef {

    private ClassDef classDef;
    private boolean changeAccessFlags = false;
    private int accessFlags;
    private List<Method> virtualMethods = new ArrayList<>();
    private List<Method> directMethods = new ArrayList<>();
    private String changeSupperClass;

    public ClassDefWrapper(ClassDef classDef) {
        this.classDef = classDef;
        for (Method method : classDef.getVirtualMethods()) {
            virtualMethods.add(method);
        }
        for (Method method : classDef.getDirectMethods()) {
            directMethods.add(method);
        }
    }

    public void setAccessFlags(int accessFlags) {
        this.accessFlags = accessFlags;
        changeAccessFlags = true;
    }


    @Override
    public String getType() {
        return classDef.getType();
    }

    @Override
    public int compareTo(CharSequence o) {
        return classDef.compareTo(o);
    }

    @Override
    public int getAccessFlags() {
        if (changeAccessFlags)
            return accessFlags;

        return classDef.getAccessFlags();
    }


    public void setSupperClass(String supperClass) {
        this.changeSupperClass = supperClass;
    }

    @Override
    public String getSuperclass() {
        if (!StringUtils.isEmpty(changeSupperClass)) {
            return changeSupperClass;
        }
        return classDef.getSuperclass();
    }


    @Override
    public List<String> getInterfaces() {
        return classDef.getInterfaces();
    }

    @Override
    public String getSourceFile() {
        return classDef.getSourceFile();
    }


    @Override
    public Set<? extends Annotation> getAnnotations() {
        return classDef.getAnnotations();
    }


    @Override
    public Iterable<? extends Field> getStaticFields() {
        return classDef.getStaticFields();
    }


    @Override
    public Iterable<? extends Field> getInstanceFields() {
        return classDef.getInstanceFields();
    }


    @Override
    public Iterable<? extends Field> getFields() {
        return classDef.getFields();
    }


    @Override
    public Iterable<? extends Method> getDirectMethods() {
        return directMethods;
    }


    public List<Method> getOriginDirectMethods() {
        return directMethods;
    }


    @Override
    public Iterable<? extends Method> getVirtualMethods() {
        return virtualMethods;
    }


    @Override
    public Iterable<? extends Method> getMethods() {
        return classDef.getMethods();
    }

    @Override
    public int length() {
        return classDef.length();
    }

    @Override
    public char charAt(int index) {
        return classDef.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return classDef.subSequence(start, end);
    }

    public void addMethod(Method method) {
        virtualMethods.add(method);
    }

    public void replaceVirtualMethod(Method method) {
        for (int i = 0; i < virtualMethods.size(); i++) {
            if (virtualMethods.get(i).getName().equals(method.getName())) {
                virtualMethods.set(i, method);
                return;
            }
        }
        // 没找到则直接添加
        addMethod(method);
    }

    public Method getVirtualMethod(String name) {
        for (Method method : virtualMethods) {
            if (method.getName().equals(name))
                return method;
        }
        return null;
    }

    @Override
    public void validateReference() throws InvalidReferenceException {
        classDef.validateReference();
    }
}