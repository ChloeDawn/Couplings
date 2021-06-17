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
