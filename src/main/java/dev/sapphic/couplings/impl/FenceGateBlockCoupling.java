/*
 * Copyright 2023 Chloe Dawn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public final class FenceGateBlockCoupling {
  private FenceGateBlockCoupling() {}

  public static void used(
      final BlockState state, final Level level, final BlockPos pos, final Player player) {
    if (Couplings.couplesFenceGates(level)
        && (!player.isCrouching() || Couplings.ignoresSneaking(player))) {
      tryOpenCloseEach(state, level, pos, player, state.getValue(FenceGateBlock.OPEN));
    }
  }

  public static void neighborChanged(
      final BlockState state, final Level level, final BlockPos pos, final boolean powered) {
    if (Couplings.couplesFenceGates(level)
        && (!powered || (level.getBestNeighborSignal(pos) >= Couplings.COUPLING_SIGNAL))) {
      tryOpenCloseEach(state, level, pos, null, powered);
    }
  }

  public static void tryOpenCloseEach(
      final BlockState state,
      final Level level,
      final BlockPos pos,
      final @Nullable Player player,
      final boolean open) {
    final var axis = state.getValue(HorizontalDirectionalBlock.FACING).getAxis();
    final var distance = Couplings.COUPLING_DISTANCE;
    var continueUp = true;
    var continueDown = true;

    for (var offset = 1; (offset <= distance) && (continueUp || continueDown); ++offset) {
      if (continueUp) {
        final var above = pos.above(offset);

        continueUp =
            ((player == null) || level.mayInteract(player, above))
                && tryOpenClose(state, level, above, player, axis, open);
      }

      if (continueDown) {
        final var below = pos.below(offset);

        continueDown =
            ((player == null) || level.mayInteract(player, below))
                && tryOpenClose(state, level, below, player, axis, open);
      }
    }
  }

  private static boolean tryOpenClose(
      final BlockState state,
      final Level level,
      final BlockPos offset,
      final @Nullable Player player,
      final Axis axis,
      final boolean open) {
    final var other = level.getBlockState(offset);

    if ((state.getBlock() == other.getBlock()) && (open != other.getValue(FenceGateBlock.OPEN))) {
      if (axis == other.getValue(HorizontalDirectionalBlock.FACING).getAxis()) {
        final var newOther = other.setValue(FenceGateBlock.OPEN, open);

        if (player != null) {
          final var facing = state.getValue(HorizontalDirectionalBlock.FACING);

          level.setBlock(offset, newOther.setValue(HorizontalDirectionalBlock.FACING, facing), 2);
        } else {
          level.setBlock(offset, newOther, 2);
        }

        level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, offset);

        return true;
      }
    }

    return false;
  }
}
