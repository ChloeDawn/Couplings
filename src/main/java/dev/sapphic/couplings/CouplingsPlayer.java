package dev.sapphic.couplings;

import net.minecraft.world.entity.player.Player;

public interface CouplingsPlayer {
  static boolean ignoresSneaking(final Player player) {
    return ((CouplingsPlayer) player).couplingIgnoresSneaking();
  }

  static void ignoresSneaking(final Player player, final boolean value) {
    ((CouplingsPlayer) player).couplingIgnoresSneaking(value);
  }

  boolean couplingIgnoresSneaking();

  void couplingIgnoresSneaking(boolean value);
}
