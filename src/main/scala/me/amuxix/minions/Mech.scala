package me.amuxix.minions

import java.util.UUID

final case class Mech(
  damage: Int,
  hp: Int,
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean = false,
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with races.Mech {

  override type T = Mech
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (Mech.apply _).tupled(t)
}
