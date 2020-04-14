package me.amuxix.minions.tier5

import java.util.UUID

import me.amuxix.Named
import me.amuxix.minions.Minion

object BaronRivendare extends Named

final case class BaronRivendare(
  damage: Int = Minion.defaultDamage(BaronRivendare.name),
  hp: Int = Minion.defaultHp(BaronRivendare.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(BaronRivendare.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion {

  override type T = BaronRivendare
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (BaronRivendare.apply _).tupled(t)
}
