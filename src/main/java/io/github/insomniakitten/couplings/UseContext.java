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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class UseContext {
  private final World world;
  private final PlayerEntity user;
  private final Hand hand;
  private final BlockHitResult originHit;
  private final BlockHitResult targetHit;
  private final BlockState originState;
  private final BlockState otherState;

  private UseContext(
    final BlockState originState,
    final BlockState otherState,
    final World world,
    final PlayerEntity user,
    final Hand hand,
    final BlockHitResult originHit,
    final BlockHitResult targetHit
  ) {
    this.originState = originState;
    this.otherState = otherState;
    this.world = world;
    this.user = user;
    this.hand = hand;
    this.originHit = originHit;
    this.targetHit = targetHit;
  }

  public static UseContext of(
    final BlockState state,
    final BlockState other,
    final World world,
    final BlockPos pos,
    final PlayerEntity user,
    final Hand hand,
    final Direction side,
    final Vec3d hit,
    final BlockPos offset
  ) {
    final BlockHitResult origin = new BlockHitResult(hit, side, pos, false);
    final BlockHitResult target = new BlockHitResult(hit, side, offset, false);
    return new UseContext(state, other, world, user, hand, origin, target);
  }

  public static UseContext of(
    final BlockState state,
    final BlockState other,
    final World world,
    final PlayerEntity user,
    final Hand hand,
    final BlockHitResult origin,
    final BlockPos offset
  ) {
    final BlockHitResult target = new BlockHitResult(origin.getPos(), origin.getSide(), offset, false);
    return new UseContext(state, other, world, user, hand, origin, target);
  }

  public World world() {
    return this.world;
  }

  public PlayerEntity user() {
    return this.user;
  }

  public Hand hand() {
    return this.hand;
  }

  public BlockHitResult originHit() {
    return this.originHit;
  }

  public BlockHitResult targetHit() {
    return this.targetHit;
  }

  public BlockState originState() {
    return this.originState;
  }

  public BlockState otherState() {
    return this.otherState;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("world", this.world)
      .add("user", this.user)
      .add("hand", this.hand)
      .add("originHit", this.originHit)
      .add("targetHit", this.targetHit)
      .add("originState", this.originState)
      .add("otherState", this.otherState)
      .toString();
  }
}
