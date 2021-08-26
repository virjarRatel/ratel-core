// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto.schema;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.analysis.EnqueuerAnalysis;
import com.android.tools.r8.ir.analysis.proto.GeneratedMessageLiteShrinker;
import com.android.tools.r8.ir.analysis.proto.ProtoEnqueuerUseRegistry;
import com.android.tools.r8.ir.analysis.proto.ProtoReferences;
import com.android.tools.r8.ir.analysis.proto.ProtoShrinker;
import com.android.tools.r8.ir.analysis.proto.RawMessageInfoDecoder;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.optimize.info.FieldOptimizationInfo;
import com.android.tools.r8.shaking.Enqueuer;
import com.android.tools.r8.shaking.EnqueuerWorklist;
import com.android.tools.r8.shaking.KeepReason;
import com.android.tools.r8.utils.BitUtils;
import com.android.tools.r8.utils.OptionalBool;
import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

// TODO(b/112437944): Handle cycles in the graph + add a test that fails with the current
//  implementation. The current caching mechanism is unsafe, because we may mark a message as not
//  containing a map/required field in presence of cycles, although it does.

// TODO(b/112437944): Handle extensions in the map/required field detection + add a test that fails
//  with the current implementation. If there is a field whose type is an extension, then we should
//  look if any of the applicable extensions could contain a map/required field.

// TODO(b/112437944): Handle incomplete information about extensions + add a test that fails with
//  the current implementation. If there are some extensions that cannot be resolved, then we should
//  keep fields that could reach extensions to be conservative.
public class ProtoEnqueuerExtension extends EnqueuerAnalysis {

  private final AppView<?> appView;
  private final RawMessageInfoDecoder decoder;
  private final ProtoFieldTypeFactory factory;
  private final ProtoReferences references;

  // Mapping for the set of proto message that have already become live.
  private final Map<DexType, ProtoMessageInfo> liveProtos = new IdentityHashMap<>();

  // Mapping for additional proto messages that have not yet become live. If there is a proto that
  // has become live, then its schema may refer to another proto message that has not yet become
  // live. In that case, we still need to decode the schema of the not-yet-live proto message,
  // because we need to check if it has a required/map field.
  private final Map<DexType, ProtoMessageInfo> seenButNotLiveProtos = new IdentityHashMap<>();

  // To cache whether a proto message contains a map/required field directly or indirectly.
  private final Map<ProtoMessageInfo, OptionalBool> reachesMapOrRequiredFieldFromMessageCache =
      new IdentityHashMap<>();

  // Keeps track of the set of dynamicMethod() methods for which we have traced const-class and
  // static-get instructions.
  private final Set<DexEncodedMethod> dynamicMethodsWithTracedProtoObjects =
      Sets.newIdentityHashSet();

  public ProtoEnqueuerExtension(AppView<?> appView) {
    ProtoShrinker protoShrinker = appView.protoShrinker();
    this.appView = appView;
    this.decoder = protoShrinker.decoder;
    this.factory = protoShrinker.factory;
    this.references = protoShrinker.references;
  }

  /**
   * When a dynamicMethod() of a proto message becomes live, then build the corresponding {@link
   * ProtoMessageInfo} object, and create a mapping from the holder to it.
   */
  @Override
  public void processNewlyLiveMethod(DexEncodedMethod encodedMethod) {
    if (!references.isDynamicMethod(encodedMethod)) {
      return;
    }

    DexType holder = encodedMethod.method.holder;
    if (seenButNotLiveProtos.containsKey(holder)) {
      // The proto is now live instead of dead.
      liveProtos.put(holder, seenButNotLiveProtos.remove(holder));
      return;
    }

    // Since this dynamicMethod() only becomes live once, and it has just become live, it must be
    // the case that the proto is not already live.
    assert !liveProtos.containsKey(holder);
    createProtoMessageInfoFromDynamicMethod(encodedMethod, liveProtos);
  }

  private void createProtoMessageInfoFromDynamicMethod(
      DexEncodedMethod dynamicMethod, Map<DexType, ProtoMessageInfo> protos) {
    DexType holder = dynamicMethod.method.holder;
    assert !protos.containsKey(holder);

    DexClass context = appView.definitionFor(holder);
    if (context == null || !context.isProgramClass()) {
      // TODO(b/112437944): What if a proto message references a proto message on the classpath or
      //  library path? We should treat them as having a map/required field to be conservative.
      assert false; // Should generally not happen.
      return;
    }

    IRCode code = dynamicMethod.buildIR(appView, context.origin);
    InvokeMethod newMessageInfoInvoke =
        GeneratedMessageLiteShrinker.getNewMessageInfoInvoke(code, references);
    ProtoMessageInfo protoMessageInfo =
        newMessageInfoInvoke != null
            ? decoder.run(dynamicMethod, context, newMessageInfoInvoke)
            : null;
    protos.put(holder, protoMessageInfo);
  }

