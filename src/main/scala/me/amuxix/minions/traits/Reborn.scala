package me.amuxix.minions.traits

import cats.data.NonEmptyList
import me.amuxix.Battlefield
import me.amuxix.minions.Minion

trait Reborn extends LeavesBattlefieldTrigger { this: Minion =>
  override val hasReborn: Boolean = true

  override def onLeavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] =
    super
    .onLeavesBattlefieldTrigger(battlefield, deadMinion)
}
