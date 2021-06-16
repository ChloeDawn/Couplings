/*
 * Copyright (C) 2020 Chloe Dawn
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

package dev.sapphic.couplings;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CouplingsMixins implements IMixinConfigPlugin {
  @Override
  public void onLoad(final String mixinPackage) {
  }

  @Override
  public @Nullable String getRefMapperConfig() {
    return null;
  }

  @Override
  public boolean shouldApplyMixin(final String target, final String mixin) {
    switch (mixin) {
      case "dev.sapphic.couplings.mixin.DoorBlockMixin":
        return Couplings.COUPLE_DOORS;

      case "dev.sapphic.couplings.mixin.FenceGateBlockMixin":
        return Couplings.COUPLE_FENCE_GATES;

      case "dev.sapphic.couplings.mixin.TrapdoorBlockMixin":
        return Couplings.COUPLE_TRAPDOORS;

      default:
        throw new IllegalArgumentException(mixin);
    }
  }

  @Override
  public void acceptTargets(final Set<String> targets, final Set<String> otherTargets) {
  }

  @Override
  public @Nullable List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(final String target, final ClassNode targetClass, final String mixin, final IMixinInfo info) {
  }

  @Override
  public void postApply(final String target, final ClassNode targetClass, final String mixin, final IMixinInfo info) {
  }
}
