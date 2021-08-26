// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.dex;

import com.android.tools.r8.dex.VirtualFile.VirtualFileCycler;
import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.graph.DexApplication;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexProgramClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.utils.ThreadUtils;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Partition classes among dex files to limit LinearAlloc usage during DexOpt.
 *
 * This is achieved by ensuring that each class has all it's hierarchy either in the bootclasspath
 * or in the same secondary dex. This is preventing linking errors for those classes. Then if it's
 * not possible to respect this constraint for some classes, instead ensure that those classes are
 * put in a different secondary dex than all their link dependents (i.e. subclasses,
 * implementations or sub interfaces). Those classes will failed to link during DexOpt but they will
 * be loaded only once, this ensures that DexOpt will not use any extra LinearAlloc space for those
 * classes in linking error.
 */
public class InheritanceClassInDexDistributor {

  private static final Comparator<DexProgramClass> DEX_PROGRAM_CLASS_COMPARATOR =
      (a, b) -> a.type.descriptor.slowCompareTo(b.type.descriptor);

  private static final int DEX_FULL_ENOUGH_THRESHOLD = VirtualFile.MAX_ENTRIES - 100;
  private final ExecutorService executorService;

  /**
   * Group of classes.
   */
  private class ClassGroup implements Comparable<ClassGroup> {

    public final Set<DexProgramClass> members;
    public int numberOfFieldIds = -1;
    public int numberOfMethodIds = -1;
    public boolean dependsOnMainDexClasses = false;

    public ClassGroup() {
      members = new HashSet<>();
    }

    public ClassGroup(Set<DexProgramClass> members) {
      this.members = members;
      updateNumbersOfIds();
    }

    public void updateNumbersOfIds() {
      // Use a temporary VirtualFile to evaluate the number of ids in the group.
      VirtualFile virtualFile = new VirtualFile(0, namingLens);
      // Note: sort not needed.
      for (DexProgramClass clazz : members) {
        virtualFile.addClass(clazz);
      }
      numberOfFieldIds = virtualFile.getNumberOfFields();
      numberOfMethodIds = virtualFile.getNumberOfMethods();
    }

    public boolean canFitInOneDex() {
      return numberOfFieldIds < VirtualFile.MAX_ENTRIES
          && numberOfMethodIds < VirtualFile.MAX_ENTRIES;
    }

    // This is used for sorting. Compared groups must be disjoint.
    @Override
    public int compareTo(ClassGroup other) {
      assert !(
          members.isEmpty()
          || other.members.isEmpty()
          || numberOfFieldIds == -1 || numberOfMethodIds == -1);
      if (this == other) {
        return 0;
      }
      if (numberOfMethodIds != other.numberOfMethodIds) {
        return numberOfMethodIds - other.numberOfMethodIds;
      }
      if (numberOfFieldIds != other.numberOfFieldIds) {
        return numberOfFieldIds - other.numberOfFieldIds;
      }
      if (members.size() != other.members.size()) {
        return members.size() - other.members.size();
      }
      // We can end up here frequently with one element groups, but it seems very unlikely if the
      // groups grow significantly bigger.
      int result = DEX_PROGRAM_CLASS_COMPARATOR.compare(
          getSortedCopy(members).iterator().next(),
          getSortedCopy(other.members).iterator().next());
      assert result != 0;
      return result;
    }
  }

  /**
   * For {@link ClassGroup} with a dependency to the main dex classes. Allow to split the
   * members in 3 categories:
   * - Category 1: members which could go to a secondary dex altogether without facing linking
   *   error.
   * - Category 2: members which could go to the main dex without needing category 1 members to link
   * - Category 3: others members of the group, those which will fail to link unless the whole group
   *   goes into the main dex.
   */
  private class CategorizedInheritanceGroupWithMainDexDependency {

    /** Category 1. */
    final Set<DexProgramClass> mainDexIndependents = new HashSet<>();
    /** Category 2. Main dex dependents independents from mainDexIndependents elements. */
    final Set<DexProgramClass> independentsFromMainDexIndependents = new HashSet<>();
    /** Category 3. Main dex dependents also depending on some mainDexIndependents elements. */
    final Set<DexProgramClass> dependentsOfMainDexIndependents = new HashSet<>();

