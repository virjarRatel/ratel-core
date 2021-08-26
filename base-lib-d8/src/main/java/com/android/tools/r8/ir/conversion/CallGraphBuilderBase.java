// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedField;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexField;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.graph.GraphLense.GraphLenseLookupResult;
import com.android.tools.r8.graph.ResolutionResult;
import com.android.tools.r8.graph.UseRegistry;
import com.android.tools.r8.ir.code.Invoke;
import com.android.tools.r8.ir.conversion.CallGraph.Node;
import com.android.tools.r8.ir.conversion.CallGraphBuilderBase.CycleEliminator.CycleEliminationResult;
import com.android.tools.r8.logging.Log;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.InternalOptions;
import com.android.tools.r8.utils.SetUtils;
import com.android.tools.r8.utils.Timing;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

abstract class CallGraphBuilderBase {
  final AppView<AppInfoWithLiveness> appView;
  final Map<DexMethod, Node> nodes = new IdentityHashMap<>();
  private final Map<DexMethod, Set<DexEncodedMethod>> possibleTargetsCache =
      new ConcurrentHashMap<>();

  CallGraphBuilderBase(AppView<AppInfoWithLiveness> appView) {
    this.appView = appView;
  }

  public CallGraph build(ExecutorService executorService, Timing timing) throws ExecutionException {
    process(executorService);
    assert verifyAllMethodsWithCodeExists();

    timing.begin("Cycle elimination");
    // Sort the nodes for deterministic cycle elimination.
    Set<Node> nodesWithDeterministicOrder = Sets.newTreeSet(nodes.values());
    CycleEliminator cycleEliminator =
        new CycleEliminator(nodesWithDeterministicOrder, appView.options());
    CycleEliminationResult cycleEliminationResult = cycleEliminator.breakCycles();
    timing.end();
    assert cycleEliminator.breakCycles().numberOfRemovedEdges() == 0; // The cycles should be gone.

    return new CallGraph(nodesWithDeterministicOrder, cycleEliminationResult);
  }

  abstract void process(ExecutorService executorService) throws ExecutionException;

  Node getOrCreateNode(DexEncodedMethod method) {
    synchronized (nodes) {
      return nodes.computeIfAbsent(method.method, ignore -> new Node(method));
    }
  }

  abstract boolean verifyAllMethodsWithCodeExists();

  class InvokeExtractor extends UseRegistry {

    private final Node caller;
    private final Predicate<DexEncodedMethod> targetTester;

    InvokeExtractor(Node caller, Predicate<DexEncodedMethod> targetTester) {
      super(appView.dexItemFactory());
      this.caller = caller;
      this.targetTester = targetTester;
    }

    private void addClassInitializerTarget(DexClass clazz) {
      assert clazz != null;
      if (clazz.isProgramClass() && clazz.hasClassInitializer()) {
        addTarget(clazz.getClassInitializer(), false);
      }
    }

    private void addClassInitializerTarget(DexType type) {
      assert type.isClassType();
      DexClass clazz = appView.definitionFor(type);
      if (clazz != null) {
        addClassInitializerTarget(clazz);
      }
    }

    private void addTarget(DexEncodedMethod callee, boolean likelySpuriousCallEdge) {
      if (!targetTester.test(callee)) {
        return;
      }
      if (callee.accessFlags.isAbstract()) {
        // Not a valid target.
        return;
      }
      if (appView.appInfo().isPinned(callee.method)) {
        // Since the callee is kept, we cannot inline it into the caller, and we also cannot collect
        // any optimization info for the method. Therefore, we drop the call edge to reduce the
        // total number of call graph edges, which should lead to fewer call graph cycles.
        return;
      }
      assert callee.isProgramMethod(appView);
      getOrCreateNode(callee).addCallerConcurrently(caller, likelySpuriousCallEdge);
    }

    private void processInvoke(Invoke.Type originalType, DexMethod originalMethod) {
      DexEncodedMethod source = caller.method;
      DexMethod context = source.method;
      GraphLenseLookupResult result =
          appView.graphLense().lookupMethod(originalMethod, context, originalType);
      DexMethod method = result.getMethod();
      Invoke.Type type = result.getType();
      if (type == Invoke.Type.INTERFACE || type == Invoke.Type.VIRTUAL) {
        // For virtual and interface calls add all potential targets that could be called.
        ResolutionResult resolutionResult = appView.appInfo().resolveMethod(method.holder, method);
        DexEncodedMethod target = resolutionResult.getSingleTarget();
        if (target != null) {
          processInvokeWithDynamicDispatch(type, target);
        }
      } else {
        DexEncodedMethod singleTarget =
            appView.appInfo().lookupSingleTarget(type, method, context.holder);
        if (singleTarget != null) {
          assert !source.accessFlags.isBridge() || singleTarget != caller.method;
          DexClass clazz = appView.definitionFor(singleTarget.method.holder);
          assert clazz != null;
          if (clazz.isProgramClass()) {
            // For static invokes, the class could be initialized.
            if (type == Invoke.Type.STATIC) {
              addClassInitializerTarget(clazz);
            }
            addTarget(singleTarget, false);
          }
        }
      }
    }

