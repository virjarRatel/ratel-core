// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.naming;

import com.android.tools.r8.naming.MemberNaming.FieldSignature;
import com.android.tools.r8.naming.MemberNaming.MethodSignature;
import com.android.tools.r8.naming.MemberNaming.Signature;
import com.android.tools.r8.naming.MemberNaming.Signature.SignatureKind;
import com.android.tools.r8.utils.ThrowingConsumer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stores name information for a class.
 * <p>
 * This includes how the class was renamed and information on the classes members.
 */
public class ClassNamingForNameMapper implements ClassNaming {

  public static class Builder extends ClassNaming.Builder {
    private final String originalName;
    private final String renamedName;
    private final Map<MethodSignature, MemberNaming> methodMembers = Maps.newHashMap();
    private final Map<FieldSignature, MemberNaming> fieldMembers = Maps.newHashMap();
    private final Map<String, List<MappedRange>> mappedRangesByName = Maps.newHashMap();
    private final Map<String, List<MemberNaming>> mappedNamingsByName = Maps.newHashMap();

    private Builder(String renamedName, String originalName) {
      this.originalName = originalName;
      this.renamedName = renamedName;
    }

    @Override
    public ClassNaming.Builder addMemberEntry(MemberNaming entry) {
      if (entry.isMethodNaming()) {
        methodMembers.put((MethodSignature) entry.getRenamedSignature(), entry);
      } else {
        fieldMembers.put((FieldSignature) entry.getRenamedSignature(), entry);
      }
      mappedNamingsByName
          .computeIfAbsent(entry.getRenamedName(), m -> new ArrayList<>())
          .add(entry);
      return this;
    }

    @Override
    public ClassNamingForNameMapper build() {
      Map<String, MappedRangesOfName> map;

      if (mappedRangesByName.isEmpty()) {
        map = Collections.emptyMap();
      } else {
        map = new HashMap<>(mappedRangesByName.size());
        for (Map.Entry<String, List<MappedRange>> entry : mappedRangesByName.entrySet()) {
          map.put(entry.getKey(), new MappedRangesOfName(entry.getValue()));
        }
      }

      return new ClassNamingForNameMapper(
          renamedName, originalName, methodMembers, fieldMembers, map, mappedNamingsByName);
    }

    /** The parameters are forwarded to MappedRange constructor, see explanation there. */
    @Override
    public void addMappedRange(
        Range minifiedRange,
        MemberNaming.MethodSignature originalSignature,
        Object originalRange,
        String renamedName) {
      mappedRangesByName
          .computeIfAbsent(renamedName, k -> new ArrayList<>())
          .add(new MappedRange(minifiedRange, originalSignature, originalRange, renamedName));
    }
  }

  /** List of MappedRanges that belong to the same renamed name. */
  public static class MappedRangesOfName {
    private final List<MappedRange> mappedRanges;

    public MappedRangesOfName(List<MappedRange> mappedRanges) {
      this.mappedRanges = mappedRanges;
    }

    /**
     * Return the first MappedRange that contains {@code line}. Return general MappedRange ("a() ->
     * b") if no concrete mapping found or null if nothing found.
     */
    public MappedRange firstRangeForLine(int line) {
      MappedRange bestRange = null;
      for (MappedRange range : mappedRanges) {
        if (range.minifiedRange == null) {
          if (bestRange == null) {
            // This is an "a() -> b" mapping (no concrete line numbers), remember this if there'll
            // be no better one.
            bestRange = range;
          }
        } else if (range.minifiedRange.contains(line)) {
          // Concrete minified range found ("x:y:a()[:u[:v]] -> b")
          return range;
        }
      }
      return bestRange;
    }

    /**
     * Search for a MappedRange where the minified range contains the specified {@code line} and
     * return that and the subsequent MappedRanges with the same minified range. Return general
     * MappedRange ("a() -> b") if no concrete mapping found or empty list if nothing found.
     */
    public List<MappedRange> allRangesForLine(int line) {
      return allRangesForLine(line, true);
    }

    /**
     * Search for a MappedRange where the minified range contains the specified {@code line} and
     * return that and the subsequent MappedRanges with the same minified range.
     *
     * @param line The line number to find the range for
     * @param takeFirstWithNoLineRange Specify if no range is found, to take a general one that.
     * @return The list with all ranges for line.
     */
    public List<MappedRange> allRangesForLine(int line, boolean takeFirstWithNoLineRange) {
      MappedRange noLineRange = null;
      for (int i = 0; i < mappedRanges.size(); ++i) {
        MappedRange rangeI = mappedRanges.get(i);
        if (rangeI.minifiedRange == null) {
          if (noLineRange == null && takeFirstWithNoLineRange) {
            // This is an "a() -> b" mapping (no concrete line numbers), remember this if there'll
            // be no better one.
            noLineRange = rangeI;
          }
        } else if (rangeI.minifiedRange.contains(line)) {
          // Concrete minified range found ("x:y:a()[:u[:v]] -> b")
          int j = i + 1;
          for (; j < mappedRanges.size(); ++j) {
            if (!Objects.equals(mappedRanges.get(j).minifiedRange, rangeI.minifiedRange)) {
              break;
            }
          }
          return mappedRanges.subList(i, j);
        }
      }
      return noLineRange == null ? Collections.emptyList() : Collections.singletonList(noLineRange);
    }

