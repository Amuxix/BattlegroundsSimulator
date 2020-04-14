package me.amuxix.minions.tier2

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.minions.Minion
import me.amuxix.minions.traits.LeavesBattlefieldTrigger
import me.amuxix.{Battlefield, Named}

object UnstableGhoul extends Named

case class UnstableGhoul(
  damage: Int = Minion.defaultDamage(UnstableGhoul.name),
  hp: Int = Minion.defaultHp(UnstableGhoul.name),
  override val hasTaunt: Boolean = true,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(UnstableGhoul.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with LeavesBattlefieldTrigger {

  override type T = UnstableGhoul
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (UnstableGhoul.apply _).tupled(t)

  /**
    * Trigger that does something after anything leaves the battlefield
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] =
    battlefield
      .modifyAllMinionsWhen(deadMinion.uuid == this.uuid)(_.takeDamageFrom(deadMinion, deadMinion.goldenMultiplier))
      .nel
}