    private void processInvokeWithDynamicDispatch(
        Invoke.Type type, DexEncodedMethod encodedTarget) {
      DexMethod target = encodedTarget.method;
      DexClass clazz = appView.definitionFor(target.holder);
      if (clazz == null) {
        assert false : "Unable to lookup holder of `" + target.toSourceString() + "`";
        return;
      }

      if (!appView.options().testing.addCallEdgesForLibraryInvokes) {
        if (clazz.isLibraryClass()) {
          // Likely to have many possible targets.
          return;
        }
      }

      boolean isInterface = type == Invoke.Type.INTERFACE;
      Set<DexEncodedMethod> possibleTargets =
          possibleTargetsCache.computeIfAbsent(
              target,
              method -> {
                ResolutionResult resolution =
                    appView.appInfo().resolveMethod(method.holder, method, isInterface);
                if (resolution.isValidVirtualTarget(appView.options())) {
                  return resolution.lookupVirtualDispatchTargets(isInterface, appView.appInfo());
                }
                return null;
              });
      if (possibleTargets != null) {
        boolean likelySpuriousCallEdge =
            possibleTargets.size() >= appView.options().callGraphLikelySpuriousCallEdgeThreshold;
        for (DexEncodedMethod possibleTarget : possibleTargets) {
          if (possibleTarget.isProgramMethod(appView)) {
            addTarget(possibleTarget, likelySpuriousCallEdge);
          }
        }
      }
    }

    private void processFieldAccess(DexField field) {
      // Any field access implicitly calls the class initializer.
      if (field.holder.isClassType()) {
        DexEncodedField encodedField = appView.appInfo().resolveField(field);
        if (encodedField != null && encodedField.isStatic()) {
          addClassInitializerTarget(field.holder);
        }
      }
    }

    @Override
    public boolean registerInvokeVirtual(DexMethod method) {
      processInvoke(Invoke.Type.VIRTUAL, method);
      return false;
    }

    @Override
    public boolean registerInvokeDirect(DexMethod method) {
      processInvoke(Invoke.Type.DIRECT, method);
      return false;
    }

    @Override
    public boolean registerInvokeStatic(DexMethod method) {
      processInvoke(Invoke.Type.STATIC, method);
      return false;
    }

    @Override
    public boolean registerInvokeInterface(DexMethod method) {
      processInvoke(Invoke.Type.INTERFACE, method);
      return false;
    }

    @Override
    public boolean registerInvokeSuper(DexMethod method) {
      processInvoke(Invoke.Type.SUPER, method);
      return false;
    }

    @Override
    public boolean registerInstanceFieldWrite(DexField field) {
      processFieldAccess(field);
      return false;
    }

    @Override
    public boolean registerInstanceFieldRead(DexField field) {
      processFieldAccess(field);
      return false;
    }

    @Override
    public boolean registerNewInstance(DexType type) {
      if (type.isClassType()) {
        addClassInitializerTarget(type);
      }
      return false;
    }

    @Override
    public boolean registerStaticFieldRead(DexField field) {
      processFieldAccess(field);
      return false;
    }

    @Override
    public boolean registerStaticFieldWrite(DexField field) {
      processFieldAccess(field);
      return false;
    }

    @Override
    public boolean registerTypeReference(DexType type) {
      return false;
    }

    @Override
    public void registerCallSite(DexCallSite callSite) {
      registerMethodHandle(
          callSite.bootstrapMethod, MethodHandleUse.NOT_ARGUMENT_TO_LAMBDA_METAFACTORY);
    }
  }

  static class CycleEliminator {

    static final String CYCLIC_FORCE_INLINING_MESSAGE =
        "Unable to satisfy force inlining constraints due to cyclic force inlining";

    private static class CallEdge {

      private final Node caller;
      private final Node callee;

      CallEdge(Node caller, Node callee) {
        this.caller = caller;
        this.callee = callee;
      }

      public void remove() {
        callee.removeCaller(caller);
      }
    }

