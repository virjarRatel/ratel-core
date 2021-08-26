// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import static com.android.tools.r8.utils.InternalOptions.ASM_VERSION;
import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_DEPRECATED;
import static org.objectweb.asm.Opcodes.V1_6;
import static org.objectweb.asm.Opcodes.V9;

import com.android.tools.r8.ProgramResource.Kind;
import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.DexProgramClass.ChecksumSupplier;
import com.android.tools.r8.graph.DexValue.DexValueAnnotation;
import com.android.tools.r8.graph.DexValue.DexValueArray;
import com.android.tools.r8.graph.DexValue.DexValueBoolean;
import com.android.tools.r8.graph.DexValue.DexValueByte;
import com.android.tools.r8.graph.DexValue.DexValueChar;
import com.android.tools.r8.graph.DexValue.DexValueDouble;
import com.android.tools.r8.graph.DexValue.DexValueEnum;
import com.android.tools.r8.graph.DexValue.DexValueFloat;
import com.android.tools.r8.graph.DexValue.DexValueInt;
import com.android.tools.r8.graph.DexValue.DexValueLong;
import com.android.tools.r8.graph.DexValue.DexValueNull;
import com.android.tools.r8.graph.DexValue.DexValueShort;
import com.android.tools.r8.graph.DexValue.DexValueString;
import com.android.tools.r8.graph.DexValue.DexValueType;
import com.android.tools.r8.jar.CfApplicationWriter;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.ProguardKeepAttributes;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.FieldSignatureEquivalence;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.MethodSignatureEquivalence;
import com.android.tools.r8.utils.StringDiagnostic;
import com.android.tools.r8.utils.StringUtils;
import com.google.common.base.Equivalence.Wrapper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.CRC32;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;

/**
 * Java/Jar class reader for constructing dex/graph structure.
 */
public class JarClassFileReader {

  private static final byte[] CLASSFILE_HEADER = ByteBuffer.allocate(4).putInt(0xCAFEBABE).array();

  // Hidden ASM "synthetic attribute" bit we need to clear.
  private static final int ACC_SYNTHETIC_ATTRIBUTE = 0x40000;

  private final JarApplicationReader application;
  private final Consumer<DexClass> classConsumer;

  public JarClassFileReader(
      JarApplicationReader application, Consumer<DexClass> classConsumer) {
    this.application = application;
    this.classConsumer = classConsumer;
  }

  public void read(Origin origin, ClassKind classKind, InputStream input) throws IOException {
    if (!input.markSupported()) {
      input = new BufferedInputStream(input);
    }
    byte[] header = new byte[CLASSFILE_HEADER.length];
    input.mark(header.length);
    int size = 0;
    while (size < header.length) {
      int read = input.read(header, size, header.length - size);
      if (read < 0) {
        throw new CompilationError("Invalid empty classfile", origin);
      }
      size += read;
    }
    if (!Arrays.equals(CLASSFILE_HEADER, header)) {
      throw new CompilationError("Invalid classfile header", origin);
    }
    input.reset();

    ClassReader reader = new ClassReader(input);

    int parsingOptions = SKIP_FRAMES | SKIP_CODE;

    // If the source-file and source-debug-extension attributes are not kept we can skip all debug
    // related attributes when parsing the class structure.
    if (application.options.getProguardConfiguration() != null) {
      ProguardKeepAttributes keep =
          application.options.getProguardConfiguration().getKeepAttributes();
      if (!keep.sourceFile && !keep.sourceDebugExtension) {
        parsingOptions |= SKIP_DEBUG;
      }
    }
    reader.accept(
        new CreateDexClassVisitor(origin, classKind, reader.b, application, classConsumer),
        parsingOptions);

    // Read marker.
    if (reader.getItemCount() > CfApplicationWriter.MARKER_STRING_CONSTANT_POOL_INDEX
        && reader.getItem(CfApplicationWriter.MARKER_STRING_CONSTANT_POOL_INDEX) > 0) {
      try {
        Object maybeMarker =
            reader.readConst(
                CfApplicationWriter.MARKER_STRING_CONSTANT_POOL_INDEX,
                new char[reader.getMaxStringLength()]);
        if (maybeMarker instanceof String) {
          application.getFactory().createString((String) maybeMarker);
        }
      } catch (IllegalArgumentException e) {
        // Ignore if the type of the constant is not something readConst() allows.
      }
    }
  }

