// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexCallSite;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexEncodedMethod;
import com.android.tools.r8.graph.DexMethod;
import com.android.tools.r8.graph.DexString;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.DisjointSets;
import com.android.tools.r8.utils.MethodJavaSignatureEquivalence;
import com.android.tools.r8.utils.MethodSignatureEquivalence;
import com.android.tools.r8.utils.Timing;
import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Assigning names to interface methods can be done in different ways, but here we try to assign the
 * same name to equivalent methods. Arguments for grouping equivalent methods is that clients will
 * work out of the box if they implement multiple interfaces and the penalty of not having more
 * locality is insignificant in DEX because the proto will only be listed once in the DEX file.
 *
 * ----------- Library -----------
 *
 * class A { }
 *
 * class Z extends A { a(); }
 *
 * ----------- Program -----------
 *
 *      interface I { x(); c() }
 *
 *          /                 \
 *         /                   \
 *        /                     \
 *       v                       v
 *
 *  interface J { b() }     interface K { d() }           interface L { b() }
 *
 * B extends A implements J { }
 *
 * C extends B implements K { }
 *
 * -keep L { *; }
 *
 * Because of the way this algorithm work, we will try to bundle the naming together into groups. In
 * the example above, the group states should identify that:
 *
 * - We are bundling J.b() with L.b() so we need to keep the name of both
 * - When giving name to I.x() or I.c() we cannot use b() because those names would collide in C.
 *
 * A further complication is that call sites can implement methods with the same name but different
 * proto's. The canonical example of this is the identity function. We compute all callsites that
 * needs to be named together by using union-find.
 *
 * A small sample of the state of the above example could be like so:
 * Group a()       -> { State(A) }
 * Group a(Object) -> { State(J), State(I), State(A) }
 * Group b()       -> { State(J), State(I), State(L), State(A) }
 * Group c()       -> { State(J), State(I), State(A) }
 *
 * Because of the frontier state computation in {@link MethodNameMinifier}, all reservations are
 * bubbled up to the library frontier and naming is top-down to not re-use the same names. The
 * {@link InterfaceMethodNameMinifier} is run after ordinary method reservation but before new
 * method name assignment. Thus each group only has to keep track of the states in the interface
 * inheritance tree and the frontiers of their implementations.
 *
 * To cache all interface reservation states we use interfaceStateMap that maps each type to its
 * {@link InterfaceReservationState} that allows for querying and updating the interface inheritance
 * tree. This caching is crucial for the time spent computing interface names because most states
 * will not have a high depth.
 *
 * We then map each group from Equivalence(Method) to {@link InterfaceMethodGroupState} that
 * maintains a collection of {@link InterfaceReservationState} for each method the group represent.
 */
class InterfaceMethodNameMinifier {

  class InterfaceReservationState {

    // Used for iterating the parent hierarchy tree.
    final DexClass iface;
    // Used for iterating the sub trees that has this node as root.
    final Set<DexType> children = new HashSet<>();
    // Collection of the frontier reservation types and the interface type itself.
    final Set<DexType> reservationTypes = new HashSet<>();

    InterfaceReservationState(DexClass iface) {
      this.iface = iface;
    }

    DexString getReservedName(DexMethod method) {
      // If an interface is kept and we are using applymapping, the renamed name for this method
      // is tracked on this level.
      if (appView.options().getProguardConfiguration().hasApplyMappingFile()) {
        DexEncodedMethod encodedMethod = appView.definitionFor(method);
        if (encodedMethod != null) {
          DexString reservedName = minifierState.getReservedName(encodedMethod, iface);
          if (reservedName != null) {
            return reservedName;
          }
        }
      }
      // Otherwise, we just search the hierarchy for the first identity reservation since
      // applymapping is no longer in effect.
      Boolean isReserved =
          forAny(
              s -> {
                for (DexType reservationType : s.reservationTypes) {
                  Set<DexString> reservedNamesFor =
                      minifierState
                          .getReservationState(reservationType)
                          .getReservedNamesFor(method);
                  assert reservedNamesFor == null || !reservedNamesFor.isEmpty();
                  if (reservedNamesFor != null && reservedNamesFor.contains(method.name)) {
                    return true;
                  }
                }
                return null;
              });
      return isReserved == null ? null : method.name;
    }

    void reserveName(DexString reservedName, DexMethod method) {
      forAll(
          s -> {
            s.reservationTypes.forEach(
                resType -> {
                  MethodReservationState<?> state = minifierState.getReservationState(resType);
                  state.reserveName(reservedName, method);
                });
          });
    }

