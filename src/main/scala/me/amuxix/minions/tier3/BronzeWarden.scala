package me.amuxix.minions.tier3

import java.util.UUID

import me.amuxix.Named
import me.amuxix.minions.{Minion, races}

object BronzeWarden extends Named

final case class BronzeWarden(
  damage: Int = Minion.defaultDamage(BronzeWarden.name),
  hp: Int = Minion.defaultHp(BronzeWarden.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(BronzeWarden.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = true,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
  with races.Dragon {

  override type T = BronzeWarden
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (BronzeWarden.apply _).tupled(t)
}