  private static int cleanAccessFlags(int access) {
    // Clear the "synthetic attribute" and "deprecated" attribute-flags if present.
    return access & ~ACC_SYNTHETIC_ATTRIBUTE & ~ACC_DEPRECATED;
  }

  public static FieldAccessFlags createFieldAccessFlags(int access) {
    return FieldAccessFlags.fromCfAccessFlags(cleanAccessFlags(access));
  }

  public static MethodAccessFlags createMethodAccessFlags(String name, int access) {
    boolean isConstructor =
        name.equals(Constants.INSTANCE_INITIALIZER_NAME)
            || name.equals(Constants.CLASS_INITIALIZER_NAME);
    return MethodAccessFlags.fromCfAccessFlags(cleanAccessFlags(access), isConstructor);
  }

  private static AnnotationVisitor createAnnotationVisitor(String desc, boolean visible,
      List<DexAnnotation> annotations,
      JarApplicationReader application) {
    assert annotations != null;
    if (visible || retainCompileTimeAnnotation(desc, application)) {
      int visiblity = visible ? DexAnnotation.VISIBILITY_RUNTIME : DexAnnotation.VISIBILITY_BUILD;
      return new CreateAnnotationVisitor(
          application,
          (names, values) ->
              annotations.add(
                  new DexAnnotation(
                      visiblity, createEncodedAnnotation(desc, names, values, application))));
    }
    return null;
  }

  private static boolean retainCompileTimeAnnotation(
      String desc, JarApplicationReader application) {
    return application.options.readCompileTimeAnnotations
        || DexAnnotation.retainCompileTimeAnnotation(
            application.getTypeFromDescriptor(desc), application.options);
  }

  private static DexEncodedAnnotation createEncodedAnnotation(String desc,
      List<DexString> names, List<DexValue> values, JarApplicationReader application) {
    assert (names == null && values.isEmpty())
        || (names != null && !names.isEmpty() && names.size() == values.size());
    DexAnnotationElement[] elements = new DexAnnotationElement[values.size()];
    for (int i = 0; i < values.size(); i++) {
      elements[i] = new DexAnnotationElement(names.get(i), values.get(i));
    }
    return new DexEncodedAnnotation(application.getTypeFromDescriptor(desc), elements);
  }

  private static class CreateDexClassVisitor extends ClassVisitor {

    private final Origin origin;
    private final ClassKind classKind;
    private final JarApplicationReader application;
    private final Consumer<DexClass> classConsumer;
    private final ReparseContext context = new ReparseContext();

    // DexClass data.
    private int version;
    private DexType type;
    private ClassAccessFlags accessFlags;
    private DexType superType;
    private DexTypeList interfaces;
    private DexString sourceFile;
    private NestHostClassAttribute nestHost = null;
    private final List<NestMemberClassAttribute> nestMembers = new ArrayList<>();
    private EnclosingMethodAttribute enclosingMember = null;
    private final List<InnerClassAttribute> innerClasses = new ArrayList<>();
    private List<DexAnnotation> annotations = null;
    private List<DexAnnotationElement> defaultAnnotations = null;
    private final List<DexEncodedField> staticFields = new ArrayList<>();
    private final List<DexEncodedField> instanceFields = new ArrayList<>();
    private final Set<Wrapper<DexField>> fieldSignatures = new HashSet<>();
    private final List<DexEncodedMethod> directMethods = new ArrayList<>();
    private final List<DexEncodedMethod> virtualMethods = new ArrayList<>();
    private final Set<Wrapper<DexMethod>> methodSignatures = new HashSet<>();
    private boolean hasReachabilitySensitiveMethod = false;

