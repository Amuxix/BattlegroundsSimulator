package me.amuxix.minions.tier1

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named}
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Beast
import me.amuxix.minions.traits.{EntersBattlefieldTrigger, LeavesBattlefieldTrigger}

object DireWolfAlpha extends Named

final case class DireWolfAlpha(
  damage: Int = Minion.defaultDamage(DireWolfAlpha.name),
  hp: Int = Minion.defaultHp(DireWolfAlpha.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean = Minion.defaultDivineShield.contains(DireWolfAlpha.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion with Beast with LeavesBattlefieldTrigger with EntersBattlefieldTrigger {

  override type T = DireWolfAlpha

  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (DireWolfAlpha.apply _).tupled(t)

  /**
   * Trigger that does something after anything leaves the battlefield
   *
   * @param deadMinion The minion that just left the battlefield
   * @return The possible battlefields after effects have been applied
   */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(deadMinion)
    if (deadMinion.uuid == this.uuid) {
      val adjacentMinions = alliedTeam.lastAdjacentToDead(deadMinion)
      val adjacentMinionsUUIDs = adjacentMinions.map(_.uuid)
      battlefield.modifyMultipleTeamMinions(deadMinion.side, 2, m => adjacentMinionsUUIDs.contains(m.uuid)) { minion =>
        minion.copy(damage = minion.damage - goldenMultiplier)
      }
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
