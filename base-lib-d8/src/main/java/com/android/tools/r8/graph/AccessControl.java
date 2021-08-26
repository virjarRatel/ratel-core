// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

/**
 * Definitions of access control routines.
 *
 * <p>Follows SE 11, jvm spec, section 5.4.4 on "Access Control", except for aspects related to
 * "run-time module", for which all items are assumed to be in the same single such module.
 */
public class AccessControl {

  public static boolean isClassAccessible(DexClass clazz, DexProgramClass context) {
    if (clazz.accessFlags.isPublic()) {
      return true;
    }
    return clazz.getType().isSamePackage(context.getType());
  }

  public static boolean isMethodAccessible(
      DexEncodedMethod method,
      DexClass holder,
      DexProgramClass context,
      AppInfoWithSubtyping appInfo) {
    return isMemberAccessible(method.accessFlags, holder, context, appInfo);
  }

  public static boolean isFieldAccessible(
      DexEncodedField field,
      DexClass holder,
      DexProgramClass context,
      AppInfoWithSubtyping appInfo) {
    return isMemberAccessible(field.accessFlags, holder, context, appInfo);
  }

  private static boolean isMemberAccessible(
      AccessFlags<?> memberFlags,
      DexClass holder,
      DexProgramClass context,
      AppInfoWithSubtyping appInfo) {
    if (!isClassAccessible(holder, context)) {
      return false;
    }
    if (memberFlags.isPublic()) {
      return true;
    }
    if (memberFlags.isPrivate()) {
      return isNestMate(holder, context);
    }
    if (holder.getType().isSamePackage(context.getType())) {
      return true;
    }
    if (!memberFlags.isProtected()) {
      return false;
    }
    return appInfo.isSubtype(context.getType(), holder.getType());
  }

  private static boolean isNestMate(DexClass clazz, DexProgramClass context) {
    if (clazz == context) {
      return true;
    }
    if (!clazz.isInANest() || !context.isInANest()) {
      return false;
    }
    return clazz.getNestHost() == context.getNestHost();
  }
}
