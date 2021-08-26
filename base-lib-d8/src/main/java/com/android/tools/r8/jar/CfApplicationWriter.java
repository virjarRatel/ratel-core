// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.jar;

import static com.android.tools.r8.utils.InternalOptions.ASM_VERSION;

import com.android.tools.r8.ByteDataView;
import com.android.tools.r8.ClassFileConsumer;
import com.android.tools.r8.dex.ApplicationWriter;
import com.android.tools.r8.dex.Marker;
import com.android.tools.r8.errors.Unimplemented;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationElement;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexEncodedAnnotation;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueAnnotation;
import com.android.tools.r8.graph.DexValue.DexValueArray;
import com.android.tools.r8.graph.DexValue.DexValueEnum;
import com.android.tools.r8.graph.DexValue.DexValueField;
import com.android.tools.r8.graph.DexValue.DexValueInt;
import com.android.tools.r8.graph.DexValue.DexValueMethod;
import com.android.tools.r8.graph.DexValue.DexValueMethodHandle;
import com.android.tools.r8.graph.DexValue.DexValueMethodType;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.graph.DexValue.DexValueType;
import com.android.tools.r8.graph.DexValue.UnknownDexValue;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.InnerClassAttribute;
import com.android.tools.r8.graph.NestMemberClassAttribute;
import com.android.tools.r8.graph.ParameterAnnotationsList;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.naming.ProguardMapSupplier;
import com.android.tools.r8.utils.ExceptionUtils;
import com.android.tools.r8.utils.InternalOptions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class CfApplicationWriter {
  private static final boolean RUN_VERIFIER = false;
  private static final boolean PRINT_CF = false;

  // First item inserted into the constant pool is the marker string which generates an UTF8 to
  // pool index #1 and a String entry to #2, referencing #1.
  public static final int MARKER_STRING_CONSTANT_POOL_INDEX = 2;

  private final DexApplication application;
  private final AppView<?> appView;
  private final GraphLense graphLense;
  private final NamingLens namingLens;
  private final InternalOptions options;
  private final Marker marker;

  public final ProguardMapSupplier proguardMapSupplier;

  public CfApplicationWriter(
      DexApplication application,
      AppView<?> appView,
      InternalOptions options,
      Marker marker,
      GraphLense graphLense,
      NamingLens namingLens,
      ProguardMapSupplier proguardMapSupplier) {
    this.application = application;
    this.appView = appView;
    this.graphLense = graphLense;
    this.namingLens = namingLens;
    this.options = options;
    assert marker != null;
    this.marker = marker;
    this.proguardMapSupplier = proguardMapSupplier;
  }

  public void write(ClassFileConsumer consumer, ExecutorService executor) {
    application.timing.begin("CfApplicationWriter.write");
    try {
      writeApplication(consumer, executor);
    } finally {
      application.timing.end();
    }
  }

  private void writeApplication(ClassFileConsumer consumer, ExecutorService executor) {
    ProguardMapSupplier.ProguardMapAndId proguardMapAndId = null;
    if (proguardMapSupplier != null && options.proguardMapConsumer != null) {
      proguardMapAndId = proguardMapSupplier.getProguardMapAndId();
      if (proguardMapAndId != null) {
        marker.setPgMapId(proguardMapAndId.id);
      }
    }
    String markerString = marker.toString();
    for (DexProgramClass clazz : application.classes()) {
      if (clazz.getSynthesizedFrom().isEmpty() || options.isDesugaredLibraryCompilation()) {
        writeClass(clazz, consumer, markerString);
      } else {
        throw new Unimplemented("No support for synthetics in the Java bytecode backend.");
      }
    }
    ApplicationWriter.supplyAdditionalConsumers(
        application,
        appView,
        graphLense,
        namingLens,
        options,
        proguardMapAndId == null ? null : proguardMapAndId.map);
  }

  private void writeClass(DexProgramClass clazz, ClassFileConsumer consumer, String markerString) {
    ClassWriter writer = new ClassWriter(0);
    int markerStringPoolIndex = writer.newConst(markerString);
    assert markerStringPoolIndex == MARKER_STRING_CONSTANT_POOL_INDEX;
    writer.visitSource(clazz.sourceFile != null ? clazz.sourceFile.toString() : null, null);
    int version = getClassFileVersion(clazz);
    int access = clazz.accessFlags.getAsCfAccessFlags();
    String desc = namingLens.lookupDescriptor(clazz.type).toString();
    String name = namingLens.lookupInternalName(clazz.type);
    String signature = getSignature(clazz.annotations);
    String superName =
        clazz.type == options.itemFactory.objectType
            ? null
            : namingLens.lookupInternalName(clazz.superType);
    String[] interfaces = new String[clazz.interfaces.values.length];
    for (int i = 0; i < clazz.interfaces.values.length; i++) {
      interfaces[i] = namingLens.lookupInternalName(clazz.interfaces.values[i]);
    }
    writer.visit(version, access, name, signature, superName, interfaces);
    writeAnnotations(writer::visitAnnotation, clazz.annotations.annotations);
    ImmutableMap<DexString, DexValue> defaults = getAnnotationDefaults(clazz.annotations);

    if (clazz.getEnclosingMethod() != null) {
      clazz.getEnclosingMethod().write(writer, namingLens);
    }

    if (clazz.getNestHostClassAttribute() != null) {
      clazz.getNestHostClassAttribute().write(writer, namingLens);
    }

    for (NestMemberClassAttribute entry : clazz.getNestMembersClassAttributes()) {
      entry.write(writer, namingLens);
      assert clazz.getNestHostClassAttribute() == null
          : "A nest host cannot also be a nest member.";
    }

    for (InnerClassAttribute entry : clazz.getInnerClasses()) {
      entry.write(writer, namingLens, options);
    }

    for (DexEncodedField field : clazz.staticFields()) {
      writeField(field, writer);
    }
    for (DexEncodedField field : clazz.instanceFields()) {
      writeField(field, writer);
    }
    for (DexEncodedMethod method : clazz.directMethods()) {
      writeMethod(method, writer, defaults, version);
    }
    for (DexEncodedMethod method : clazz.virtualMethods()) {
      writeMethod(method, writer, defaults, version);
    }
    writer.visitEnd();

    byte[] result = writer.toByteArray();
    if (PRINT_CF) {
      System.out.print(printCf(result));
      System.out.flush();
    }
    if (RUN_VERIFIER) {
      // Generally, this will fail with ClassNotFoundException,
      // so don't assert that verifyCf() returns true.
      verifyCf(result);
    }
    ExceptionUtils.withConsumeResourceHandler(
        options.reporter, handler -> consumer.accept(ByteDataView.of(result), desc, handler));
  }

  private int getClassFileVersion(DexEncodedMethod method) {
    if (!method.hasClassFileVersion()) {
      // In this case bridges have been introduced for the Cf back-end,
      // which do not have class file version.
      assert options.testing.enableForceNestBasedAccessDesugaringForTest
          || options.isDesugaredLibraryCompilation();
      return 0;
    }
    return method.getClassFileVersion();
  }

  private int getClassFileVersion(DexProgramClass clazz) {
    int version = clazz.hasClassFileVersion() ? clazz.getInitialClassFileVersion() : 50;
    for (DexEncodedMethod method : clazz.directMethods()) {
      version = Math.max(version, getClassFileVersion(method));
    }
    for (DexEncodedMethod method : clazz.virtualMethods()) {
      version = Math.max(version, getClassFileVersion(method));
    }
    return version;
  }

  private DexValue getSystemAnnotationValue(DexAnnotationSet annotations, DexType type) {
    DexAnnotation annotation = annotations.getFirstMatching(type);
    if (annotation == null) {
      return null;
    }
    assert annotation.visibility == DexAnnotation.VISIBILITY_SYSTEM;
    DexEncodedAnnotation encodedAnnotation = annotation.annotation;
    assert encodedAnnotation.elements.length == 1;
    return encodedAnnotation.elements[0].value;
  }

  private String getSignature(DexAnnotationSet annotations) {
    DexValueArray value =
        (DexValueArray)
            getSystemAnnotationValue(annotations, application.dexItemFactory.annotationSignature);
    if (value == null) {
      return null;
    }
    // Signature has already been minified by ClassNameMinifier.renameTypesInGenericSignatures().
    DexValue[] parts = value.getValues();
    StringBuilder res = new StringBuilder();
    for (DexValue part : parts) {
      res.append(((DexValueString) part).getValue().toString());
    }
    return res.toString();
  }

  private ImmutableMap<DexString, DexValue> getAnnotationDefaults(DexAnnotationSet annotations) {
    DexValueAnnotation value =
        (DexValueAnnotation)
            getSystemAnnotationValue(annotations, application.dexItemFactory.annotationDefault);
    if (value == null) {
      return ImmutableMap.of();
    }
    DexEncodedAnnotation annotation = value.value;
    Builder<DexString, DexValue> builder = ImmutableMap.builder();
    for (DexAnnotationElement element : annotation.elements) {
      builder.put(element.name, element.value);
    }
    return builder.build();
  }

  private String[] getExceptions(DexAnnotationSet annotations) {
    DexValueArray value =
        (DexValueArray)
            getSystemAnnotationValue(annotations, application.dexItemFactory.annotationThrows);
    if (value == null) {
      return null;
    }
    DexValue[] values = value.getValues();
    String[] res = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      res[i] = namingLens.lookupInternalName(((DexValueType) values[i]).value);
    }
    return res;
  }

  private Object getStaticValue(DexEncodedField field) {
    if (!field.accessFlags.isStatic() || !field.hasExplicitStaticValue()) {
      return null;
    }
    return field.getStaticValue().asAsmEncodedObject();
  }

  private void writeField(DexEncodedField field, ClassWriter writer) {
    int access = field.accessFlags.getAsCfAccessFlags();
    String name = namingLens.lookupName(field.field).toString();
    String desc = namingLens.lookupDescriptor(field.field.type).toString();
    String signature = getSignature(field.annotations);
    Object value = getStaticValue(field);
    FieldVisitor visitor = writer.visitField(access, name, desc, signature, value);
    writeAnnotations(visitor::visitAnnotation, field.annotations.annotations);
    visitor.visitEnd();
  }

  private void writeMethod(
      DexEncodedMethod method,
      ClassWriter writer,
      ImmutableMap<DexString, DexValue> defaults,
      int classFileVersion) {
    int access = method.accessFlags.getAsCfAccessFlags();
    String name = namingLens.lookupName(method.method).toString();
    String desc = method.descriptor(namingLens);
    String signature = getSignature(method.annotations);
    String[] exceptions = getExceptions(method.annotations);
    MethodVisitor visitor = writer.visitMethod(access, name, desc, signature, exceptions);
    if (defaults.containsKey(method.method.name)) {
      AnnotationVisitor defaultVisitor = visitor.visitAnnotationDefault();
      if (defaultVisitor != null) {
        writeAnnotationElement(defaultVisitor, null, defaults.get(method.method.name));
        defaultVisitor.visitEnd();
      }
    }
    writeMethodParametersAnnotation(visitor, method.annotations.annotations);
    writeAnnotations(visitor::visitAnnotation, method.annotations.annotations);
    writeParameterAnnotations(visitor, method.parameterAnnotationsList);
    if (!method.shouldNotHaveCode()) {
      writeCode(method, visitor, classFileVersion);
    }
    visitor.visitEnd();
  }

  private void writeMethodParametersAnnotation(MethodVisitor visitor, DexAnnotation[] annotations) {
    for (DexAnnotation annotation : annotations) {
      if (annotation.annotation.type == appView.dexItemFactory().annotationMethodParameters) {
        assert annotation.visibility == DexAnnotation.VISIBILITY_SYSTEM;
        assert annotation.annotation.elements.length == 2;
        assert annotation.annotation.elements[0].name.toString().equals("names");
        assert annotation.annotation.elements[1].name.toString().equals("accessFlags");
        DexValueArray names = (DexValueArray) annotation.annotation.elements[0].value;
        DexValueArray accessFlags = (DexValueArray) annotation.annotation.elements[1].value;
        assert names != null && accessFlags != null;
        assert names.getValues().length == accessFlags.getValues().length;
        for (int i = 0; i < names.getValues().length; i++) {
          DexValueString name = (DexValueString) names.getValues()[i];
          DexValueInt access = (DexValueInt) accessFlags.getValues()[i];
          visitor.visitParameter(name.value.toString(), access.value);
        }
      }
    }
  }

  private void writeParameterAnnotations(
      MethodVisitor visitor, ParameterAnnotationsList parameterAnnotations) {
    // TODO(113565942): We currently assume that the annotable parameter count
    // it the same for visible and invisible annotations. That doesn't actually
    // seem to be the case in the class file format.
    visitor.visitAnnotableParameterCount(
        parameterAnnotations.getAnnotableParameterCount(), true);
    visitor.visitAnnotableParameterCount(
        parameterAnnotations.getAnnotableParameterCount(), false);

    for (int i = 0; i < parameterAnnotations.size(); i++) {
      int iFinal = i;
      writeAnnotations(
          (d, vis) -> visitor.visitParameterAnnotation(iFinal, d, vis),
          parameterAnnotations.get(i).annotations);
    }
  }

  private interface AnnotationConsumer {
    AnnotationVisitor visit(String desc, boolean visible);
  }

  private void writeAnnotations(AnnotationConsumer visitor, DexAnnotation[] annotations) {
    for (DexAnnotation dexAnnotation : annotations) {
      if (dexAnnotation.visibility == DexAnnotation.VISIBILITY_SYSTEM) {
        // Annotations with VISIBILITY_SYSTEM are not annotations in CF, but are special
        // annotations in Dex, i.e. default, enclosing class, enclosing method, member classes,
        // signature, throws.
        continue;
      }
      AnnotationVisitor v =
          visitor.visit(
              namingLens.lookupDescriptor(dexAnnotation.annotation.type).toString(),
              dexAnnotation.visibility == DexAnnotation.VISIBILITY_RUNTIME);
      if (v != null) {
        writeAnnotation(v, dexAnnotation.annotation);
        v.visitEnd();
      }
    }
  }

  private void writeAnnotation(AnnotationVisitor v, DexEncodedAnnotation annotation) {
    for (DexAnnotationElement element : annotation.elements) {
      writeAnnotationElement(v, element.name.toString(), element.value);
    }
  }

  private void writeAnnotationElement(AnnotationVisitor visitor, String name, DexValue value) {
    if (value instanceof DexValueAnnotation) {
      DexValueAnnotation valueAnnotation = (DexValueAnnotation) value;
      AnnotationVisitor innerVisitor =
          visitor.visitAnnotation(
              name, namingLens.lookupDescriptor(valueAnnotation.value.type).toString());
      if (innerVisitor != null) {
        writeAnnotation(innerVisitor, valueAnnotation.value);
        innerVisitor.visitEnd();
      }
    } else if (value instanceof DexValueArray) {
      DexValue[] values = ((DexValueArray) value).getValues();
      AnnotationVisitor innerVisitor = visitor.visitArray(name);
      if (innerVisitor != null) {
        for (DexValue arrayValue : values) {
          writeAnnotationElement(innerVisitor, null, arrayValue);
        }
        innerVisitor.visitEnd();
      }
    } else if (value instanceof DexValueEnum) {
      DexValueEnum en = (DexValueEnum) value;
      visitor.visitEnum(
          name, namingLens.lookupDescriptor(en.value.type).toString(), en.value.name.toString());
    } else if (value instanceof DexValueField) {
      throw new Unreachable("writeAnnotationElement of DexValueField");
    } else if (value instanceof DexValueMethod) {
      throw new Unreachable("writeAnnotationElement of DexValueMethod");
    } else if (value instanceof DexValueMethodHandle) {
      throw new Unreachable("writeAnnotationElement of DexValueMethodHandle");
    } else if (value instanceof DexValueMethodType) {
      throw new Unreachable("writeAnnotationElement of DexValueMethodType");
    } else if (value instanceof DexValueString) {
      DexValueString str = (DexValueString) value;
      visitor.visit(name, str.getValue().toString());
    } else if (value instanceof DexValueType) {
      DexValueType ty = (DexValueType) value;
      visitor.visit(name, Type.getType(namingLens.lookupDescriptor(ty.value).toString()));
    } else if (value instanceof UnknownDexValue) {
      throw new Unreachable("writeAnnotationElement of UnknownDexValue");
    } else {
      visitor.visit(name, value.getBoxedValue());
    }
  }

  private void writeCode(DexEncodedMethod method, MethodVisitor visitor, int classFileVersion) {
    method.getCode().asCfCode().write(method, visitor, namingLens, appView, classFileVersion);
  }

  public static String printCf(byte[] result) {
    ClassReader reader = new ClassReader(result);
    ClassNode node = new ClassNode(ASM_VERSION);
    reader.accept(node, ASM_VERSION);
    StringWriter writer = new StringWriter();
    for (MethodNode method : node.methods) {
      writer.append(method.name).append(method.desc).append('\n');
      TraceMethodVisitor visitor = new TraceMethodVisitor(new Textifier());
      method.accept(visitor);
      visitor.p.print(new PrintWriter(writer));
      writer.append('\n');
    }
    return writer.toString();
  }

  private static void verifyCf(byte[] result) {
    ClassReader reader = new ClassReader(result);
    PrintWriter pw = new PrintWriter(System.out);
    CheckClassAdapter.verify(reader, false, pw);
  }
}
