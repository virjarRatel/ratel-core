// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheTable<R, C, V> extends LinkedHashMap<R, Map<C, V>> {
  private static final float LOAD_FACTOR = 0.75f;

  static class LRUCacheRow<C, V> extends LinkedHashMap<C, V> {
    private final int columnCapacity;

    public LRUCacheRow(int columnCapacity, float loadFactor) {
      super(columnCapacity, loadFactor);
      this.columnCapacity = columnCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<C, V> eldest){
      return size() > this.columnCapacity;
    }
  }

  private final int rowCapacity;
  private final int columnCapacity;

  private LRUCacheTable(int rowCapacity, int columnCapacity, float loadFactor) {
    super(rowCapacity, loadFactor);
    this.rowCapacity = rowCapacity;
    this.columnCapacity = columnCapacity;
  }

  public static <R, C, V> LRUCacheTable<R, C, V> create(int rowCapacity, int columnCapacity) {
    return new LRUCacheTable<>(rowCapacity, columnCapacity, LOAD_FACTOR);
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<R, Map<C, V>> eldest){
    return size() > this.rowCapacity;
  }

  public V put(R rowKey, C columnKey, V value) {
    Map<C, V> row = computeIfAbsent(rowKey, k -> new LRUCacheRow<>(columnCapacity, LOAD_FACTOR));
    return row.putIfAbsent(columnKey, value);
  }

  public boolean contains(R rowKey, C columnKey) {
    return getOrDefault(rowKey, ImmutableMap.of()).containsKey(columnKey);
  }

  public V get(R rowKey, C columnKey) {
    return getOrDefault(rowKey, ImmutableMap.of()).get(columnKey);
  }

  public V getOrDefault(R rowKey, C columnKey, V defaultValue) {
    return getOrDefault(rowKey, ImmutableMap.of()).getOrDefault(columnKey, defaultValue);
  }

}