    static class CycleEliminationResult {

      private Map<Node, Set<Node>> removedEdges;

      CycleEliminationResult(Map<Node, Set<Node>> removedEdges) {
        this.removedEdges = removedEdges;
      }

      void forEachRemovedCaller(Node callee, Consumer<Node> fn) {
        removedEdges.getOrDefault(callee, ImmutableSet.of()).forEach(fn);
      }

      int numberOfRemovedEdges() {
        int numberOfRemovedEdges = 0;
        for (Set<Node> nodes : removedEdges.values()) {
          numberOfRemovedEdges += nodes.size();
        }
        return numberOfRemovedEdges;
      }
    }

    private final Collection<Node> nodes;
    private final InternalOptions options;

    // DFS stack.
    private Deque<Node> stack = new ArrayDeque<>();

    // Set of nodes on the DFS stack.
    private Set<Node> stackSet = Sets.newIdentityHashSet();

    // Set of nodes that have been visited entirely.
    private Set<Node> marked = Sets.newIdentityHashSet();

    // Mapping from callee to the set of callers that were removed from the callee.
    private Map<Node, Set<Node>> removedEdges = new IdentityHashMap<>();

    private int maxDepth = 0;

    CycleEliminator(Collection<Node> nodes, InternalOptions options) {
      this.options = options;

      // Call to reorderNodes must happen after assigning options.
      this.nodes =
          options.testing.nondeterministicCycleElimination
              ? reorderNodes(new ArrayList<>(nodes))
              : nodes;
    }

    CycleEliminationResult breakCycles() {
      // Break cycles in this call graph by removing edges causing cycles.
      traverse();

      CycleEliminationResult result = new CycleEliminationResult(removedEdges);
      if (Log.ENABLED) {
        Log.info(getClass(), "# call graph cycles broken: %s", result.numberOfRemovedEdges());
        Log.info(getClass(), "# max call graph depth: %s", maxDepth);
      }
      reset();
      return result;
    }

    private void reset() {
      assert stack.isEmpty();
      assert stackSet.isEmpty();
      marked.clear();
      maxDepth = 0;
      removedEdges = new IdentityHashMap<>();
    }

    private static class WorkItem {
      boolean isNode() {
        return false;
      }

      NodeWorkItem asNode() {
        return null;
      }

      boolean isIterator() {
        return false;
      }

      IteratorWorkItem asIterator() {
        return null;
      }
    }

    private static class NodeWorkItem extends WorkItem {
      private final Node node;

      NodeWorkItem(Node node) {
        this.node = node;
      }

      @Override
      boolean isNode() {
        return true;
      }

      @Override
      NodeWorkItem asNode() {
        return this;
      }
    }

    private static class IteratorWorkItem extends WorkItem {
      private final Node caller;
      private final Iterator<Node> callees;

      IteratorWorkItem(Node caller, Iterator<Node> callees) {
        this.caller = caller;
        this.callees = callees;
      }

      @Override
      boolean isIterator() {
        return true;
      }

      @Override
      IteratorWorkItem asIterator() {
        return this;
      }
    }

    private void traverse() {
      Deque<WorkItem> workItems = new ArrayDeque<>(nodes.size());
      for (Node node : nodes) {
        workItems.addLast(new NodeWorkItem(node));
      }
      while (!workItems.isEmpty()) {
        WorkItem workItem = workItems.removeFirst();
        if (workItem.isNode()) {
          Node node = workItem.asNode().node;
          if (Log.ENABLED) {
            if (stack.size() > maxDepth) {
              maxDepth = stack.size();
            }
          }

          if (marked.contains(node)) {
            // Already visited all nodes that can be reached from this node.
            continue;
          }

          push(node);

          // The callees must be sorted before calling traverse recursively. This ensures that
          // cycles are broken the same way across multiple compilations.
          Collection<Node> callees = node.getCalleesWithDeterministicOrder();

          if (options.testing.nondeterministicCycleElimination) {
            callees = reorderNodes(new ArrayList<>(callees));
          }
          workItems.addFirst(new IteratorWorkItem(node, callees.iterator()));
        } else {
          assert workItem.isIterator();
          IteratorWorkItem iteratorWorkItem = workItem.asIterator();
          Node newCaller = iterateCallees(iteratorWorkItem.callees, iteratorWorkItem.caller);
          if (newCaller != null) {
            // We did not finish the work on this iterator, so add it again.
            workItems.addFirst(iteratorWorkItem);
            workItems.addFirst(new NodeWorkItem(newCaller));
          } else {
            assert !iteratorWorkItem.callees.hasNext();
            pop(iteratorWorkItem.caller);
            marked.add(iteratorWorkItem.caller);
          }
        }
      }
    }

