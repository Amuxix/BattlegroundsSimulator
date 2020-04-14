package me.amuxix.minions.tier1

import java.util.UUID

import me.amuxix.Named
import me.amuxix.minions.Minion

object RighteousProtector extends Named

final case class RighteousProtector(
  damage: Int = Minion.defaultDamage(RighteousProtector.name),
  hp: Int = Minion.defaultHp(RighteousProtector.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(RighteousProtector.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = true,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion {

  override type T = RighteousProtector
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (RighteousProtector.apply _).tupled(t)
}