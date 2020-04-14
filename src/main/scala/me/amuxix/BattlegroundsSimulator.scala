package me.amuxix

import cats.effect.{ContextShift, ExitCode, IO, IOApp}
import me.amuxix.Sides.{A, B}
import me.amuxix.minions.{Demon, Murloc}
import me.amuxix.minions.tier1.SelflessHero
import me.amuxix.minions.tier2.{KaboomBot, RatPack, ScavengingHyena, SpawnOfNzoth, UnstableGhoul}
import me.amuxix.minions.tier3.{BronzeWarden, InfestedWolf, Khadgar, PackLeader, SoulJuggler}
import me.amuxix.minions.tier5.{BaronRivendare, Goldrinn}
import me.amuxix.minions.traits.Taunt

object BattlegroundsSimulator extends IOApp {
  //TODO keep track of deaths to implement Kangor's Apprentice
  //Create list of possible spawns for Imp Mama, Ghastcoiler, Sneeds, etc.
  //Create triggers for minions taking damage, overkill damage, losing divine shield, and pre combat
  //Maybe implement reborn as a on death trigger?
  implicit val cs = this.contextShift

  override def run(args: List[String]): IO[ExitCode] = {

    /*    val teamA = Team(A)(
      Murloc(3, 7),
      Murloc(3, 1),
      Murloc(2, 1),
    )
    val teamB = Team(B)(
      RatPack(),
      PackLeader(),
      ScavengingHyena(),
    )*/

    /*val teamA = Team(A)(
      Murloc(10, 10),
    )
    val teamB = Team(B)(
      UnstableGhoul(1, 1),
      InfestedWolf(1, 1),
      SpawnOfNzoth(1, 1),
      Goldrinn(1, 1),
      Goldrinn(1, 1),
      SelflessHero(isGolden = true),
      //BaronRivendare(isGolden = true),
    )*/

    val teamA = Team(A)(
      BronzeWarden(4, 2),
    )
    val teamB = Team(B)(
      Murloc(3, 10),
    )

    val battlefield = Battlefield(teamA, teamB)
    prettyPrintSimulations(battlefield, toFile = true)

    val odds = calculateOdds(battlefield)
    val (teamAWins, teamBWins, draws) = odds(battlefield)
    val total = (teamAWins + teamBWins + draws).toDouble
    println(s"Team A win chance = ${formatPercent(teamAWins / total)}")
    println(s"Team B win chance = ${formatPercent(teamBWins / total)}")
    println(s"Draw chance = ${formatPercent(draws / total)}")

    IO.pure(ExitCode.Success)
  }

  def formatPercent(number: Double): String = {
    val rounded = math.round(number * 10000)
    s"${rounded / 100d}%"
  }

  implicit class Tuple3Ops(t: (Int, Int, Int)) {
    def +(t2: (Int, Int, Int)): (Int, Int, Int) = (t._1 + t2._1, t._2 + t2._2, t._3 + t2._3)
  }

  implicit class ResultOptionOps(result: Option[Side]) {

    def toOdds: (Int, Int, Int) = result.fold((0, 0, 1)) {
      case Sides.A => (1, 0, 0)
      case Sides.B => (0, 1, 0)
    }
  }

  //type Result = Either[Set[Battlefield], Option[Side]]
  //Battlefield#Simulations = Map[Battlefield, Result]
  type Odds = Map[Battlefield, (Int, Int, Int)]

  def calculateOdds(battlefield: Battlefield): Odds = {
    def extractOdds(simulations: Battlefield#Simulations, odds: Odds): Odds =
      simulations.collect {
        case (battlefield, Left(possibleBattlefields)) if possibleBattlefields.forall(odds.contains) =>
          val extractedOdds = possibleBattlefields.map(odds(_)).foldLeft((0, 0, 0))(_ + _)
          battlefield -> extractedOdds
      }

    val simulations: battlefield.Simulations = battlefield.simulations

    var odds: Odds = simulations.collect {
      case (battlefield, Right(result)) => battlefield -> (result.toOdds)
    }
    while (odds.size < simulations.size) {
      val odds1 = extractOdds(simulations, odds)
      odds ++= odds1
    }
    odds
  }

  def prettyPrintSimulations(battlefield: Battlefield, toFile: Boolean = false): Unit = {
    import java.io._
    def innerPrint(
      write: String => Unit,
      simulations: Battlefield#Simulations,
      battlefield: Battlefield,
      depth: Int = 0
    ): Unit = {
      if (depth == 0) {
        write("Starting " + battlefield.prettyPrint(depth))
      } else {
        write(battlefield.prettyPrint(depth))
      }

      simulations(battlefield) match {
        case Left(possibleOutcomes) =>
          write(PrettyPrinter.indentation(depth) + "Possible outcomes" + "\n")
          possibleOutcomes.map(innerPrint(write, simulations, _, depth + 1))
        case Right(result) =>
          write(PrettyPrinter.indentation(depth) + result.fold("Draw!")(side => s"Team $side wins!") + "\n")
      }
    }
    if (toFile) {
      val bw = new BufferedWriter(new FileWriter(new File("log.log")))
      innerPrint(bw.write, battlefield.simulations, battlefield)
      bw.close()
    } else {
      innerPrint(print, battlefield.simulations, battlefield)
    }
  }
}
