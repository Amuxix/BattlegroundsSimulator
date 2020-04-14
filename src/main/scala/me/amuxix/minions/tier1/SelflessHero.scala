package me.amuxix.minions.tier1

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named}
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Beast
import me.amuxix.minions.traits.LeavesBattlefieldTrigger

object SelflessHero extends Named

case class SelflessHero(
  damage: Int = Minion.defaultDamage(SelflessHero.name),
  hp: Int = Minion.defaultHp(SelflessHero.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(SelflessHero.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
  with LeavesBattlefieldTrigger {
  override type T = SelflessHero

  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (SelflessHero.apply _).tupled(t)

  /**
   * Trigger that does something after anything leaves the battlefield
   *
   * @param deadMinion The minion that just left the battlefield
   * @return The possible battlefields after effects have been applied
   */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(this)

    //Requirement for this trigger to activate
    def requirement(minion: Minion): Boolean = minion.hp > 0 && !minion.hasDivineShield

    if (deadMinion.uuid == this.uuid && alliedTeam.minions.exists(requirement)) {
      battlefield.modifyMultipleTeamMinions(
        deadMinion.side, deadMinion.goldenMultiplier, requirement
      )(_.copy(hasDivineShield = true))
    } else {
      battlefield.nel
    }
  }
}
