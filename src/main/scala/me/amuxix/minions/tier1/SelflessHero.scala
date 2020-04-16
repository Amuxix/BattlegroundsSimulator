package me.amuxix.minions.tier1

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named, Team}
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Beast
import me.amuxix.minions.traits.{LeavesBattlefieldTrigger, TargetedDeathrattle}

object SelflessHero extends Named

case class SelflessHero(
  damage: Int = Minion.defaultDamage(SelflessHero.name),
  hp: Int = Minion.defaultHp(SelflessHero.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean = Minion.defaultDivineShield.contains(SelflessHero.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with TargetedDeathrattle {
  override type T = SelflessHero

  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (SelflessHero.apply _).tupled(t)

  private def minionRequirement(minion: Minion): Boolean = minion.hp > 0 && !minion.hasDivineShield

  /**
    * Requirement for this trigger to activate
    */
  override def requirement(battlefield: Battlefield, deadMinion: Minion, alliedTeam: Team, enemyTeam: Team): Boolean =
    alliedTeam.minions.exists(minionRequirement)

  override def deathrattleTrigger(
    battlefield: Battlefield,
    deadMinion: Minion,
    alliedTeam: Team,
    enemyTeam: Team
  ): NonEmptyList[Battlefield] =
    battlefield.modifyMultipleTeamMinions(
      deadMinion.side,
      deadMinion.goldenMultiplier,
      minionRequirement
    )(_.copy(hasDivineShield = true))
}
