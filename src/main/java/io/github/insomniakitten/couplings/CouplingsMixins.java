/*
 * Copyright (C) 2018 InsomniaKitten
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
  private static final String PACKAGE = "io.github.insomniakitten.couplings.mixin";

  private static final ImmutableMap<String, BooleanSupplier> MIXIN_STATES = ImmutableMap.of(
    CouplingsMixins.PACKAGE + ".DoorInvoker", Couplings::areDoorsEnabled,
    CouplingsMixins.PACKAGE + ".DoorMixin", Couplings::areDoorsEnabled,
    CouplingsMixins.PACKAGE + ".FenceGateMixin", Couplings::areFenceGatesEnabled,
    CouplingsMixins.PACKAGE + ".TrapdoorMixin", Couplings::areTrapdoorsEnabled
  );

  private static boolean constructed = false;

  @Deprecated
  public CouplingsMixins() {
    if (constructed) {
      throw new UnsupportedOperationException("Already constructed");
    }
    constructed = true;
  }

  @Override
  public void onLoad(final String mixinPackage) {
    if (!CouplingsMixins.PACKAGE.equals(mixinPackage)) {
      throw new IllegalArgumentException(mixinPackage);
    }
  }

  @Override
  public String getRefMapperConfig() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean shouldApplyMixin(final String target, final String mixin) {
    @Nullable final BooleanSupplier state = CouplingsMixins.MIXIN_STATES.get(mixin);
    if (state == null) {
      throw new IllegalArgumentException(mixin);
    }
    return state.getAsBoolean();
  }

  @Override
  public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {

  }

  @Override
  public List<String> getMixins() {
    return ImmutableList.of("DoorInvoker", "DoorMixin", "FenceGateMixin", "TrapdoorMixin");
  }

  @Override
  public void preApply(final String target, final ClassNode targetClass, final String mixin, final IMixinInfo info) {

  }

  @Override
  public void postApply(final String target, final ClassNode targetClass, final String mixin, final IMixinInfo info) {

  }
}
