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

import dev.sapphic.couplings.mixin.DoorAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class Doors {
  private static final ThreadLocal<Boolean> USE_NEIGHBOR = ThreadLocal.withInitial(() -> true);

  private Doors() {
  }

  public static void used(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult hit, final ActionResult usageResult) {
    if (usageResult.isAccepted() && Couplings.areDoorsEnabled() && USE_NEIGHBOR.get() && (!player.isSneaking() || Couplings.isSneakingIgnored())) {
      USE_NEIGHBOR.set(false);
      final BlockPos adjacent = getAdjacent(state, pos);
      if (Couplings.isUsable(world, adjacent, player)) {
        final BlockState adjacentState = world.getBlockState(adjacent);
        if ((state.getBlock() == adjacentState.getBlock()) && shouldUpdate(state, adjacentState)) {
          if (Couplings.use(adjacentState, world, hand, player, hit, adjacent, usageResult)) {
            USE_NEIGHBOR.set(true);
            return;
          }
        }
      }
      USE_NEIGHBOR.set(true);
    }
  }

  public static void toggled(final BlockState state, final World world, final BlockPos pos, final boolean open) {
    if (Couplings.areDoorsEnabled()) {
      final BlockPos adjacent = getAdjacent(state, pos);
      BlockState adjacentState = world.getBlockState(adjacent);
      if (state.getBlock() == adjacentState.getBlock()) {
        adjacentState = adjacentState.with(DoorBlock.OPEN, open);
        if (shouldUpdate(state, adjacentState)) {
          world.setBlockState(adjacent, adjacentState, 10);
          playSound(adjacentState, world, adjacent, open);
        }
      }
    }
  }

  public static void neighborUpdated(final BlockState state, final World world, final BlockPos pos, final boolean isPowered) {
    if (Couplings.areDoorsEnabled() && ((!isPowered && state.get(DoorBlock.POWERED)) || isSufficientlyPowered(state, world, pos))) {
      final BlockPos adjacent = getAdjacent(state, pos);
      BlockState adjacentState = world.getBlockState(adjacent);
      if (state.getBlock() == adjacentState.getBlock()) {
        adjacentState = adjacentState.with(DoorBlock.OPEN, isPowered);
        if (shouldUpdate(state, adjacentState)) {
          world.setBlockState(adjacent, adjacentState, 2);
          playSound(adjacentState, world, adjacent, isPowered);
        }
      }
    }
  }

  private static boolean shouldUpdate(final BlockState self, final BlockState other) {
    return (self.get(DoorBlock.FACING) == other.get(DoorBlock.FACING))
      && (self.get(DoorBlock.HALF) == other.get(DoorBlock.HALF))
      && (self.get(DoorBlock.OPEN) != other.get(DoorBlock.OPEN))
      && (self.get(DoorBlock.HINGE) != other.get(DoorBlock.HINGE));
  }

  private static BlockPos getAdjacent(final BlockState self, final BlockPos origin) {
    final Direction facing = self.get(DoorBlock.FACING);
    final boolean left = self.get(DoorBlock.HINGE) == DoorHinge.LEFT;
    return origin.offset(left ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
  }

  private static boolean isSufficientlyPowered(final BlockState state, final World world, final BlockPos pos) {
    if (world.getReceivedRedstonePower(pos) >= Couplings.MIN_SIGNAL) {
      return true;
    }
    final boolean lower = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
    final BlockPos offset = pos.offset(lower ? Direction.UP : Direction.DOWN);
    return world.getReceivedRedstonePower(offset) >= Couplings.MIN_SIGNAL;
  }

  private static void playSound(final BlockState state, final World world, final BlockPos pos, final boolean isPowered) {
    ((DoorAccessor) state.getBlock()).invokePlaySound(world, pos, isPowered);
  }
}
