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
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class TrapdoorHooks {
  private static final ThreadLocal<Boolean> CHECK_OPPOSITE =
    ThreadLocal.withInitial(() -> true);

  private static final ThreadLocal<Boolean> CHECK_CCW =
    ThreadLocal.withInitial(() -> true);

  private static final ThreadLocal<Boolean> CHECK_CW =
    ThreadLocal.withInitial(() -> true);

  private TrapdoorHooks() {}

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
    final Direction facing = TrapdoorHooks.getFacing(state);
    if (TrapdoorHooks.CHECK_CW.get()) {
      TrapdoorHooks.CHECK_CCW.set(false);
      final BlockPos offset = pos.offset(facing.rotateYClockwise());
      Couplings.useNeighbor(state, world, pos, offset, player, hand, side, x, y, z, usageResult, TrapdoorHooks::areEqual);
      TrapdoorHooks.CHECK_CCW.set(true);
    }
    if (TrapdoorHooks.CHECK_CCW.get()) {
      TrapdoorHooks.CHECK_CW.set(false);
      final BlockPos offset = pos.offset(facing.rotateYCounterclockwise());
      Couplings.useNeighbor(state, world, pos, offset, player, hand, side, x, y, z, usageResult, TrapdoorHooks::areEqual);
      TrapdoorHooks.CHECK_CW.set(true);
    }
    if (TrapdoorHooks.CHECK_OPPOSITE.get()) {
      TrapdoorHooks.CHECK_OPPOSITE.set(false);
      final BlockPos mirror = pos.offset(facing);
      Couplings.useNeighbor(state, world, pos, mirror, player, hand, side, x, y, z, usageResult, TrapdoorHooks::areEqualMirrored);
      TrapdoorHooks.CHECK_OPPOSITE.set(true);
    }
  }

  private static boolean areEquivalent(final BlockState self, final BlockState other) {
    return TrapdoorHooks.isOpen(self) != TrapdoorHooks.isOpen(other)
      && TrapdoorHooks.getHalf(self) == TrapdoorHooks.getHalf(other);
  }

  private static boolean areEqualMirrored(final BlockState self, final BlockState other) {
    return TrapdoorHooks.areEquivalent(self, other)
      && TrapdoorHooks.getFacing(self) == TrapdoorHooks.getFacing(other).getOpposite();
  }

  private static boolean areEqual(final BlockState self, final BlockState other) {
    return TrapdoorHooks.areEquivalent(self, other)
      && TrapdoorHooks.getFacing(self) == TrapdoorHooks.getFacing(other);
  }

  private static boolean isOpen(final BlockState state) {
    return state.get(TrapdoorBlock.field_11631);
  }

  private static Direction getFacing(final BlockState state) {
    return state.get(HorizontalFacingBlock.field_11177);
  }

  private static BlockHalf getHalf(final BlockState state) {
    return state.get(TrapdoorBlock.field_11625);
  }
}
