package me.amuxix.minions.traits

import cats.data.NonEmptyList
import me.amuxix.minions.Minion
import me.amuxix.minions.tier5.BaronRivendare
import me.amuxix.{Battlefield, Team}

trait LeavesBattlefieldTrigger { this: Minion =>

  /**
    * Trigger that does something after anything leaves the battlefield
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  protected def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield]

  def onLeavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(this)
    var battlefields = NonEmptyList.one(battlefield)
    var remainingTriggers = LeavesBattlefieldTrigger.baronMultiplier(alliedTeam)
    while (remainingTriggers > 0) {
      remainingTriggers -= 1
      battlefields = battlefields.flatMap(leavesBattlefieldTrigger(_, deadMinion))
    }
    battlefields
  }
}

object LeavesBattlefieldTrigger {

  def baronMultiplier(team: Team): Int =
    team.minions
      .collectFirst {
        case baron: BaronRivendare if baron.isGolden => 3
        case _: BaronRivendare                       => 2
      }
      .getOrElse(1)
}