    public CreateDexClassVisitor(
        Origin origin,
        ClassKind classKind,
        byte[] classCache,
        JarApplicationReader application,
        Consumer<DexClass> classConsumer) {
      super(ASM_VERSION);
      this.origin = origin;
      this.classKind = classKind;
      this.classConsumer = classConsumer;
      this.context.classCache = classCache;
      this.application = application;
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
      if (outerName != null && innerName != null) {
        String separator = DescriptorUtils.computeInnerClassSeparator(outerName, name, innerName);
        if (separator == null && getMajorVersion() < V9) {
          application.options.reporter.info(
              new StringDiagnostic(
                  StringUtils.lines(
                      "Malformed inner-class attribute:",
                      "\touterTypeInternal: " + outerName,
                      "\tinnerTypeInternal: " + name,
                      "\tinnerName: " + innerName)));
        }
      }
      innerClasses.add(
          new InnerClassAttribute(
              access,
              application.getTypeFromName(name),
              outerName == null ? null : application.getTypeFromName(outerName),
              innerName == null ? null : application.getString(innerName)));
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
      // This is called for anonymous and local inner classes defined in classes or in methods.
      assert enclosingMember == null;
      DexType ownerType = application.getTypeFromName(owner);
      enclosingMember =
          name == null
              ? new EnclosingMethodAttribute(ownerType)
              : new EnclosingMethodAttribute(application.getMethod(ownerType, name, desc));
    }

    @Override
    public void visitNestHost(String nestHost) {
      assert this.nestHost == null && nestMembers.isEmpty();
      DexType nestHostType = application.getTypeFromName(nestHost);
      this.nestHost = new NestHostClassAttribute(nestHostType);
    }

    @Override
    public void visitNestMember(String nestMember) {
      assert nestHost == null;
      DexType nestMemberType = application.getTypeFromName(nestMember);
      nestMembers.add(new NestMemberClassAttribute(nestMemberType));
    }

    private String illegalClassFilePrefix(ClassAccessFlags accessFlags, String name) {
      return "Illegal class file: "
          + (accessFlags.isInterface() ? "Interface" : "Class")
          + " "
          + name;
    }

    private String illegalClassFilePostfix(int version) {
      return "Class file version " + version;
    }

    private String illegalClassFileMessage(
        ClassAccessFlags accessFlags, String name, int version, String message) {
      return illegalClassFilePrefix(accessFlags, name)
          + " " + message
          + ". " + illegalClassFilePostfix(version) + ".";
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
        String[] interfaces) {
      this.version = version;
      if (InternalOptions.SUPPORTED_CF_MAJOR_VERSION < getMajorVersion()) {
        throw new CompilationError("Unsupported class file version: " + getMajorVersion(), origin);
      }
      accessFlags = ClassAccessFlags.fromCfAccessFlags(cleanAccessFlags(access));
      type = application.getTypeFromName(name);
      // Check if constraints from
      // https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.1 are met.
      if (!accessFlags.areValid(getMajorVersion(), name.endsWith("/package-info"))) {
        throw new CompilationError(
            illegalClassFileMessage(accessFlags, name, version,
                "has invalid access flags. Found: " + accessFlags.toString()), origin);
      }
      if (superName == null && !name.equals(Constants.JAVA_LANG_OBJECT_NAME)) {
        throw new CompilationError(
            illegalClassFileMessage(accessFlags, name, version,
                "is missing a super type"), origin);
      }
      if (accessFlags.isInterface()
          && !Objects.equals(superName, Constants.JAVA_LANG_OBJECT_NAME)) {
        throw new CompilationError(
            illegalClassFileMessage(accessFlags, name, version,
                "must extend class java.lang.Object. Found: " + Objects.toString(superName)),
            origin);
      }
      checkName(name);
      assert superName != null || name.equals(Constants.JAVA_LANG_OBJECT_NAME);
      superType = superName == null ? null : application.getTypeFromName(superName);
      this.interfaces = application.getTypeListFromNames(interfaces);
      if (signature != null && !signature.isEmpty()) {
        addAnnotation(DexAnnotation.createSignatureAnnotation(signature, application.getFactory()));
      }
    }

    @Override
    public void visitSource(String source, String debug) {
      if (source != null) {
        sourceFile = application.getString(source);
      }
      if (debug != null) {
        getAnnotations().add(
            DexAnnotation.createSourceDebugExtensionAnnotation(
                new DexValueString(application.getString(debug)), application.getFactory()));
      }
    }