    CategorizedInheritanceGroupWithMainDexDependency(ClassGroup group) {

      int totalClassNumber = group.members.size();

      /**
       * Category 2 + category 3 elements. Used during construction only.
       */
      Set<DexProgramClass> mainDexDependents = new HashSet<>();
      // split group members between mainDexIndependents and mainDexDependents
      // Note: sort not needed.
      for (DexProgramClass candidate : group.members) {
        isDependingOnMainDexClass(mainDexDependents, candidate);
      }

      // split mainDexDependents members between independentsFromMainDexIndependents and
      // dependentsOfMainDexIndependents
      for (DexProgramClass candidate : mainDexDependents) {
        isDependingOnMainDexIndependents(candidate);
      }
      assert totalClassNumber ==
          mainDexIndependents.size()
              + dependentsOfMainDexIndependents.size()
              + independentsFromMainDexIndependents.size();

    }

    private boolean isDependingOnMainDexClass(Set<DexProgramClass> mainDexDependents,
        DexProgramClass dexProgramClass) {
      if (dexProgramClass == null) {
        return false;
      }

      // Think: build on one Map<dexProgramClass, Boolean> and split in a second step.
      if (mainDexIndependents.contains(dexProgramClass)) {
        return false;
      }
      if (mainDexDependents.contains(dexProgramClass)) {
        return true;
      }
      if (mainDex.classes().contains(dexProgramClass)) {
        return true;
      }
      boolean isDependent = false;
      if (isDependingOnMainDexClass(mainDexDependents,
          app.programDefinitionFor(dexProgramClass.superType))) {
        isDependent = true;
      } else {
        for (DexType interfaze : dexProgramClass.interfaces.values) {
          if (isDependingOnMainDexClass(mainDexDependents, app.programDefinitionFor(interfaze))) {
            isDependent = true;
            break;
          }
        }
      }

      if (isDependent) {
        mainDexDependents.add(dexProgramClass);
      } else {
        mainDexIndependents.add(dexProgramClass);
      }
      return isDependent;
    }


    private boolean isDependingOnMainDexIndependents(DexProgramClass dexProgramClass) {
      if (dexProgramClass == null) {
        return false;
      }

      // Think: build on one Map<dexProgramClass, Boolean> and split in a second step.
      if (independentsFromMainDexIndependents.contains(dexProgramClass)) {
        return false;
      }
      if (dependentsOfMainDexIndependents.contains(dexProgramClass)) {
        return true;
      }
      if (mainDex.classes().contains(dexProgramClass)) {
        return false;
      }
      if (mainDexIndependents.contains(dexProgramClass)) {
        return true;
      }
      boolean isDependent = false;
      if (isDependingOnMainDexIndependents(app.programDefinitionFor(dexProgramClass.superType))) {
        isDependent = true;
      } else {
        for (DexType interfaze : dexProgramClass.interfaces.values) {
          if (isDependingOnMainDexIndependents(app.programDefinitionFor(interfaze))) {
            isDependent = true;
            break;
          }
        }
      }

      if (isDependent) {
        dependentsOfMainDexIndependents.add(dexProgramClass);
      } else {
        independentsFromMainDexIndependents.add(dexProgramClass);
      }
      return isDependent;
    }
  }

  /**
   * Collect direct inheritance relation between a set of {@link DexProgramClass}.
   * This is not a general purpose tool: it's ignoring any inheritance relation with classes outside
   * of the provided set.
   */
  private static class DirectSubClassesInfo {
    /** Class or interface to direct subclasses or direct sub interfaces and implementing classes */
    private final Map<DexProgramClass, Collection<DexProgramClass>> directSubClasses;
    private final Set<DexProgramClass> classes;

    DirectSubClassesInfo(DexApplication app, Set<DexProgramClass> classes) {
      Map<DexProgramClass, Collection<DexProgramClass>> directSubClasses = new HashMap<>();
      for (DexProgramClass clazz : classes) {
        addDirectSubClass(app, classes, directSubClasses, clazz.superType, clazz);
        for (DexType interfaze : clazz.interfaces.values) {
          addDirectSubClass(app, classes, directSubClasses, interfaze, clazz);
        }
      }

      this.classes = classes;
      this.directSubClasses = directSubClasses;
    }

    Collection<DexProgramClass> getDirectSubClasses(DexProgramClass clazz) {
      assert classes.contains(clazz);
      return directSubClasses.getOrDefault(clazz, Collections.emptyList());
    }

