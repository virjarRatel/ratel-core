// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import static com.android.tools.r8.graph.UseRegistry.MethodHandleUse.ARGUMENT_TO_LAMBDA_METAFACTORY;
import static com.android.tools.r8.graph.UseRegistry.MethodHandleUse.NOT_ARGUMENT_TO_LAMBDA_METAFACTORY;
import static com.android.tools.r8.ir.code.Invoke.Type.STATIC;
import static com.android.tools.r8.ir.code.Invoke.Type.VIRTUAL;

import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.AppInfoWithSubtyping;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexItemFactory;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexMethodHandle;
import com.android.tools.r8.graph.DexMethodHandle.MethodHandleType;
import com.android.tools.r8.graph.DexProto;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.DexValue;
import com.android.tools.r8.graph.DexValue.DexValueMethodHandle;
import com.android.tools.r8.graph.DexValue.DexValueMethodType;
import com.android.tools.r8.graph.DexValue.DexValueType;
import com.android.tools.r8.graph.GraphLense;
import com.android.tools.r8.graph.GraphLense.GraphLenseLookupResult;
import com.android.tools.r8.graph.GraphLense.RewrittenPrototypeDescription;
import com.android.tools.r8.graph.GraphLense.RewrittenPrototypeDescription.RemovedArgumentsInfo;
import com.android.tools.r8.graph.UseRegistry.MethodHandleUse;
import com.android.tools.r8.graph.classmerging.VerticallyMergedClasses;
import com.android.tools.r8.ir.analysis.type.DestructivePhiTypeUpdater;
import com.android.tools.r8.ir.analysis.type.TypeLatticeElement;
import com.android.tools.r8.ir.code.BasicBlock;
import com.android.tools.r8.ir.code.CatchHandlers;
import com.android.tools.r8.ir.code.CheckCast;
import com.android.tools.r8.ir.code.ConstClass;
import com.android.tools.r8.ir.code.ConstInstruction;
import com.android.tools.r8.ir.code.ConstMethodHandle;
import com.android.tools.r8.ir.code.IRCode;
import com.android.tools.r8.ir.code.InstanceGet;
import com.android.tools.r8.ir.code.InstanceOf;
import com.android.tools.r8.ir.code.InstancePut;
import com.android.tools.r8.ir.code.Instruction;
import com.android.tools.r8.ir.code.InstructionListIterator;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.code.Invoke.Type;
import com.android.tools.r8.ir.code.InvokeCustom;
import com.android.tools.r8.ir.code.InvokeDirect;
import com.android.tools.r8.ir.code.InvokeMethod;
import com.android.tools.r8.ir.code.InvokeMultiNewArray;
import com.android.tools.r8.ir.code.InvokeNewArray;
import com.android.tools.r8.ir.code.InvokeStatic;
import com.android.tools.r8.ir.code.MoveException;
import com.android.tools.r8.ir.code.NewArrayEmpty;
import com.android.tools.r8.ir.code.NewInstance;
import com.android.tools.r8.ir.code.Phi;
import com.android.tools.r8.ir.code.StaticGet;
import com.android.tools.r8.ir.code.StaticPut;
import com.android.tools.r8.ir.code.Value;
import com.android.tools.r8.logging.Log;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class LensCodeRewriter {

  private final AppView<? extends AppInfoWithSubtyping> appView;

  private final Map<DexProto, DexProto> protoFixupCache = new ConcurrentHashMap<>();

  LensCodeRewriter(AppView<? extends AppInfoWithSubtyping> appView) {
    this.appView = appView;
  }

  private Value makeOutValue(Instruction insn, IRCode code) {
    if (insn.outValue() != null) {
      TypeLatticeElement oldType = insn.outValue().getTypeLattice();
      TypeLatticeElement newType =
          oldType.fixupClassTypeReferences(appView.graphLense()::lookupType, appView);
      return code.createValue(newType, insn.getLocalInfo());
    }
    return null;
  }

  /** Replace type appearances, invoke targets and field accesses with actual definitions. */
  public void rewrite(IRCode code, DexEncodedMethod method) {
    GraphLense graphLense = appView.graphLense();
    DexItemFactory factory = appView.dexItemFactory();
    // Rewriting types that affects phi can cause us to compute TOP for cyclic phi's. To solve this
    // we track all phi's that needs to be re-computed.
    Set<Phi> affectedPhis = Sets.newIdentityHashSet();
    ListIterator<BasicBlock> blocks = code.listIterator();
    boolean mayHaveUnreachableBlocks = false;
    while (blocks.hasNext()) {
      BasicBlock block = blocks.next();
      if (block.hasCatchHandlers() && appView.options().enableVerticalClassMerging) {
        boolean anyGuardsRenamed = block.renameGuardsInCatchHandlers(graphLense);
        if (anyGuardsRenamed) {
          mayHaveUnreachableBlocks |= unlinkDeadCatchHandlers(block);
        }
      }
      InstructionListIterator iterator = block.listIterator(code);
      while (iterator.hasNext()) {
        Instruction current = iterator.next();
        if (current.isInvokeCustom()) {
          InvokeCustom invokeCustom = current.asInvokeCustom();
          DexCallSite callSite = invokeCustom.getCallSite();
          DexCallSite newCallSite = rewriteCallSite(callSite, method);
          if (newCallSite != callSite) {
            Value newOutValue = makeOutValue(invokeCustom, code);
            InvokeCustom newInvokeCustom =
                new InvokeCustom(newCallSite, newOutValue, invokeCustom.inValues());
            iterator.replaceCurrentInstruction(newInvokeCustom);
            if (newOutValue != null
                && newOutValue.getTypeLattice() != invokeCustom.outValue().getTypeLattice()) {
              affectedPhis.addAll(newOutValue.uniquePhiUsers());
            }
          }
        } else if (current.isConstMethodHandle()) {
          DexMethodHandle handle = current.asConstMethodHandle().getValue();
          DexMethodHandle newHandle = rewriteDexMethodHandle(
              handle, method, NOT_ARGUMENT_TO_LAMBDA_METAFACTORY);
          if (newHandle != handle) {
            Value newOutValue = makeOutValue(current, code);
            iterator.replaceCurrentInstruction(new ConstMethodHandle(newOutValue, newHandle));
            if (newOutValue != null
                && newOutValue.getTypeLattice() != current.outValue().getTypeLattice()) {
              affectedPhis.addAll(newOutValue.uniquePhiUsers());
            }
          }
        } else if (current.isInvokeMethod()) {
          InvokeMethod invoke = current.asInvokeMethod();
          DexMethod invokedMethod = invoke.getInvokedMethod();
          DexType invokedHolder = invokedMethod.holder;
          if (invokedHolder.isArrayType()) {
            DexType baseType = invokedHolder.toBaseType(factory);
            new InstructionReplacer(code, current, iterator, affectedPhis)
                .replaceInstructionIfTypeChanged(
                    baseType,
                    (t, v) -> {
                      DexType mappedHolder = invokedHolder.replaceBaseType(t, factory);
                      // Just reuse proto and name, as no methods on array types cant be renamed nor
                      // change signature.
                      DexMethod actualTarget =
                          factory.createMethod(
                              mappedHolder, invokedMethod.proto, invokedMethod.name);
                      return Invoke.create(VIRTUAL, actualTarget, null, v, invoke.inValues());
                    });
            continue;
          }
          if (!invokedHolder.isClassType()) {
            assert false;
            continue;
          }
          if (invoke.isInvokeDirect()) {
            checkInvokeDirect(method.method, invoke.asInvokeDirect());
          }
          GraphLenseLookupResult lenseLookup =
              graphLense.lookupMethod(invokedMethod, method.method, invoke.getType());
          DexMethod actualTarget = lenseLookup.getMethod();
          Invoke.Type actualInvokeType = lenseLookup.getType();
          if (actualInvokeType == Type.VIRTUAL) {
            actualTarget =
                rebindVirtualInvokeToMostSpecific(
                    actualTarget, invoke.inValues().get(0), method.method.holder);
          }
          if (actualTarget != invokedMethod || invoke.getType() != actualInvokeType) {
            RewrittenPrototypeDescription prototypeChanges =
                graphLense.lookupPrototypeChanges(actualTarget);
            RemovedArgumentsInfo removedArgumentsInfo = prototypeChanges.getRemovedArgumentsInfo();

            ConstInstruction constantReturnMaterializingInstruction = null;
            if (prototypeChanges.hasBeenChangedToReturnVoid() && invoke.outValue() != null) {
              constantReturnMaterializingInstruction =
                  prototypeChanges.getConstantReturn(code, invoke.getPosition());
              if (invoke.outValue().hasLocalInfo()) {
                constantReturnMaterializingInstruction
                    .outValue()
                    .setLocalInfo(invoke.outValue().getLocalInfo());
              }
              invoke.outValue().replaceUsers(constantReturnMaterializingInstruction.outValue());
              if (graphLense.lookupType(invoke.getReturnType()) != invoke.getReturnType()) {
                affectedPhis.addAll(
                    constantReturnMaterializingInstruction.outValue().uniquePhiUsers());
              }
            }

            Value newOutValue =
                prototypeChanges.hasBeenChangedToReturnVoid() ? null : makeOutValue(invoke, code);

            List<Value> newInValues;
            if (removedArgumentsInfo.hasRemovedArguments()) {
              if (Log.ENABLED) {
                Log.info(
                    getClass(),
                    "Invoked method "
                        + invokedMethod.toSourceString()
                        + " with "
                        + removedArgumentsInfo.numberOfRemovedArguments()
                        + " arguments removed");
              }
              // Remove removed arguments from the invoke.
              newInValues = new ArrayList<>(actualTarget.proto.parameters.size());
              for (int i = 0; i < invoke.inValues().size(); i++) {
                if (!removedArgumentsInfo.isArgumentRemoved(i)) {
                  newInValues.add(invoke.inValues().get(i));
                }
              }
            } else {
              newInValues = invoke.inValues();
            }

            if (prototypeChanges.hasExtraNullParameter()) {
              iterator.previous();
              Value extraNullValue = iterator.insertConstNullInstruction(code, appView.options());
              iterator.next();
              newInValues.add(extraNullValue);
            }

            assert newInValues.size()
                == actualTarget.proto.parameters.size() + (actualInvokeType == STATIC ? 0 : 1);

            Invoke newInvoke =
                Invoke.create(actualInvokeType, actualTarget, null, newOutValue, newInValues);
            iterator.replaceCurrentInstruction(newInvoke);
            if (newOutValue != null
                && newOutValue.getTypeLattice() != current.outValue().getTypeLattice()) {
              affectedPhis.addAll(newOutValue.uniquePhiUsers());
            }

            if (constantReturnMaterializingInstruction != null) {
              if (block.hasCatchHandlers()) {
                // Split the block to ensure no instructions after throwing instructions.
                iterator
                    .split(code, blocks)
                    .listIterator(code)
                    .add(constantReturnMaterializingInstruction);
              } else {
                iterator.add(constantReturnMaterializingInstruction);
              }
            }

            DexType actualReturnType = actualTarget.proto.returnType;
            DexType expectedReturnType = graphLense.lookupType(invokedMethod.proto.returnType);
            if (newInvoke.outValue() != null && actualReturnType != expectedReturnType) {
              throw new Unreachable(
                  "Unexpected need to insert a cast. Possibly related to resolving b/79143143.\n"
                      + invokedMethod
                      + " type changed from " + expectedReturnType
                      + " to " + actualReturnType);
            }
          }
        } else if (current.isInstanceGet()) {
          InstanceGet instanceGet = current.asInstanceGet();
          DexField field = instanceGet.getField();
          DexField actualField = graphLense.lookupField(field);
          DexMethod replacementMethod =
              graphLense.lookupGetFieldForMethod(actualField, method.method);
          if (replacementMethod != null) {
            Value newOutValue = makeOutValue(current, code);
            iterator.replaceCurrentInstruction(
                new InvokeStatic(replacementMethod, newOutValue, current.inValues()));
            if (newOutValue != null
                && newOutValue.getTypeLattice() != current.outValue().getTypeLattice()) {
              affectedPhis.addAll(current.outValue().uniquePhiUsers());
            }
          } else if (actualField != field) {
            Value newOutValue = makeOutValue(instanceGet, code);
            iterator.replaceCurrentInstruction(
                new InstanceGet(newOutValue, instanceGet.object(), actualField));
            if (newOutValue != null
                && newOutValue.getTypeLattice() != current.outValue().getTypeLattice()) {
              affectedPhis.addAll(newOutValue.uniquePhiUsers());
            }
          }
        } else if (current.isInstancePut()) {
          InstancePut instancePut = current.asInstancePut();
          DexField field = instancePut.getField();
          DexField actualField = graphLense.lookupField(field);
          DexMethod replacementMethod =
              graphLense.lookupPutFieldForMethod(actualField, method.method);
          if (replacementMethod != null) {
            iterator.replaceCurrentInstruction(
                new InvokeStatic(replacementMethod, null, current.inValues()));
          } else if (actualField != field) {
            InstancePut newInstancePut =
                new InstancePut(actualField, instancePut.object(), instancePut.value());
            iterator.replaceCurrentInstruction(newInstancePut);
          }
        } else if (current.isStaticGet()) {
          StaticGet staticGet = current.asStaticGet();
          DexField field = staticGet.getField();
          DexField actualField = graphLense.lookupField(field);
          DexMethod replacementMethod =
              graphLense.lookupGetFieldForMethod(actualField, method.method);
          if (replacementMethod != null) {
            Value newOutValue = makeOutValue(current, code);
            iterator.replaceCurrentInstruction(
                new InvokeStatic(replacementMethod, newOutValue, current.inValues()));
            if (newOutValue != null
                && newOutValue.getTypeLattice() != current.outValue().getTypeLattice()) {
              affectedPhis.addAll(newOutValue.uniquePhiUsers());
            }
          } else if (actualField != field) {
            Value newOutValue = makeOutValue(staticGet, code);
            iterator.replaceCurrentInstruction(new StaticGet(newOutValue, actualField));
            if (newOutValue != null
                && newOutValue.getTypeLattice() != current.outValue().getTypeLattice()) {
              affectedPhis.addAll(newOutValue.uniquePhiUsers());
            }
          }
        } else if (current.isStaticPut()) {
          StaticPut staticPut = current.asStaticPut();
          DexField field = staticPut.getField();
          DexField actualField = graphLense.lookupField(field);
          DexMethod replacementMethod =
              graphLense.lookupPutFieldForMethod(actualField, method.method);
          if (replacementMethod != null) {
            iterator.replaceCurrentInstruction(
                new InvokeStatic(replacementMethod, current.outValue(), current.inValues()));
          } else if (actualField != field) {
            StaticPut newStaticPut = new StaticPut(staticPut.value(), actualField);
            iterator.replaceCurrentInstruction(newStaticPut);
          }
        } else if (current.isCheckCast()) {
          CheckCast checkCast = current.asCheckCast();
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(
                  checkCast.getType(), (t, v) -> new CheckCast(v, checkCast.object(), t));
        } else if (current.isConstClass()) {
          ConstClass constClass = current.asConstClass();
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(
                  constClass.getValue(), (t, v) -> new ConstClass(v, t));
        } else if (current.isInstanceOf()) {
          InstanceOf instanceOf = current.asInstanceOf();
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(
                  instanceOf.type(), (t, v) -> new InstanceOf(v, instanceOf.value(), t));
        } else if (current.isInvokeMultiNewArray()) {
          InvokeMultiNewArray multiNewArray = current.asInvokeMultiNewArray();
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(
                  multiNewArray.getArrayType(),
                  (t, v) -> new InvokeMultiNewArray(t, v, multiNewArray.inValues()));
        } else if (current.isInvokeNewArray()) {
          InvokeNewArray newArray = current.asInvokeNewArray();
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(
                  newArray.getArrayType(), (t, v) -> new InvokeNewArray(t, v, newArray.inValues()));
        } else if (current.isMoveException()) {
          MoveException moveException = current.asMoveException();
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(
                  moveException.getExceptionType(),
                  (t, v) -> new MoveException(v, t, appView.options()));
        } else if (current.isNewArrayEmpty()) {
          NewArrayEmpty newArrayEmpty = current.asNewArrayEmpty();
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(
                  newArrayEmpty.type, (t, v) -> new NewArrayEmpty(v, newArrayEmpty.size(), t));
        } else if (current.isNewInstance()) {
          DexType type = current.asNewInstance().clazz;
          new InstructionReplacer(code, current, iterator, affectedPhis)
              .replaceInstructionIfTypeChanged(type, NewInstance::new);
        } else if (current.outValue() != null) {
          // For all other instructions, substitute any changed type.
          TypeLatticeElement typeLattice = current.outValue().getTypeLattice();
          TypeLatticeElement substituted =
              typeLattice.fixupClassTypeReferences(graphLense::lookupType, appView);
          if (substituted != typeLattice) {
            current.outValue().setTypeLattice(substituted);
            affectedPhis.addAll(current.outValue().uniquePhiUsers());
          }
        }
      }
    }
    if (mayHaveUnreachableBlocks) {
      code.removeUnreachableBlocks();
    }
    if (!affectedPhis.isEmpty()) {
      new DestructivePhiTypeUpdater(appView).recomputeAndPropagateTypes(code, affectedPhis);
      assert code.verifyTypes(appView);
    }
    assert code.isConsistentSSA();
    assert code.hasNoVerticallyMergedClasses(appView);
  }

  public DexCallSite rewriteCallSite(DexCallSite callSite, DexEncodedMethod context) {
    DexItemFactory dexItemFactory = appView.dexItemFactory();
    DexProto newMethodProto =
        dexItemFactory.applyClassMappingToProto(
            callSite.methodProto, appView.graphLense()::lookupType, protoFixupCache);
    DexMethodHandle newBootstrapMethod =
        rewriteDexMethodHandle(
            callSite.bootstrapMethod, context, NOT_ARGUMENT_TO_LAMBDA_METAFACTORY);
    boolean isLambdaMetaFactory =
        dexItemFactory.isLambdaMetafactoryMethod(callSite.bootstrapMethod.asMethod());
    MethodHandleUse methodHandleUse =
        isLambdaMetaFactory ? ARGUMENT_TO_LAMBDA_METAFACTORY : NOT_ARGUMENT_TO_LAMBDA_METAFACTORY;
    List<DexValue> newArgs = rewriteBootstrapArgs(callSite.bootstrapArgs, context, methodHandleUse);
    if (!newMethodProto.equals(callSite.methodProto)
        || newBootstrapMethod != callSite.bootstrapMethod
        || !newArgs.equals(callSite.bootstrapArgs)) {
      return dexItemFactory.createCallSite(
          callSite.methodName, newMethodProto, newBootstrapMethod, newArgs);
    }
    return callSite;
  }

  // If the given invoke is on the form "invoke-direct A.<init>, v0, ..." and the definition of
  // value v0 is "new-instance v0, B", where B is a subtype of A (see the Art800 and B116282409
  // tests), then fail with a compilation error if A has previously been merged into B.
  //
  // The motivation for this is that the vertical class merger cannot easily recognize the above
  // code pattern, since it runs prior to IR construction. Therefore, we currently allow merging
  // A and B although this will lead to invalid code, because this code pattern does generally
  // not occur in practice (it leads to a verification error on the JVM, but not on Art).
  private void checkInvokeDirect(DexMethod method, InvokeDirect invoke) {
    VerticallyMergedClasses verticallyMergedClasses = appView.verticallyMergedClasses();
    if (verticallyMergedClasses == null) {
      // No need to check the invocation.
      return;
    }
    DexMethod invokedMethod = invoke.getInvokedMethod();
    if (invokedMethod.name != appView.dexItemFactory().constructorMethodName) {
      // Not a constructor call.
      return;
    }
    if (invoke.arguments().isEmpty()) {
      // The new instance should always be passed to the constructor call, but continue gracefully.
      return;
    }
    Value receiver = invoke.arguments().get(0);
    if (!receiver.isPhi() && receiver.definition.isNewInstance()) {
      NewInstance newInstance = receiver.definition.asNewInstance();
      if (newInstance.clazz != invokedMethod.holder
          && verticallyMergedClasses.hasBeenMergedIntoSubtype(invokedMethod.holder)) {
        // Generated code will not work. Fail with a compilation error.
        throw appView
            .options()
            .reporter
            .fatalError(
                String.format(
                    "Unable to rewrite `invoke-direct %s.<init>(new %s, ...)` in method `%s` after "
                        + "type `%s` was merged into `%s`. Please add the following rule to your "
                        + "Proguard configuration file: `-keep,allowobfuscation class %s`.",
                    invokedMethod.holder.toSourceString(),
                    newInstance.clazz,
                    method.toSourceString(),
                    invokedMethod.holder,
                    verticallyMergedClasses.getTargetFor(invokedMethod.holder),
                    invokedMethod.holder.toSourceString()));
      }
    }
  }

  /**
   * Due to class merging, it is possible that two exception classes have been merged into one. This
   * function removes catch handlers where the guards ended up being the same as a previous one.
   *
   * @return true if any dead catch handlers were removed.
   */
  private boolean unlinkDeadCatchHandlers(BasicBlock block) {
    assert block.hasCatchHandlers();
    CatchHandlers<BasicBlock> catchHandlers = block.getCatchHandlers();
    List<DexType> guards = catchHandlers.getGuards();
    List<BasicBlock> targets = catchHandlers.getAllTargets();

    Set<DexType> previouslySeenGuards = new HashSet<>();
    List<BasicBlock> deadCatchHandlers = new ArrayList<>();
    for (int i = 0; i < guards.size(); i++) {
      // The type may have changed due to class merging.
      DexType guard = appView.graphLense().lookupType(guards.get(i));
      boolean guardSeenBefore = !previouslySeenGuards.add(guard);
      if (guardSeenBefore) {
        deadCatchHandlers.add(targets.get(i));
      }
    }
    // Remove the guards that are guaranteed to be dead.
    for (BasicBlock deadCatchHandler : deadCatchHandlers) {
      deadCatchHandler.unlinkCatchHandler();
    }
    assert block.consistentCatchHandlers();
    return !deadCatchHandlers.isEmpty();
  }

  private List<DexValue> rewriteBootstrapArgs(
      List<DexValue> bootstrapArgs, DexEncodedMethod method, MethodHandleUse use) {
    List<DexValue> newBootstrapArgs = null;
    boolean changed = false;
    for (int i = 0; i < bootstrapArgs.size(); i++) {
      DexValue argument = bootstrapArgs.get(i);
      DexValue newArgument = null;
      if (argument instanceof DexValueMethodHandle) {
        newArgument = rewriteDexValueMethodHandle(argument.asDexValueMethodHandle(), method, use);
      } else if (argument instanceof DexValueMethodType) {
        newArgument = rewriteDexMethodType(argument.asDexValueMethodType());
      } else if (argument instanceof DexValueType) {
        DexType oldType = ((DexValueType) argument).value;
        DexType newType = appView.graphLense().lookupType(oldType);
        if (newType != oldType) {
          newArgument = new DexValueType(newType);
        }
      }
      if (newArgument != null) {
        if (newBootstrapArgs == null) {
          newBootstrapArgs = new ArrayList<>(bootstrapArgs.subList(0, i));
        }
        newBootstrapArgs.add(newArgument);
        changed = true;
      } else if (newBootstrapArgs != null) {
        newBootstrapArgs.add(argument);
      }
    }
    return changed ? newBootstrapArgs : bootstrapArgs;
  }

  private DexValueMethodHandle rewriteDexValueMethodHandle(
      DexValueMethodHandle methodHandle, DexEncodedMethod context, MethodHandleUse use) {
    DexMethodHandle oldHandle = methodHandle.value;
    DexMethodHandle newHandle = rewriteDexMethodHandle(oldHandle, context, use);
    return newHandle != oldHandle ? new DexValueMethodHandle(newHandle) : methodHandle;
  }

  private DexMethodHandle rewriteDexMethodHandle(
      DexMethodHandle methodHandle, DexEncodedMethod context, MethodHandleUse use) {
    if (methodHandle.isMethodHandle()) {
      DexMethod invokedMethod = methodHandle.asMethod();
      MethodHandleType oldType = methodHandle.type;
      GraphLenseLookupResult lenseLookup =
          appView.graphLense().lookupMethod(invokedMethod, context.method, oldType.toInvokeType());
      DexMethod rewrittenTarget = lenseLookup.getMethod();
      DexMethod actualTarget;
      MethodHandleType newType;
      if (use == ARGUMENT_TO_LAMBDA_METAFACTORY) {
        // Lambda metafactory arguments will be lambda desugared away and therefore cannot flow
        // to a MethodHandle.invokeExact call. We can therefore member-rebind with no issues.
        actualTarget = rewrittenTarget;
        newType = lenseLookup.getType().toMethodHandle(actualTarget);
      } else {
        assert use == NOT_ARGUMENT_TO_LAMBDA_METAFACTORY;
        // MethodHandles that are not arguments to a lambda metafactory will not be desugared
        // away. Therefore they could flow to a MethodHandle.invokeExact call which means that
        // we cannot member rebind. We therefore keep the receiver and also pin the receiver
        // with a keep rule (see Enqueuer.registerMethodHandle).
        actualTarget =
            appView
                .dexItemFactory()
                .createMethod(invokedMethod.holder, rewrittenTarget.proto, rewrittenTarget.name);
        newType = oldType;
        if (oldType.isInvokeDirect()) {
          // For an invoke direct, the rewritten target must have the same holder as the original.
          // If the method has changed from private to public we need to use virtual instead of
          // direct.
          assert rewrittenTarget.holder == actualTarget.holder;
          newType = lenseLookup.getType().toMethodHandle(actualTarget);
          assert newType == MethodHandleType.INVOKE_DIRECT
              || newType == MethodHandleType.INVOKE_INSTANCE;
        }
      }
      if (newType != oldType || actualTarget != invokedMethod || rewrittenTarget != actualTarget) {
        DexClass holder = appView.definitionFor(actualTarget.holder);
        boolean isInterface = holder != null ? holder.isInterface() : methodHandle.isInterface;
        return new DexMethodHandle(
            newType,
            actualTarget,
            isInterface,
            rewrittenTarget != actualTarget ? rewrittenTarget : null);
      }
    } else {
      DexField field = methodHandle.asField();
      DexField actualField = appView.graphLense().lookupField(field);
      if (actualField != field) {
        return new DexMethodHandle(methodHandle.type, actualField, methodHandle.isInterface);
      }
    }
    return methodHandle;
  }

  private DexValueMethodType rewriteDexMethodType(DexValueMethodType type) {
    DexProto oldProto = type.value;
    DexProto newProto =
        appView
            .dexItemFactory()
            .applyClassMappingToProto(oldProto, appView.graphLense()::lookupType, protoFixupCache);
    return newProto != oldProto ? new DexValueMethodType(newProto) : type;
  }

  /**
   * This rebinds invoke-virtual instructions to their most specific target.
   *
   * <p>As a simple example, consider the instruction "invoke-virtual A.foo(v0)", and assume that v0
   * is defined by an instruction "new-instance v0, B". If B is a subtype of A, and B overrides the
   * method foo(), then we rewrite the invocation into "invoke-virtual B.foo(v0)".
   *
   * <p>If A.foo() ends up being unused, this helps to ensure that we can get rid of A.foo()
   * entirely. Without this rewriting, we would have to keep A.foo() because the method is targeted.
   */
  private DexMethod rebindVirtualInvokeToMostSpecific(
      DexMethod target, Value receiver, DexType context) {
    if (!receiver.getTypeLattice().isClassType()) {
      return target;
    }
    DexEncodedMethod encodedTarget = appView.definitionFor(target);
    if (encodedTarget == null
        || !canInvokeTargetWithInvokeVirtual(encodedTarget)
        || !hasAccessToInvokeTargetFromContext(encodedTarget, context)) {
      // Don't rewrite this instruction as it could remove an error from the program.
      return target;
    }
    DexType receiverType =
        appView
            .graphLense()
            .lookupType(receiver.getTypeLattice().asClassTypeLatticeElement().getClassType());
    if (receiverType == target.holder) {
      // Virtual invoke is already as specific as it can get.
      return target;
    }
    DexEncodedMethod newTarget = appView.appInfo().lookupVirtualTarget(receiverType, target);
    if (newTarget == null || newTarget.method == target) {
      // Most likely due to a missing class, or invoke is already as specific as it gets.
      return target;
    }
    DexClass newTargetClass = appView.definitionFor(newTarget.method.holder);
    if (newTargetClass == null
        || newTargetClass.isLibraryClass()
        || !canInvokeTargetWithInvokeVirtual(newTarget)
        || !hasAccessToInvokeTargetFromContext(newTarget, context)) {
      // Not safe to invoke `newTarget` with virtual invoke from the current context.
      return target;
    }
    return newTarget.method;
  }

  private boolean canInvokeTargetWithInvokeVirtual(DexEncodedMethod target) {
    return target.isVirtualMethod() && appView.isInterface(target.method.holder).isFalse();
  }

  private boolean hasAccessToInvokeTargetFromContext(DexEncodedMethod target, DexType context) {
    assert !target.accessFlags.isPrivate();
    DexType holder = target.method.holder;
    if (holder == context) {
      // It is always safe to invoke a method from the same enclosing class.
      return true;
    }
    DexClass clazz = appView.definitionFor(holder);
    if (clazz == null) {
      // Conservatively report an illegal access.
      return false;
    }
    if (holder.isSamePackage(context)) {
      // The class must be accessible (note that we have already established that the method is not
      // private).
      return !clazz.accessFlags.isPrivate();
    }
    // If the method is in another package, then the method and its holder must be public.
    return clazz.accessFlags.isPublic() && target.accessFlags.isPublic();
  }

  class InstructionReplacer {

    private final IRCode code;
    private final Instruction current;
    private final InstructionListIterator iterator;
    private final Set<Phi> affectedPhis;

    InstructionReplacer(
        IRCode code, Instruction current, InstructionListIterator iterator, Set<Phi> affectedPhis) {
      this.code = code;
      this.current = current;
      this.iterator = iterator;
      this.affectedPhis = affectedPhis;
    }

    void replaceInstructionIfTypeChanged(
        DexType type, BiFunction<DexType, Value, Instruction> constructor) {
      DexType newType = appView.graphLense().lookupType(type);
      if (newType != type) {
        Value newOutValue = makeOutValue(current, code);
        Instruction newInstruction = constructor.apply(newType, newOutValue);
        iterator.replaceCurrentInstruction(newInstruction);
        if (newOutValue != null) {
          if (newOutValue.getTypeLattice() != current.outValue().getTypeLattice()) {
            affectedPhis.addAll(newOutValue.uniquePhiUsers());
          } else {
            assert current.hasInvariantOutType();
            assert current.isConstClass()
                || current.isInstanceOf()
                || (current.isInvokeVirtual()
                    && current.asInvokeVirtual().getInvokedMethod().holder.isArrayType());
          }
        }
      }
    }
  }
}
