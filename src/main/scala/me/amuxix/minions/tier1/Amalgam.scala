package me.amuxix.minions.tier1

import java.util.UUID

import me.amuxix.Named
import me.amuxix.minions.Minion

object Amalgam  extends Named

final case class Amalgam(
  damage: Int = Minion.defaultDamage(Amalgam.name),
  hp: Int = Minion.defaultHp(Amalgam.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(Amalgam.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion {
  override lazy val isBeast: Boolean = true
  override lazy val isDemon: Boolean = true
  override lazy val isDragon: Boolean = true
  override lazy val isMech: Boolean = true
  override lazy val isMurloc: Boolean = true
  override type T = Amalgam
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (Amalgam.apply _).tupled(t)
}