    @Override
    public FieldVisitor visitField(
        int access, String name, String desc, String signature, Object value) {
      if (classKind == ClassKind.LIBRARY) {
        FieldAccessFlags flags = createFieldAccessFlags(access);
        if (flags.isPrivate()) {
          return null;
        }
      }
      checkName(name);
      return new CreateFieldVisitor(
          this, access, name, desc, signature, classKind == ClassKind.LIBRARY ? null : value);
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String desc, String signature, String[] exceptions) {
      if (classKind == ClassKind.LIBRARY) {
        MethodAccessFlags flags = createMethodAccessFlags(name, access);
        if ((flags.isStatic() && flags.isConstructor()) || flags.isPrivate()) {
          return null;
        }
      }
      checkName(name);
      return new CreateMethodVisitor(access, name, desc, signature, exceptions, this);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return createAnnotationVisitor(desc, visible, getAnnotations(), application);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc,
        boolean visible) {
      // Java 8 type annotations are not supported by Dex, thus ignore them.
      return null;
    }

    @Override
    public void visitAttribute(Attribute attr) {
      // Unknown attribute must only be ignored
    }

    @Override
    public void visitEnd() {
      if (defaultAnnotations != null) {
        addAnnotation(DexAnnotation.createAnnotationDefaultAnnotation(
            type, defaultAnnotations, application.getFactory()));
      }
      checkReachabilitySensitivity();
      DexClass clazz =
          classKind.create(
              type,
              Kind.CF,
              origin,
              accessFlags,
              superType,
              interfaces,
              sourceFile,
              nestHost,
              nestMembers,
              enclosingMember,
              innerClasses,
              createAnnotationSet(annotations, application.options),
              staticFields.toArray(DexEncodedField.EMPTY_ARRAY),
              instanceFields.toArray(DexEncodedField.EMPTY_ARRAY),
              directMethods.toArray(DexEncodedMethod.EMPTY_ARRAY),
              virtualMethods.toArray(DexEncodedMethod.EMPTY_ARRAY),
              application.getFactory().getSkipNameValidationForTesting(),
              getChecksumSupplier(classKind));
      InnerClassAttribute innerClassAttribute = clazz.getInnerClassAttributeForThisClass();
      // A member class should not be a local or anonymous class.
      if (innerClassAttribute != null && innerClassAttribute.getOuter() != null) {
        if (innerClassAttribute.isAnonymous()) {
          assert innerClassAttribute.getInnerName() == null;
          // If the enclosing member is not null, the intention would be an anonymous class, and
          // thus the outer-class reference should have been null.  If the enclosing member is null,
          // it is likely due to the missing enclosing member.  In either case, we can recover
          // InnerClasses attribute by erasing the outer-class reference.
          InnerClassAttribute recoveredAttribute = new InnerClassAttribute(
              innerClassAttribute.getAccess(), innerClassAttribute.getInner(), null, null);
          clazz.replaceInnerClassAttributeForThisClass(recoveredAttribute);
        } else if (enclosingMember != null) {
          assert innerClassAttribute.isNamed();
          // It is unclear whether the intention was a member class or a local class. Fail hard.
          throw new CompilationError(
              StringUtils.lines(
                  "A member class cannot also be a (non-member) local class at the same time.",
                  "This is likely due to invalid EnclosingMethod and InnerClasses attributes:",
                  enclosingMember.toString(),
                  innerClassAttribute.toString()),
              origin);
        }
      }
      if (enclosingMember == null
          && (clazz.isLocalClass() || clazz.isAnonymousClass())
          && getMajorVersion() > V1_6) {
        application.options.warningMissingEnclosingMember(clazz.type, clazz.origin, version);
      }
      if (!clazz.isLibraryClass()) {
        context.owner = clazz;
      }
      if (clazz.isProgramClass()) {
        DexProgramClass programClass = clazz.asProgramClass();
        programClass.setInitialClassFileVersion(version);
      }
      classConsumer.accept(clazz);
    }

    private ChecksumSupplier getChecksumSupplier(ClassKind classKind) {
      if (application.options.encodeChecksums && classKind == ClassKind.PROGRAM) {
        CRC32 crc = new CRC32();
        crc.update(this.context.classCache, 0, this.context.classCache.length);
        final long value = crc.getValue();
        return clazz -> value;
      }
      return DexProgramClass::invalidChecksumRequest;
    }

    private void checkName(String name) {
      if (!application.getFactory().getSkipNameValidationForTesting()
          && !DexString.isValidSimpleName(application.options.minApiLevel, name)) {
        throw new CompilationError("Space characters in SimpleName '"
          + name + "' are not allowed prior to DEX version 040");
      }
    }

