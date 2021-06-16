/*
 * Copyright (C) 2020 Chloe Dawn
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

package dev.sapphic.couplings;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;

public final class Couplings {
  public static final int COUPLING_DISTANCE = 64;

  public static final boolean IGNORE_SNEAKING;
  public static final boolean COUPLE_DOORS;
  public static final boolean COUPLE_FENCE_GATES;
  public static final boolean COUPLE_TRAPDOORS;

  static {
    final CommentedFileConfig config = CommentedFileConfig.of(
      FabricLoader.getInstance().getConfigDir().resolve("couplings.toml"));

    config.load();

    final String ignoreSneaking = "ignore_sneaking";
    final String coupleDoors = "couple_doors";
    final String coupleFenceGates = "couple_fence_gates";
    final String coupleTrapdoors = "couple_trapdoors";

    final ConfigSpec spec = new ConfigSpec();

    spec.define(ignoreSneaking, true);
    spec.define(coupleDoors, true);
    spec.define(coupleFenceGates, true);
    spec.define(coupleTrapdoors, true);

    spec.correct(config);

    config.setComment(ignoreSneaking, "Couple regardless of whether the player is sneaking when interacting");
    config.setComment(coupleDoors, "Couple neighboring doors with opposing hinges");
    config.setComment(coupleFenceGates, "Couple neighboring fence gates above and below on the same axis");
    config.setComment(coupleTrapdoors, "Couple neighboring trapdoors along either sides and opposing");

    config.save();

    IGNORE_SNEAKING = config.getOrElse(ignoreSneaking, true);
    COUPLE_DOORS = config.getOrElse(coupleDoors, true);
    COUPLE_FENCE_GATES = config.getOrElse(coupleFenceGates, true);
    COUPLE_TRAPDOORS = config.getOrElse(coupleTrapdoors, true);

    if (!COUPLE_DOORS || !COUPLE_FENCE_GATES || !COUPLE_TRAPDOORS) {
      LogManager.getLogger().warn("No features are enabled, this could be a bug!");
    }
  }

  private Couplings() {
  }
}