  @Override
  public void notifyFixpoint(Enqueuer enqueuer, EnqueuerWorklist worklist) {
    markMapOrRequiredFieldsAsReachable(enqueuer, worklist);

    // The ProtoEnqueuerUseRegistry does not trace the const-class instructions in dynamicMethod().
    // Therefore, we manually trace the const-class instructions in each dynamicMethod() here,
    // ***but only those that will remain after the proto schema has been optimized***.
    if (enqueuer.getUseRegistryFactory() == ProtoEnqueuerUseRegistry.getFactory()) {
      // We only use the ProtoEnqueuerUseRegistry in the second round of tree shaking. This means
      // that the initial round of tree shaking will be less precise, but likely faster.
      assert enqueuer.getMode().isFinalTreeShaking();
      if (worklist.isEmpty()) {
        tracePendingInstructionsInDynamicMethods(enqueuer, worklist);
      }
    }
  }

  private void markMapOrRequiredFieldsAsReachable(Enqueuer enqueuer, EnqueuerWorklist worklist) {
    // TODO(b/112437944): We only need to check if a given field can reach a map/required field
    //  once. Maybe maintain a map `newlyLiveProtos` that store the set of proto messages that have
    //  become live since the last intermediate fixpoint.

    // TODO(b/112437944): We only need to visit the subset of protos in `liveProtos` that has at
    //  least one field that is not yet live. Maybe split `liveProtos` into `partiallyLiveProtos`
    //  and `fullyLiveProtos`.
    for (ProtoMessageInfo protoMessageInfo : liveProtos.values()) {
      if (protoMessageInfo == null || !protoMessageInfo.hasFields()) {
        continue;
      }

      DexEncodedMethod dynamicMethod = protoMessageInfo.getDynamicMethod();
      DexProgramClass clazz = appView.definitionFor(dynamicMethod.method.holder).asProgramClass();

      for (ProtoFieldInfo protoFieldInfo : protoMessageInfo.getFields()) {
        DexEncodedField valueStorage = protoFieldInfo.getValueStorage(appView, protoMessageInfo);
        if (valueStorage == null) {
          continue;
        }

        boolean valueStorageIsLive;
        if (enqueuer.isFieldLive(valueStorage)) {
          if (enqueuer.isFieldRead(valueStorage)
              || enqueuer.isFieldWrittenOutsideDefaultConstructor(valueStorage)
              || reachesMapOrRequiredField(protoFieldInfo)) {
            // Mark that the field is both read and written by reflection such that we do not
            // (i) optimize field reads into loading the default value of the field or (ii) remove
            // field writes to proto fields that could be read using reflection by the proto
            // library.
            enqueuer.registerFieldAccess(valueStorage.field, dynamicMethod);
          }
          valueStorageIsLive = true;
        } else if (reachesMapOrRequiredField(protoFieldInfo)) {
          // Map/required fields cannot be removed. Therefore, we mark such fields as both read and
          // written such that we cannot optimize any field reads or writes.
          enqueuer.registerFieldAccess(valueStorage.field, dynamicMethod);
          worklist.enqueueMarkReachableFieldAction(
              clazz, valueStorage, KeepReason.reflectiveUseIn(dynamicMethod));
          valueStorageIsLive = true;
        } else {
          valueStorageIsLive = false;
        }

        DexEncodedField newlyLiveField = null;
        if (valueStorageIsLive) {
          // For one-of fields, mark the corresponding one-of-case field as live, and for proto2
          // singular fields, mark the corresponding hazzer-bit field as live.
          if (protoFieldInfo.getType().isOneOf()) {
            newlyLiveField = protoFieldInfo.getOneOfCaseField(appView, protoMessageInfo);
          } else if (protoFieldInfo.hasHazzerBitField(protoMessageInfo)) {
            newlyLiveField = protoFieldInfo.getHazzerBitField(appView, protoMessageInfo);
          }
        } else {
          // For one-of fields, mark the one-of field as live if the one-of-case field is live, and
          // for proto2 singular fields, mark the field as live if the corresponding hazzer-bit
          // field is live.
          if (protoFieldInfo.getType().isOneOf()) {
            DexEncodedField oneOfCaseField =
                protoFieldInfo.getOneOfCaseField(appView, protoMessageInfo);
            if (oneOfCaseField != null && enqueuer.isFieldLive(oneOfCaseField)) {
              newlyLiveField = valueStorage;
            }
          } else if (protoFieldInfo.hasHazzerBitField(protoMessageInfo)) {
            DexEncodedField hazzerBitField =
                protoFieldInfo.getHazzerBitField(appView, protoMessageInfo);
            if (hazzerBitField == null || !enqueuer.isFieldLive(hazzerBitField)) {
              continue;
            }

            if (appView.options().enableFieldBitAccessAnalysis && appView.isAllCodeProcessed()) {
              FieldOptimizationInfo optimizationInfo = hazzerBitField.getOptimizationInfo();
              int hazzerBitIndex = protoFieldInfo.getHazzerBitFieldIndex(protoMessageInfo);
              if (!BitUtils.isBitSet(optimizationInfo.getReadBits(), hazzerBitIndex)) {
                continue;
              }
            }

            newlyLiveField = valueStorage;
          }
        }

        if (newlyLiveField != null) {
          // Mark hazzer and one-of proto fields as read from dynamicMethod() if they are written in
          // the app. This is needed to ensure that field writes are not removed from the app.
          DexEncodedMethod defaultInitializer = clazz.getDefaultInitializer();
          assert defaultInitializer != null;
          Predicate<DexEncodedMethod> neitherDefaultConstructorNorDynamicMethod =
              writer -> writer != defaultInitializer && writer != dynamicMethod;
          if (enqueuer.isFieldWrittenInMethodSatisfying(
              newlyLiveField, neitherDefaultConstructorNorDynamicMethod)) {
            enqueuer.registerFieldRead(newlyLiveField.field, dynamicMethod);
          }

          // Unconditionally register the hazzer and one-of proto fields as written from
          // dynamicMethod().
          if (enqueuer.registerFieldWrite(newlyLiveField.field, dynamicMethod)) {
            worklist.enqueueMarkReachableFieldAction(
                clazz, newlyLiveField, KeepReason.reflectiveUseIn(dynamicMethod));
          }
        }
      }

      registerWriteToOneOfObjectsWithLiveOneOfCaseObject(protoMessageInfo, enqueuer, worklist);
    }
  }

