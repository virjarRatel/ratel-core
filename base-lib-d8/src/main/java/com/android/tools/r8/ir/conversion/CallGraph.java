// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.conversion;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.ir.conversion.CallGraphBuilderBase.CycleEliminator.CycleEliminationResult;
import com.android.tools.r8.ir.conversion.CallSiteInformation.CallGraphBasedCallSiteInformation;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Call graph representation.
 *
 * <p>Each node in the graph contain the methods called and the calling methods. For virtual and
 * interface calls all potential calls from subtypes are recorded.
 *
 * <p>Only methods in the program - not library methods - are represented.
 *
 * <p>The directional edges are represented as sets of nodes in each node (called methods and
 * callees).
 *
 * <p>A call from method <code>a</code> to method <code>b</code> is only present once no matter how
 * many calls of <code>a</code> there are in <code>a</code>.
 *
 * <p>Recursive calls are not present.
 */
public class CallGraph {

  public static class Node implements Comparable<Node> {

    public static Node[] EMPTY_ARRAY = {};

    public final DexEncodedMethod method;
    private int numberOfCallSites = 0;

    // Outgoing calls from this method.
    private final Set<Node> callees = new TreeSet<>();

    // Incoming calls to this method.
    private final Set<Node> callers = new TreeSet<>();

    public Node(DexEncodedMethod method) {
      this.method = method;
    }

    public void addCallerConcurrently(Node caller) {
      addCallerConcurrently(caller, false);
    }

    public void addCallerConcurrently(Node caller, boolean likelySpuriousCallEdge) {
      if (caller != this && !likelySpuriousCallEdge) {
        synchronized (callers) {
          callers.add(caller);
          numberOfCallSites++;
        }
        synchronized (caller.callees) {
          caller.callees.add(this);
        }
      } else {
        synchronized (callers) {
          numberOfCallSites++;
        }
      }
    }

    public void removeCaller(Node caller) {
      callers.remove(caller);
      caller.callees.remove(this);
    }

    public void cleanCalleesForRemoval() {
      assert callers.isEmpty();
      for (Node callee : callees) {
        callee.callers.remove(this);
      }
    }

    public void cleanCallersForRemoval() {
      assert callees.isEmpty();
      for (Node caller : callers) {
        caller.callees.remove(this);
      }
    }

    public Set<Node> getCallersWithDeterministicOrder() {
      return callers;
    }

    public Set<Node> getCalleesWithDeterministicOrder() {
      return callees;
    }

    public int getNumberOfCallSites() {
      return numberOfCallSites;
    }

    public boolean hasCallee(Node method) {
      return callees.contains(method);
    }

    public boolean hasCaller(Node method) {
      return callers.contains(method);
    }

    public boolean isRoot() {
      return callers.isEmpty();
    }

    public boolean isLeaf() {
      return callees.isEmpty();
    }

    @Override
    public int compareTo(Node other) {
      return method.method.slowCompareTo(other.method.method);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("MethodNode for: ");
      builder.append(method.toSourceString());
      builder.append(" (");
      builder.append(callees.size());
      builder.append(" callees, ");
      builder.append(callers.size());
      builder.append(" callers");
      builder.append(", invoke count ").append(numberOfCallSites);
      builder.append(").");
      builder.append(System.lineSeparator());
      if (callees.size() > 0) {
        builder.append("Callees:");
        builder.append(System.lineSeparator());
        for (Node call : callees) {
          builder.append("  ");
          builder.append(call.method.toSourceString());
          builder.append(System.lineSeparator());
        }
      }
      if (callers.size() > 0) {
        builder.append("Callers:");
        builder.append(System.lineSeparator());
        for (Node caller : callers) {
          builder.append("  ");
          builder.append(caller.method.toSourceString());
          builder.append(System.lineSeparator());
        }
      }
      return builder.toString();
    }
  }

  final Set<Node> nodes;
  final CycleEliminationResult cycleEliminationResult;

  CallGraph(Set<Node> nodes, CycleEliminationResult cycleEliminationResult) {
    this.nodes = nodes;
    this.cycleEliminationResult = cycleEliminationResult;
  }

  static CallGraphBuilder builder(AppView<AppInfoWithLiveness> appView) {
    return new CallGraphBuilder(appView);
  }

  CallSiteInformation createCallSiteInformation(AppView<AppInfoWithLiveness> appView) {
    // Don't leverage single/dual call site information when we are not tree shaking.
    return appView.options().isShrinking()
        ? new CallGraphBasedCallSiteInformation(appView, this)
        : CallSiteInformation.empty();
  }

  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  public Set<DexEncodedMethod> extractLeaves() {
    return extractNodes(Node::isLeaf, Node::cleanCallersForRemoval);
  }

  public Set<DexEncodedMethod> extractRoots() {
    return extractNodes(Node::isRoot, Node::cleanCalleesForRemoval);
  }

  private Set<DexEncodedMethod> extractNodes(Predicate<Node> predicate, Consumer<Node> clean) {
    Set<DexEncodedMethod> result = Sets.newIdentityHashSet();
    Set<Node> removed = Sets.newIdentityHashSet();
    Iterator<Node> nodeIterator = nodes.iterator();
    while (nodeIterator.hasNext()) {
      Node node = nodeIterator.next();
      if (predicate.test(node)) {
        result.add(node.method);
        nodeIterator.remove();
        removed.add(node);
      }
    }
    removed.forEach(clean);
    return result;
  }
}
