package me.amuxix

import java.util.UUID

import cats.data
import cats.data.NonEmptyList
import cats.effect.IO
import me.amuxix.Sides.{A, B}
import me.amuxix.minions.Minion
import me.amuxix.minions.traits.{EntersBattlefieldTrigger, LeavesBattlefieldTrigger}

object Battlefield {
  implicit def prettyPrinter(implicit teamPrinter: PrettyPrinter[Team]) =
    new PrettyPrinter[Battlefield] {
      override protected def print(f: Battlefield)(indentLevel: Int): String =
        s"""Battlefield (Last attack ${f.lastAttackingSide.fold("Nobody")(_.entryName)})
           |${f.teamA.prettyPrint(indentLevel + 1)}\n${f.teamB.prettyPrint(indentLevel + 1)}
           |""".stripMargin
    }

  def unordered(team1: Team, team2: Team, lastAttackingSide: Option[Side] = None): Battlefield = {
    assert(team1.side != team2.side, "Teams must have different sides")
    if (team1.side == A) {
      Battlefield(team1, team2, lastAttackingSide)
    } else {
      Battlefield(team2, team1, lastAttackingSide)
    }
  }
}

case class Battlefield(teamA: Team, teamB: Team, lastAttackingSide: Option[Side] = None) extends Printer[Battlefield] {
  assert(teamA.side == A && teamB.side == B)
  type Result = Either[NonEmptyList[Battlefield], Option[Side]]
  type Simulations = Map[Battlefield, Result]

  private val allMinions: List[Minion] = teamA.minions ++ teamB.minions

  /**
    * @return A tuple with the team of the given minion and the opposing team.
    */
  def teams(minion: Minion): (Team, Team) =
    if (minion.side == A) (teamA, teamB) else (teamB, teamA)

  //def team(side: Side): Team = if (side == A) teamA else teamB

  def modifyTeam(side: Side)(f: Team => Team): Battlefield =
    if (side == A) {
      copy(teamA = f(teamA))
    } else {
      copy(teamB = f(teamB))
    }

  private def when(condition: Boolean)(f: => Battlefield): Battlefield =
    if (condition) f else this

  def modifyTeamWhen(condition: Boolean)(side: Side)(f: Team => Team): Battlefield =
    when(condition)(modifyTeam(side)(f))

  def modifyMinion(minion: Minion)(f: Minion => Minion): Battlefield =
    modifyTeam(minion.side)(_.modifyMinion(minion)(f))

  def modifyMinionWhen(condition: Boolean)(minion: Minion)(f: Minion => Minion): Battlefield =
    when(condition)(modifyMinion(minion)(f))

  def modifyMultipleTeamMinions(
    side: Side,
    amount: Int,
    filter: Minion => Boolean
  )(
    f: Minion => Minion
  ): NonEmptyList[Battlefield] = {
    if (side == A) {
      teamA.modifyMultipleMinions(amount, filter)(f).map { team =>
        copy(teamA = team)
      }
    } else {
      teamB.modifyMultipleMinions(amount, filter)(f).map { team =>
        copy(teamB = team)
      }
    }
  }

  def modifyAllTeamMinions(side: Side)(f: Minion => Minion): Battlefield =
    modifyTeam(side)(_.modifyAllMinions(f))

  def modifyAllTeamMinionsWhen(condition: Boolean)(side: Side)(f: Minion => Minion): Battlefield =
    when(condition)(modifyAllTeamMinions(side)(f))

  def modifyAllMinions(f: Minion => Minion): Battlefield =
    copy(teamA = teamA.modifyAllMinions(f), teamB = teamB.modifyAllMinions(f))

  def modifyAllMinionsWhen(condition: Boolean)(f: Minion => Minion): Battlefield =
    when(condition)(modifyAllMinions(f))

  private lazy val isSettled: Boolean =
    teamA.size == 0 || teamB.size == 0 || allMinions.forall(_.damage <= 0)

  private lazy val winner: Option[Side] = {
    assert(isSettled, "Battlefield is not settled!")
    Option.when(allMinions.exists(_.damage > 0)) {
      //When a minion with damage remains, one of the teams won, otherwise it's a draw
      if (teamA.size > teamB.size) teamA.side else teamB.side
    }
  }

  private lazy val firstDamage: NonEmptyList[Battlefield] =
    teamA.size match {
      case teamB.size                    => teamA.attack(this) ::: teamB.attack(this)
      case larger if larger > teamB.size => teamA.attack(this)
      case _                             => teamB.attack(this)
    }

  private lazy val dealDamage: NonEmptyList[Battlefield] =
    lastAttackingSide.fold(firstDamage) {
      case A => teamB.attack(this)
      case B => teamA.attack(this)
    }

  private lazy val hasDead: Boolean = teamA.hasDead || teamB.hasDead

  private lazy val withoutDead: Battlefield = copy(teamA.withoutDead, teamB.withoutDead)

  private lazy val reviveMinionsWithReborn: Battlefield =
    copy(teamA.reviveMinionsWithReborn, teamB.reviveMinionsWithReborn)

  private lazy val handleTriggers: NonEmptyList[Battlefield] = {
    val deadMinions = teamA.deadMinions ++ teamB.deadMinions
    val battlefield = withoutDead
    val aliveMinions = battlefield.teamA.minions ++ battlefield.teamB.minions
    lazy val leavesBattlefieldTriggerMinions: List[Minion with LeavesBattlefieldTrigger] =
      (deadMinions ++ aliveMinions).collect {
        case minion: LeavesBattlefieldTrigger => minion
      }
    //println(s"Dead Minions ${deadMinions.map(_.prettyPrint()).mkString(", ")}")
    //println(s"Minions with triggers ${leavesBattlefieldTriggerMinions.map(_.prettyPrint()).mkString(", ")}")
    //TODO figgure order of triggers, maybe its random?
    deadMinions
      .foldLeft(battlefield.nel) {
        case (battlefields, deadMinion) =>
          leavesBattlefieldTriggerMinions.foldLeft(battlefields) {
            case (battlefields, triggerMinion) =>
              battlefields.flatMap { battlefield =>
                triggerMinion.onLeavesBattlefieldTrigger(battlefield, deadMinion)
              }
          }
      }
      .flatMap { battlefield =>
        val battlefieldMinions = battlefield.teamA.minions ++ battlefield.teamB.minions
        val entersBattlefieldTriggerMinions: List[Minion with EntersBattlefieldTrigger] =
          battlefieldMinions.collect {
            case minion: EntersBattlefieldTrigger => minion
          }

        val newTeamAMinions = teamA.newMinions(battlefield.teamA)
        val newTeamBMinions = teamB.newMinions(battlefield.teamB)
        val newMinions = newTeamAMinions ++ newTeamBMinions
        newMinions.foldLeft(battlefield.nel) {
          case (battlefields, newMinion) =>
            entersBattlefieldTriggerMinions.foldLeft(battlefields) {
              case (battlefields, triggerMinion) =>
                battlefields.flatMap { battlefield =>
                  triggerMinion.onEntersBattlefieldTrigger(battlefield, newMinion)
                }
            }
        }
      }
  }

  private lazy val handleAllTriggers: NonEmptyList[Battlefield] = {
    var withTriggers: NonEmptyList[Battlefield] = NonEmptyList.one(this)
    while (withTriggers.exists(_.hasDead)) {
      //println("Running next trigger chain")
      withTriggers = withTriggers.flatMap {
        case battlefield if battlefield.hasDead =>
          //println(battlefield.prettyPrint())
          val bfs = battlefield.handleTriggers
          //println("After trigger chain:")
          //println(bfs.map(_.prettyPrint(1)).toList.mkString("\n"))
          //println("end")
          bfs
        case battlefield => battlefield.nel
      }
    }
    withTriggers
  }

  lazy val battle: Result = Either.cond(
    isSettled,
    winner,
    dealDamage.map(_.reviveMinionsWithReborn).flatMap(_.handleAllTriggers)
  )

  private def simulate(previousSimulations: Simulations = Map.empty): Simulations = {
    val simulations: Simulations = previousSimulations + (this -> battle)
    battle match {
      case Left(possibleBattlefieldStates) =>
        possibleBattlefieldStates.foldLeft(simulations) {
          case (acc, battlefield) if acc.contains(battlefield) => acc
          case (acc, battlefield)                              => battlefield.simulate(acc)
        }
      case Right(_) => simulations
    }
  }

  lazy val simulations: Simulations = simulate()

  def nel: NonEmptyList[Battlefield] = NonEmptyList.one(this)
}
