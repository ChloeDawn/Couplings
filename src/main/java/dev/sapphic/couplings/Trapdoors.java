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
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class Trapdoors {
  private static final ThreadLocal<Boolean> USE_NEIGHBORS = ThreadLocal.withInitial(() -> true);

  private Trapdoors() {
  }

  public static void used(
    final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand,
    final BlockHitResult hit, final ActionResult usageResult
  ) {
    if (usageResult.isAccepted() && Couplings.areTrapdoorsEnabled() && USE_NEIGHBORS.get() && (
      !player.isSneaking() || Couplings.isSneakingIgnored()
    )) {
      final Block block = state.getBlock();
      final boolean open = state.get(TrapdoorBlock.OPEN);
      final BlockHalf half = state.get(TrapdoorBlock.HALF);
      final Direction facing = state.get(HorizontalFacingBlock.FACING);
      final boolean positive = facing.getDirection() == Direction.AxisDirection.POSITIVE;
      final Direction opposite = facing.getOpposite();
      final BlockPos.Mutable offset = pos.mutableCopy();
      final int max = Couplings.getCouplingRange() / 2;
      final boolean alongZ = facing.getAxis() == Direction.Axis.X;
      final int mirror = positive ? 1 : -1;
      for (int step = 0; step <= max; ++step) {
        boolean end = false;

        if (step != 0) {
          offset.move(alongZ ? 0 : step, 0, alongZ ? step : 0);
          end = use(block, open, half, offset, facing, world, player, hand, hit, usageResult);
        }

        offset.move(alongZ ? mirror : 0, 0, alongZ ? 0 : mirror);
        end |= use(block, open, half, offset, opposite, world, player, hand, hit, usageResult);

        offset.set(pos);

        if (step != 0) {
          offset.move(alongZ ? 0 : -step, 0, alongZ ? -step : 0);
          end |= use(block, open, half, offset, facing, world, player, hand, hit, usageResult);

          offset.move(alongZ ? mirror : 0, 0, alongZ ? 0 : mirror);
          end |= use(block, open, half, offset, opposite, world, player, hand, hit, usageResult);

          offset.set(pos);
        }

        if (end && (step != 0)) {
          return;
        }
      }
    }
  }

  public static void neighborUpdated(
    final BlockState state, final World world, final BlockPos pos, final boolean isPowered
  ) {
    if (Couplings.areTrapdoorsEnabled()) {
      // TODO
    }
  }

  private static boolean use(
    final Block block, final boolean open, final BlockHalf half, final BlockPos offset, final Direction facing,
    final World world, final PlayerEntity player, final Hand hand, final BlockHitResult hit,
    final ActionResult usageResult
  ) {
    if (Couplings.isUsable(world, offset, player)) {
      final BlockState mirror = world.getBlockState(offset);
      if ((block == mirror.getBlock()) && equals(open, half, facing, mirror)) {
        if (Couplings.use(mirror, world, hand, player, hit, offset, usageResult)) {
          USE_NEIGHBORS.set(true);
          return true;
        }
        return false;
      }
    }
    return true;
  }

  private static boolean equals(
    final boolean open, final BlockHalf half, final Direction facing, final BlockState state
  ) {
    return (open != state.get(TrapdoorBlock.OPEN))
      && (half == state.get(TrapdoorBlock.HALF))
      && (facing == state.get(HorizontalFacingBlock.FACING));
  }
}
