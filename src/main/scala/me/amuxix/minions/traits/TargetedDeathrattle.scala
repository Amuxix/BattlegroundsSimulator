package me.amuxix.minions.traits
import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Team}
import me.amuxix.minions.Minion

trait TargetedDeathrattle extends LeavesBattlefieldTrigger { this: Minion =>
  /**
    * Requirement for this trigger to activate
    */
  def requirement(battlefield: Battlefield, deadMinion: Minion, alliedTeam: Team, enemyTeam: Team): Boolean

  def deathrattleTrigger(
    battlefield: Battlefield,
    deadMinion: Minion,
    alliedTeam: Team,
    enemyTeam: Team
  ): NonEmptyList[Battlefield]

  /**
    * Trigger that does something after anything leaves the battlefield
    *
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override protected def leavesBattlefieldTrigger(
    battlefield: Battlefield,
    deadMinion: Minion,
  ): NonEmptyList[Battlefield] = {
    val (alliedTeam, enemyTeam) = battlefield.teams(deadMinion)
    if (deadMinion.uuid == this.uuid && requirement(battlefield, deadMinion, alliedTeam, enemyTeam)) {
      deathrattleTrigger(battlefield, deadMinion, alliedTeam, enemyTeam)
    } else {
      battlefield.nel
    }
  }
}