    // If anything is marked reachability sensitive, all methods need to be parsed including
    // locals information. This propagates the reachability sensitivity bit so that if any field
    // or method is annotated, all methods get parsed with locals information.
    private void checkReachabilitySensitivity() {
      if (hasReachabilitySensitiveMethod || hasReachabilitySensitiveField()) {
        for (DexEncodedMethod method : directMethods) {
          Code code = method.getCode();
          if (code != null && code.isCfCode()) {
            code.asLazyCfCode().markReachabilitySensitive();
          }
        }
        for (DexEncodedMethod method : virtualMethods) {
          Code code = method.getCode();
          if (code != null && code.isCfCode()) {
            code.asLazyCfCode().markReachabilitySensitive();
          }
        }
      }
    }

    private boolean hasReachabilitySensitiveField() {
      DexType reachabilitySensitive = application.getFactory().annotationReachabilitySensitive;
      for (DexEncodedField field : instanceFields) {
        for (DexAnnotation annotation : field.annotations.annotations) {
          if (annotation.annotation.type == reachabilitySensitive) {
            return true;
          }
        }
      }
      for (DexEncodedField field : staticFields) {
        for (DexAnnotation annotation : field.annotations.annotations) {
          if (annotation.annotation.type == reachabilitySensitive) {
            return true;
          }
        }
      }
      return false;
    }

    private void addDefaultAnnotation(String name, DexValue value) {
      if (defaultAnnotations == null) {
        defaultAnnotations = new ArrayList<>();
      }
      defaultAnnotations.add(new DexAnnotationElement(application.getString(name), value));
    }

    private void addAnnotation(DexAnnotation annotation) {
      getAnnotations().add(annotation);
    }

    private List<DexAnnotation> getAnnotations() {
      if (annotations == null) {
        annotations = new ArrayList<>();
      }
      return annotations;
    }

    private int getMajorVersion() {
      return version & 0xFFFF;
    }

    private int getMinorVersion() {
      return ((version >> 16) & 0xFFFF);
    }

    public boolean isInANest() {
      return !nestMembers.isEmpty() || nestHost != null;
    }
  }

  private static DexAnnotationSet createAnnotationSet(
      List<DexAnnotation> annotations, InternalOptions options) {
    if (annotations == null || annotations.isEmpty()) {
      return DexAnnotationSet.empty();
    }
    if (options.isGeneratingDex()) {
      DexType dupType = DexAnnotationSet.findDuplicateEntryType(annotations);
      if (dupType != null) {
        throw new CompilationError(
            "Multiple annotations of type `" + dupType.toSourceString() + "`");
      }
    }
    return new DexAnnotationSet(annotations.toArray(DexAnnotation.EMPTY_ARRAY));
  }

  private static class CreateFieldVisitor extends FieldVisitor {

    private final CreateDexClassVisitor parent;
    private final int access;
    private final String name;
    private final String desc;
    private final Object value;
    private List<DexAnnotation> annotations = null;

    public CreateFieldVisitor(CreateDexClassVisitor parent,
        int access, String name, String desc, String signature, Object value) {
      super(ASM_VERSION);
      this.parent = parent;
      this.access = access;
      this.name = name;
      this.desc = desc;
      this.value = value;
      if (signature != null && !signature.isEmpty()) {
        addAnnotation(DexAnnotation.createSignatureAnnotation(
            signature, parent.application.getFactory()));
      }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return createAnnotationVisitor(desc, visible, getAnnotations(), parent.application);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc,
        boolean visible) {
      // Java 8 type annotations are not supported by Dex, thus ignore them.
      return null;
    }

    @Override
    public void visitEnd() {
      FieldAccessFlags flags = createFieldAccessFlags(access);
      DexField dexField = parent.application.getField(parent.type, name, desc);
      Wrapper<DexField> signature = FieldSignatureEquivalence.get().wrap(dexField);
      if (parent.fieldSignatures.add(signature)) {
        DexAnnotationSet annotationSet =
            createAnnotationSet(annotations, parent.application.options);
        DexValue staticValue = flags.isStatic() ? getStaticValue(value, dexField.type) : null;
        DexEncodedField field = new DexEncodedField(dexField, flags, annotationSet, staticValue);
        if (flags.isStatic()) {
          parent.staticFields.add(field);
        } else {
          parent.instanceFields.add(field);
        }
      } else {
        parent.application.options.reporter.warning(
            new StringDiagnostic(
                String.format("Field `%s` has multiple definitions", dexField.toSourceString())));
      }
    }

