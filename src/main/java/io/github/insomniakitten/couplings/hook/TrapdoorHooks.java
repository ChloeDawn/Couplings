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
  private static final ThreadLocal<Boolean> USE_NEIGHBORS =
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
    if (TrapdoorHooks.USE_NEIGHBORS.get()) {
      if (player.isSneaking()) return; // todo config

      TrapdoorHooks.USE_NEIGHBORS.set(false);

      final boolean open = TrapdoorHooks.isOpen(state);
      final BlockHalf half = TrapdoorHooks.getHalf(state);
      final Direction facing = TrapdoorHooks.getFacing(state);
      final Direction opposite = facing.getOpposite();

      for (final BlockPos.Mutable offset : BlockPos.iterateBoxPositionsMutable(
        pos.offset(facing.rotateYCounterclockwise(), Couplings.COUPLING_RANGE),
        pos.offset(facing.rotateYClockwise(), Couplings.COUPLING_RANGE)
      )) {
        Couplings.useNeighbor(state, world, pos, offset, player, hand, side, x, y, z, usageResult,
          (self, other) -> TrapdoorHooks.includesStates(open, half, facing, other)
        );

        offset.setOffset(facing);

        Couplings.useNeighbor(state, world, pos, offset, player, hand, side, x, y, z, usageResult,
          (self, other) -> TrapdoorHooks.includesStates(open, half, opposite, other)
        );

        offset.setOffset(opposite);
      }

      TrapdoorHooks.USE_NEIGHBORS.set(true);
    }
  }

  private static boolean includesStates(
    final boolean open,
    final BlockHalf half,
    final Direction facing,
    final BlockState state
  ) {
    return open != TrapdoorHooks.isOpen(state)
      && half == TrapdoorHooks.getHalf(state)
      && facing == TrapdoorHooks.getFacing(state);
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