    public List<MappedRange> getMappedRanges() {
      return mappedRanges;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      MappedRangesOfName that = (MappedRangesOfName) o;

      return mappedRanges.equals(that.mappedRanges);
    }

    @Override
    public int hashCode() {
      return mappedRanges.hashCode();
    }
  }

  static Builder builder(String renamedName, String originalName) {
    return new Builder(renamedName, originalName);
  }

  public final String originalName;
  private final String renamedName;

  /**
   * Mapping from the renamed signature to the naming information for a member.
   *
   * <p>A renamed signature is a signature where the member's name has been renamed but not the type
   * information.
   */
  private final ImmutableMap<MethodSignature, MemberNaming> methodMembers;
  private final ImmutableMap<FieldSignature, MemberNaming> fieldMembers;

  /** Map of renamed name -> MappedRangesOfName */
  public final Map<String, MappedRangesOfName> mappedRangesByRenamedName;

  public final Map<String, List<MemberNaming>> mappedNamingsByName;

  private ClassNamingForNameMapper(
      String renamedName,
      String originalName,
      Map<MethodSignature, MemberNaming> methodMembers,
      Map<FieldSignature, MemberNaming> fieldMembers,
      Map<String, MappedRangesOfName> mappedRangesByRenamedName,
      Map<String, List<MemberNaming>> mappedNamingsByName) {
    this.renamedName = renamedName;
    this.originalName = originalName;
    this.methodMembers = ImmutableMap.copyOf(methodMembers);
    this.fieldMembers = ImmutableMap.copyOf(fieldMembers);
    this.mappedRangesByRenamedName = mappedRangesByRenamedName;
    this.mappedNamingsByName = mappedNamingsByName;
  }

  @Override
  public MemberNaming lookup(Signature renamedSignature) {
    if (renamedSignature.kind() == SignatureKind.METHOD) {
      assert renamedSignature instanceof MethodSignature;
      return methodMembers.get(renamedSignature);
    } else {
      assert renamedSignature.kind() == SignatureKind.FIELD;
      assert renamedSignature instanceof FieldSignature;
      return fieldMembers.get(renamedSignature);
    }
  }

  @Override
  public MemberNaming lookupByOriginalSignature(Signature original) {
    if (original.kind() == SignatureKind.METHOD) {
      for (MemberNaming memberNaming: methodMembers.values()) {
        if (memberNaming.signature.equals(original)) {
          return memberNaming;
        }
      }
      return null;
    } else {
      assert original.kind() == SignatureKind.FIELD;
      for (MemberNaming memberNaming : fieldMembers.values()) {
        if (memberNaming.signature.equals(original)) {
          return memberNaming;
        }
      }
      return null;
    }
  }

  public List<MemberNaming> lookupByOriginalName(String originalName) {
    List<MemberNaming> result = new ArrayList<>();
    for (MemberNaming naming : methodMembers.values()) {
      if (naming.signature.name.equals(originalName)) {
        result.add(naming);
      }
    }
    for (MemberNaming naming : fieldMembers.values()) {
      if (naming.signature.name.equals(originalName)) {
        result.add(naming);
      }
    }
    return result;
  }

  @Override
  public <T extends Throwable> void forAllMemberNaming(
      ThrowingConsumer<MemberNaming, T> consumer) throws T {
    forAllFieldNaming(consumer);
    forAllMethodNaming(consumer);
  }

  @Override
  public <T extends Throwable> void forAllFieldNaming(
      ThrowingConsumer<MemberNaming, T> consumer) throws T {
    for (MemberNaming naming : fieldMembers.values()) {
      consumer.accept(naming);
    }
  }

  @Override
  public <T extends Throwable> void forAllMethodNaming(
      ThrowingConsumer<MemberNaming, T> consumer) throws T {
    for (MemberNaming naming : methodMembers.values()) {
      consumer.accept(naming);
    }
  }

  void write(Writer writer) throws IOException {
    writer.append(originalName);
    writer.append(" -> ");
    writer.append(renamedName);
    writer.append(":\n");

    // First print non-method MemberNamings.
    forAllMemberNaming(
        m -> {
          if (!m.isMethodNaming()) {
            writer.append("    ").append(m.toString()).append('\n');
          }
        });

    // Sort MappedRanges by sequence number to restore construction order (original Proguard-map
    // input).
    List<MappedRange> mappedRangesSorted = new ArrayList<>();
    for (MappedRangesOfName ranges : mappedRangesByRenamedName.values()) {
      mappedRangesSorted.addAll(ranges.mappedRanges);
    }
    mappedRangesSorted.sort(Comparator.comparingInt(range -> range.sequenceNumber));
    for (MappedRange range : mappedRangesSorted) {
      writer.append("    ").append(range.toString()).append('\n');
    }
  }

