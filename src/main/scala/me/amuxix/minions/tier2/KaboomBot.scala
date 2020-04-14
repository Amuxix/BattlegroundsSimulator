package me.amuxix.minions.tier2

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Mech
import me.amuxix.minions.traits.LeavesBattlefieldTrigger
import me.amuxix.{Battlefield, Named}

object KaboomBot extends Named

case class KaboomBot(
  damage: Int = Minion.defaultDamage(KaboomBot.name),
  hp: Int = Minion.defaultHp(KaboomBot.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(KaboomBot.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with Mech
    with LeavesBattlefieldTrigger {
  override type T = KaboomBot
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (KaboomBot.apply _).tupled(t)

  /**
    * Trigger that does something after anything leaves the battlefield
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (_, enemyTeam) = battlefield.teams(this)
    //Requirement for this trigger to activate
    def requirement(minion: Minion): Boolean = minion.hp > 0
    if (deadMinion.uuid == this.uuid && enemyTeam.minions.exists(requirement)) {
      NonEmptyList.fromListUnsafe(
        enemyTeam.minions.collect {
          //Bombs don't hit minions that have "died" from triggers in the same chain
          case targetMinion if requirement(targetMinion) =>
            //println(s"Targeting ${targetMinion.prettyPrint()}")
            battlefield.modifyMinion(targetMinion)(_.takeDamageFrom(deadMinion, 4))
        }
      )
    } else {
      battlefield.nel
    }
  }
}