    private static void addDirectSubClass(DexApplication app,
        Set<DexProgramClass> classes,
        Map<DexProgramClass, Collection<DexProgramClass>> directSubClasses,
        DexType superType,
        DexProgramClass clazz) {
      DexProgramClass zuper = app.programDefinitionFor(superType);
      // Don't bother collecting subclasses info that we won't use.
      if (zuper != null && classes.contains(zuper)) {
        Collection<DexProgramClass> subClasses =
            directSubClasses.computeIfAbsent(zuper, unused -> new ArrayList<>());
        subClasses.add(clazz);
      }
    }

  }

  private final VirtualFile mainDex;
  private final List<VirtualFile> dexes;
  private final BitSet fullDex = new BitSet();
  private final Set<DexProgramClass> classes;
  private final DexApplication app;
  private int dexIndexOffset;
  private final NamingLens namingLens;
  private final DirectSubClassesInfo directSubClasses;

  public InheritanceClassInDexDistributor(
      VirtualFile mainDex,
      List<VirtualFile> dexes,
      Set<DexProgramClass> classes,
      Map<DexProgramClass, String> originalNames,
      int dexIndexOffset,
      NamingLens namingLens,
      DexApplication app,
      ExecutorService executorService) {
    this.mainDex = mainDex;
    this.dexes = dexes;
    this.classes = classes;
    this.dexIndexOffset = dexIndexOffset;
    this.namingLens = namingLens;
    this.app = app;
    this.executorService = executorService;

    directSubClasses = new DirectSubClassesInfo(app, classes);
  }

  public void distribute() {
    List<ClassGroup> remainingInheritanceGroups = collectInheritanceGroups();
    // Sort to ensure reproducible allocation
    remainingInheritanceGroups.sort(null);
    // Starts with big groups since they are more likely to cause problem.
    Collections.reverse(remainingInheritanceGroups);

    // Allocate member of groups depending on
    // the main dex members
    for (Iterator<ClassGroup> iter = remainingInheritanceGroups.iterator(); iter.hasNext();) {
      ClassGroup group = iter.next();
      if (group.dependsOnMainDexClasses) {

        iter.remove();

        // Try to assign the whole group to the main dex
        if (group.canFitInOneDex()
            && !isDexFull(mainDex)
            && assignAll(mainDex, group.members)) {
          // It fitted, so work done
          continue;
        }

        // We can't put the whole group in the main dex, let's split the group to try to make the
        // best of it.
        CategorizedInheritanceGroupWithMainDexDependency groupSplit =
            new CategorizedInheritanceGroupWithMainDexDependency(group);

        // Attempt to assign the "main dex dependent classes independents from mainDexIndependents"
        // to the main dex where they can link.
        Collection<DexProgramClass> classesMissingMainDex =
            assignFromRoot(mainDex, groupSplit.independentsFromMainDexIndependents);

        // Assign mainDexIndependents classes, those can link as long as the group fit into one dex
        ClassGroup mainDexIndependentGroup =
            new ClassGroup(groupSplit.mainDexIndependents);

        Collection<VirtualFile> mainDexInpendentsDexes =
            assignGroup(mainDexIndependentGroup, Collections.singletonList(mainDex));

        Set<DexProgramClass> classesWithLinkingError =
            new HashSet<>(groupSplit.dependentsOfMainDexIndependents);
        classesWithLinkingError.addAll(classesMissingMainDex);
        assignClassesWithLinkingError(classesWithLinkingError, mainDexInpendentsDexes);
      }
    }

    // Allocate member of groups independents from the main dex members
    for (ClassGroup group : remainingInheritanceGroups) {
      if (!group.dependsOnMainDexClasses) {
        assignGroup(group, Collections.emptyList());
      }
    }
  }

  private static int getTotalClassNumber(List<ClassGroup> groups) {
    int groupClassNumber = 0;
    for (ClassGroup group : groups) {
      groupClassNumber += group.members.size();
    }
    return groupClassNumber;
  }

