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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public final class FenceGates {
  private static final ThreadLocal<Boolean> USE_NEIGHBORS = ThreadLocal.withInitial(() -> true);
  private static final Direction[] HORIZONTALS = Direction.Type.HORIZONTAL.stream().toArray(Direction[]::new);

  private static final int FENCE_GATE_OPENED = 1008;
  private static final int FENCE_GATE_CLOSED = 1014;

  private FenceGates() {
  }

  public static void used(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult hit, final ActionResult usageResult) {
    if (usageResult.isAccepted() && Couplings.areFenceGatesEnabled() && USE_NEIGHBORS.get() && (!player.isSneaking() || Couplings.isSneakingIgnored())) {
      USE_NEIGHBORS.set(false);
      final Block block = state.getBlock();
      final boolean open = state.get(FenceGateBlock.OPEN);
      final Axis axis = state.get(HorizontalFacingBlock.FACING).getAxis();
      final int range = Couplings.getCouplingRange() / 2;
      for (int y = -range; y <= range; y++) {
        final BlockPos offset = pos.up(y);
        if (Couplings.isUsable(world, offset, player)) {
          final BlockState other = world.getBlockState(offset);
          if ((block == other.getBlock()) && equals(open, axis, other)) {
            if (Couplings.use(other, world, hand, player, hit, offset, usageResult)) {
              USE_NEIGHBORS.set(true);
              return;
            }
          }
        }
      }
      USE_NEIGHBORS.set(true);
    }
  }

  public static void neighborUpdated(final BlockState state, final World world, final BlockPos pos, final boolean isPowered) {
    if (Couplings.areFenceGatesEnabled()) {
      if (isPowered == isSufficientlyPowered(world, pos)) {
        final Axis axis = state.get(HorizontalFacingBlock.FACING).getAxis();
        final int max = Couplings.getCouplingRange() / 2;
        for (int y = -max; y <= max; ++y) {
          final BlockPos offset = pos.up(y);
          final BlockState other = world.getBlockState(offset);
          if ((state.getBlock() == other.getBlock()) && equals(isPowered, axis, other)) {
            world.setBlockState(offset, other.with(FenceGateBlock.OPEN, isPowered), 2);
            world.syncWorldEvent(null, isPowered ? FENCE_GATE_OPENED : FENCE_GATE_CLOSED, pos, 0);
          }
        }
      } else if (!isPowered && isSufficientlyPowered(world, pos)) {
        world.setBlockState(pos, state.with(FenceGateBlock.POWERED, false).with(FenceGateBlock.OPEN, true), 2);
      }
    }
  }

  private static boolean equals(final boolean open, final Axis axis, final BlockState state) {
    return (open != state.get(FenceGateBlock.OPEN)) && (axis == state.get(HorizontalFacingBlock.FACING).getAxis());
  }

  private static boolean isSufficientlyPowered(final World world, final BlockPos pos) {
    if (world.getReceivedRedstonePower(pos) >= Couplings.MIN_SIGNAL) {
      return true;
    }
    final int max = (Couplings.getCouplingRange() / 2) + 1;
    for (int y = -max; y <= max; ++y) {
      if (y == 0) { // Origin already queried
        continue;
      }
      final BlockPos offset = pos.up(y);
      if ((y == -max) || (y == max)) { // Above or below adjacent
        final Direction dir = (y == -max) ? Direction.DOWN : Direction.UP;
        if (world.getEmittedRedstonePower(offset, dir) >= Couplings.MIN_SIGNAL) {
          return true;
        }
      } else {
        for (final Direction dir : HORIZONTALS) {
          if (world.getEmittedRedstonePower(offset.offset(dir), dir) >= Couplings.MIN_SIGNAL) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
