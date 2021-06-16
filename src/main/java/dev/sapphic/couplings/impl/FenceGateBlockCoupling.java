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

package dev.sapphic.couplings.impl;

import dev.sapphic.couplings.Couplings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class FenceGateBlockCoupling {
  private static final Direction[] HORIZONTALS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

  private FenceGateBlockCoupling() {
  }

  public static void used(final BlockState state, final Level level, final BlockPos pos, final Player player) {
    if (!player.isCrouching() || Couplings.IGNORE_SNEAKING) {
      tryOpenCloseEach(state, level, pos, player, state.getValue(FenceGateBlock.OPEN));
    }
  }

  public static void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final boolean powered) {
    if (powered != isSufficientlyPowered(state, level, pos, powered)) {
      level.setBlock(pos, state.setValue(FenceGateBlock.POWERED, false).setValue(FenceGateBlock.OPEN, true), 2);
    } else {
      tryOpenCloseEach(state, level, pos, null, powered);
    }
  }

  public static void tryOpenCloseEach(final BlockState state, final Level level, final BlockPos pos, final @Nullable Player player, final boolean open) {
    final Axis axis = state.getValue(HorizontalDirectionalBlock.FACING).getAxis();
    final int distance = Couplings.COUPLING_DISTANCE;
    boolean continueUp = true;
    boolean continueDown = true;

    for (int offset = 1; (offset <= distance) && (continueUp || continueDown); ++offset) {
      if (continueUp) {
        final BlockPos above = pos.above(offset);
        final boolean modifiable = (player == null) || level.mayInteract(player, above);

        continueUp = modifiable && tryOpenClose(state, level, above, player, axis, open);
      }

      if (continueDown) {
        final BlockPos below = pos.below(offset);
        final boolean modifiable = (player == null) || level.mayInteract(player, below);

        continueDown = modifiable && tryOpenClose(state, level, below, player, axis, open);
      }
    }
  }

  private static boolean tryOpenClose(final BlockState state, final Level level, final BlockPos offset, final @Nullable Player player, final Axis axis, final boolean open) {
    final BlockState other = level.getBlockState(offset);

    if ((state.getBlock() == other.getBlock()) && (open != other.getValue(FenceGateBlock.OPEN))) {
      if (axis == other.getValue(HorizontalDirectionalBlock.FACING).getAxis()) {
        final BlockState newOther = other.setValue(FenceGateBlock.OPEN, open);

        if (player != null) {
          final Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);

          level.setBlock(offset, newOther.setValue(HorizontalDirectionalBlock.FACING, facing), 2);
        } else {
          level.setBlock(offset, newOther, 2);
        }

        return true;
      }
    }

    return false;
  }

  private static boolean isSufficientlyPowered(final BlockState state, final Level level, final BlockPos pos, final boolean powered) {
    if (powered) {
      return true;
    }

    final Axis axis = state.getValue(HorizontalDirectionalBlock.FACING).getAxis();
    final int distance = Couplings.COUPLING_DISTANCE;
    boolean continueUp = true;
    boolean continueDown = true;

    for (int offset = 1; (offset <= distance) && (continueUp || continueDown); ++offset) {
      if (continueUp) {
        final BlockPos above = pos.above(offset);
        final BlockState other = level.getBlockState(above);

        if ((state.getBlock() != other.getBlock()) || (axis != other.getValue(HorizontalDirectionalBlock.FACING).getAxis())) {
          continueUp = false;
        } else if (level.hasNeighborSignal(above)) {
          return true;
        }
      }

      if (continueDown) {
        final BlockPos below = pos.below(offset);
        final BlockState other = level.getBlockState(below);

        if ((state.getBlock() != other.getBlock()) || (axis != other.getValue(HorizontalDirectionalBlock.FACING).getAxis())) {
          continueDown = false;
        } else if (level.hasNeighborSignal(below)) {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean matches(final BlockState state, final BlockState other, final Axis axis, final boolean open) {
    if ((state.getBlock() == other.getBlock()) && (open != other.getValue(FenceGateBlock.OPEN))) {
      return axis == other.getValue(HorizontalDirectionalBlock.FACING).getAxis();
    }
    return false;
  }
}
