// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.android.tools.r8.errors.CompilationError;
import com.android.tools.r8.errors.Unreachable;
import com.android.tools.r8.graph.ClassKind;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a collection of classes. Collection can be fully loaded, lazy loaded or have preloaded
 * classes along with lazy loaded content.
 *
 * The {@link #get(DexType)} operation for loading the type of a class is non-locking if a class was
 * loaded before but may block if a class has not yet been loaded.
 *
 * {@link #forceLoad(Predicate)} can be used to load all classes available from the given class
 * provider. Only after
 */
public abstract class ClassMap<T extends DexClass> {

  /**
   * For each type which has ever been queried stores one class loaded from resources provided by
   * different resource providers.
   * <p>
   * <b>NOTE:</b> mutated concurrently but we require that the value assigned to a keys never
   * changes its meaning, i.e., the supplier object might change but the contained value does not.
   * We also allow the transition from Supplier of a null value to the actual value null and vice
   * versa.
   */
  private final Map<DexType, Supplier<T>> classes;

  /**
   * Class provider if available.
   * <p>
   * If the class provider is `null` it indicates that all classes are already present in a map
   * referenced by `classes` and thus the collection is fully loaded.
   * <p>
   * <b>NOTE:</b> the field may only transition from a value to null while the this object is
   * locked. Furthermore, it may never transition back from null.
   */
  private final AtomicReference<ClassProvider<T>> classProvider = new AtomicReference<>();

  ClassMap(Map<DexType, Supplier<T>> classes, ClassProvider<T> classProvider) {
    assert classProvider == null || classProvider.getClassKind() == getClassKind();
    this.classes = classes == null ? new ConcurrentHashMap<>() : classes;
    this.classProvider.set(classProvider);
  }

  /**
   * Resolves a class conflict by selecting a class, may generate compilation error.
   */
  abstract T resolveClassConflict(T a, T b);

  /**
   * Return supplier for preloaded class.
   */
  abstract Supplier<T> getTransparentSupplier(T clazz);

  /**
   * Kind of the classes supported by this collection.
   */
  abstract ClassKind getClassKind();

  @Override
  public String toString() {
    return classes.size() + " loaded, provider: " + Objects.toString(this.classProvider.get());
  }

  /**
   * Returns a definition for a class or `null` if there is no such class in the collection.
   */
  public T get(DexType type) {
    // If this collection is fully loaded, just return the found result.
    if (classProvider.get() == null) {
      Supplier<T> supplier = classes.get(type);
      return supplier == null ? null : supplier.get();
    }

    Supplier<T> supplier = classes.get(type);
    // If we find a result, we can just return it as it won't change.
    if (supplier != null) {
      return supplier.get();
    }

    // Otherwise, we have to do the full dance with locking to avoid creating two suppliers.
    // Lock on this to ensure classProvider is not changed concurrently, so we do not create
    // a concurrent class loader with a null classProvider.
    synchronized (this) {
      supplier = classes.computeIfAbsent(type, key -> {
        // Get class supplier, create it if it does not
        // exist and the collection is NOT fully loaded.
        if (classProvider.get() == null) {
          // There is no supplier, the collection is fully loaded.
          return null;
        }

        return new ConcurrentClassLoader<>(this, classProvider.get(), type);
      });
    }

    return supplier == null ? null : supplier.get();
  }

  /**
   * Returns all classes from the collection. The collection must be force-loaded.
   */
  public List<T> getAllClasses() {
    if (classProvider.get() != null) {
      throw new Unreachable("Getting all classes from not fully loaded collection.");
    }
    List<T> loadedClasses = new ArrayList<>();
    // This is fully loaded, so the class map will no longer change.
    for (Supplier<T> supplier : classes.values()) {
      // Since the class map is fully loaded, all suppliers must be
      // loaded and non-null.
      T clazz = supplier.get();
      assert clazz != null;
      loadedClasses.add(clazz);
    }
    return loadedClasses;
  }

  public Map<DexType, T> getAllClassesInMap() {
    if (classProvider.get() != null) {
      throw new Unreachable("Getting all classes from not fully loaded collection.");
    }
    ImmutableMap.Builder<DexType, T> builder = ImmutableMap.builder();
    // This is fully loaded, so the class map will no longer change.
    for (Map.Entry<DexType, Supplier<T>> entry : classes.entrySet()) {
      builder.put(entry.getKey(), entry.getValue().get());
    }
    return builder.build();
  }

  public Iterable<DexType> getAllTypes() {
    return classes.keySet();
  }

  /**
   * Forces loading of all the classes satisfying the criteria specified.
   * <p>
   * NOTE: after this method finishes, the class map is considered to be fully-loaded and thus
   * sealed. This has one side-effect: if we filter out some of the classes with `load` predicate,
   * these classes will never be loaded.
   */
  public void forceLoad(Predicate<DexType> load) {
    Set<DexType> knownClasses;
    ClassProvider<T> classProvider;

    // Cache value of class provider, as it might change concurrently.
    if (isFullyLoaded()) {
      return;
    }
    classProvider = this.classProvider.get();

    // Collects the types which might be represented in fully loaded class map.
    knownClasses = Sets.newIdentityHashSet();
    knownClasses.addAll(classes.keySet());

    // Add all types the class provider provides. Note that it may take time for class
    // provider to collect these types, so we do it outside synchronized context.
    knownClasses.addAll(classProvider.collectTypes());

    // Make sure all the types in `knownClasses` are loaded.
    //
    // We just go and touch every class, thus triggering their loading if they
    // are not loaded so far. In case the class has already been loaded,
    // touching the class will be a no-op with minimal overhead.
    for (DexType type : knownClasses) {
      if (load.test(type)) {
        get(type);
      }
    }

    // Lock on this to prevent concurrent changes to classProvider state and to ensure that
    // only one thread proceeds to rewriting the map.
    synchronized (this) {
      if (this.classProvider.get() == null) {
        return; // Has been force-loaded concurrently.
      }

      // We avoid calling get() on a class supplier unless we know it was loaded.
      // At this time `classes` may have more types then `knownClasses`, but for
      // all extra classes we expect the supplier to return 'null' after loading.
      Iterator<Map.Entry<DexType, Supplier<T>>> iterator = classes.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<DexType, Supplier<T>> e = iterator.next();

        if (knownClasses.contains(e.getKey())) {
          // Get the class (it is expected to be loaded by this time).
          T clazz = e.getValue().get();
          if (clazz != null) {
            // Since the class is already loaded, get rid of possible wrapping suppliers.
            assert clazz.type == e.getKey();
            e.setValue(getTransparentSupplier(clazz));
            continue;
          }
        }

        // If the type is not in `knownClasses` or resolves to `null`,
        // just remove the record from the map.
        iterator.remove();
      }

      // Mark the class map as fully loaded. This has to be the last operation, as this toggles
      // the class map into fully loaded state and the get operation will no longer try to load
      // classes by blocking on 'this' and hence wait for the loading operation to finish.
      this.classProvider.set(null);
    }
  }

  public boolean isFullyLoaded() {
    return this.classProvider.get() == null;
  }

  // Supplier implementing a thread-safe loader for a class loaded from a
  // class provider. Helps avoid synchronizing on the whole class map
  // when loading a class.
  private static class ConcurrentClassLoader<T extends DexClass> implements Supplier<T> {

    private ClassMap<T> classMap;
    private ClassProvider<T> provider;
    private DexType type;

    private T clazz = null;
    private volatile boolean ready = false;

    private ConcurrentClassLoader(ClassMap<T> classMap, ClassProvider<T> provider, DexType type) {
      this.classMap = classMap;
      this.provider = provider;
      this.type = type;
    }

    @Override
    public T get() {
      if (ready) {
        return clazz;
      }

      synchronized (this) {
        if (!ready) {
          assert classMap != null && provider != null && type != null;
          provider.collectClass(type, createdClass -> {
            assert createdClass != null;
            assert classMap.getClassKind().isOfKind(createdClass);
            assert !ready;

            if (createdClass.type != type) {
              throw new CompilationError(
                  "Class content provided for type descriptor " + type.toSourceString() +
                      " actually defines class " + createdClass.type.toSourceString());
            }

            if (clazz == null) {
              clazz = createdClass;
            } else {
              // The class resolution *may* generate a compilation error as one of
              // possible resolutions. In this case we leave `value` in (false, null)
              // state so in rare case of another thread trying to get the same class
              // before this error is propagated it will get the same conflict.
              T oldClass = clazz;
              clazz = null;
              clazz = classMap.resolveClassConflict(oldClass, createdClass);
            }
          });

          classMap = null;
          provider = null;
          type = null;
          ready = true;
        }
      }

      assert ready;
      assert classMap == null && provider == null && type == null;
      return clazz;
    }
  }
}