    private DexValue getStaticValue(Object value, DexType type) {
      if (value == null) {
        return null;
      }
      DexItemFactory factory = parent.application.getFactory();
      if (type == factory.booleanType) {
        int i = (Integer) value;
        assert 0 <= i && i <= 1;
        return DexValueBoolean.create(i == 1);
      }
      if (type == factory.byteType) {
        return DexValueByte.create(((Integer) value).byteValue());
      }
      if (type == factory.shortType) {
        return DexValueShort.create(((Integer) value).shortValue());
      }
      if (type == factory.charType) {
        return DexValueChar.create((char) ((Integer) value).intValue());
      }
      if (type == factory.intType) {
        return DexValueInt.create((Integer) value);
      }
      if (type == factory.floatType) {
        return DexValueFloat.create((Float) value);
      }
      if (type == factory.longType) {
        return DexValueLong.create((Long) value);
      }
      if (type == factory.doubleType) {
        return DexValueDouble.create((Double) value);
      }
      if (type == factory.stringType) {
        return new DexValueString(factory.createString((String) value));
      }
      throw new Unreachable("Unexpected static-value type " + type);
    }

    private void addAnnotation(DexAnnotation annotation) {
      getAnnotations().add(annotation);
    }

    private List<DexAnnotation> getAnnotations() {
      if (annotations == null) {
        annotations = new ArrayList<>();
      }
      return annotations;
    }
  }

  private static class CreateMethodVisitor extends MethodVisitor {

    private final String name;
    final CreateDexClassVisitor parent;
    private final int parameterCount;
    private List<DexAnnotation> annotations = null;
    private DexValue defaultAnnotation = null;
    private int annotableParameterCount = -1;
    private List<List<DexAnnotation>> parameterAnnotationsLists = null;
    private List<DexValue> parameterNames = null;
    private List<DexValue> parameterFlags = null;
    final DexMethod method;
    final MethodAccessFlags flags;
    Code code = null;

    public CreateMethodVisitor(int access, String name, String desc, String signature,
        String[] exceptions, CreateDexClassVisitor parent) {
      super(ASM_VERSION);
      this.name = name;
      this.parent = parent;
      this.method = parent.application.getMethod(parent.type, name, desc);
      this.flags = createMethodAccessFlags(name, access);
      parameterCount = JarApplicationReader.getArgumentCount(desc);
      if (exceptions != null && exceptions.length > 0) {
        DexValue[] values = new DexValue[exceptions.length];
        for (int i = 0; i < exceptions.length; i++) {
          values[i] = new DexValueType(parent.application.getTypeFromName(exceptions[i]));
        }
        addAnnotation(DexAnnotation.createThrowsAnnotation(
            values, parent.application.getFactory()));
      }
      if (signature != null && !signature.isEmpty()) {
        addAnnotation(DexAnnotation.createSignatureAnnotation(
            signature, parent.application.getFactory()));
      }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return createAnnotationVisitor(desc, visible, getAnnotations(), parent.application);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
      return new CreateAnnotationVisitor(parent.application, (names, elements) -> {
        assert elements.size() == 1;
        defaultAnnotation = elements.get(0);
      });
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc,
        boolean visible) {
      // Java 8 type annotations are not supported by Dex, thus ignore them.
      return null;
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
      if (annotableParameterCount != -1) {
        // TODO(113565942): We assume that the runtime visible and runtime invisible parameter
        // count is always the same. It doesn't have to be according to the classfile format, so
        // we really should split it into two sets.
        assert annotableParameterCount == parameterCount;
      }
      annotableParameterCount = parameterCount;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      if (parameterAnnotationsLists == null) {
        if (annotableParameterCount == -1) {
          annotableParameterCount = parameterCount;
        }
        parameterAnnotationsLists = new ArrayList<>(annotableParameterCount);
        for (int i = 0; i < annotableParameterCount; i++) {
          parameterAnnotationsLists.add(new ArrayList<>());
        }
      }
      assert mv == null;
      return createAnnotationVisitor(desc, visible,
          parameterAnnotationsLists.get(parameter), parent.application);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc,
        boolean visible) {
      // Java 8 type annotations are not supported by Dex, thus ignore them.
      return null;
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath,
        Label[] start, Label[] end, int[] index, String desc, boolean visible) {
      // Java 8 type annotations are not supported by Dex, thus ignore them.
      return null;
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc,
        boolean visible) {
      // Java 8 type annotations are not supported by Dex, thus ignore them.
      return null;
    }

