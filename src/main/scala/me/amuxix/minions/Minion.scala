package me.amuxix.minions

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix._
import me.amuxix.minions.tier1._
import me.amuxix.minions.tier2._
import me.amuxix.minions.tier3._
import me.amuxix.minions.tier5._

import scala.collection.immutable.HashMap

object Minion {
  implicit val prettyPrinter = new PrettyPrinter[Minion] {
    private def properties(minion: Minion): String = {
      val taunt = Option.when(minion.hasTaunt)("Taunt")
      val poison = Option.when(minion.hasPoison)("Poison")
      val divineShield = Option.when(minion.hasDivineShield)("Divine Shield")
      val windFury = Option.when(minion.hasWindFury)("Windfury")
      val reborn = Option.when(minion.hasReborn)("Reborn")
      val properties = List(taunt, poison, divineShield, windFury, reborn).flatten
      if (properties.isEmpty) "" else properties.mkString(" with ", ", ", "")
    }

    override protected def print(minion: Minion)(indentLevel: Int): String = {
      val minionName = if (minion.name.isBlank) minion.race.fold("Minion")(_.entryName) else minion.name
      val golden = if (minion.isGolden) "Golden " else ""
      s"$golden$minionName(${minion.damage}, ${minion.hp})${properties(minion)} (${minion.uuid.toString.take(6)})"
    }
  }

  val defaultStats: Map[String, (Int, Int)] = HashMap(
    Amalgam.name -> (1, 1),
    SelflessHero.name -> (2, 1),
    KaboomBot.name -> (2, 2),
    RatPack.name -> (2, 2),
    ScavengingHyena.name -> (2, 2),
    SpawnOfNzoth.name -> (2, 2),
    UnstableGhoul.name -> (1, 3),
    BronzeWarden.name -> (2, 1),
    InfestedWolf.name -> (3, 3),
    Khadgar.name -> (2, 2),
    PackLeader.name -> (3, 3),
    SoulJuggler.name -> (3, 3),
    BaronRivendare.name -> (1, 7),
    Goldrinn.name -> (4, 4),
    RighteousProtector.name -> (1, 1),
    FiendishServant.name -> (2, 1),
  )

  val (defaultDamage: Map[String, Int], defaultHp: Map[String, Int]) = {
    val (damageIter, hpIter) = defaultStats.map {
      case (name, (damage, hp)) => (name -> damage, name -> hp)
    }.unzip
    (damageIter.toMap.withDefault(_ => 1), hpIter.toMap.withDefault(_ => 1))
  }

  lazy val defaultDivineShield: Set[String] = Set(
    BronzeWarden.name,
    RighteousProtector.name,
  )
}

abstract class Minion extends Printer[Minion] with Named {
  val damage: Int
  val hp: Int
  val hasTaunt: Boolean
  val hasDivineShield: Boolean
  val hasWindFury: Boolean
  val hasReborn: Boolean
  val hasPoison: Boolean
  protected val race: Option[Race] = None
  var side: Side = _
  var id: Int = -1
  val uuid: UUID
  val lastAttacker: Option[Minion]
  val isGolden: Boolean

  type T <: Minion

  def create(t: (Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean, UUID, Option[Minion], Boolean)): T

  final def copy(
    damage: Int = damage,
    hp: Int = hp,
    hasDivineShield: Boolean = hasDivineShield,
    hasReborn: Boolean = hasReborn,
    lastAttacker: Option[Minion] = None,
  ): T =
    create((damage, hp, hasTaunt, hasDivineShield, hasWindFury, hasReborn, hasPoison, uuid, lastAttacker, isGolden))

  lazy val isBeast: Boolean = race.contains(Races.Beast)
  lazy val isDemon: Boolean = race.contains(Races.Demon)
  lazy val isDragon: Boolean = race.contains(Races.Dragon)
  lazy val isMech: Boolean = race.contains(Races.Mech)
  lazy val isMurloc: Boolean = race.contains(Races.Murloc)

  lazy val goldenMultiplier: Int = if (isGolden) 2 else 1

  def setSideAndId(side: Side, id: Int): Minion = {
    this.side = side
    this.id = id
    this
  }

  val isDead: Boolean = hp <= 0 && !hasReborn

  def takeDamageFrom(from: Minion, amount: Int, lethal: Boolean = false): Minion =
    if (amount > 0) {
      if (hasDivineShield) {
        copy(hasDivineShield = false)
      } else {
        val finalHP = if (lethal) 0 else hp - amount
        copy(hp = finalHP, lastAttacker = Some(from))
      }
    } else {
      this
    }

  def takeDamageCombatFrom(minion: Minion): Minion =
    takeDamageFrom(minion, minion.damage, minion.hasPoison)

  /**
    * Calculates the possible outcomes from this minion attacking the opposing team on the battlefield
    * @return List of possible battlefields states after the damage is dealt but before triggers
    */
  def attack(battlefield: Battlefield): NonEmptyList[Battlefield] = {
    val (alliedTeam, enemyTeam) = battlefield.teams(this)
    enemyTeam.possibleTargets.map { defendingMinion =>
      //print(battlefield.prettyPrint())
      //println(s"-> ${this.prettyPrint()} vs ${defendingMinion.prettyPrint()}")

      val damagedEnemyTeam = enemyTeam.modifyMinion(defendingMinion)(_.takeDamageCombatFrom(this))
      val damagedAlliedTeam = alliedTeam.modifyMinion(this)(_.takeDamageCombatFrom(defendingMinion))

      Battlefield.unordered(damagedEnemyTeam, damagedAlliedTeam, Some(alliedTeam.side))
    }
  }

  def isAdjacent(minion: Minion): Boolean = math.abs(this.id - minion.id) == 1

  //This is implemented by classes that extend Minion since they are case classes
  def canEqual(other: Any): Boolean

  override def equals(other: Any): Boolean = other match {
    case that: Minion =>
      (that canEqual this) &&
        damage == that.damage &&
        hp == that.hp &&
        hasTaunt == that.hasTaunt &&
        hasDivineShield == that.hasDivineShield &&
        hasWindFury == that.hasWindFury &&
        hasReborn == that.hasReborn &&
        hasPoison == that.hasPoison &&
        lastAttacker == that.lastAttacker &&
        isGolden == that.isGolden
    case _ => false
  }

  override def hashCode(): Int = {
    val state: Seq[Any] =
      Seq(damage, hp, hasTaunt, hasDivineShield, hasWindFury, hasReborn, hasPoison, lastAttacker, isGolden)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
