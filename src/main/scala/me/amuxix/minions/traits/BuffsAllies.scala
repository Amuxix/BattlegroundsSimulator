package me.amuxix.minions.traits

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Team}
import me.amuxix.minions.Minion

trait BuffsAllies extends EntersBattlefieldTrigger with LeavesBattlefieldTrigger { this: Minion =>

  def validMinions(team: Team): List[Minion]

  def addBuff(minion: Minion): Minion

  def removeBuff(minion: Minion): Minion

  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(deadMinion)
    if (deadMinion.uuid == this.uuid) {
      val adjacentMinionsUUIDs = validMinions(alliedTeam).map(_.uuid)
      battlefield.modifyAllTeamMinions(deadMinion.side) {
        case minion if adjacentMinionsUUIDs.contains(minion.uuid) => removeBuff(minion)
        case minion => minion
      }.nel
    } else {
      battlefield.nel
    }
  }

  /**
   * Trigger that does something when anything enters the battlefield
   *
   * @param newMinion The minion that just entered the battlefield
   * @return The possible battlefields after effects have been applied
   */
  override def onEntersBattlefieldTrigger(battlefield: Battlefield, newMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(newMinion)
    if (newMinion.uuid == this.uuid) {
      val adjacentMinions = alliedTeam.adjacentMinions(newMinion)
      val adjacentMinionsUUIDs = adjacentMinions.map(_.uuid)
      battlefield.modifyMultipleTeamMinions(newMinion.side, 2, m => adjacentMinionsUUIDs.contains(m.uuid)) { minion =>
        minion.copy(damage = minion.damage + goldenMultiplier)
      }
    } else {
      battlefield.nel
    }
  }
}
