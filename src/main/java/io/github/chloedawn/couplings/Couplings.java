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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class Couplings {
  public static final int DEFAULT_RANGE = 128;
  public static final String JSON = "couplings.json";

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Logger LOGGER = LogManager.getLogger();

  private static Options options = new Options();
  private static boolean loaded = false;

  private Couplings() {
  }

  public static void load() {
    if (loaded) {
      throw new IllegalStateException();
    }
    readOptions();
    loaded = true;
  }

  public static boolean isSneakingIgnored() {
    return options.ignoreSneaking;
  }

  public static int getCouplingRange() {
    return options.couplingRange;
  }

  public static boolean areDoorsEnabled() {
    return options.enabledFeatures.doors;
  }

  public static boolean areFenceGatesEnabled() {
    return options.enabledFeatures.fenceGates;
  }

  public static boolean areTrapdoorsEnabled() {
    return options.enabledFeatures.trapdoors;
  }

  public static boolean isUsable(final CollisionView world, final BlockPos pos, final PlayerEntity player) {
    return player.canModifyWorld() && world.getWorldBorder().contains(pos);
  }

  public static boolean use(final BlockState other, final World world, final Hand hand, final PlayerEntity player, final BlockHitResult origin, final BlockPos offset, final ActionResult originResult) {
    return !other.onUse(world, player, hand, new BlockHitResult(origin.getPos(), origin.getSide(), offset, false)).isAccepted();
  }

  private static void readOptions() {
    try (final Reader reader = Files.newBufferedReader(getPathToJson())) {
      final @Nullable Options o = GSON.fromJson(reader, Options.class);
      if (o == null) {
        writeOptions();
      } else {
        Couplings.options = o;
      }
    } catch (final JsonSyntaxException e) {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
        throw e;
      }
      writeOptions();
    } catch (final NoSuchFileException e) {
      writeOptions();
    } catch (final IOException e) {
      throw new RuntimeException("Reading options", e);
    }
  }

  private static void writeOptions() {
    try (final Writer writer = Files.newBufferedWriter(getPathToJson())) {
      writer.write(GSON.toJson(options));
    } catch (final IOException e) {
      throw new RuntimeException("Writing options", e);
    }
  }

  private static Path getPathToJson() {
    return FabricLoader.getInstance().getConfigDirectory().toPath().resolve(JSON);
  }

  private static final class Options {
    @SerializedName("ignore_sneaking")
    private final boolean ignoreSneaking;

    @SerializedName("coupling_range")
    private final int couplingRange;

    @SerializedName("enabled_features")
    private final Features enabledFeatures;

    private Options(final boolean ignoreSneaking, final int couplingRange, final Features enabledFeatures) {
      this.ignoreSneaking = ignoreSneaking;
      this.couplingRange = couplingRange;
      this.enabledFeatures = enabledFeatures;
    }

    private Options() {
      this(true, DEFAULT_RANGE, new Features(true, true, true));
    }

    private static final class Features {
      @SerializedName("doors")
      private final boolean doors;

      @SerializedName("fence_gates")
      private final boolean fenceGates;

      @SerializedName("trapdoors")
      private final boolean trapdoors;

      private Features(final boolean doors, final boolean fenceGates, final boolean trapdoors) {
        this.doors = doors;
        this.fenceGates = fenceGates;
        this.trapdoors = trapdoors;
      }
    }
  }
}
