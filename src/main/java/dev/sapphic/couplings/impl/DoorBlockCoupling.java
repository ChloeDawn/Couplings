/*
 * Copyright 2021 Chloe Dawn
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
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.gameevent.GameEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class DoorBlockCoupling {
  private DoorBlockCoupling() {
  }

  public static void used(final BlockState state, final Level level, final BlockPos pos, final Player player) {
    if (Couplings.couplesDoors(level) && (!player.isCrouching() || Couplings.ignoresSneaking(player))) {
      final BlockPos offset = getCoupledDoorPos(state, pos);

      if (level.mayInteract(player, offset)) {
        final BlockState other = level.getBlockState(offset);

        if (state.getBlock() == other.getBlock()) {
          final boolean open = state.getValue(DoorBlock.OPEN);

          if (areCoupled(state, other, open)) {
            level.setBlock(offset, other.setValue(DoorBlock.OPEN, open), 2);
            level.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, offset);
          }
        }
      }
    }
  }

  public static void openStateChanged(final @Nullable Entity entity, final BlockState state, final Level level, final BlockPos pos, final boolean open) {
    if (Couplings.couplesDoors(level)) {
      final BlockPos offset = getCoupledDoorPos(state, pos);
      final BlockState other = level.getBlockState(offset);

      if ((state.getBlock() == other.getBlock()) && areCoupled(state, other, open)) {
        level.setBlock(offset, other.setValue(DoorBlock.OPEN, open), 10);
        level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, offset);
      }
    }
  }

  public static void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final boolean powered) {
    if (Couplings.couplesDoors(level) && (!powered || (level.getBestNeighborSignal(pos) >= Couplings.COUPLING_SIGNAL))) {
      final BlockPos offset = getCoupledDoorPos(state, pos);
      final BlockState other = level.getBlockState(offset);

      if ((state.getBlock() == other.getBlock()) && areCoupled(state, other, powered)) {
        level.setBlock(offset, other.setValue(DoorBlock.OPEN, powered), 2);
        level.gameEvent(powered ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, offset);
      }
    }
  }

  private static boolean areCoupled(final BlockState self, final BlockState other, final boolean open) {
    return (open != other.getValue(DoorBlock.OPEN))
      && (self.getValue(DoorBlock.FACING) == other.getValue(DoorBlock.FACING))
      && (self.getValue(DoorBlock.HALF) == other.getValue(DoorBlock.HALF))
      && (self.getValue(DoorBlock.HINGE) != other.getValue(DoorBlock.HINGE));
  }

  private static BlockPos getCoupledDoorPos(final BlockState state, final BlockPos pos) {
    final Direction facing = state.getValue(DoorBlock.FACING);
    final boolean leftHinge = state.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT;

    return pos.relative(leftHinge ? facing.getClockWise() : facing.getCounterClockWise());
  }
}
