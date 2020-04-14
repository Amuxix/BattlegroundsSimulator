package me.amuxix.minions.tier2

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.minions.Minion
import me.amuxix.minions.traits.LeavesBattlefieldTrigger
import me.amuxix.{Battlefield, Named}

object SpawnOfNzoth extends Named {
  override lazy val name: String = "Spawn of N'Zoth"
}

case class SpawnOfNzoth(
  damage: Int = Minion.defaultDamage(SpawnOfNzoth.name),
  hp: Int = Minion.defaultHp(SpawnOfNzoth.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(SpawnOfNzoth.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with LeavesBattlefieldTrigger {

  override lazy val name: String = SpawnOfNzoth.name

  override type T = SpawnOfNzoth
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (SpawnOfNzoth.apply _).tupled(t)

  /**
    * Trigger that does something after anything leaves the battlefield
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(this)
    battlefield
      .modifyAllTeamMinionsWhen(deadMinion.uuid == this.uuid && alliedTeam.minions.nonEmpty)(side) { minion =>
        minion.copy(damage = minion.damage + deadMinion.goldenMultiplier, hp = minion.hp + deadMinion.goldenMultiplier)
      }
      .nel
  }
}
