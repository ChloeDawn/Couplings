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
import io.github.insomniakitten.couplings.mixin.DoorInvoker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class DoorHooks {
  private static final ThreadLocal<Boolean> USE_NEIGHBOR = ThreadLocal.withInitial(() -> true);

  private DoorHooks() {}

  public static void usageCallback(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult hit, final boolean usageResult) {
    if (!Couplings.areDoorsEnabled()) return;
    if (!DoorHooks.USE_NEIGHBOR.get()) return;
    if (player.isSneaking() && Couplings.requiresNoSneaking()) return;
    DoorHooks.USE_NEIGHBOR.set(false);
    final BlockPos offset = DoorHooks.getOtherDoor(state, pos);
    if (Couplings.isUsable(world, offset, player)) {
      final BlockState other = world.getBlockState(offset);
      if (state.getBlock() == other.getBlock() && DoorHooks.areEquivalent(state, other)) {
        Couplings.use(state, other, world, hand, player, hit, offset, usageResult);
      }
    }
    DoorHooks.USE_NEIGHBOR.set(true);
  }

  public static void openCallback(final BlockState state, final World world, final BlockPos pos, final boolean open) {
    if (!Couplings.areDoorsEnabled()) return;
    final BlockPos offset = DoorHooks.getOtherDoor(state, pos);
    final BlockState other = world.getBlockState(offset);
    if (state.getBlock() == other.getBlock()) {
      if (DoorHooks.areEquivalent(state, other.with(DoorBlock.OPEN, open))) {
        world.setBlockState(offset, other.with(DoorBlock.OPEN, open), 10);
        DoorHooks.fireWorldEvent(other, world, offset, open);
      }
    }
  }

  public static void neighborUpdateCallback(final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos neighborPos, final boolean isPowered) {
    if (!Couplings.areDoorsEnabled()) return;
    if (!isPowered && state.get(DoorBlock.POWERED) || DoorHooks.isSufficientlyPowered(state, world, pos)) {
      final BlockPos offset = DoorHooks.getOtherDoor(state, pos);
      final BlockState other = world.getBlockState(offset);
      if (state.getBlock() == other.getBlock()) {
        if (DoorHooks.areEquivalent(state, other.with(DoorBlock.OPEN, isPowered))) {
          world.setBlockState(offset, other.with(DoorBlock.OPEN, isPowered), 2);
          DoorHooks.fireWorldEvent(other, world, offset, isPowered);
        }
      }
    }
  }

  private static boolean areEquivalent(final BlockState self, final BlockState other) {
    return self.get(DoorBlock.FACING) == other.get(DoorBlock.FACING)
      && self.get(DoorBlock.HALF) == other.get(DoorBlock.HALF)
      && (boolean) self.get(DoorBlock.OPEN) != other.get(DoorBlock.OPEN)
      && self.get(DoorBlock.HINGE) != other.get(DoorBlock.HINGE);
  }

  private static BlockPos getOtherHalf(final BlockState state, final BlockPos pos) {
    return pos.offset(state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);
  }

  private static BlockPos getOtherDoor(final BlockState self, final BlockPos origin) {
    final Direction facing = self.get(DoorBlock.FACING);
    final boolean left = DoorHinge.LEFT == self.get(DoorBlock.HINGE);
    return origin.offset(left ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
  }

  private static boolean isSufficientlyPowered(final BlockState state, final World world, final BlockPos pos) {
    final int power = Math.max(
      world.getReceivedRedstonePower(pos),
      world.getReceivedRedstonePower(DoorHooks.getOtherHalf(state, pos))
    );
    return power > 7;
  }

  private static void fireWorldEvent(final BlockState state, final World world, final BlockPos pos, final boolean isPowered) {
    final Block block = state.getBlock();
    if (!(block instanceof DoorInvoker)) {
      throw new IllegalArgumentException("Not invokable: " + block);
    }
    ((DoorInvoker) block).playUseSound(world, pos, isPowered);
  }
}
