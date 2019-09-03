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

package io.github.insomniakitten.couplings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

public final class CouplingsMixins implements IMixinConfigPlugin {
  public static final String PACKAGE = "io.github.insomniakitten.couplings.mixin";
  public static final String DOOR_INVOKER = "DoorInvoker";
  public static final String DOOR_MIXIN = "DoorMixin";
  public static final String FENCE_GATE_MIXIN = "FenceGateMixin";
  public static final String TRAPDOOR_MIXIN = "TrapdoorMixin";

  private static final ImmutableMap<String, BooleanSupplier> MIXIN_STATES = ImmutableMap.of(
    PACKAGE + '.' + DOOR_INVOKER,     Couplings::areDoorsEnabled,
    PACKAGE + '.' + DOOR_MIXIN,       Couplings::areDoorsEnabled,
    PACKAGE + '.' + FENCE_GATE_MIXIN, Couplings::areFenceGatesEnabled,
    PACKAGE + '.' + TRAPDOOR_MIXIN,   Couplings::areTrapdoorsEnabled
  );

  private static boolean constructed = false;

  @Deprecated
  public CouplingsMixins() {
    if (constructed) {
      throw new UnsupportedOperationException();
    }
    constructed = true;
  }

  @Override
  public void onLoad(final String mixinPackage) {
    if (!PACKAGE.equals(mixinPackage)) {
      throw new IllegalArgumentException(mixinPackage);
    }
  }

  @Override
  public String getRefMapperConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean shouldApplyMixin(final String target, final String mixin) {
    @Nullable final BooleanSupplier state = MIXIN_STATES.get(mixin);
    if (state == null) {
      throw new IllegalArgumentException(mixin + " -> " + target);
    }
    return state.getAsBoolean();
  }

  @Override
  public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {

  }

  @Override
  public List<String> getMixins() {
    return ImmutableList.of(DOOR_INVOKER, DOOR_MIXIN, FENCE_GATE_MIXIN, TRAPDOOR_MIXIN);
  }

  @Override
  public void preApply(final String target, final ClassNode targetClass, final String mixin, final IMixinInfo info) {

  }

  @Override
  public void postApply(final String target, final ClassNode targetClass, final String mixin, final IMixinInfo info) {

  }
}
