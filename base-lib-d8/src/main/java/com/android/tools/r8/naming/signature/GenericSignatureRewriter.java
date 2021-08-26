// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming.signature;

import static com.android.tools.r8.utils.DescriptorUtils.getClassBinaryNameFromDescriptor;
import static com.android.tools.r8.utils.DescriptorUtils.getDescriptorFromClassBinaryName;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexAnnotation;
import com.android.tools.r8.graph.DexAnnotationSet;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexDefinition;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.origin.Origin;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.DescriptorUtils;
import com.android.tools.r8.utils.Reporter;
import com.android.tools.r8.utils.StringDiagnostic;
import com.google.common.collect.Maps;
import java.lang.reflect.GenericSignatureFormatError;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GenericSignatureRewriter {

  private final AppView<AppInfoWithLiveness> appView;
  private final Map<DexType, DexString> renaming;
  private final Reporter reporter;

  public GenericSignatureRewriter(AppView<AppInfoWithLiveness> appView) {
    this(appView, Maps.newIdentityHashMap());
  }

  public GenericSignatureRewriter(
      AppView<AppInfoWithLiveness> appView, Map<DexType, DexString> renaming) {
    this.appView = appView;
    this.renaming = renaming;
    this.reporter = appView.options().reporter;
  }

  public void run(Iterable<? extends DexClass> classes) {
    final GenericSignatureCollector genericSignatureCollector = new GenericSignatureCollector();
    final GenericSignatureParser<DexType> genericSignatureParser =
        new GenericSignatureParser<>(genericSignatureCollector);
    // classes may not be the same as appInfo().classes() if applymapping is used on classpath
    // arguments. If that is the case, the ProguardMapMinifier will pass in all classes that is
    // either ProgramClass or has a mapping. This is then transitively called inside the
    // ClassNameMinifier.
    for (DexClass clazz : classes) {
      clazz.annotations =
          rewriteGenericSignatures(
              clazz.annotations,
              genericSignatureParser::parseClassSignature,
              genericSignatureCollector::getRenamedSignature,
              (signature, e) -> parseError(clazz, clazz.getOrigin(), signature, e));
      clazz.forEachField(
          field ->
              field.annotations =
                  rewriteGenericSignatures(
                      field.annotations,
                      genericSignatureParser::parseFieldSignature,
                      genericSignatureCollector::getRenamedSignature,
                      (signature, e) -> parseError(field, clazz.getOrigin(), signature, e)));
      clazz.forEachMethod(
          method ->
              method.annotations =
                  rewriteGenericSignatures(
                      method.annotations,
                      genericSignatureParser::parseMethodSignature,
                      genericSignatureCollector::getRenamedSignature,
                      (signature, e) -> parseError(method, clazz.getOrigin(), signature, e)));
    }
  }

  private DexAnnotationSet rewriteGenericSignatures(
      DexAnnotationSet annotations,
      Consumer<String> parser,
      Supplier<String> collector,
      BiConsumer<String, GenericSignatureFormatError> parseError) {
    // There can be no more than one signature annotation in an annotation set.
    final int VALID = -1;
    int invalid = VALID;
    DexAnnotation[] rewrittenAnnotations = null;
    for (int i = 0; i < annotations.annotations.length && invalid == VALID; i++) {
      DexAnnotation annotation = annotations.annotations[i];
      if (DexAnnotation.isSignatureAnnotation(annotation, appView.dexItemFactory())) {
        if (rewrittenAnnotations == null) {
          rewrittenAnnotations = new DexAnnotation[annotations.annotations.length];
          System.arraycopy(annotations.annotations, 0, rewrittenAnnotations, 0, i);
        }
        String signature = DexAnnotation.getSignature(annotation);
        try {
          parser.accept(signature);
          DexAnnotation signatureAnnotation =
              DexAnnotation.createSignatureAnnotation(collector.get(), appView.dexItemFactory());
          rewrittenAnnotations[i] = signatureAnnotation;
        } catch (GenericSignatureFormatError e) {
          parseError.accept(signature, e);
          invalid = i;
        }
      } else if (rewrittenAnnotations != null) {
        rewrittenAnnotations[i] = annotation;
      }
    }

    // Return the rewritten signatures if it was valid and could be rewritten.
    if (invalid == VALID) {
      return rewrittenAnnotations != null
          ? new DexAnnotationSet(rewrittenAnnotations)
          : annotations;
    }
    // Remove invalid signature if found.
    DexAnnotation[] prunedAnnotations =
        new DexAnnotation[annotations.annotations.length - 1];
    int dest = 0;
    for (int i = 0; i < annotations.annotations.length; i++) {
      if (i != invalid) {
        prunedAnnotations[dest++] = annotations.annotations[i];
      }
    }
    assert dest == prunedAnnotations.length;
    return new DexAnnotationSet(prunedAnnotations);
  }

  private void parseError(
      DexDefinition item, Origin origin, String signature, GenericSignatureFormatError e) {
    StringBuilder message = new StringBuilder("Invalid signature '");
    message.append(signature);
    message.append("' for ");
    if (item.isDexClass()) {
      message.append("class ");
      message.append((item.asDexClass()).getType().toSourceString());
    } else if (item.isDexEncodedField()) {
      message.append("field ");
      message.append(item.toSourceString());
    } else {
      assert item.isDexEncodedMethod();
      message.append("method ");
      message.append(item.toSourceString());
    }
    message.append(".\n");
    message.append("Signature is ignored and will not be present in the output.\n");
    message.append("Parser error: ");
    message.append(e.getMessage());
    reporter.warning(new StringDiagnostic(message.toString(), origin));
  }

  private class GenericSignatureCollector implements GenericSignatureAction<DexType> {
    private StringBuilder renamedSignature;

    public String getRenamedSignature() {
      return renamedSignature.toString();
    }

    @Override
    public void parsedSymbol(char symbol) {
      renamedSignature.append(symbol);
    }

    @Override
    public void parsedIdentifier(String identifier) {
      renamedSignature.append(identifier);
    }

    @Override
    public DexType parsedTypeName(String name) {
      DexType type = appView.dexItemFactory().createType(getDescriptorFromClassBinaryName(name));
      type = appView.graphLense().lookupType(type);
      if (appView.appInfo().wasPruned(type)) {
        type = appView.dexItemFactory().objectType;
      }
      DexString renamedDescriptor = renaming.getOrDefault(type, type.descriptor);
      renamedSignature.append(getClassBinaryNameFromDescriptor(renamedDescriptor.toString()));
      return type;
    }

    @Override
    public DexType parsedInnerTypeName(DexType enclosingType, String name) {
      assert enclosingType.isClassType();
      String enclosingDescriptor = enclosingType.toDescriptorString();
      DexType type =
          appView
              .dexItemFactory()
              .createType(
                  getDescriptorFromClassBinaryName(
                      getClassBinaryNameFromDescriptor(enclosingDescriptor)
                          + DescriptorUtils.INNER_CLASS_SEPARATOR
                          + name));
      String enclosingRenamedBinaryName =
          getClassBinaryNameFromDescriptor(
              renaming.getOrDefault(enclosingType, enclosingType.descriptor).toString());
      type = appView.graphLense().lookupType(type);
      DexString renamedDescriptor = renaming.get(type);
      if (renamedDescriptor != null) {
        // Pick the renamed inner class from the fully renamed binary name.
        String fullRenamedBinaryName =
            getClassBinaryNameFromDescriptor(renamedDescriptor.toString());
        int innerClassPos = enclosingRenamedBinaryName.length() + 1;
        if (innerClassPos < fullRenamedBinaryName.length()) {
          renamedSignature.append(fullRenamedBinaryName.substring(innerClassPos));
        } else {
          reporter.warning(
              new StringDiagnostic(
                  "Should have retained InnerClasses attribute of " + type + ".",
                  appView.appInfo().originFor(type)));
          renamedSignature.append(name);
        }
      } else {
        // Did not find the class - keep the inner class name as is.
        // TODO(110085899): Warn about missing classes in signatures?
        renamedSignature.append(name);
      }
      return type;
    }

    @Override
    public void start() {
      renamedSignature = new StringBuilder();
    }

    @Override
    public void stop() {
      // nothing to do
    }
  }
}