  private Collection<VirtualFile> assignGroup(ClassGroup group, List<VirtualFile> dexBlackList) {
    VirtualFileCycler cycler = new VirtualFileCycler(dexes, namingLens, dexIndexOffset);
    if (group.members.isEmpty()) {
      return Collections.emptyList();
    } else if (group.canFitInOneDex()) {
      VirtualFile currentDex;
      while (true) {
        currentDex = cycler.nextOrCreate(dex -> !dexBlackList.contains(dex) && !isDexFull(dex));
        if (assignAll(currentDex, group.members)) {
          break;
        }
      }
      return Collections.singletonList(currentDex);
    } else {
      // put as much as possible of the group in an empty dex.
      // About the existing dexes: only case when there can be an empty one is when this.dexes
      // contains only one empty dex created as initial dex of a minimal main dex run. So no need to
      // run through all the existing dexes to find for a possible empty candidate, if the next one
      // is not empty, no other existing dex is. This means the black list check is redundant but
      // its cost is negligible so no need to go wild on optimization and let's be safe.
      VirtualFile dexForLinkingClasses = cycler.nextOrCreate(
              dex -> dex.isEmpty() && !dexBlackList.contains(dex) && !isDexFull(dex));
      Set<DexProgramClass> remaining = assignFromRoot(dexForLinkingClasses, group.members);

      // Assign remainingclasses so that they are never in the same dex as their subclasses.
      // They will fail to link during DexOpt but they will be loaded only once.
      // For now use a "leaf layer by dex" algorithm to ensure the constrainst.
      Collection<VirtualFile> blackList = new HashSet<>(dexBlackList);
      blackList.add(dexForLinkingClasses);

      Collection<VirtualFile> usedDex = assignClassesWithLinkingError(remaining, blackList);
      usedDex.add(dexForLinkingClasses);
      return usedDex;
    }
  }

  /**
   * Assign classes so that they are never in the same dex as their subclasses.
   * They will fail to link during DexOpt but they will be loaded only once.
   * @param classes set of classes to assign, the set will be destroyed during assignment.
   */
  private Collection<VirtualFile> assignClassesWithLinkingError(
      Set<DexProgramClass> classes, Collection<VirtualFile> dexBlackList) {

    List<ClassGroup> layers = collectNoDirectInheritanceGroups(classes);

    Collections.sort(layers);

    Collection<VirtualFile> usedDex = new ArrayList<>();
    VirtualFileCycler cycler = new VirtualFileCycler(dexes, namingLens, dexIndexOffset);
    // Don't modify input dexBlackList. Think about modifying the input collection considering this
    // is private API.
    Set<VirtualFile> currentBlackList = new HashSet<>(dexBlackList);
    // Don't put class expected to fail linking in the main dex, save main dex space for classes
    // that may benefit to be in the main dex.
    currentBlackList.add(mainDex);

    for (ClassGroup group : layers) {
      cycler.reset();
      VirtualFile dexForLayer =
          cycler.nextOrCreate(dex -> !currentBlackList.contains(dex) && !isDexFull(dex));
      currentBlackList.add(dexForLayer);
      usedDex.add(dexForLayer);
      for (DexProgramClass dexProgramClass : getSortedCopy(group.members)) {
        while (true) {
          dexForLayer.addClass(dexProgramClass);
          if (dexForLayer.isFull()) {
            dexForLayer.abortTransaction();
            if (dexForLayer.isEmpty()) {
              // The class is too big to fit in one dex
              throw new CompilationError("Class '" + dexProgramClass.toSourceString()
                  + "' from " + dexProgramClass.getOrigin().toString()
                  + " is too big to fit in a dex.");
            }
            if (dexForLayer.isFull(DEX_FULL_ENOUGH_THRESHOLD)) {
              markDexFull(dexForLayer);
            }
            // Current dex is too full, continue to next dex.
            dexForLayer =
                cycler.nextOrCreate(dex -> !currentBlackList.contains(dex) && !isDexFull(dex));
            currentBlackList.add(dexForLayer);
            usedDex.add(dexForLayer);
          } else {
            dexForLayer.commitTransaction();
            break;
          }

        }
      }

    }

    return usedDex;
  }

