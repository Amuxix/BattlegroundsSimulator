package me.amuxix.minions.tier3

import java.util.UUID

import me.amuxix.Named
import me.amuxix.minions.{Beast, Minion, TokenSpawner, races}

object InfestedWolf extends Named

final case class InfestedWolf(
  damage: Int = Minion.defaultDamage(InfestedWolf.name),
  hp: Int = Minion.defaultHp(InfestedWolf.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(InfestedWolf.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with races.Beast
    with TokenSpawner {
  override type T = InfestedWolf
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (InfestedWolf.apply _).tupled(t)

  override def spawnedTokens: List[Minion] = List.fill(2)(Beast(goldenMultiplier, goldenMultiplier))
}
