package me.amuxix.minions
import cats.data.NonEmptyList
import me.amuxix.minions.tier3.Khadgar
import me.amuxix.{Battlefield, Team}
import me.amuxix.minions.traits.LeavesBattlefieldTrigger

trait TokenSpawner extends LeavesBattlefieldTrigger { this: Minion =>
  def spawnedTokens: List[Minion]

  /**
    * Trigger that does something after anything leaves the battlefield
    *
    * @param deadMinion The minion that just left the battlefield
    * @return The possible battlefields after effects have been applied
    */
  override def leavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] = {
    def tokensToSpawn(team: Team): List[Minion] = List.fill(TokenSpawner.tokenMultiplier(team))(spawnedTokens).flatten
    battlefield
      .modifyTeamWhen(this.uuid == deadMinion.uuid)(deadMinion.side) { team =>
        team.insertMax(deadMinion.id, tokensToSpawn(team))
      }
      .nel
  }

  override def onLeavesBattlefieldTrigger(battlefield: Battlefield, deadMinion: Minion): NonEmptyList[Battlefield] =
    leavesBattlefieldTrigger(battlefield, deadMinion)
}

object TokenSpawner {

  def tokenMultiplier(team: Team): Int =
    LeavesBattlefieldTrigger.baronMultiplier(team) + khadgarMultiplier(team)

  def khadgarMultiplier(team: Team): Int =
    team.minions
      .collectFirst {
        case khadgar: Khadgar if khadgar.isGolden => 3
        case _: Khadgar                           => 2
      }
      .getOrElse(1)
}