  private void tracePendingInstructionsInDynamicMethods(
      Enqueuer enqueuer, EnqueuerWorklist worklist) {
    for (ProtoMessageInfo protoMessageInfo : liveProtos.values()) {
      if (protoMessageInfo == null || !protoMessageInfo.hasFields()) {
        continue;
      }

      DexEncodedMethod dynamicMethod = protoMessageInfo.getDynamicMethod();
      if (!dynamicMethodsWithTracedProtoObjects.add(dynamicMethod)) {
        continue;
      }

      for (ProtoFieldInfo protoFieldInfo : protoMessageInfo.getFields()) {
        List<ProtoObject> objects = protoFieldInfo.getObjects();
        if (objects == null || objects.isEmpty()) {
          // Nothing to trace.
          continue;
        }

        // NOTE: If `valueStorage` is not a live field, then code for it will not be emitted in the
        // schema, and therefore we do need to trace the const-class instructions that will be
        // emitted for it.
        DexEncodedField valueStorage = protoFieldInfo.getValueStorage(appView, protoMessageInfo);
        if (valueStorage != null && enqueuer.isFieldLive(valueStorage)) {
          for (ProtoObject object : objects) {
            if (object.isProtoObjectFromStaticGet()) {
              worklist.enqueueTraceStaticFieldRead(
                  object.asProtoObjectFromStaticGet().getField(), dynamicMethod);
            } else if (object.isProtoTypeObject()) {
              worklist.enqueueTraceConstClassAction(
                  object.asProtoTypeObject().getType(), dynamicMethod);
            }
          }
        }
      }
    }
  }

  /** Marks each oneof field whose corresponding oneof-case field is live as being written. */
  private void registerWriteToOneOfObjectsWithLiveOneOfCaseObject(
      ProtoMessageInfo protoMessageInfo, Enqueuer enqueuer, EnqueuerWorklist worklist) {
    if (protoMessageInfo.numberOfOneOfObjects() == 0) {
      return;
    }

    for (ProtoOneOfObjectPair oneOfObjectPair : protoMessageInfo.getOneOfObjects()) {
      registerWriteToOneOfObjectIfOneOfCaseObjectIsLive(oneOfObjectPair, enqueuer, worklist);
    }
  }