  /**
   * Distribute given classes in groups where each group never contains 2 classes with a direct
   * inheritance relation. For example:<br>
   * I3 --> I1<br>
   * I4 --> I2<br>
   * I5 --> I1, I2<br>
   * One valid result can be {I3, I4, I5} and {I1,I2}. This method attempts to return a small number
   * of groups but does not guaranty to produce the minimal possible solution.
   */
  private List<ClassGroup> collectNoDirectInheritanceGroups(Set<DexProgramClass> classes) {

    int totalClassNumber = classes.size();
    List<DexProgramClass> sortedClasses = getTopologicalOrder(classes);
    assert sortedClasses.size() == totalClassNumber;
    // make a graph colorization starting by the end of the topological order (leafs)
    ArrayList<ClassGroup> layers = new ArrayList<>();
    Map<DexProgramClass, Integer> assignedLayer = Maps.newHashMapWithExpectedSize(classes.size());
    for (int i = sortedClasses.size() - 1; i >= 0; i--) {
      DexProgramClass toAssign = sortedClasses.get(i);

      Collection<DexProgramClass> subClasses = directSubClasses.getDirectSubClasses(toAssign);
      BitSet used = new BitSet();
      for (DexProgramClass subclass : subClasses) {
        used.set(assignedLayer.get(subclass).intValue());
      }

      // Assign the lowest available color (layer)
      int layer = used.nextClearBit(0);
      assignedLayer.put(toAssign, Integer.valueOf(layer));
      if (layers.size() == layer) {
        layers.add(new ClassGroup());
      }
      layers.get(layer).members.add(toAssign);
    }

    updateGroupsNumberOfIds(layers);

    assert totalClassNumber == getTotalClassNumber(layers);

    return layers;
  }

  /**
   * Collect groups of classes with an inheritance relation. This relation can be indirect:<br>
   * I3 --> I1<br>
   * I4 --> I2<br>
   * I5 --> I1, I2<br>
   * I3 and I4 will be in the same group even if they have no relation with each other.
   */
  private List<ClassGroup> collectInheritanceGroups() {
    // Considering classes are the nodes of a graph which edges are the inheritance relation between
    // classes. We just want to isolate every connected sub-graphs.
    // To do that we just pick one node, walk through the connected sub-graph, then repeat until we
    // have collected all nodes.

    Collection<DexProgramClass> remainingClasses = new HashSet<>(classes);
    List<ClassGroup> groups = new LinkedList<>();
    while (!remainingClasses.isEmpty()) {
      DexProgramClass clazz = remainingClasses.iterator().next();
      ClassGroup group = new ClassGroup();
      groups.add(group);
      collectGroup(remainingClasses, group, clazz);
      assert !group.members.isEmpty();
    }

    updateGroupsNumberOfIds(groups);
    assert classes.size() == getTotalClassNumber(groups);
    return groups;
  }

  private void updateGroupsNumberOfIds(List<ClassGroup> groups) {
    Collection<Future<?>> updateIdsTasks = new ArrayList<>(groups.size());
    for (ClassGroup group : groups) {
      updateIdsTasks.add(executorService.submit(() -> group.updateNumbersOfIds()));
    }
    try {
      ThreadUtils.awaitFutures(updateIdsTasks);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else if (cause instanceof Error) {
        throw (Error) cause;
      } else {
        // no checked exception thrown in task.
        throw new AssertionError(e);
      }
    }
  }

  private void collectGroup(Collection<DexProgramClass> classes, ClassGroup group,
      DexProgramClass clazz) {
    if (clazz == null) {
      return;
    }
    if (!classes.remove(clazz)) {
      if (!group.dependsOnMainDexClasses) {
        group.dependsOnMainDexClasses = mainDex.classes().contains(clazz);
      }
      // If the class is not in the classes list it's either that it's a non program class, part of
      // the main dex or it was already added to this group: so either we're not interested in the
      // class or it's dependencies and dependants are already being taken care of.
      return;
    }

    group.members.add(clazz);

    // Check dependencies are added to the group.
    collectGroup(classes, group, app.programDefinitionFor(clazz.superType));
    for (DexType interfaze : clazz.interfaces.values) {
      collectGroup(classes, group, app.programDefinitionFor(interfaze));
    }

    // Check that dependants are added to the group.
    for (DexProgramClass directSubClass : directSubClasses.getDirectSubClasses(clazz)) {
      collectGroup(classes, group, directSubClass);
    }
  }

  /**
   * Assign all given classes or none.
   * @return true if it managed to assign all the classes, false otherwise.
   */
  private boolean assignAll(VirtualFile dex, Collection<DexProgramClass> classes) {
    int totalClasses = classes.size();
    int assignedClasses = 0;
    int dexInitialSize = dex.classes().size();
    for (DexProgramClass clazz : classes) {
      dex.addClass(clazz);
      assignedClasses++;
      if (dex.isFull()) {
        dex.abortTransaction();
        if (dex.isFull(DEX_FULL_ENOUGH_THRESHOLD)) {
          markDexFull(dex);
        }
        assert dexInitialSize == dex.classes().size();
        return false;
      }
    }
    dex.commitTransaction();
    assert totalClasses == assignedClasses
      && dexInitialSize + assignedClasses == dex.classes().size();
    return true;
  }

