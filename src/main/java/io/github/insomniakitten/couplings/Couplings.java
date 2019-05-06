package io.github.insomniakitten.couplings;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.knot.Knot;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;

public final class Couplings {
  public static final int COUPLING_RANGE = 128;

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final boolean DEVELOPMENT = Knot.getLauncher().isDevelopment();
  private static final Logger LOGGER = LogManager.getLogger("coupled");

  @Nullable
  private static Options options;

  private Couplings() {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  @SneakyThrows
  public static void loadOptions() {
    final Path configs = FabricLoader.getInstance().getConfigDirectory().toPath();
    final File config = configs.resolve("couplings.json").toFile();
    if (config.exists()) {
      @Cleanup final FileReader reader = new FileReader(config);
      final Options options = Couplings.GSON.fromJson(reader, Options.class);
      checkState(options != null, "Null config %s", config);
      Couplings.options = options;
    } else {
      if (config.createNewFile()) {
        final Options.Features features = new Options.Features(true, true, true);
        Couplings.options = new Options(true, Couplings.COUPLING_RANGE, features);
        @Cleanup final FileWriter writer = new FileWriter(config);
        writer.append(Couplings.GSON.toJson(Couplings.options));
      }
    }
  }

  public static boolean requiresNoSneaking() {
    return !Couplings.options().ignoreSneaking();
  }

  public static int getCouplingRange() {
    return Couplings.options().couplingRange();
  }

  public static boolean areDoorsEnabled() {
    return Couplings.options().enabledFeatures().areDoorsEnabled();
  }

  public static boolean areFenceGatesEnabled() {
    return Couplings.options().enabledFeatures().areFenceGatesEnabled();
  }

  public static boolean areTrapdoorsEnabled() {
    return Couplings.options().enabledFeatures().areTrapdoorsEnabled();
  }

  public static boolean isUsable(final World world, final BlockPos pos, final PlayerEntity player) {
    if (!player.canModifyWorld()) {
      if (Couplings.DEVELOPMENT) {
        Couplings.LOGGER.warn("Skipping usage as world modification is disallowed for user {}", player);
      }
      return false;
    }
    if (!world.isBlockLoaded(pos)) {
      if (Couplings.DEVELOPMENT) {
        Couplings.LOGGER.warn("Skipping usage as block is unloaded at {}", pos);
      }
      return false;
    }
    if (!world.getWorldBorder().contains(pos)) {
      if (Couplings.DEVELOPMENT) {
        Couplings.LOGGER.warn("Skipping usage as block is outside of the world border at {}", pos);
      }
      return false;
    }
    return true;
  }

  public static void use(final BlockState state, final BlockState other, final World world, final Hand hand, final PlayerEntity player, final BlockHitResult origin, final BlockPos offset, final boolean usageResult) {
    final BlockHitResult target = new BlockHitResult(origin.getPos(), origin.getSide(), offset, false);
    final boolean otherUsageResult = other.activate(world, player, hand, target);
    if (usageResult != otherUsageResult) {
      final String result1 = Couplings.toString(world, player, hand, state, origin, usageResult);
      final String result2 = Couplings.toString(world, player, hand, other, target, usageResult);
      if (Couplings.DEVELOPMENT) {
        throw new IllegalStateException("Usage result mismatch between " + result1 + " and " + result2);
      }
      Couplings.LOGGER.warn("Usage result mismatch between {} and {}", result1, result2);
    }
  }

  private static String toString(final World world, final PlayerEntity player, final Hand hand, final BlockState state, final BlockHitResult hit, final boolean result) {
    return MoreObjects.toStringHelper(result ? "Success" : "Failure")
      .add("world", world)
      .add("player", player)
      .add("hand", hand)
      .add("state", state)
      .add("hit", hit)
      .toString();
  }

  private static Options options() {
    final Options options = Couplings.options;
    checkState(options != null, "Options not loaded");
    return options;
  }

  @Value
  @Accessors(fluent = true)
  private static final class Options {
    @SerializedName("ignore_sneaking")
    private final boolean ignoreSneaking;

    @SerializedName("coupling_range")
    private final int couplingRange;

    @SerializedName("enabled_features")
    private final Options.Features enabledFeatures;

    @Value
    public static final class Features {
      @SerializedName("doors")
      private final boolean areDoorsEnabled;

      @SerializedName("fence_gates")
      private final boolean areFenceGatesEnabled;

      @SerializedName("trapdoors")
      private final boolean areTrapdoorsEnabled;
    }
  }
}
