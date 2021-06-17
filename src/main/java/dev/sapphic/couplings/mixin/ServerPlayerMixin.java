package dev.sapphic.couplings.mixin;

import com.mojang.authlib.GameProfile;
import dev.sapphic.couplings.CouplingsPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerMixin extends Player implements CouplingsPlayer {
  @Unique private boolean couplingIgnoresSneaking = true;

  ServerPlayerMixin(final Level level, final BlockPos pos, final float spawnAngle, final GameProfile profile) {
    super(level, pos, spawnAngle, profile);
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