    @Override
    public void visitParameter(String name, int access) {
      if (parameterNames == null) {
        assert parameterFlags == null;
        parameterNames = new ArrayList<>(parameterCount);
        parameterFlags = new ArrayList<>(parameterCount);
      }
      parameterNames.add(new DexValueString(parent.application.getFactory().createString(name)));
      parameterFlags.add(DexValueInt.create(access));
      super.visitParameter(name, access);
    }

    @Override
    public void visitCode() {
      throw new Unreachable("visitCode() should not be called when SKIP_CODE is set");
    }

    private boolean classRequiresCode() {
      return parent.classKind == ClassKind.PROGRAM
          || (parent.application.options.enableNestBasedAccessDesugaring
              && !parent.application.options.canUseNestBasedAccess()
              && parent.classKind == ClassKind.CLASSPATH
              && parent.isInANest());
    }

    @Override
    public void visitEnd() {
      InternalOptions options = parent.application.options;
      if (!flags.isAbstract() && !flags.isNative() && classRequiresCode()) {
        code = new LazyCfCode(method, parent.origin, parent.context, parent.application);
      }
      ParameterAnnotationsList parameterAnnotationsList;
      if (parameterAnnotationsLists == null) {
        parameterAnnotationsList = ParameterAnnotationsList.empty();
      } else {
        DexAnnotationSet[] sets = new DexAnnotationSet[parameterAnnotationsLists.size()];
        for (int i = 0; i < parameterAnnotationsLists.size(); i++) {
          sets[i] = createAnnotationSet(parameterAnnotationsLists.get(i), options);
        }
        parameterAnnotationsList = new ParameterAnnotationsList(sets);
      }
      if (parameterNames != null) {
        assert parameterFlags != null;
        if (parameterNames.size() != parameterCount) {
          options.warningInvalidParameterAnnotations(
              method, parent.origin, parameterCount, parameterNames.size());
        }
        getAnnotations().add(DexAnnotation.createMethodParametersAnnotation(
            parameterNames.toArray(DexValue.EMPTY_ARRAY),
            parameterFlags.toArray(DexValue.EMPTY_ARRAY),
            parent.application.getFactory()));
      }
      DexEncodedMethod dexMethod =
          new DexEncodedMethod(
              method,
              flags,
              createAnnotationSet(annotations, options),
              parameterAnnotationsList,
              code,
              parent.version);
      Wrapper<DexMethod> signature = MethodSignatureEquivalence.get().wrap(method);
      if (parent.methodSignatures.add(signature)) {
        parent.hasReachabilitySensitiveMethod |= isReachabilitySensitive();
        if (flags.isStatic() || flags.isConstructor() || flags.isPrivate()) {
          parent.directMethods.add(dexMethod);
        } else {
          parent.virtualMethods.add(dexMethod);
        }
      } else {
        options.reporter.warning(
            new StringDiagnostic(
                String.format(
                    "Ignoring an implementation of the method `%s` because it has multiple "
                        + "definitions",
                    method.toSourceString())));
      }
      if (defaultAnnotation != null) {
        parent.addDefaultAnnotation(name, defaultAnnotation);
      }
    }

    private boolean isReachabilitySensitive() {
      DexType reachabilitySensitive =
          parent.application.getFactory().annotationReachabilitySensitive;
      for (DexAnnotation annotation : getAnnotations()) {
        if (annotation.annotation.type == reachabilitySensitive) {
          return true;
        }
      }
      return false;
    }

    private List<DexAnnotation> getAnnotations() {
      if (annotations == null) {
        annotations = new ArrayList<>();
      }
      return annotations;
    }

    private void addAnnotation(DexAnnotation annotation) {
      getAnnotations().add(annotation);
    }
  }

