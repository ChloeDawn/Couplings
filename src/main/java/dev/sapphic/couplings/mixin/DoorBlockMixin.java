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

package dev.sapphic.couplings.mixin;

import dev.sapphic.couplings.impl.DoorBlockCoupling;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DoorBlock.class)
abstract class DoorBlockMixin extends Block {
  DoorBlockMixin(final Properties properties) {
    super(properties);
  }

  @Inject(
    method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
    at = @At(shift = At.Shift.AFTER, value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
    require = 1, allow = 1)
  private void used(final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit, final CallbackInfoReturnable<InteractionResult> cir) {
    DoorBlockCoupling.used(state, level, pos, player);
  }

  @Inject(
    method = "setOpen(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Z)V",
    at = @At(shift = At.Shift.AFTER, value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/world/level/block/DoorBlock;playSound(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Z)V"),
    require = 1, allow = 1)
  private void openStateChanged(final Level level, final BlockState state, final BlockPos pos, final boolean open, final CallbackInfo ci) {
    DoorBlockCoupling.openStateChanged(state, level, pos, open);
  }

  @Inject(
    method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;Z)V",
    at = @At(shift = At.Shift.AFTER, value = "INVOKE", opcode = Opcodes.INVOKEVIRTUAL,
      target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
    require = 1, allow = 1, locals = LocalCapture.CAPTURE_FAILHARD)
  private void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block, final BlockPos offset, final boolean moved, final CallbackInfo ci, final boolean powered) {
    DoorBlockCoupling.neighborChanged(state, level, pos, powered);
  }
}
