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

package io.github.insomniakitten.couplings.hook;

import io.github.insomniakitten.couplings.Couplings;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public final class FenceGateHooks {
  private static final ThreadLocal<Boolean> CHECK_BELOW =
    ThreadLocal.withInitial(() -> true);

  private static final ThreadLocal<Boolean> CHECK_ABOVE =
    ThreadLocal.withInitial(() -> true);

  private FenceGateHooks() {}

  public static void usageCallback(
    final BlockState state,
    final World world,
    final BlockPos pos,
    final PlayerEntity player,
    final Hand hand,
    final Direction side,
    final float x,
    final float y,
    final float z,
    final boolean usageResult
  ) {
    if (player.isSneaking()) return; // todo config
    if (FenceGateHooks.CHECK_BELOW.get()) {
      FenceGateHooks.CHECK_ABOVE.set(false);
      Couplings.useNeighbor(state, world, pos, pos.down(), player, hand, side, x, y, z, usageResult, FenceGateHooks::areEquivalent);
      FenceGateHooks.CHECK_ABOVE.set(true);
    }
    if (FenceGateHooks.CHECK_ABOVE.get()) {
      FenceGateHooks.CHECK_BELOW.set(false);
      Couplings.useNeighbor(state, world, pos, pos.up(), player, hand, side, x, y, z, usageResult, FenceGateHooks::areEquivalent);
      FenceGateHooks.CHECK_BELOW.set(true);
    }
  }

  private static boolean areEquivalent(final BlockState self, final BlockState neighbor) {
    return FenceGateHooks.isOpen(self) != FenceGateHooks.isOpen(neighbor)
      && FenceGateHooks.getAxis(self) == FenceGateHooks.getAxis(neighbor);
  }

  private static boolean isOpen(final BlockState state) {
    return state.get(FenceGateBlock.field_11026);
  }

  private static Axis getAxis(final BlockState state) {
    return state.get(HorizontalFacingBlock.field_11177).getAxis();
  }
}
