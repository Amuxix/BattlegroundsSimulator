package me.amuxix.minions.tier5

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.{Battlefield, Named}
import me.amuxix.minions.Minion
import me.amuxix.minions.races.Beast
import me.amuxix.minions.traits.LeavesBattlefieldTrigger

object Goldrinn extends Named {
  override lazy val name: String = "Goldrinn, the Great Wolf"
}

case class Goldrinn(
  damage: Int = Minion.defaultDamage(Goldrinn.name),
  hp: Int = Minion.defaultHp(Goldrinn.name),
  override val hasTaunt: Boolean = false,
  override val hasDivineShield: Boolean =  Minion.defaultDivineShield.contains(Goldrinn.name),
  override val hasWindFury: Boolean = false,
  override val hasReborn: Boolean = false,
  override val hasPoison: Boolean = false,
  override val uuid: UUID = UUID.randomUUID(),
  override val lastAttacker: Option[Minion] = None,
  override val isGolden: Boolean = false,
) extends Minion
    with Beast
    with LeavesBattlefieldTrigger {

  override lazy val name: String = Goldrinn.name

  override type T = Goldrinn
  override def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T =
    (Goldrinn.apply _).tupled(t)

  /**
    * Trigger that does something after anything leaves the battlefield
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    lazy val (alliedTeam, _) = battlefield.teams(this)
    battlefield
      .modifyAllTeamMinionsWhen(deadMinion.uuid == this.uuid && alliedTeam.minions.exists(_.isBeast))(side) {
        case minion if minion.isBeast => minion.copy(damage = minion.damage + 4 * goldenMultiplier, hp = minion.hp + 4 * goldenMultiplier)
        case minion                   => minion
      }
      .nel
  }
}
