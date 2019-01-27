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

package io.github.insomniakitten.couplings.mixin;

import io.github.insomniakitten.couplings.hook.DoorHooks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DoorBlock.class)
final class DoorMixin {
  private static final String ACTIVATE =
    "Lnet/minecraft/block/DoorBlock;" +
      "activate(" +
        "Lnet/minecraft/block/BlockState;" +
        "Lnet/minecraft/world/World;" +
        "Lnet/minecraft/util/math/BlockPos;" +
        "Lnet/minecraft/entity/player/PlayerEntity;" +
        "Lnet/minecraft/util/Hand;" +
        "Lnet/minecraft/util/hit/BlockHitResult;" +
      ")Z";

  private static final String NEIGHBOR_UPDATE =
    "Lnet/minecraft/block/DoorBlock;" +
      "neighborUpdate(" +
        "Lnet/minecraft/block/BlockState;" +
        "Lnet/minecraft/world/World;" +
        "Lnet/minecraft/util/math/BlockPos;" +
        "Lnet/minecraft/block/Block;" +
        "Lnet/minecraft/util/math/BlockPos;" +
      ")V";

  private static final String SET_BLOCK_STATE =
    "Lnet/minecraft/world/World;" +
      "setBlockState(" +
        "Lnet/minecraft/util/math/BlockPos;" +
        "Lnet/minecraft/block/BlockState;" +
        "I" +
      ")Z";

  private DoorMixin() {}

  @Inject(
    method = DoorMixin.ACTIVATE,
    at = @At(
      value = "RETURN",
      ordinal = 1
    ),
    allow = 1
  )
  private void coupled$use(
    final BlockState state,
    final World world,
    final BlockPos pos,
    final PlayerEntity player,
    final Hand hand,
    final BlockHitResult hit,
    final CallbackInfoReturnable<Boolean> cir
  ) {
    DoorHooks.usageCallback(state, world, pos, player, hand, hit, cir.getReturnValueZ());
  }

  @Inject(
    method = DoorMixin.NEIGHBOR_UPDATE,
    at = @At(
      value = "INVOKE",
      target = DoorMixin.SET_BLOCK_STATE,
      shift = Shift.AFTER
    ),
    locals = LocalCapture.CAPTURE_FAILHARD
  )
  private void coupled$neighborUpdated(
    final BlockState state,
    final World world,
    final BlockPos pos,
    final Block block,
    final BlockPos neighborPos,
    final CallbackInfo ci,
    final boolean isPowered
  ) {
    DoorHooks.neighborUpdateCallback(state, world, pos, block, neighborPos, isPowered);
  }
}
