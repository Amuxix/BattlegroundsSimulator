package me.amuxix.minions.tier3

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named}
import me.amuxix.minions.Minion
import me.amuxix.minions.races.{Beast, Mech}
import me.amuxix.minions.traits.LeavesBattlefieldTrigger

object DeflectOBot extends Named

case class DeflectOBot(
  damage: Int = Minion.defaultDamage(DeflectOBot.name),
  hp: Int = Minion.defaultHp(DeflectOBot.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(DeflectOBot.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
  with Mech
  with LeavesBattlefieldTrigger {
  override type T = DeflectOBot
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (DeflectOBot.apply _).tupled(t)

  /**
   * Trigger that does something after anything leaves the battlefield
   * @param deadMinion The minion that just left the battlefield
   * @return The possible battlefields after effects have been applied
   */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    if (deadMinion.side == this.side && deadMinion.isMech) {
      battlefield.modifyMinion(this)(minion => minion.copy(damage = minion.damage + minion.goldenMultiplier, hasDivineShield = true)).nel
    } else {
      battlefield.nel
    }
  }
}
