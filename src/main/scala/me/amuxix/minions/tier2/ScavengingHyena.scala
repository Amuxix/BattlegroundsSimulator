package me.amuxix.minions.tier2

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named}
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Beast
import me.amuxix.minions.traits.LeavesBattlefieldTrigger

object ScavengingHyena extends Named

case class ScavengingHyena(
  damage: Int = Minion.defaultDamage(ScavengingHyena.name),
  hp: Int = Minion.defaultHp(ScavengingHyena.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(ScavengingHyena.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with Beast
    with LeavesBattlefieldTrigger {

  override type T = ScavengingHyena
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (ScavengingHyena.apply _).tupled(t)

  /**
    * Trigger that does something after anything leaves the battlefield
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] =
    battlefield
      .modifyMinionWhen(deadMinion.uuid != this.uuid && deadMinion.side == this.side && deadMinion.isBeast)(this)(
        _.copy(damage = damage + 2 * goldenMultiplier, hp = hp + goldenMultiplier)
      )
      .nel
}