  private static class CreateAnnotationVisitor extends AnnotationVisitor {

    private final JarApplicationReader application;
    private final BiConsumer<List<DexString>, List<DexValue>> onVisitEnd;
    private List<DexString> names = null;
    private final List<DexValue> values = new ArrayList<>();

    public CreateAnnotationVisitor(
        JarApplicationReader application, BiConsumer<List<DexString>, List<DexValue>> onVisitEnd) {
      super(ASM_VERSION);
      this.application = application;
      this.onVisitEnd = onVisitEnd;
    }

    @Override
    public void visit(String name, Object value) {
      addElement(name, getDexValue(value));
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
      DexType owner = application.getTypeFromDescriptor(desc);
      addElement(name, new DexValueEnum(application.getField(owner, value, desc)));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      return new CreateAnnotationVisitor(application, (names, values) ->
          addElement(name, new DexValueAnnotation(
              createEncodedAnnotation(desc, names, values, application))));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
      return new CreateAnnotationVisitor(application, (names, values) -> {
        assert names == null;
        addElement(name, new DexValueArray(values.toArray(DexValue.EMPTY_ARRAY)));
      });
    }

    @Override
    public void visitEnd() {
      onVisitEnd.accept(names, values);
    }

    private void addElement(String name, DexValue value) {
      if (name != null) {
        if (names == null) {
          names = new ArrayList<>();
        }
        names.add(application.getString(name));
      }
      values.add(value);
    }

    private static DexValueArray getDexValueArray(Object value) {
      if (value instanceof byte[]) {
        byte[] values = (byte[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueByte.create(values[i]);
        }
        return new DexValueArray(elements);
      } else if (value instanceof boolean[]) {
        boolean[] values = (boolean[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueBoolean.create(values[i]);
        }
        return new DexValueArray(elements);
      } else if (value instanceof char[]) {
        char[] values = (char[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueChar.create(values[i]);
        }
        return new DexValueArray(elements);
      } else if (value instanceof short[]) {
        short[] values = (short[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueShort.create(values[i]);
        }
        return new DexValueArray(elements);
      } else if (value instanceof int[]) {
        int[] values = (int[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueInt.create(values[i]);
        }
        return new DexValueArray(elements);
      } else if (value instanceof long[]) {
        long[] values = (long[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueLong.create(values[i]);
        }
        return new DexValueArray(elements);
      } else if (value instanceof float[]) {
        float[] values = (float[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueFloat.create(values[i]);
        }
        return new DexValueArray(elements);
      } else if (value instanceof double[]) {
        double[] values = (double[]) value;
        DexValue[] elements = new DexValue[values.length];
        for (int i = 0; i < values.length; i++) {
          elements[i] = DexValueDouble.create(values[i]);
        }
        return new DexValueArray(elements);
      }
      throw new Unreachable("Unexpected type of annotation value: " + value);
    }

    private DexValue getDexValue(Object value) {
      if (value == null) {
        return DexValueNull.NULL;
      }
      if (value instanceof Byte) {
        return DexValueByte.create((Byte) value);
      } else if (value instanceof Boolean) {
        return DexValueBoolean.create((Boolean) value);
      } else if (value instanceof Character) {
        return DexValueChar.create((Character) value);
      } else if (value instanceof Short) {
        return DexValueShort.create((Short) value);
      } else if (value instanceof Integer) {
        return DexValueInt.create((Integer) value);
      } else if (value instanceof Long) {
        return DexValueLong.create((Long) value);
      } else if (value instanceof Float) {
        return DexValueFloat.create((Float) value);
      } else if (value instanceof Double) {
        return DexValueDouble.create((Double) value);
      } else if (value instanceof String) {
        return new DexValueString(application.getString((String) value));
      } else if (value instanceof Type) {
        return new DexValueType(application.getTypeFromDescriptor(((Type) value).getDescriptor()));
      }
      return getDexValueArray(value);
    }
  }

  public static class ReparseContext {

    // This will hold the content of the whole class. Once all the methods of the class are swapped
    // from this to the actual JarCode, no other references would be left and the content can be
    // GC'd.
    public byte[] classCache;
    public DexClass owner;
    public final List<Code> codeList = new ArrayList<>();
  }
}