  @Override
  public String toString() {
    try {
      StringWriter writer = new StringWriter();
      write(writer);
      return writer.toString();
    } catch (IOException e) {
      return e.toString();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClassNamingForNameMapper)) {
      return false;
    }

    ClassNamingForNameMapper that = (ClassNamingForNameMapper) o;

    return originalName.equals(that.originalName)
        && renamedName.equals(that.renamedName)
        && methodMembers.equals(that.methodMembers)
        && fieldMembers.equals(that.fieldMembers)
        && mappedRangesByRenamedName.equals(that.mappedRangesByRenamedName);
  }

  @Override
  public int hashCode() {
    int result = originalName.hashCode();
    result = 31 * result + renamedName.hashCode();
    result = 31 * result + methodMembers.hashCode();
    result = 31 * result + fieldMembers.hashCode();
    result = 31 * result + mappedRangesByRenamedName.hashCode();
    return result;
  }

  /**
   * MappedRange describes an (original line numbers, signature) <-> (minified line numbers)
   * mapping. It can describe 3 different things:
   *
   * <p>1. The method is renamed. The original source lines are preserved. The corresponding
   * Proguard-map syntax is "a(...) -> b"
   *
   * <p>2. The source lines of a method in the original range are renumbered to the minified range.
   * In this case the {@link MappedRange#originalRange} is either a {@code Range} or null,
   * indicating that the original range is unknown or is the same as the {@link
   * MappedRange#minifiedRange}. The corresponding Proguard-map syntax is "x:y:a(...) -> b" or
   * "x:y:a(...):u:v -> b"
   *
   * <p>3. The source line of a method is the inlining caller of the previous {@code MappedRange}.
   * In this case the {@link MappedRange@originalRange} is either an {@code int} or null, indicating
   * that the original source line is unknown, or may be identical to a line of the minified range.
   * The corresponding Proguard-map syntax is "x:y:a(...) -> b" or "x:y:a(...):u -> b"
   */
  public static class MappedRange {

    private static int nextSequenceNumber = 0;

    private synchronized int getNextSequenceNumber() {
      return nextSequenceNumber++;
    }

    public final Range minifiedRange; // Can be null, if so then originalRange must also be null.
    public final MethodSignature signature;
    public final Object originalRange; // null, Integer or Range.
    public final String renamedName;

    /**
     * The sole purpose of {@link #sequenceNumber} is to preserve the order of members read from a
     * Proguard-map.
     */
    private final int sequenceNumber = getNextSequenceNumber();

    private MappedRange(
        Range minifiedRange, MethodSignature signature, Object originalRange, String renamedName) {

      assert minifiedRange != null || originalRange == null;
      assert originalRange == null
          || originalRange instanceof Integer
          || originalRange instanceof Range;

      this.minifiedRange = minifiedRange;
      this.signature = signature;
      this.originalRange = originalRange;
      this.renamedName = renamedName;
    }

    public int getOriginalLineNumber(int lineNumberAfterMinification) {
      if (minifiedRange == null) {
        // General mapping without concrete line numbers: "a() -> b"
        return lineNumberAfterMinification;
      }
      assert minifiedRange.contains(lineNumberAfterMinification);
      if (originalRange == null) {
        // Concrete identity mapping: "x:y:a() -> b"
        return lineNumberAfterMinification;
      } else if (originalRange instanceof Integer) {
        // Inlinee: "x:y:a():u -> b"
        return (int) originalRange;
      } else {
        // "x:y:a():u:v -> b"
        assert originalRange instanceof Range;
        return ((Range) originalRange).from + lineNumberAfterMinification - minifiedRange.from;
      }
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      if (minifiedRange != null) {
        builder.append(minifiedRange).append(':');
      }
      builder.append(signature);
      if (originalRange != null && !minifiedRange.equals(originalRange)) {
        builder.append(":").append(originalRange);
      }
      builder.append(" -> ").append(renamedName);
      return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
      // sequenceNumber is intentionally omitted from equality test since it doesn't contribute to
      // identity.
      if (this == o) {
        return true;
      }
      if (!(o instanceof MappedRange)) {
        return false;
      }

      MappedRange that = (MappedRange) o;

      return Objects.equals(minifiedRange, that.minifiedRange)
          && Objects.equals(originalRange, that.originalRange)
          && signature.equals(that.signature)
          && renamedName.equals(that.renamedName);
    }

    @Override
    public int hashCode() {
      // sequenceNumber is intentionally omitted from hashCode since it's not used in equality test.
      int result = Objects.hashCode(minifiedRange);
      result = 31 * result + Objects.hashCode(originalRange);
      result = 31 * result + signature.hashCode();
      result = 31 * result + renamedName.hashCode();
      return result;
    }
  }
}

