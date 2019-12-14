/*
 * Copyright (C) 2019 InsomniaKitten
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

package io.github.chloedawn.couplings;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public final class FenceGates {
  private static final ThreadLocal<Boolean> USE_NEIGHBORS = ThreadLocal.withInitial(() -> true);

  private FenceGates() {
  }

  public static void used(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult hit, final ActionResult usageResult) {
    if (usageResult.isAccepted() && Couplings.areFenceGatesEnabled() && USE_NEIGHBORS.get() && (!player.isSneaking() || Couplings.isSneakingIgnored())) {
      USE_NEIGHBORS.set(false);
      final Block block = state.getBlock();
      final boolean open = state.get(FenceGateBlock.OPEN);
      final Axis axis = state.get(FenceGateBlock.FACING).getAxis();
      for (final BlockPos offset : BlockPos.iterate(
        pos.down(Couplings.getCouplingRange()),
        pos.up(Couplings.getCouplingRange())
      )) {
        if (Couplings.isUsable(world, offset, player)) {
          final BlockState other = world.getBlockState(offset);

          if (block == other.getBlock() && equals(open, axis, other)) {
            if (Couplings.use(other, world, hand, player, hit, offset, usageResult)) {
              USE_NEIGHBORS.set(false);
              return;
            }
          }
        }
      }
      USE_NEIGHBORS.set(true);
    }
  }

  private static boolean equals(final boolean open, final Axis axis, final BlockState state) {
    return open != state.get(FenceGateBlock.OPEN) && axis == state.get(FenceGateBlock.FACING).getAxis();
  }
}
