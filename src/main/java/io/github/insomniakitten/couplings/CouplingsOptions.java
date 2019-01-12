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

package io.github.insomniakitten.couplings;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.FabricLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class CouplingsOptions {
  private static final CouplingsOptions DEFAULT =
    new CouplingsOptions(false, 128, Features.DEFAULT);

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static CouplingsOptions instance =
    CouplingsOptions.DEFAULT;

  @SerializedName("ignore_sneaking")
  private final boolean ignoreSneaking;

  @SerializedName("coupling_range")
  private final int couplingRange;

  @SerializedName("enabled_features")
  private final Features enabledFeatures;

  private CouplingsOptions(final boolean ignoreSneaking, final int couplingRange, final Features enabledFeatures) {
    this.ignoreSneaking = ignoreSneaking;
    this.couplingRange = couplingRange;
    this.enabledFeatures = enabledFeatures;
  }

  public static boolean isSneakingIgnored() {
    return CouplingsOptions.instance.ignoreSneaking;
  }

  public static int getCouplingRange() {
    return CouplingsOptions.instance.couplingRange;
  }

  public static Features getFeatures() {
    return CouplingsOptions.instance.enabledFeatures;
  }

  private static void load() {
    final Path configs = FabricLoader.INSTANCE.getConfigDirectory().toPath();
    final File config = configs.resolve("couplings.json").toFile();
    if (config.exists()) {
      try (final FileReader reader = new FileReader(config)) {
        CouplingsOptions.instance = CouplingsOptions.GSON.fromJson(reader, CouplingsOptions.class);
        if (CouplingsOptions.instance == null) {
          throw new IllegalArgumentException(reader.toString());
        }
      } catch (final FileNotFoundException e) {
        throw new IllegalStateException();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      try {
        if (config.createNewFile()) {
          try (final FileWriter writer = new FileWriter(config)) {
            writer.append(CouplingsOptions.GSON.toJson(CouplingsOptions.instance));
          }
        }
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Deprecated
  public static final class Loader implements ModInitializer {
    @Override
    public void onInitialize() {
      CouplingsOptions.load();
    }
  }

  public static final class Features {
    private static final Features DEFAULT =
      new Features(true, true, true);

    private final boolean doors;

    @SerializedName("fence_gates")
    private final boolean fenceGates;

    private final boolean trapdoors;

    private Features(final boolean doors, final boolean fenceGates, final boolean trapdoors) {
      this.doors = doors;
      this.fenceGates = fenceGates;
      this.trapdoors = trapdoors;
    }

    public boolean areDoorsEnabled() {
      return this.doors;
    }

    public boolean areFenceGatesEnabled() {
      return this.fenceGates;
    }

    public boolean areTrapdoorsEnabled() {
      return this.trapdoors;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.doors, this.fenceGates, this.trapdoors);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() == obj.getClass()) {
        final Features features = (Features) obj;
        return this.doors == features.doors &&
          this.fenceGates == features.fenceGates &&
          this.trapdoors == features.trapdoors;
      }
      return false;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("doors", this.doors)
        .add("fenceGates", this.fenceGates)
        .add("trapdoors", this.trapdoors)
        .toString();
    }
  }

  @Override
  public boolean equals(@Nullable final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() == obj.getClass()) {
      final CouplingsOptions that = (CouplingsOptions) obj;
      return this.ignoreSneaking == that.ignoreSneaking &&
        this.couplingRange == that.couplingRange &&
        Objects.equals(this.enabledFeatures, that.enabledFeatures);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.ignoreSneaking, this.couplingRange, this.enabledFeatures);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("ignoreSneaking", this.ignoreSneaking)
      .add("couplingRange", this.couplingRange)
      .add("enabledFeatures", this.enabledFeatures)
      .toString();
  }
}
