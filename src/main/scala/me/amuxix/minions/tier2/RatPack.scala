package me.amuxix.minions.tier2

import java.util.UUID

import me.amuxix.Named
import me.amuxix.minions.traits.TokenSpawner
import me.amuxix.minions.{Beast, Minion, races}

object RatPack extends Named

final case class RatPack(
  damage: Int = Minion.defaultDamage(RatPack.name),
  hp: Int = Minion.defaultHp(RatPack.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean = Minion.defaultDivineShield.contains(RatPack.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with races.Beast
    with TokenSpawner {
  override type T = RatPack
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (RatPack.apply _).tupled(t)

  override def spawnedTokens: List[Minion] = List.fill(damage)(Beast(goldenMultiplier, goldenMultiplier))
}
