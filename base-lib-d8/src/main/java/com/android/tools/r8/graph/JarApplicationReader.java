// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexMethodHandle.MethodHandleType;
import com.android.tools.r8.utils.InternalOptions;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.Type;

/**
 * Common structures used while reading in a Java application from jar files.
 *
 * The primary use of this class is to canonicalize dex items during read.
 * The addition of classes to the builder also takes place through this class.
 * It does not currently support multithreaded reading.
 */
public class JarApplicationReader {

  public final InternalOptions options;
  private final ConcurrentHashMap<String, Type> asmObjectTypeCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Type> asmTypeCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, DexString> stringCache = new ConcurrentHashMap<>();

  public JarApplicationReader(InternalOptions options) {
    this.options = options;
  }

  public Type getAsmObjectType(String name) {
    return asmObjectTypeCache.computeIfAbsent(name, (key) -> Type.getObjectType(key));
  }

  public Type getAsmType(String name) {
    return asmTypeCache.computeIfAbsent(name, (key) -> Type.getType(key));
  }

  public DexItemFactory getFactory() {
    return options.itemFactory;
  }

  public DexString getString(String string) {
    return stringCache.computeIfAbsent(string, options.itemFactory::createString);
  }

  public DexType getType(Type type) {
    return getTypeFromDescriptor(type.getDescriptor());
  }

  public DexType getTypeFromName(String name) {
    assert isValidInternalName(name);
    return getType(getAsmObjectType(name));
  }

  public DexType getTypeFromDescriptor(String desc) {
    assert isValidDescriptor(desc);
    return options.itemFactory.createType(getString(desc));
  }

  public DexTypeList getTypeListFromNames(String[] names) {
    if (names.length == 0) {
      return DexTypeList.empty();
    }
    DexType[] types = new DexType[names.length];
    for (int i = 0; i < names.length; i++) {
      types[i] = getTypeFromName(names[i]);
    }
    return new DexTypeList(types);
  }

  public DexTypeList getTypeListFromDescriptors(String[] descriptors) {
    if (descriptors.length == 0) {
      return DexTypeList.empty();
    }
    DexType[] types = new DexType[descriptors.length];
    for (int i = 0; i < descriptors.length; i++) {
      types[i] = getTypeFromDescriptor(descriptors[i]);
    }
    return new DexTypeList(types);
  }

  public DexField getField(String owner, String name, String desc) {
    return getField(getTypeFromName(owner), name, desc);
  }

  public DexField getField(DexType owner, String name, String desc) {
    return options.itemFactory.createField(owner, getTypeFromDescriptor(desc), getString(name));
  }

  public DexMethod getMethod(String owner, String name, String desc) {
    return getMethod(getTypeFromName(owner), name, desc);
  }

  public DexMethod getMethod(DexType owner, String name, String desc) {
    return options.itemFactory.createMethod(owner, getProto(desc), getString(name));
  }

  public DexCallSite getCallSite(String methodName, String methodProto,
      DexMethodHandle bootstrapMethod, List<DexValue> bootstrapArgs) {
    return options.itemFactory.createCallSite(
        getString(methodName), getProto(methodProto), bootstrapMethod, bootstrapArgs);
  }

  public DexMethodHandle getMethodHandle(
      MethodHandleType type,
      Descriptor<? extends DexItem, ? extends Descriptor<?, ?>> fieldOrMethod,
      boolean isInterface) {
    return options.itemFactory.createMethodHandle(type, fieldOrMethod, isInterface);
  }

  public DexProto getProto(String desc) {
    assert isValidDescriptor(desc);
    String returnTypeDescriptor = getReturnTypeDescriptor(desc);
    String[] argumentDescriptors = getArgumentTypeDescriptors(desc);
    StringBuilder shortyDescriptor = new StringBuilder();
    shortyDescriptor.append(getShortyDescriptor(returnTypeDescriptor));
    for (int i = 0; i < argumentDescriptors.length; i++) {
      shortyDescriptor.append(getShortyDescriptor(argumentDescriptors[i]));
    }
    DexProto proto =
        options.itemFactory.createProto(
            getTypeFromDescriptor(returnTypeDescriptor),
            getTypeListFromDescriptors(argumentDescriptors),
            getString(shortyDescriptor.toString()));
    return proto;
  }

  private static String getShortyDescriptor(String descriptor) {
    if (descriptor.length() == 1) {
      return descriptor;
    }
    assert descriptor.charAt(0) == 'L' || descriptor.charAt(0) == '[';
    return "L";
  }

  private boolean isValidDescriptor(String desc) {
    return getAsmType(desc).getDescriptor().equals(desc);
  }

  private boolean isValidInternalName(String name) {
    return getAsmObjectType(name).getInternalName().equals(name);
  }

  public Type getReturnType(final String methodDescriptor) {
    return getAsmType(getReturnTypeDescriptor(methodDescriptor));
  }

  private static String getReturnTypeDescriptor(final String methodDescriptor) {
    assert methodDescriptor.indexOf(')') != -1;
    return methodDescriptor.substring(methodDescriptor.indexOf(')') + 1);
  }

  public static int getArgumentCount(final String methodDescriptor) {
    int charIdx = 1;
    char c;
    int argCount = 0;
    while ((c = methodDescriptor.charAt(charIdx++)) != ')') {
      if (c == 'L') {
        while (methodDescriptor.charAt(charIdx++) != ';');
        argCount++;
      } else if (c != '[') {
        argCount++;
      }
    }
    return argCount;
  }

  public Type[] getArgumentTypes(final String methodDescriptor) {
    String[] argDescriptors = getArgumentTypeDescriptors(methodDescriptor);
    Type[] args = new Type[argDescriptors.length];
    int argIdx = 0;
    for (String argDescriptor : argDescriptors) {
      args[argIdx++] = getAsmType(argDescriptor);
    }
    return args;
  }

  private static String[] getArgumentTypeDescriptors(final String methodDescriptor) {
    String[] argDescriptors = new String[getArgumentCount(methodDescriptor)];
    int charIdx = 1;
    char c;
    int argIdx = 0;
    int startType;
    while ((c = methodDescriptor.charAt(charIdx)) != ')') {
      switch (c) {
        case 'V':
          throw new Unreachable();
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
        case 'F':
        case 'J':
        case 'D':
          argDescriptors[argIdx++] = Character.toString(c);
          break;
        case '[':
          startType = charIdx;
          while (methodDescriptor.charAt(++charIdx) == '[') {
          }
          if (methodDescriptor.charAt(charIdx) == 'L') {
            while (methodDescriptor.charAt(++charIdx) != ';');
          }
          argDescriptors[argIdx++] = methodDescriptor.substring(startType, charIdx + 1);
          break;
        case 'L':
          startType = charIdx;
          while (methodDescriptor.charAt(++charIdx) != ';');
          argDescriptors[argIdx++] = methodDescriptor.substring(startType, charIdx + 1);
          break;
        default:
          throw new Unreachable();
      }
      charIdx++;
    }
    return argDescriptors;
  }
}
