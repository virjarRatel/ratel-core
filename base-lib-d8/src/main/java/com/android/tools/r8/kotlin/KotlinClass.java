// Copyright (c) 2018, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.kotlin;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.graph.DexClass;
import com.android.tools.r8.graph.DexType;
import com.android.tools.r8.naming.NamingLens;
import com.android.tools.r8.shaking.AppInfoWithLiveness;
import com.android.tools.r8.utils.Box;
import com.android.tools.r8.utils.DescriptorUtils;
import java.util.ListIterator;
import kotlinx.metadata.KmClass;
import kotlinx.metadata.KmType;
import kotlinx.metadata.KmTypeVisitor;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;

public class KotlinClass extends KotlinInfo<KotlinClassMetadata.Class> {

  private KmClass kmClass;

  static KotlinClass fromKotlinClassMetadata(
      KotlinClassMetadata kotlinClassMetadata, DexClass clazz) {
    assert kotlinClassMetadata instanceof KotlinClassMetadata.Class;
    KotlinClassMetadata.Class kClass = (KotlinClassMetadata.Class) kotlinClassMetadata;
    return new KotlinClass(kClass, clazz);
  }

  private KotlinClass(KotlinClassMetadata.Class metadata, DexClass clazz) {
    super(metadata, clazz);
  }

  @Override
  void processMetadata() {
    assert !isProcessed;
    isProcessed = true;
    kmClass = metadata.toKmClass();
  }

  @Override
  void rewrite(AppView<AppInfoWithLiveness> appView, NamingLens lens) {
    ListIterator<KmType> superTypeIterator = kmClass.getSupertypes().listIterator();
    while (superTypeIterator.hasNext()) {
      KmType kmType = superTypeIterator.next();
      Box<Boolean> isLive = new Box<>(false);
      Box<DexType> renamed = new Box<>(null);
      kmType.accept(new KmTypeVisitor() {
        @Override
        public void visitClass(String name) {
          String descriptor = DescriptorUtils.getDescriptorFromKotlinClassifier(name);
          DexType type = appView.dexItemFactory().createType(descriptor);
          isLive.set(appView.appInfo().isLiveProgramType(type));
          DexType renamedType = lens.lookupType(type, appView.dexItemFactory());
          if (renamedType != type) {
            renamed.set(renamedType);
          }
        }
      });
      if (!isLive.get()) {
        superTypeIterator.remove();
        continue;
      }
      if (renamed.get() != null) {
        // TODO(b/70169921): need a general util to convert the current clazz's access flag.
        KmType renamedKmType = new KmType(kmType.getFlags());
        renamedKmType.visitClass(
            DescriptorUtils.descriptorToInternalName(renamed.get().toDescriptorString()));
        superTypeIterator.remove();
        superTypeIterator.add(renamedKmType);
      }
    }
  }

  @Override
  KotlinClassHeader createHeader() {
    KotlinClassMetadata.Class.Writer writer = new KotlinClassMetadata.Class.Writer();
    kmClass.accept(writer);
    return writer.write().getHeader();
  }

  @Override
  public Kind getKind() {
    return Kind.Class;
  }

  @Override
  public boolean isClass() {
    return true;
  }

  @Override
  public KotlinClass asClass() {
    return this;
  }

}
