/*
 * Copyright 2022 Chloe Dawn
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

import com.mojang.authlib.GameProfile;
import dev.sapphic.couplings.CouplingsPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin extends Player implements CouplingsPlayer {
  @Unique private boolean couplingIgnoresSneaking = true;

  ServerPlayerMixin(
      final Level level, final BlockPos pos, final float spawnAngle, final GameProfile profile, final @Nullable ProfilePublicKey profilePublicKey) {
    super(level, pos, spawnAngle, profile, profilePublicKey);
  }

  @Unique
  @Override
  public final boolean couplingIgnoresSneaking() {
    return this.couplingIgnoresSneaking;
  }

  @Unique
  @Override
  public final void couplingIgnoresSneaking(final boolean value) {
    this.couplingIgnoresSneaking = value;
  }
}
