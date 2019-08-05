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

package io.github.insomniakitten.couplings;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
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
import java.io.IOException;
import java.nio.file.Path;

public final class Couplings {
  private static final boolean DEVELOPMENT = Knot.getLauncher().isDevelopment();
  private static final Logger LOGGER = LogManager.getLogger("couplings");

  private Couplings() {}

  @Deprecated
  public static void loadOptions() {
    try {
      final Path configs = FabricLoader.getInstance().getConfigDirectory().toPath();
      final File config = configs.resolve("couplings.json").toFile();
      if (config.exists()) {
        try (final FileReader reader = new FileReader(config)) {
          @Nullable final Options options = Options.fromJson(reader);
          if (options == null) {
            LOGGER.error("Invalid config: {}", config);
          } else {
            Options.instance = options;
            return;
          }
        }
      }
      try (final FileWriter writer = new FileWriter(config)) {
        writer.append(Options.toJson());
      }
    } catch (final IOException e) {
      if (DEVELOPMENT) {
        throw new RuntimeException("Loading options from file", e);
      }
      LOGGER.error("Failed to load options from file");
    }
  }

  public static boolean requiresNoSneaking() {
    return !Options.instance.ignoreSneaking();
  }

  public static int getCouplingRange() {
    return Options.instance.couplingRange();
  }

  public static boolean areDoorsEnabled() {
    return Options.instance.enabledFeatures().areDoorsEnabled();
  }

  public static boolean areFenceGatesEnabled() {
    return Options.instance.enabledFeatures().areFenceGatesEnabled();
  }

  public static boolean areTrapdoorsEnabled() {
    return Options.instance.enabledFeatures().areTrapdoorsEnabled();
  }

  public static boolean isUsable(final World world, final BlockPos pos, final PlayerEntity player) {
    return player.canModifyWorld() && world.getWorldBorder().contains(pos);
  }

  public static void use(final BlockState state, final BlockState other, final World world, final Hand hand, final PlayerEntity player, final BlockHitResult origin, final BlockPos offset, final boolean usageResult) {
    final BlockHitResult target = new BlockHitResult(origin.getPos(), origin.getSide(), offset, false);
    if (usageResult != other.activate(world, player, hand, target)) {
      final String result1 = toString(world, player, hand, state, origin, usageResult);
      final String result2 = toString(world, player, hand, other, target, usageResult);
      if (DEVELOPMENT) {
        throw new IllegalStateException("Usage result mismatch between " + result1 + " and " + result2);
      }
      LOGGER.warn("Usage result mismatch between {} and {}", result1, result2);
    }
  }

  private static String toString(final World world, final PlayerEntity player, final Hand hand, final BlockState state, final BlockHitResult hit, final boolean result) {
    return MoreObjects.toStringHelper(result ? "Success" : "Failure").add("world", world).add("player", player).add("hand", hand).add("state", state).add("hit", hit).toString();
  }

  @Value
  @Accessors(fluent = true)
  private static final class Options {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Options DEFAULT = new Options(true, 128, Features.DEFAULT);

    private static Options instance = DEFAULT;

    @SerializedName("ignore_sneaking")
    private final boolean ignoreSneaking;

    @SerializedName("coupling_range")
    private final int couplingRange;

    @SerializedName("enabled_features")
    private final Features enabledFeatures;

    @Nullable
    private static Options fromJson(final FileReader reader) {
      try {
        return GSON.fromJson(reader, Options.class);
      } catch (final JsonSyntaxException e) {
        if (DEVELOPMENT) {
          throw e;
        }
        return null;
      }
    }

    private static String toJson() {
      return GSON.toJson(Options.instance);
    }

    @Value
    private static final class Features {
      private static final Features DEFAULT = new Features(true, true, true);

      @SerializedName("doors")
      private final boolean areDoorsEnabled;

      @SerializedName("fence_gates")
      private final boolean areFenceGatesEnabled;

      @SerializedName("trapdoors")
      private final boolean areTrapdoorsEnabled;
    }
  }
}
