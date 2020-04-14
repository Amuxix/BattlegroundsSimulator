package me.amuxix.minions.traits

import cats.data.NonEmptyList
import me.amuxix.Battlefield
import me.amuxix.minions.Minion

trait EntersBattlefieldTrigger { this: Minion =>

  /**
    * Trigger that does something when anything enters the battlefield
    * @param newMinion The minion that just entered the battlefield
    * @return The possible battlefields after effects have been applied
    */
  def onEntersBattlefieldTrigger(battlefield: Battlefield, newMinion: Minion): NonEmptyList[Battlefield]
}
