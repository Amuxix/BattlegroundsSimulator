package me.amuxix.minions.tier3

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.minions.Minion
import me.amuxix.minions.traits.LeavesBattlefieldTrigger
import me.amuxix.{Battlefield, Named}

object SoulJuggler extends Named

case class SoulJuggler(
  damage: Int = Minion.defaultDamage(SoulJuggler.name),
  hp: Int = Minion.defaultHp(SoulJuggler.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(SoulJuggler.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
  with LeavesBattlefieldTrigger {
  override type T = SoulJuggler
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (SoulJuggler.apply _).tupled(t)

  /**
   * Trigger that does something after anything leaves the battlefield
   * @param deadMinion The minion that just left the battlefield
   * @return The possible battlefields after effects have been applied
   */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (_, enemyTeam) = battlefield.teams(this)

    //Requirement for this trigger to activate
    def requirement(minion: Minion): Boolean = minion.hp > 0

    if (deadMinion.side == this.side && deadMinion.isDemon && enemyTeam.minions.exists(requirement)) {
      battlefield.modifyMultipleTeamMinions(
        enemyTeam.side, this.goldenMultiplier, requirement
      )(_.takeDamageFrom(this, 3))
    } else {
      battlefield.nel
    }
  }
}
