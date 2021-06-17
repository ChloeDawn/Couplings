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

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class Couplings implements IMixinConfigPlugin {
  public static final int COUPLING_DISTANCE = 64;
  public static final int COUPLING_SIGNAL = 8;

  public static final boolean IGNORE_SNEAKING;
  public static final boolean COUPLE_DOORS;
  public static final boolean COUPLE_FENCE_GATES;
  public static final boolean COUPLE_TRAPDOORS;

  static {
    final CommentedFileConfig config = CommentedFileConfig.of(
      FabricLoader.getInstance().getConfigDir().resolve("couplings.toml"));

    config.load();

    final String ignoreSneaking = "ignore_sneaking";
    final String coupleDoors = "couple_doors";
    final String coupleFenceGates = "couple_fence_gates";
    final String coupleTrapdoors = "couple_trapdoors";

    final ConfigSpec spec = new ConfigSpec();

    spec.define(ignoreSneaking, true);
    spec.define(coupleDoors, true);
    spec.define(coupleFenceGates, true);
    spec.define(coupleTrapdoors, true);

    spec.correct(config);

    config.setComment(ignoreSneaking, "Couple regardless of whether the player is sneaking when interacting");
    config.setComment(coupleDoors, "Couple neighboring doors with opposing hinges");
    config.setComment(coupleFenceGates, "Couple neighboring fence gates above and below on the same axis");
    config.setComment(coupleTrapdoors, "Couple neighboring trapdoors along either sides and opposing");

    config.save();

    IGNORE_SNEAKING = config.getOrElse(ignoreSneaking, true);
    COUPLE_DOORS = config.getOrElse(coupleDoors, true);
    COUPLE_FENCE_GATES = config.getOrElse(coupleFenceGates, true);
    COUPLE_TRAPDOORS = config.getOrElse(coupleTrapdoors, true);

    if (!COUPLE_DOORS || !COUPLE_FENCE_GATES || !COUPLE_TRAPDOORS) {
      LogManager.getLogger().warn("No features are enabled, this could be a bug!");
    }
  }

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
        return COUPLE_DOORS;

      case "dev.sapphic.couplings.mixin.FenceGateBlockMixin":
        return COUPLE_FENCE_GATES;

      case "dev.sapphic.couplings.mixin.TrapdoorBlockMixin":
        return COUPLE_TRAPDOORS;

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
