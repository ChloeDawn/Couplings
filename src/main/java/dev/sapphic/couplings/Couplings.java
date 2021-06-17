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
import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;

public final class Couplings implements ModInitializer {
  public static final int COUPLING_DISTANCE = 64;
  public static final int COUPLING_SIGNAL = 8;

  static final ResourceLocation CLIENT_CONFIG = new ResourceLocation("couplings", "client_config");
  static final ResourceLocation SERVER_CONFIG = new ResourceLocation("couplings", "server_config");

  static final boolean IGNORE_SNEAKING;
  static final boolean COUPLE_DOORS;
  static final boolean COUPLE_FENCE_GATES;
  static final boolean COUPLE_TRAPDOORS;

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

    IGNORE_SNEAKING = config.get(ignoreSneaking);
    COUPLE_DOORS = config.get(coupleDoors);
    COUPLE_FENCE_GATES = config.get(coupleFenceGates);
    COUPLE_TRAPDOORS = config.get(coupleTrapdoors);

    if (!COUPLE_DOORS || !COUPLE_FENCE_GATES || !COUPLE_TRAPDOORS) {
      LogManager.getLogger().warn("No features are enabled, this could be a bug!");
    }
  }

  public static boolean ignoresSneaking(final Player player) {
    if (player instanceof CouplingsPlayer) {
      return CouplingsPlayer.ignoresSneaking(player);
    }

    return IGNORE_SNEAKING;
  }

  public static boolean couplesDoors(final Level level) {
    return level.isClientSide() ? CouplingsClient.serverCouplesDoors() : COUPLE_DOORS;
  }

  public static boolean couplesFenceGates(final Level level) {
    return level.isClientSide() ? CouplingsClient.serverCouplesFenceGates() : COUPLE_FENCE_GATES;
  }

  public static boolean couplesTrapdoors(final Level level) {
    return level.isClientSide() ? CouplingsClient.serverCouplesTrapdoors() : COUPLE_TRAPDOORS;
  }

  @Override
  public void onInitialize() {
    ServerPlayNetworking.registerGlobalReceiver(CLIENT_CONFIG, (server, player, listener, buf, sender) -> {
      Preconditions.checkArgument(buf.isReadable(Byte.BYTES), buf);
      CouplingsPlayer.ignoresSneaking(player, buf.readBoolean());
      Preconditions.checkArgument(!buf.isReadable(), buf);
    });

    ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
      ServerPlayNetworking.send(listener.player, SERVER_CONFIG, new FriendlyByteBuf(
        Unpooled.buffer(Byte.BYTES * 3, Byte.BYTES * 3)
          .writeBoolean(COUPLE_DOORS)
          .writeBoolean(COUPLE_FENCE_GATES)
          .writeBoolean(COUPLE_TRAPDOORS)
          .asReadOnly()));
    });
  }
}