  /** Marks the given oneof field as being written if the corresponding oneof-case field is live. */
  private void registerWriteToOneOfObjectIfOneOfCaseObjectIsLive(
      ProtoOneOfObjectPair oneOfObjectPair, Enqueuer enqueuer, EnqueuerWorklist worklist) {
    ProtoFieldObject oneOfCaseObject = oneOfObjectPair.getOneOfCaseObject();
    if (!oneOfCaseObject.isLiveProtoFieldObject()) {
      assert false;
      return;
    }

    DexField oneOfCaseField = oneOfCaseObject.asLiveProtoFieldObject().getField();
    DexEncodedField encodedOneOfCaseField = appView.appInfo().resolveField(oneOfCaseField);
    if (encodedOneOfCaseField == null) {
      assert false;
      return;
    }

    DexClass clazz = appView.definitionFor(encodedOneOfCaseField.field.holder);
    if (clazz == null || !clazz.isProgramClass()) {
      assert false;
      return;
    }

    DexEncodedMethod dynamicMethod = clazz.lookupVirtualMethod(references::isDynamicMethod);
    if (dynamicMethod == null) {
      assert false;
      return;
    }

    if (!enqueuer.isFieldLive(encodedOneOfCaseField)) {
      return;
    }

    ProtoFieldObject oneOfObject = oneOfObjectPair.getOneOfObject();
    if (!oneOfObject.isLiveProtoFieldObject()) {
      assert false;
      return;
    }

    DexField oneOfField = oneOfObject.asLiveProtoFieldObject().getField();
    DexEncodedField encodedOneOfField = appView.appInfo().resolveField(oneOfField);
    if (encodedOneOfField == null) {
      assert false;
      return;
    }

    if (encodedOneOfField.field.holder != encodedOneOfCaseField.field.holder) {
      assert false;
      return;
    }

    if (enqueuer.registerFieldWrite(encodedOneOfField.field, dynamicMethod)) {
      worklist.enqueueMarkReachableFieldAction(
          clazz.asProgramClass(), encodedOneOfField, KeepReason.reflectiveUseIn(dynamicMethod));
    }
  }

  /**
   * Traverses the proto schema graph.
   *
   * @return true if this proto field contains a map/required field directly or indirectly.
   */
  private boolean reachesMapOrRequiredField(ProtoFieldInfo protoFieldInfo) {
    ProtoFieldType protoFieldType = protoFieldInfo.getType();

    // If it is a map/required field, return true.
    if (protoFieldType.isMap() || protoFieldType.isRequired()) {
      return true;
    }

    if (!appView.options().protoShrinking().traverseOneOfAndRepeatedProtoFields) {
      if (protoFieldType.isOneOf() || protoFieldType.isRepeated()) {
        return false;
      }
    }

    // Otherwise, check if the type of the field may contain a map/required field.
    DexType baseMessageType = protoFieldInfo.getBaseMessageType(factory);
    if (baseMessageType != null) {
      ProtoMessageInfo protoMessageInfo = getOrCreateProtoMessageInfo(baseMessageType);
      assert protoMessageInfo != null;
      return reachesMapOrRequiredField(protoMessageInfo);
    }
    return false;
  }

  /**
   * Traverses the proto schema graph.
   *
   * @return true if this proto message contains a map/required field directly or indirectly.
   */
  private boolean reachesMapOrRequiredField(ProtoMessageInfo protoMessageInfo) {
    if (!protoMessageInfo.hasFields()) {
      return false;
    }
    OptionalBool cache =
        reachesMapOrRequiredFieldFromMessageCache.getOrDefault(
            protoMessageInfo, OptionalBool.unknown());
    if (!cache.isUnknown()) {
      return cache.isTrue();
    }

    // To guard against infinite recursion, we set the cache for this message to false, although
    // we may later find out that this message actually contains a map/required field.
    reachesMapOrRequiredFieldFromMessageCache.put(protoMessageInfo, OptionalBool.of(false));

    // Check if any of the fields contains a map/required field.
    for (ProtoFieldInfo protoFieldInfo : protoMessageInfo.getFields()) {
      if (reachesMapOrRequiredField(protoFieldInfo)) {
        reachesMapOrRequiredFieldFromMessageCache.put(protoMessageInfo, OptionalBool.of(true));
        return true;
      }
    }

    return false;
  }

  private ProtoMessageInfo getOrCreateProtoMessageInfo(DexType type) {
    if (liveProtos.containsKey(type)) {
      return liveProtos.get(type);
    }

    if (seenButNotLiveProtos.containsKey(type)) {
      return seenButNotLiveProtos.get(type);
    }

    DexClass clazz = appView.definitionFor(type);
    if (clazz == null || !clazz.isProgramClass()) {
      seenButNotLiveProtos.put(type, null);
      return null;
    }

    DexEncodedMethod dynamicMethod = clazz.lookupVirtualMethod(references::isDynamicMethod);
    if (dynamicMethod == null) {
      seenButNotLiveProtos.put(type, null);
      return null;
    }

    createProtoMessageInfoFromDynamicMethod(dynamicMethod, seenButNotLiveProtos);

    return seenButNotLiveProtos.get(type);
  }
}
