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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.State;
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
      final BlockPos offset = getOtherDoor(state, pos);
      if (Couplings.isUsable(world, offset, player)) {
        final BlockState other = world.getBlockState(offset);
        if ((state.getBlock() == other.getBlock()) && areEquivalent(state, other)) {
          if (Couplings.use(other, world, hand, player, hit, offset, usageResult)) {
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
      final BlockPos offset = getOtherDoor(state, pos);
      final BlockState other = world.getBlockState(offset);
      if (state.getBlock() == other.getBlock()) {
        if (areEquivalent(state, other.with(DoorBlock.OPEN, open))) {
          world.setBlockState(offset, other.with(DoorBlock.OPEN, open), 10);
          playSound(other, world, offset, open);
        }
      }
    }
  }

  public static void neighborUpdated(final BlockState state, final World world, final BlockPos pos, final boolean isPowered) {
    if (Couplings.areDoorsEnabled() && ((!isPowered && state.get(DoorBlock.POWERED)) || isSufficientlyPowered(state, world, pos))) {
      final BlockPos offset = getOtherDoor(state, pos);
      final BlockState other = world.getBlockState(offset);
      if (state.getBlock() == other.getBlock()) {
        if (areEquivalent(state, other.with(DoorBlock.OPEN, isPowered))) {
          world.setBlockState(offset, other.with(DoorBlock.OPEN, isPowered), 2);
          playSound(other, world, offset, isPowered);
        }
      }
    }
  }

  private static boolean areEquivalent(final State<Block, BlockState> self, final State<Block, BlockState> other) {
    return (self.get(DoorBlock.FACING) == other.get(DoorBlock.FACING))
      && (self.get(DoorBlock.HALF) == other.get(DoorBlock.HALF))
      && (self.get(DoorBlock.OPEN) != other.get(DoorBlock.OPEN))
      && (self.get(DoorBlock.HINGE) != other.get(DoorBlock.HINGE));
  }

  private static BlockPos getOtherHalf(final State<Block, BlockState> state, final BlockPos pos) {
    return pos.offset((state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) ? Direction.UP : Direction.DOWN);
  }

  private static BlockPos getOtherDoor(final BlockState self, final BlockPos origin) {
    final Direction facing = self.get(DoorBlock.FACING);
    final boolean left = self.get(DoorBlock.HINGE) == DoorHinge.LEFT;
    return origin.offset(left ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
  }

  private static boolean isSufficientlyPowered(final State<Block, BlockState> state, final World world, final BlockPos pos) {
    return Math.max(world.getReceivedRedstonePower(pos), world.getReceivedRedstonePower(getOtherHalf(state, pos))) > 7;
  }

  private static void playSound(final BlockState state, final World world, final BlockPos pos, final boolean isPowered) {
    ((DoorAccessor) state.getBlock()).invokePlaySound(world, pos, isPowered);
  }
}
