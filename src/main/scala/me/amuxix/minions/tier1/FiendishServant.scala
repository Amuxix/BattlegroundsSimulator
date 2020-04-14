package me.amuxix.minions.tier1

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named}
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Demon
import me.amuxix.minions.traits.LeavesBattlefieldTrigger

object FiendishServant extends Named

final case class FiendishServant(
  damage: Int = Minion.defaultDamage(FiendishServant.name),
  hp: Int = Minion.defaultHp(FiendishServant.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean = Minion.defaultDivineShield.contains(FiendishServant.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion with Demon with LeavesBattlefieldTrigger {

  override type T = FiendishServant

  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (FiendishServant.apply _).tupled(t)

  /**
   * Trigger that does something after anything leaves the battlefield
   *
   * @param deadMinion The minion that just left the battlefield
   * @return The possible battlefields after effects have been applied
   */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(deadMinion)

    //Requirement for this trigger to activate
    def requirement(minion: Minion): Boolean = minion.hp > 0

    if (deadMinion.uuid == this.uuid && alliedTeam.minions.exists(requirement)) {
      battlefield.modifyMultipleTeamMinions(
        deadMinion.side, deadMinion.goldenMultiplier, requirement
      )(minion => minion.copy(damage = minion.damage + deadMinion.damage))
    } else {
      battlefield.nel
    }
  }
}
