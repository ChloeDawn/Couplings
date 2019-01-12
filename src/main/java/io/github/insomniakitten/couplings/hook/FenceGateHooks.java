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
import net.minecraft.block.Block;
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
  private static final ThreadLocal<Boolean> USE_NEIGHBORS =
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
    if (FenceGateHooks.USE_NEIGHBORS.get()) {
      if (player.isSneaking()) return; // todo config

      FenceGateHooks.USE_NEIGHBORS.set(false);

      final Block block = state.getBlock();
      final boolean open = FenceGateHooks.isOpen(state);
      final Axis axis = FenceGateHooks.getAxis(state);

      for (final BlockPos.Mutable offset : BlockPos.iterateBoxPositionsMutable(
        pos.down(Couplings.COUPLING_RANGE),
        pos.up(Couplings.COUPLING_RANGE)
      )) {
        if (Couplings.isUsable(world, offset, player)) {
          final BlockState other = world.getBlockState(offset);

          if (block == other.getBlock() && FenceGateHooks.includesStates(open, axis, other)) {
            Couplings.use(state, other, world, pos, offset, player, hand, side, x, y, z, usageResult);
          }
        }
      }

      FenceGateHooks.USE_NEIGHBORS.set(true);
    }
  }

  private static boolean includesStates(final boolean open, final Axis axis, final BlockState state) {
    return open != FenceGateHooks.isOpen(state) && axis == FenceGateHooks.getAxis(state);
  }

  private static boolean isOpen(final BlockState state) {
    return state.get(FenceGateBlock.field_11026);
  }

  private static Axis getAxis(final BlockState state) {
    return state.get(HorizontalFacingBlock.field_11177).getAxis();
  }
}
