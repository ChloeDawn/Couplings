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
import io.github.insomniakitten.couplings.CouplingsOptions;
import net.minecraft.block.Block;
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
    if (!CouplingsOptions.getFeatures().areTrapdoorsEnabled()) return;
    if (!TrapdoorHooks.USE_NEIGHBORS.get()) return;
    if (player.isSneaking() && !CouplingsOptions.isSneakingIgnored()) return;

    TrapdoorHooks.USE_NEIGHBORS.set(false);

    final Block block = state.getBlock();
    final boolean open = TrapdoorHooks.isOpen(state);
    final BlockHalf half = TrapdoorHooks.getHalf(state);
    final Direction facing = TrapdoorHooks.getFacing(state);
    final Direction opposite = facing.getOpposite();

    for (final BlockPos.Mutable offset : BlockPos.iterateBoxPositionsMutable(
      pos.offset(facing.rotateYCounterclockwise(), CouplingsOptions.getCouplingRange()),
      pos.offset(facing.rotateYClockwise(), CouplingsOptions.getCouplingRange())
    )) {
      if (pos.equals(offset)) {
        offset.setOffset(facing);

        if (Couplings.isUsable(world, offset, player)) {
          final BlockState mirror = world.getBlockState(offset);

          if (block == mirror.getBlock() && TrapdoorHooks.includesStates(open, half, opposite, mirror)) {
            Couplings.use(state, mirror, world, pos, offset.toImmutable(), player, hand, side, x, y, z, usageResult);
          }
        }

        offset.setOffset(opposite);
      } else if (Couplings.isUsable(world, offset, player)) {
        final BlockState other = world.getBlockState(offset);

        if (block == other.getBlock() && TrapdoorHooks.includesStates(open, half, facing, other)) {
          Couplings.use(state, other, world, pos, offset.toImmutable(), player, hand, side, x, y, z, usageResult);

          offset.setOffset(facing);

          if (Couplings.isUsable(world, offset, player)) {
            final BlockState mirror = world.getBlockState(offset);

            if (block == mirror.getBlock() && TrapdoorHooks.includesStates(open, half, opposite, mirror)) {
              Couplings.use(state, mirror, world, pos, offset.toImmutable(), player, hand, side, x, y, z, usageResult);
            }
          }

          offset.setOffset(opposite);
        }
      }
    }

    TrapdoorHooks.USE_NEIGHBORS.set(true);
  }

  private static boolean includesStates(final boolean open, final BlockHalf half, final Direction facing, final BlockState state) {
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