  /**
   * Assign as many classes as possible by layer starting by roots.
   * @return the list of classes that were not assigned.
   */
  private Set<DexProgramClass> assignFromRoot(
      VirtualFile dex, Collection<DexProgramClass> classes) {

    int totalClasses = classes.size();
    int assignedClasses = 0;
    int dexInitialSize = dex.classes().size();
    boolean isLayerFullyAssigned = true;
    Set<DexProgramClass> remaining = new HashSet<>(classes);
    while (isLayerFullyAssigned && !remaining.isEmpty()) {
      Set<DexProgramClass> toProcess = remaining;
      remaining = new HashSet<>();
      boolean currentDexIsTooFull = false;
      for (DexProgramClass clazz : getSortedCopy(toProcess)) {
        if (currentDexIsTooFull || hasDirectInheritanceInCollection(clazz, toProcess)) {
          remaining.add(clazz);
        } else {
          dex.addClass(clazz);
          if (dex.isFull()) {
            dex.abortTransaction();
            if (dex.isEmpty()) {
              // The class is too big to fit in one dex
              throw new CompilationError("Class '" + clazz.toSourceString() + "' from "
                  + clazz.getOrigin().toString() + " is too big to fit in a dex.");
            }
            isLayerFullyAssigned = false;
            remaining.add(clazz);
            if (dex.isFull(DEX_FULL_ENOUGH_THRESHOLD)) {
              markDexFull(dex);
              currentDexIsTooFull = true;
            }
          } else {
            assignedClasses++;
            dex.commitTransaction();
          }
        }
      }
    }
    assert totalClasses == assignedClasses + remaining.size()
        && dexInitialSize + assignedClasses == dex.classes().size();
    return remaining;
  }

  private boolean hasDirectInheritanceInCollection(DexProgramClass clazz,
      Set<DexProgramClass> collection) {
    if (collection.contains(app.programDefinitionFor(clazz.superType))) {
      return true;
    }
    for (DexType interfaze : clazz.interfaces.values) {
      if (collection.contains(app.programDefinitionFor(interfaze))) {
        return true;
      }
    }
    return false;
  }

  private boolean hasDirectSubclassInCollection(DexProgramClass clazz,
      Set<DexProgramClass> collection) {
    for (DexProgramClass subClass : directSubClasses.getDirectSubClasses(clazz)) {
      if (collection.contains(subClass)) {
        return true;
      }
    }
    return false;
  }

  private static List<DexProgramClass> getSortedCopy(Collection<DexProgramClass> collection) {
    List<DexProgramClass> sorted = new ArrayList<>(collection);
    Collections.sort(sorted, DEX_PROGRAM_CLASS_COMPARATOR);
    return sorted;
  }

  /**
   * @param classes set of classes to sort, the set will be destroyed by sorting.
   */
  private List<DexProgramClass> getTopologicalOrder(Set<DexProgramClass> classes) {
    List<DexProgramClass> result = new ArrayList<>();
    while (!classes.isEmpty()) {
      DexProgramClass root = findOneRootInSetFrom(classes.iterator().next(), classes);
      classes.remove(root);
      result.add(root);
    }
    return result;
  }

  private DexProgramClass findOneRootInSetFrom(DexProgramClass searchFrom,
      Set<DexProgramClass> classSet) {
    DexProgramClass zuper = app.programDefinitionFor(searchFrom.superType);
    if (classSet.contains(zuper)) {
      return findOneRootInSetFrom(zuper, classSet);
    }
    for (DexType interfaceType : searchFrom.interfaces.values) {
      DexClass interfaceClass = app.definitionFor(interfaceType);
      if (classSet.contains(interfaceClass)) {
        return findOneRootInSetFrom((DexProgramClass) interfaceClass, classSet);
      }
    }
    return searchFrom;
  }

  private void markDexFull(VirtualFile dex) {
    fullDex.set(dex.getId());
  }

  private boolean isDexFull(VirtualFile dex) {
    return fullDex.get(dex.getId());
  }
}