    private Node iterateCallees(Iterator<Node> calleeIterator, Node node) {
      while (calleeIterator.hasNext()) {
        Node callee = calleeIterator.next();
        boolean foundCycle = stackSet.contains(callee);
        if (foundCycle) {
          // Found a cycle that needs to be eliminated.
          if (edgeRemovalIsSafe(node, callee)) {
            // Break the cycle by removing the edge node->callee.
            if (options.testing.nondeterministicCycleElimination) {
              callee.removeCaller(node);
            } else {
              // Need to remove `callee` from `node.callees` using the iterator to prevent a
              // ConcurrentModificationException. This is not needed when nondeterministic cycle
              // elimination is enabled, because we iterate a copy of `node.callees` in that case.
              calleeIterator.remove();
              callee.getCallersWithDeterministicOrder().remove(node);
            }
            recordEdgeRemoval(node, callee);

            if (Log.ENABLED) {
              Log.info(
                  CallGraph.class,
                  "Removed call edge from method '%s' to '%s'",
                  node.method.toSourceString(),
                  callee.method.toSourceString());
            }
          } else {
            assert foundCycle;

            // The cycle has a method that is marked as force inline.
            LinkedList<Node> cycle = extractCycle(callee);

            if (Log.ENABLED) {
              Log.info(
                  CallGraph.class, "Extracted cycle to find an edge that can safely be removed");
            }

            // Break the cycle by finding an edge that can be removed without breaking force
            // inlining. If that is not possible, this call fails with a compilation error.
            CallEdge edge = findCallEdgeForRemoval(cycle);

            // The edge will be null if this cycle has already been eliminated as a result of
            // another cycle elimination.
            if (edge != null) {
              assert edgeRemovalIsSafe(edge.caller, edge.callee);

              // Break the cycle by removing the edge caller->callee.
              edge.remove();
              recordEdgeRemoval(edge.caller, edge.callee);

              if (Log.ENABLED) {
                Log.info(
                    CallGraph.class,
                    "Removed call edge from force inlined method '%s' to '%s' to ensure that "
                        + "force inlining will succeed",
                    node.method.toSourceString(),
                    callee.method.toSourceString());
              }
            }

            // Recover the stack.
            recoverStack(cycle);
          }
        } else {
          return callee;
        }
      }
      return null;
    }

    private void push(Node node) {
      stack.push(node);
      boolean changed = stackSet.add(node);
      assert changed;
    }

    private void pop(Node node) {
      Node popped = stack.pop();
      assert popped == node;
      boolean changed = stackSet.remove(node);
      assert changed;
    }

    private LinkedList<Node> extractCycle(Node entry) {
      LinkedList<Node> cycle = new LinkedList<>();
      do {
        assert !stack.isEmpty();
        cycle.add(stack.pop());
      } while (cycle.getLast() != entry);
      return cycle;
    }

    private CallEdge findCallEdgeForRemoval(LinkedList<Node> extractedCycle) {
      Node callee = extractedCycle.getLast();
      for (Node caller : extractedCycle) {
        if (!caller.hasCallee(callee)) {
          // No need to break any edges since this cycle has already been broken previously.
          assert !callee.hasCaller(caller);
          return null;
        }
        if (edgeRemovalIsSafe(caller, callee)) {
          return new CallEdge(caller, callee);
        }
        callee = caller;
      }
      throw new CompilationError(CYCLIC_FORCE_INLINING_MESSAGE);
    }

    private static boolean edgeRemovalIsSafe(Node caller, Node callee) {
      // All call edges where the callee is a method that should be force inlined must be kept,
      // to guarantee that the IR converter will process the callee before the caller.
      return !callee.method.getOptimizationInfo().forceInline();
    }

    private void recordEdgeRemoval(Node caller, Node callee) {
      removedEdges.computeIfAbsent(callee, ignore -> SetUtils.newIdentityHashSet(2)).add(caller);
    }

    private void recoverStack(LinkedList<Node> extractedCycle) {
      Iterator<Node> descendingIt = extractedCycle.descendingIterator();
      while (descendingIt.hasNext()) {
        stack.push(descendingIt.next());
      }
    }

    private Collection<Node> reorderNodes(List<Node> nodes) {
      assert options.testing.nondeterministicCycleElimination;
      if (!InternalOptions.DETERMINISTIC_DEBUGGING) {
        Collections.shuffle(nodes);
      }
      return nodes;
    }
  }
}
