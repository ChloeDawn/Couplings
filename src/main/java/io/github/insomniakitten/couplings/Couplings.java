package io.github.insomniakitten.couplings;

import com.google.common.base.MoreObjects;
import net.fabricmc.loader.launch.knot.Knot;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Couplings {
  public static final int COUPLING_RANGE = 128;

  private static final Logger LOGGER = LogManager.getLogger("coupled");

  private Couplings() {}

  public static boolean isUsable(final World world, final BlockPos pos, final PlayerEntity player) {
    if (!player.canModifyWorld()) {
      if (Knot.getLauncher().isDevelopment()) {
        Couplings.LOGGER.warn("Skipping usage as world modification is disallowed for user ", player);
      }

      return false;
    }

    if (!world.isBlockLoaded(pos)) {
      if (Knot.getLauncher().isDevelopment()) {
        Couplings.LOGGER.warn("Skipping usage as block is unloaded at {}", pos);
      }

      return false;
    }

    if (!world.getWorldBorder().contains(pos)) {
      if (Knot.getLauncher().isDevelopment()) {
        Couplings.LOGGER.warn("Skipping usage as block is outside of the world border at {}", pos);
      }

      return false;
    }

    return true;
  }

  public static void use(
    final BlockState state,
    final BlockState other,
    final World world,
    final BlockPos pos,
    final BlockPos offset,
    final PlayerEntity player,
    final Hand hand,
    final Direction side,
    final float x,
    final float y,
    final float z,
    final boolean usageResult
  ) {
    final boolean otherUsageResult = other.activate(world, offset, player, hand, side, x, y, z);

    if (usageResult != otherUsageResult) {
      final String result1 = Couplings.toString(state, world, pos, player, hand, side, x, y, z, usageResult);
      final String result2 = Couplings.toString(other, world, offset, player, hand, side, x, y, z, otherUsageResult);

      if (Knot.getLauncher().isDevelopment()) {
        throw new IllegalStateException("Usage result mismatch between " + result1 + " and " + result2);
      }

      Couplings.LOGGER.warn("Usage result mismatch between {} and {}", result1, result2);
    }
  }

  private static String toString(
    final BlockState state,
    final World world,
    final BlockPos pos,
    final PlayerEntity player,
    final Hand hand,
    final Direction side,
    final float x,
    final float y,
    final float z,
    final boolean result
  ) {
    return MoreObjects.toStringHelper(result ? "Success" : "Failure")
      .add("state", state)
      .add("world", world)
      .add("pos", pos)
      .add("player", player)
      .add("hand", hand)
      .add("side", side)
      .add("x", x)
      .add("y", y)
      .add("z", z)
      .toString();
  }
}
