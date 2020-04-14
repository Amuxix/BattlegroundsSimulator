package me.amuxix.minions.tier3

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named}
import me.amuxix.minions.Minion
import me.amuxix.minions.traits.EntersBattlefieldTrigger

object PackLeader extends Named

final case class PackLeader(
  damage: Int = Minion.defaultDamage(PackLeader.name),
  hp: Int = Minion.defaultHp(PackLeader.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(PackLeader.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with EntersBattlefieldTrigger {

  override type T = PackLeader
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (PackLeader.apply _).tupled(t)

  /**
    * Trigger that does something when anything enters the battlefield
    *
    * @param newMinion The minion that just entered the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def onEntersBattlefieldTrigger(battlefield: Battlefield, newMinion: Minion): NonEmptyList[Battlefield] =
    battlefield
      .modifyTeamWhen(newMinion.side == side && newMinion.isBeast)(side)(
        _.modifyMinion(newMinion)(_.copy(damage = newMinion.damage + 3 * goldenMultiplier))
      )
      .nel
}