    boolean isAvailable(DexString candidate, DexMethod method) {
      Boolean result =
          forAny(
              s -> {
                for (DexType resType : s.reservationTypes) {
                  MethodNamingState<?> state = minifierState.getNamingState(resType);
                  if (!state.isAvailable(candidate, method)) {
                    return false;
                  }
                }
                return null;
              });
      return result == null ? true : result;
    }

    void addRenaming(DexString newName, DexMethod method) {
      forAll(
          s -> {
            s.reservationTypes.forEach(
                resType -> {
                  MethodNamingState<?> state = minifierState.getNamingState(resType);
                  state.addRenaming(newName, method);
                });
          });
    }

    <T> void forAll(Consumer<InterfaceReservationState> action) {
      forAny(
          s -> {
            action.accept(s);
            return null;
          });
    }

    private <T> T forAny(Function<InterfaceReservationState, T> action) {
      T result = action.apply(this);
      if (result != null) {
        return result;
      }
      result = forChildren(action);
      if (result != null) {
        return result;
      }
      return forParents(action);
    }

    private <T> T forParents(Function<InterfaceReservationState, T> action) {
      for (DexType parent : iface.interfaces.values) {
        InterfaceReservationState parentState = interfaceStateMap.get(parent);
        if (parentState != null) {
          T returnValue = action.apply(parentState);
          if (returnValue != null) {
            return returnValue;
          }
          returnValue = parentState.forParents(action);
          if (returnValue != null) {
            return returnValue;
          }
        }
      }
      return null;
    }

    private <T> T forChildren(Function<InterfaceReservationState, T> action) {
      for (DexType child : children) {
        InterfaceReservationState childState = interfaceStateMap.get(child);
        if (childState != null) {
          T returnValue = action.apply(childState);
          if (returnValue != null) {
            return returnValue;
          }
          returnValue = childState.forChildren(action);
          if (returnValue != null) {
            return returnValue;
          }
        }
      }
      return null;
    }

    boolean containsReservation(DexType reservationType) {
      return reservationTypes.contains(reservationType);
    }
  }

  class InterfaceMethodGroupState implements Comparable<InterfaceMethodGroupState> {

    private final Set<DexCallSite> callSites = new HashSet<>();
    private final Map<DexMethod, Set<InterfaceReservationState>> methodStates = new HashMap<>();

    void addState(DexMethod method, InterfaceReservationState interfaceState) {
      methodStates.computeIfAbsent(method, m -> new HashSet<>()).add(interfaceState);
    }

    void appendMethodGroupState(InterfaceMethodGroupState state) {
      callSites.addAll(state.callSites);
      for (DexMethod key : state.methodStates.keySet()) {
        methodStates.computeIfAbsent(key, k -> new HashSet<>()).addAll(state.methodStates.get(key));
      }
    }

    void addCallSite(DexCallSite callSite) {
      // We cannot assert !callSites.contains(callSite) because the equivalence on methods
      // may group different implementations to the same InterfaceMethodGroupState.
      callSites.add(callSite);
    }

    DexString getReservedName() {
      if (methodStates.isEmpty()) {
        return null;
      }
      // It is perfectly fine to have multiple reserved names inside a group. If we have an identity
      // reservation, we have to prioritize that over the others, otherwise we just propose the
      // first ordered reserved name since we do not allow overwriting the name.
      List<DexMethod> sortedMethods = Lists.newArrayList(methodStates.keySet());
      sortedMethods.sort(DexMethod::slowCompareTo);
      DexString reservedName = null;
      for (DexMethod method : sortedMethods) {
        for (InterfaceReservationState state : methodStates.get(method)) {
          DexString stateReserved = state.getReservedName(method);
          if (stateReserved == method.name) {
            return method.name;
          } else if (stateReserved != null) {
            reservedName = stateReserved;
          }
        }
      }
      return reservedName;
    }

    void reserveName(DexString reservedName) {
      // The proposed reserved name is basically a suggestion. Try to reserve it in as many states
      // as possible.
      forEachState(
          (method, state) -> {
            DexString stateReserved = state.getReservedName(method);
            if (stateReserved != null) {
              state.reserveName(stateReserved, method);
              minifierState.putRenaming(method, stateReserved);
            } else {
              state.reserveName(reservedName, method);
              minifierState.putRenaming(method, reservedName);
            }
          });
    }

    boolean isAvailable(DexString candidate) {
      Boolean result =
          forAnyState(
              (m, s) -> {
                if (!s.isAvailable(candidate, m)) {
                  return false;
                }
                return null;
              });
      return result == null ? true : result;
    }

