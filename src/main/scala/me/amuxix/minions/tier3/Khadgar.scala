package me.amuxix.minions.tier3

import java.util.UUID

import me.amuxix.Named
import me.amuxix.minions.Minion

object Khadgar extends Named

final case class Khadgar(
  damage: Int = Minion.defaultDamage(Khadgar.name),
  hp: Int = Minion.defaultHp(Khadgar.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(Khadgar.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion {

  override type T = Khadgar
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (Khadgar.apply _).tupled(t)
}
