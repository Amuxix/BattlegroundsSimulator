package me.amuxix.minions.tier1

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named, Side, Team}
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
) extends Minion
    with Beast
    with LeavesBattlefieldTrigger
    with EntersBattlefieldTrigger {

  override type T = DireWolfAlpha

  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (DireWolfAlpha.apply _).tupled(t)

  private def addBuff(minion: Minion): Minion = minion.copy(damage = minion.damage + goldenMultiplier)

  private def removeBuff(minion: Minion): Minion = minion.copy(damage = minion.damage - goldenMultiplier)


  private def getOtherAdjacent(team: Team, minion: Minion, adjacentMinion: Minion): Option[Minion] = {
    val offset = minion.id - adjacentMinion.id
    assert(offset == 1 || offset == -1, "The two given minions are not adjacent")
    val id = minion.id + offset * -1
    Option.when(id >= 0 && id < team.size)(team.minions(id))
  }

  private def onThisLeavesTheBattlefield(
    battlefield: Battlefield,
    deadMinion: Minion,
    alliedTeam: Team,
  ): Battlefield = {
    val adjacentMinions = alliedTeam.lastAdjacentToDead(deadMinion)
    val adjacentMinionsUUIDs = adjacentMinions.map(_.uuid)
    battlefield.modifySomeTeamMinions(deadMinion.side) {
      case minion if adjacentMinionsUUIDs.contains(minion.uuid) => removeBuff(minion)
    }
  }

  private def onOtherAllyLeavesTheBattlefield(
    battlefield: Battlefield,
    deadMinion: Minion,
  ): Battlefield = {
    val idDistance = id - deadMinion.id
    if (idDistance <= -1) { //Dead minion was adjacent
      val isNewAdjacentMinion: Minion => Boolean = if (idDistance == 0) { //Was on the left of this
        minion: Minion => minion.id == this.id - 1
      } else if (idDistance == -1) { //Was on the right of this
        minion: Minion => minion.id == this.id + 1
      } else {
        _ => false
      }
      battlefield.modifySomeTeamMinions(deadMinion.side) {
        //Buff the minion that is now adjacent to this, if it exists.
        case minion if isNewAdjacentMinion(minion) => addBuff(minion)
      }
    } else {
      battlefield
    }
  }

  private def onThisEntersTheBattlefield(
    battlefield: Battlefield,
    alliedMinion: Minion,
    alliedTeam: Team,
  ): Battlefield = {
    val adjacentMinions = alliedTeam.adjacentMinions(alliedMinion)
    val adjacentMinionsUUIDs = adjacentMinions.map(_.uuid)
    battlefield.modifySomeTeamMinions(alliedMinion.side) {
      case minion if adjacentMinionsUUIDs.contains(minion.uuid) => addBuff(minion)
    }
  }

  private def onOtherAllyEntersTheBattlefield(
    battlefield: Battlefield,
    alliedMinion: Minion,
    alliedTeam: Team,
  ): Battlefield = {
    if (isAdjacent(alliedMinion)) {
      val previouslyAdjacentMinion = getOtherAdjacent(alliedTeam, this, alliedMinion)
      battlefield.modifySomeTeamMinions(alliedMinion.side) {
          //Buff the minion that is now adjacent to this, if it exists.
        case minion if minion.id == alliedMinion.id => addBuff(minion)
          //Remove buff from previously adjacent one.
        case minion if previouslyAdjacentMinion.exists(_.id == minion.id) => removeBuff(minion)
      }
    } else {
      battlefield
    }
  }

  /**
    * Trigger that does something after anything leaves the battlefield
    *
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(deadMinion)
    if (alliedTeam.side == side) {
      if (deadMinion.uuid == this.uuid) {
        onThisLeavesTheBattlefield(battlefield, deadMinion, alliedTeam).nel
      } else {
        onOtherAllyLeavesTheBattlefield(battlefield, deadMinion).nel
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
    if (alliedTeam.side == side) {
      if (newMinion.uuid == this.uuid) {
        onThisEntersTheBattlefield(battlefield, newMinion, alliedTeam).nel
      } else {
        onOtherAllyEntersTheBattlefield(battlefield, newMinion, alliedTeam).nel
      }
    } else {
      battlefield.nel
    }

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
