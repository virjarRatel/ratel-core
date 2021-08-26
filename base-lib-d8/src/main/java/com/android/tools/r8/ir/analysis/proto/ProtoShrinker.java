// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.analysis.proto;

import com.android.tools.r8.graph.AppView;
import com.android.tools.r8.ir.analysis.proto.schema.ProtoFieldTypeFactory;
import com.android.tools.r8.shaking.AppInfoWithLiveness;

public class ProtoShrinker {

  public final RawMessageInfoDecoder decoder;
  public final ProtoFieldTypeFactory factory;
  public final GeneratedExtensionRegistryShrinker generatedExtensionRegistryShrinker;
  public final GeneratedMessageLiteShrinker generatedMessageLiteShrinker;
  public final GeneratedMessageLiteBuilderShrinker generatedMessageLiteBuilderShrinker;
  public final ProtoReferences references;

  public ProtoShrinker(AppView<AppInfoWithLiveness> appView) {
    ProtoFieldTypeFactory factory = new ProtoFieldTypeFactory();
    ProtoReferences references = new ProtoReferences(appView.dexItemFactory());
    this.decoder = new RawMessageInfoDecoder(factory, references);
    this.factory = factory;
    this.generatedExtensionRegistryShrinker =
        appView.options().protoShrinking().enableGeneratedExtensionRegistryShrinking
            ? new GeneratedExtensionRegistryShrinker(appView, references)
            : null;
    this.generatedMessageLiteShrinker =
        appView.options().protoShrinking().enableGeneratedMessageLiteShrinking
            ? new GeneratedMessageLiteShrinker(appView, decoder, references)
            : null;
    this.generatedMessageLiteBuilderShrinker =
        appView.options().protoShrinking().enableGeneratedMessageLiteBuilderShrinking
            ? new GeneratedMessageLiteBuilderShrinker(references)
            : null;
    this.references = references;
  }
}
