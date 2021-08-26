// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.utils;

// Helper for collecting timing information during execution.
// Timing t = new Timing("R8");
// A timing tree is collected by calling the following pair (nesting will create the tree):
//     t.begin("My task);
//     try { ... } finally { t.end(); }
// or alternatively:
//     t.scope("My task", () -> { ... });
// Finally a report is printed by:
//     t.report();

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public class Timing {

  private final Stack<Node> stack;
  private final boolean trackMemory;

  public Timing() {
    this("<no title>");
  }

  public Timing(String title) {
    this(title, false);
  }

  public Timing(String title, boolean trackMemory) {
    this.trackMemory = trackMemory;
    stack = new Stack<>();
    stack.push(new Node("Recorded timings for " + title));
  }

  private static class MemInfo {
    final long used;

    MemInfo(long used) {
      this.used = used;
    }

    public static MemInfo fromTotalAndFree(long total, long free) {
      return new MemInfo(total - free);
    }

    long usedDelta(MemInfo previous) {
      return used - previous.used;
    }
  }

  class Node {
    final String title;

    final Map<String, Node> children = new LinkedHashMap<>();
    long duration = 0;
    long start_time;
    Map<String, MemInfo> startMemory;
    Map<String, MemInfo> endMemory;

    Node(String title) {
      this.title = title;
      if (trackMemory) {
        startMemory = computeMemoryInformation();
      }
      this.start_time = System.nanoTime();
    }

    void restart() {
      assert start_time == -1;
      if (trackMemory) {
        startMemory = computeMemoryInformation();
      }
      start_time = System.nanoTime();
    }

    void end() {
      duration += System.nanoTime() - start_time;
      start_time = -1;
      assert duration() >= 0;
      if (trackMemory) {
        endMemory = computeMemoryInformation();
      }
    }

    long duration() {
      return duration;
    }

    @Override
    public String toString() {
      return title + ": " + prettyTime(duration());
    }

    public String toString(Node top) {
      if (this == top) return toString();
      return toString() + " (" + prettyPercentage(duration(), top.duration()) + ")";
    }

    public void report(int depth, Node top) {
      assert duration() >= 0;
      if (depth > 0) {
        for (int i = 0; i < depth; i++) {
          System.out.print("  ");
        }
        System.out.print("- ");
      }
      System.out.println(toString(top));
      if (trackMemory) {
        printMemory(depth);
      }
      children.values().forEach(p -> p.report(depth + 1, top));
    }

    private void printMemory(int depth) {
      for (Entry<String, MemInfo> start : startMemory.entrySet()) {
        if (start.getKey().equals("Memory")) {
          for (int i = 0; i <= depth; i++) {
            System.out.print("  ");
          }
          MemInfo endValue = endMemory.get(start.getKey());
          MemInfo startValue = start.getValue();
          System.out.println(
              start.getKey()
                  + " start: "
                  + prettySize(startValue.used)
                  + ", end: "
                  + prettySize(endValue.used)
                  + ", delta: "
                  + prettySize(endValue.usedDelta(startValue)));
        }
      }
    }
  }

  private static String prettyPercentage(long part, long total) {
    return (part * 100 / total) + "%";
  }

  private static String prettyTime(long value) {
    return (value / 1000000) + "ms";
  }

  private static String prettySize(long value) {
    return prettyNumber(value / 1024) + "k";
  }

  private static String prettyNumber(long value) {
    String printed = "" + Math.abs(value);
    if (printed.length() < 4) {
      return "" + value;
    }
    StringBuilder builder = new StringBuilder();
    if (value < 0) {
      builder.append('-');
    }
    int prefix = printed.length() % 3;
    builder.append(printed, 0, prefix);
    for (int i = prefix; i < printed.length(); i += 3) {
      if (i > 0) {
        builder.append('.');
      }
      builder.append(printed, i, i + 3);
    }
    return builder.toString();
  }

  public void begin(String title) {
    Node parent = stack.peek();
    Node child;
    if (parent.children.containsKey(title)) {
      child = parent.children.get(title);
      child.restart();
    } else {
      child = new Node(title);
      parent.children.put(title, child);
    }
    stack.push(child);
  }

  public void end() {
    stack.peek().end();  // record time.
    stack.pop();
  }

  public void report() {
    Node top = stack.peek();
    top.end();
    System.out.println();
    top.report(0, top);
  }

  public void scope(String title, TimingScope fn) {
    begin(title);
    try {
      fn.apply();
    } finally {
      end();
    }
  }

  public interface TimingScope {
    void apply();
  }

  private Map<String, MemInfo> computeMemoryInformation() {
    System.gc();
    Map<String, MemInfo> info = new LinkedHashMap<>();
    info.put(
        "Memory",
        MemInfo.fromTotalAndFree(
            Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory()));
    return info;
  }
}
