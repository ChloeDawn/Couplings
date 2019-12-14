/*
 * Copyright (C) 2019 InsomniaKitten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.chloedawn.couplings;

import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Set;

public final class CouplingsMixins implements IMixinConfigPlugin {
  private static final String MIXIN_PACKAGE = "io.github.chloedawn.couplings.mixin";

  private static final String DOOR_ACCESSOR = "DoorAccessor";
  private static final String DOOR_MIXIN = "DoorMixin";
  private static final String FENCE_GATE_MIXIN = "FenceGateMixin";
  private static final String TRAPDOOR_MIXIN = "TrapdoorMixin";

  @Override
  public void onLoad(final String mixinPackage) {
    if (!MIXIN_PACKAGE.equals(mixinPackage)) {
      throw new IllegalArgumentException(mixinPackage);
    }
  }

  @Override
  public String getRefMapperConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean shouldApplyMixin(final String target, final String mixin) {
    switch (mixin) {
      case MIXIN_PACKAGE + '.' + DOOR_ACCESSOR:
      case MIXIN_PACKAGE + '.' + DOOR_MIXIN:
        return Couplings.areDoorsEnabled();
      case MIXIN_PACKAGE + '.' + FENCE_GATE_MIXIN:
        return Couplings.areFenceGatesEnabled();
      case MIXIN_PACKAGE + '.' + TRAPDOOR_MIXIN:
        return Couplings.areTrapdoorsEnabled();
      default:
        return false;
    }
  }

  @Override
  public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
  }

  @Override
  public List<String> getMixins() {
    return ImmutableList.of(DOOR_ACCESSOR, DOOR_MIXIN, FENCE_GATE_MIXIN, TRAPDOOR_MIXIN);
  }

  @Override
  public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
  }

  @Override
  public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
  }
}
