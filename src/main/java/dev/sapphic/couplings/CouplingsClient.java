/*
 * Copyright 2021 Chloe Dawn
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

package dev.sapphic.couplings;

import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

@Environment(EnvType.CLIENT)
public final class CouplingsClient implements ClientModInitializer {
  private static boolean serverCouplesDoors;
  private static boolean serverCouplesFenceGates;
  private static boolean serverCouplesTrapdoors;

  static boolean serverCouplesDoors() {
    return serverCouplesDoors;
  }

  static boolean serverCouplesFenceGates() {
    return serverCouplesFenceGates;
  }

  static boolean serverCouplesTrapdoors() {
    return serverCouplesTrapdoors;
  }

  @Override
  public void onInitializeClient() {
    ClientPlayNetworking.registerGlobalReceiver(Couplings.SERVER_CONFIG, (minecraft, listener, buf, sender) -> {
      Preconditions.checkArgument(buf.readableBytes() == Byte.BYTES, buf);

      final var serverConfig = buf.readByte();

      Preconditions.checkArgument(serverConfig <= ((1 << 2) | (1 << 1) | 1), buf);

      minecraft.execute(() -> {
        serverCouplesDoors = ((serverConfig >> 2) & 1) != 0;
        serverCouplesFenceGates = ((serverConfig >> 1) & 1) != 0;
        serverCouplesTrapdoors = (serverConfig & 1) != 0;
      });
    });

    ClientPlayConnectionEvents.JOIN.register((listener, sender, minecraft) -> {
      final var clientConfig = Couplings.IGNORE_SNEAKING ? 1 : 0;

      ClientPlayNetworking.send(Couplings.CLIENT_CONFIG, new FriendlyByteBuf(
        Unpooled.buffer(Byte.BYTES, Byte.BYTES).writeByte(clientConfig).asReadOnly()));
    });
  }
}
