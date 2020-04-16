package me.amuxix.minions.tier2

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Mech
import me.amuxix.minions.traits.{LeavesBattlefieldTrigger, TargetedDeathrattle}
import me.amuxix.{Battlefield, Named, Team}

object KaboomBot extends Named

case class KaboomBot(
  damage: Int = Minion.defaultDamage(KaboomBot.name),
  hp: Int = Minion.defaultHp(KaboomBot.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean = Minion.defaultDivineShield.contains(KaboomBot.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with Mech
    with TargetedDeathrattle {
  override type T = KaboomBot
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (KaboomBot.apply _).tupled(t)

  private def minionRequirement(minion: Minion): Boolean = minion.hp > 0

  /**
    * Requirement for this trigger to activate
    */
  override def requirement(battlefield: Battlefield, deadMinion: Minion, alliedTeam: Team, enemyTeam: Team): Boolean =
    enemyTeam.minions.exists(minionRequirement)

  override def deathrattleTrigger(
    battlefield: Battlefield,
    deadMinion: Minion,
    alliedTeam: Team,
    enemyTeam: Team
  ): NonEmptyList[Battlefield] =
    NonEmptyList.fromListUnsafe(
      enemyTeam.minions.collect {
        //Bombs don't hit minions that have "died" from triggers in the same chain
        case targetMinion if minionRequirement(targetMinion) =>
          //println(s"Targeting ${targetMinion.prettyPrint()}")
          battlefield.modifyMinion(targetMinion)(_.takeDamageFrom(deadMinion, 4))
      }
    )
}