    void addRenaming(DexString newName, MethodNameMinifier.State minifierState) {
      forEachState(
          (m, s) -> {
            s.addRenaming(newName, m);
            minifierState.putRenaming(m, newName);
          });
    }

    void forEachState(BiConsumer<DexMethod, InterfaceReservationState> action) {
      forAnyState(
          (s, i) -> {
            action.accept(s, i);
            return null;
          });
    }

    <T> T forAnyState(BiFunction<DexMethod, InterfaceReservationState, T> callback) {
      T returnValue;
      for (Map.Entry<DexMethod, Set<InterfaceReservationState>> entry : methodStates.entrySet()) {
        for (InterfaceReservationState state : entry.getValue()) {
          returnValue = callback.apply(entry.getKey(), state);
          if (returnValue != null) {
            return returnValue;
          }
        }
      }
      return null;
    }

    boolean containsReservation(DexMethod method, DexType reservationType) {
      Set<InterfaceReservationState> states = methodStates.get(method);
      if (states != null) {
        for (InterfaceReservationState state : states) {
          if (state.containsReservation(reservationType)) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public int compareTo(InterfaceMethodGroupState o) {
      // Sort by most naming states to smallest.
      return o.methodStates.size() - methodStates.size();
    }
  }

  private final AppView<AppInfoWithLiveness> appView;
  private final Set<DexCallSite> desugaredCallSites;
  private final Equivalence<DexMethod> equivalence;
  private final MethodNameMinifier.State minifierState;

  private final Map<DexCallSite, DexString> callSiteRenamings = new IdentityHashMap<>();

  /** A map from DexMethods to all the states linked to interfaces they appear in. */
  private final Map<Wrapper<DexMethod>, InterfaceMethodGroupState> globalStateMap = new HashMap<>();

  /** A map for caching all interface states. */
  private final Map<DexType, InterfaceReservationState> interfaceStateMap = new HashMap<>();

  InterfaceMethodNameMinifier(
      AppView<AppInfoWithLiveness> appView,
      Set<DexCallSite> desugaredCallSites,
      MethodNameMinifier.State minifierState) {
    this.appView = appView;
    this.desugaredCallSites = desugaredCallSites;
    this.minifierState = minifierState;
    this.equivalence =
        appView.options().getProguardConfiguration().isOverloadAggressively()
            ? MethodSignatureEquivalence.get()
            : MethodJavaSignatureEquivalence.get();
  }

  private Comparator<Wrapper<DexMethod>> getDefaultInterfaceMethodOrdering() {
    return Comparator.comparing(globalStateMap::get);
  }

  Map<DexCallSite, DexString> getCallSiteRenamings() {
    return callSiteRenamings;
  }

  private void reserveNamesInInterfaces(Collection<DexClass> interfaces) {
    for (DexClass iface : interfaces) {
      assert iface.isInterface();
      minifierState.allocateReservationStateAndReserve(iface.type, iface.type);
      InterfaceReservationState iFaceState = new InterfaceReservationState(iface);
      iFaceState.reservationTypes.add(iface.type);
      interfaceStateMap.put(iface.type, iFaceState);
    }
  }

  void assignNamesToInterfaceMethods(Timing timing, Collection<DexClass> interfaces) {
    timing.begin("Interface minification");
    // Reserve all the names that are required for interfaces.
    timing.begin("Reserve direct and compute hierarchy");
    reserveNamesInInterfaces(interfaces);
    // Patch up root and children for all interfaces. Together with interfaceStateMap one can query
    // and update the entire tree.
    patchUpChildrenInReservationStates();
    timing.end();

    // Compute a map from method signatures to a set of naming states for interfaces and
    // frontier states of classes that implement them. We add the frontier states so that we can
    // reserve the names for later method naming.
    timing.begin("Compute map");
    computeReservationFrontiersForAllImplementingClasses();
    for (DexClass iface : interfaces) {
      InterfaceReservationState inheritanceState = interfaceStateMap.get(iface.type);
      assert inheritanceState != null;
      for (DexEncodedMethod method : iface.methods()) {
        Wrapper<DexMethod> key = equivalence.wrap(method.method);
        globalStateMap
            .computeIfAbsent(key, k -> new InterfaceMethodGroupState())
            .addState(method.method, inheritanceState);
      }
    }
    timing.end();

    // Collect the live call sites for multi-interface lambda expression renaming. For code with
    // desugared lambdas this is a conservative estimate, as we don't track if the generated
    // lambda classes survive into the output. As multi-interface lambda expressions are rare
    // this is not a big deal.
    Set<DexCallSite> liveCallSites = Sets.union(desugaredCallSites, appView.appInfo().callSites);
    // If the input program contains a multi-interface lambda expression that implements
    // interface methods with different protos, we need to make sure tha the implemented lambda
    // methods are renamed to the same name.
    // Union-find structure to keep track of methods that must be renamed together.
    // Note that if the input does not use multi-interface lambdas unificationParent will remain
    // empty.
    timing.begin("Union-find");
    DisjointSets<Wrapper<DexMethod>> unification = new DisjointSets<>();

    liveCallSites.forEach(
        callSite -> {
          Set<Wrapper<DexMethod>> callSiteMethods = new HashSet<>();
          // Don't report errors, as the set of call sites is a conservative estimate, and can
          // refer to interfaces which has been removed.
          Set<DexEncodedMethod> implementedMethods =
              appView.appInfo().lookupLambdaImplementedMethods(callSite);
          if (implementedMethods.isEmpty()) {
            return;
          }
          for (DexEncodedMethod method : implementedMethods) {
            Wrapper<DexMethod> wrapped = equivalence.wrap(method.method);
            InterfaceMethodGroupState groupState = globalStateMap.get(wrapped);
            assert groupState != null;
            groupState.addCallSite(callSite);
            callSiteMethods.add(wrapped);
          }
          if (callSiteMethods.size() > 1) {
            // Implemented interfaces have different protos. Unify them.
            Wrapper<DexMethod> mainKey = callSiteMethods.iterator().next();
            Wrapper<DexMethod> representative = unification.findOrMakeSet(mainKey);
            for (Wrapper<DexMethod> key : callSiteMethods) {
              unification.unionWithMakeSet(representative, key);
            }
          }
        });

    timing.end();

    // We now have roots for all unions. Add all of the states for the groups to the method state
    // for the unions to allow consistent naming across different protos.
    timing.begin("States for union");
    Map<Wrapper<DexMethod>, Set<Wrapper<DexMethod>>> unions = unification.collectSets();

    for (Wrapper<DexMethod> wrapped : unions.keySet()) {
      InterfaceMethodGroupState groupState = globalStateMap.get(wrapped);
      assert groupState != null;

      for (Wrapper<DexMethod> groupedMethod : unions.get(wrapped)) {
        DexMethod method = groupedMethod.get();
        assert method != null;
        groupState.appendMethodGroupState(globalStateMap.get(groupedMethod));
      }
    }
    timing.end();

    timing.begin("Sort");
    // Filter out the groups that is included both in the unification and in the map. We sort the
    // methods by the number of dependent states, so that we use short names for method that are
    // referenced in many places.
    List<Wrapper<DexMethod>> interfaceMethodGroups =
        globalStateMap.keySet().stream()
            .filter(unification::isRepresentativeOrNotPresent)
            .sorted(
                appView
                    .options()
                    .testing
                    .minifier
                    .getInterfaceMethodOrderingOrDefault(getDefaultInterfaceMethodOrdering()))
            .collect(Collectors.toList());
    timing.end();

    assert verifyAllMethodsAreRepresentedIn(interfaceMethodGroups);
    assert verifyAllCallSitesAreRepresentedIn(interfaceMethodGroups);

    timing.begin("Reserve in groups");
    // It is important that this entire phase is run before given new names, to ensure all
    // reservations are propagated to all naming states.
    List<Wrapper<DexMethod>> nonReservedMethodGroups = new ArrayList<>();
    for (Wrapper<DexMethod> interfaceMethodGroup : interfaceMethodGroups) {
      InterfaceMethodGroupState groupState = globalStateMap.get(interfaceMethodGroup);
      assert groupState != null;
      DexString reservedName = groupState.getReservedName();
      if (reservedName == null) {
        nonReservedMethodGroups.add(interfaceMethodGroup);
      } else {
        // Propagate reserved name to all states.
        groupState.reserveName(reservedName);
        for (DexCallSite callSite : groupState.callSites) {
          assert !callSiteRenamings.containsKey(callSite);
          callSiteRenamings.put(callSite, reservedName);
        }
      }
    }
    timing.end();

    timing.begin("Rename in groups");
    for (Wrapper<DexMethod> interfaceMethodGroup : nonReservedMethodGroups) {
      InterfaceMethodGroupState groupState = globalStateMap.get(interfaceMethodGroup);
      assert groupState != null;
      assert groupState.getReservedName() == null;
      DexString newName = assignNewName(interfaceMethodGroup.get(), groupState);
      assert newName != null;
      Set<String> loggingFilter = appView.options().extensiveInterfaceMethodMinifierLoggingFilter;
      if (!loggingFilter.isEmpty()) {
        Set<DexMethod> sourceMethods = groupState.methodStates.keySet();
        if (sourceMethods.stream()
            .map(DexMethod::toSourceString)
            .anyMatch(loggingFilter::contains)) {
          print(interfaceMethodGroup.get(), sourceMethods, System.out);
        }
      }
      for (DexCallSite callSite : groupState.callSites) {
        assert !callSiteRenamings.containsKey(callSite);
        callSiteRenamings.put(callSite, newName);
      }
    }
    timing.end();

    timing.end(); // end compute timing
  }

  private DexString assignNewName(DexMethod method, InterfaceMethodGroupState groupState) {
    assert groupState.getReservedName() == null;
    assert groupState.methodStates.containsKey(method);
    assert groupState.containsReservation(method, method.holder);
    MethodNamingState<?> namingState = minifierState.getNamingState(method.holder);
    // Check if the name is available in all states.
    DexString newName =
        namingState.newOrReservedNameFor(
            method, (candidate, ignore) -> groupState.isAvailable(candidate));
    groupState.addRenaming(newName, minifierState);
    return newName;
  }

  private void patchUpChildrenInReservationStates() {
    for (Map.Entry<DexType, InterfaceReservationState> entry : interfaceStateMap.entrySet()) {
      for (DexType parent : entry.getValue().iface.interfaces.values) {
        InterfaceReservationState parentState = interfaceStateMap.get(parent);
        if (parentState != null) {
          parentState.children.add(entry.getKey());
        }
      }
    }
  }

  private void computeReservationFrontiersForAllImplementingClasses() {
    for (DexClass clazz : appView.appInfo().app().asDirect().allClasses()) {
      // TODO(b/133091438): Extend the if check to test for !clazz.isLibrary().
      if (!clazz.isInterface()) {
        for (DexType directlyImplemented : appView.appInfo().implementedInterfaces(clazz.type)) {
          InterfaceReservationState iState = interfaceStateMap.get(directlyImplemented);
          if (iState != null) {
            DexType frontierType = minifierState.getFrontier(clazz.type);
            assert minifierState.getReservationState(frontierType) != null;
            iState.reservationTypes.add(frontierType);
          }
        }
      }
    }
  }

  private boolean verifyAllCallSitesAreRepresentedIn(List<Wrapper<DexMethod>> groups) {
    Set<Wrapper<DexMethod>> unifiedMethods = new HashSet<>(groups);
    Set<DexCallSite> unifiedSeen = new HashSet<>();
    Set<DexCallSite> seen = new HashSet<>();
    for (Map.Entry<Wrapper<DexMethod>, InterfaceMethodGroupState> state :
        globalStateMap.entrySet()) {
      for (DexCallSite callSite : state.getValue().callSites) {
        seen.add(callSite);
        if (unifiedMethods.contains(state.getKey())) {
          boolean added = unifiedSeen.add(callSite);
          assert added;
        }
      }
    }
    assert seen.size() == unifiedSeen.size();
    assert unifiedSeen.containsAll(seen);
    return true;
  }

  private boolean verifyAllMethodsAreRepresentedIn(List<Wrapper<DexMethod>> groups) {
    Set<Wrapper<DexMethod>> unifiedMethods = new HashSet<>(groups);
    Set<DexMethod> unifiedSeen = new HashSet<>();
    Set<DexMethod> seen = new HashSet<>();
    for (Map.Entry<Wrapper<DexMethod>, InterfaceMethodGroupState> state :
        globalStateMap.entrySet()) {
      for (DexMethod method : state.getValue().methodStates.keySet()) {
        seen.add(method);
        if (unifiedMethods.contains(state.getKey())) {
          boolean added = unifiedSeen.add(method);
          assert added;
        }
      }
    }
    assert seen.size() == unifiedSeen.size();
    assert unifiedSeen.containsAll(seen);
    return true;
  }

  private void print(DexMethod method, Set<DexMethod> sourceMethods, PrintStream out) {
    out.println("-----------------------------------------------------------------------");
    out.println("assignNameToInterfaceMethod(`" + method.toSourceString() + "`)");
    out.println("-----------------------------------------------------------------------");
    out.println("Source methods:");
    for (DexMethod sourceMethod : sourceMethods) {
      out.println("  " + sourceMethod.toSourceString());
    }
    out.println("States:");
    out.println();
  }
}
