package me.amuxix

import java.util.UUID

import cats.data.NonEmptyList
import me.amuxix.minions.Minion

object Team {
  implicit def prettyPrinter(implicit minionPrinter: PrettyPrinter[Minion]) =
    new PrettyPrinter[Team] {
      override protected def print(team: Team)(indentLevel: Int): String = {
        val heroPower = team.hero.fold("")(hero => s" of $hero")
        val minions = if (team.isDead) {
          " - Defeated!"
        } else {
          s" (${team.minions.size})" + team.minions
            .map(minion => minion.prettyPrint(indentLevel + 1))
            .mkString("\n", "\n", "")
        }
        val nextAttackingMinion = team.nextAttackingMinion.fold("")(_.prettyPrint())
        s"Team ${team.side}$heroPower ${team.minionsThatAttacked.map(_.toString.take(8))} $nextAttackingMinion$minions"
      }
    }

  def apply(side: Side, hero: Option[Hero] = None)(minions: Minion*): Team = {
    val minionsWithSideAndID = minions.zipWithIndex.map {
      case (minion, id) => minion.setSideAndId(side = side, id = id)
    }.toList
    new Team(side, hero, minionsWithSideAndID)
  }

  def apply(side: Side, hero: Option[Hero], minions: List[Minion], minionsThatAttacked: Set[UUID]): Team = {
    val minionsWithSideAndID = minions.zipWithIndex.map {
      case (minion, id) => minion.setSideAndId(side = side, id = id)
    }
    new Team(side, hero, minionsWithSideAndID, minionsThatAttacked)
  }
}

class Team private(
  val side: Side,
  val hero: Option[Hero],
  val minions: List[Minion],
  val minionsThatAttacked: Set[UUID] = Set.empty
) extends Printer[Team] {
  lazy val deadMinions: List[Minion] = minions.filter(_.isDead)

  lazy val hasDead: Boolean = deadMinions.nonEmpty

  lazy val size: Int = minions.size

  lazy val isDead: Boolean = minions.isEmpty

  lazy val taunts: List[Minion] = minions.filter(_.hasTaunt)
  //.getOrElse(throw new Exception(s"Trying to attack but no minions left! Team $side"))

  lazy val hasTaunts: Boolean = taunts.nonEmpty

  def adjacentMinions(minion: Minion): List[Minion] = {
    val index = minions.indexOf(minion)
    List(index - 1, index, index + 1).collect {
      case x if x >= 0 || x <= 7 => minions(x)
    }
  }

  def lastAdjacentToDead(deadMinion: Minion): List[Minion] = {
    assert(deadMinion.isDead)
    minions.slice(math.min(deadMinion.id - 1, 0), math.min(deadMinion.id + 1, 7))
  }

  //def areAdjacent(minion1: Minion, minion2: Minion): Boolean = math.abs(minion1.id - minion2.id) <= 1

  def modifyAllMinions(f: Minion => Minion): Team =
    withMinions(minions.map(f))

  /**
   * Applies the function f to the minion on this team that shares a uuid of the given minion
   *
   * @param m Minion whos uuid we will look for
   * @param f function that modifies a minion
   * @return The team with the now modified minion
   */
  def modifyMinion(m: Minion)(f: Minion => Minion): Team =
    withMinions(
      minions.map {
        case minion if minion.uuid == m.uuid => f(minion)
        case minion                          => minion
      }
    )

  def modifyMultipleMinions(amount: Int, filter: Minion => Boolean)(f: Minion => Minion): NonEmptyList[Team] = {
    val possibleMinions = minions.collect {
      case minion if filter(minion) => minion.uuid
    }
    if (possibleMinions.nonEmpty) {
      NonEmptyList.fromListUnsafe(
        if (amount == 1) {
          minions.collect {
            case minion if possibleMinions.contains(minion.uuid) => modifyMinion(minion)(f)
          }
        } else {
          possibleMinions.combinations(amount).map { minionsToModify =>
            withMinions {
              minions.map {
                case minion if minionsToModify.contains(minion.uuid) => f(minion)
                case minion                                          => minion
              }
            }
          }.toList
        }
      )
    } else {
      NonEmptyList.one(this)
    }
  }

  lazy val nextAttackingMinion: Option[Minion] =
    minions.filterNot(minion => minionsThatAttacked.contains(minion.uuid)).headOption.orElse(minions.headOption)

  private def withUpdatedMinions(minions: List[Minion], minionsThatAttacked: Set[UUID]): Team = {
    val updatedMinionsThatAttacked = if (minionsThatAttacked.size == minions.size) {
      //All minions in this team have attacked, reset the minionsThatAttacked set
      Set.empty[UUID]
    } else {
      minionsThatAttacked
    }
    Team(side, hero, minions, updatedMinionsThatAttacked)
  }

  def addAttackingMinion(minion: Minion): Team = {
    val updatedAttackers = minionsThatAttacked + minion.uuid
    withUpdatedMinions(minions, updatedAttackers)
    /*val finalAttackers = if ((minions.map(_.uuid).toSet -- updatedAttackers).isEmpty) {
      //All minions have already attacked.
      Set.empty[UUID]
    } else {
      updatedAttackers
    }
    new Team(side, hero, minions, finalAttackers)*/
  }

  def withMinions(minions: List[Minion]): Team = {
    val minionIds = minions.map(_.uuid).toSet
    val attackers = minionsThatAttacked.intersect(minionIds)
    withUpdatedMinions(minions, attackers)
    /*val updatedMinionsThatAttacked = if (attackers.size == minionIds.size) {
      //All minions in this team have attacked, reset the minionsThatAttacked set
      Set.empty[UUID]
    } else {
      attackers
    }
    Team(side, hero, minions, updatedMinionsThatAttacked)*/
  }

  lazy val withoutDead: Team = withMinions(minions.filterNot(_.isDead))

  lazy val reviveMinionsWithReborn: Team =
    withMinions(minions.map {
      case minion if minion.hp <= 0 && minion.hasReborn => minion.copy(
        hp = 1,
        damage = Minion.defaultDamage(minion.name) * minion.goldenMultiplier,
        hasDivineShield = Minion.defaultDivineShield.contains(minion.name),
        hasReborn = false
      )
      case minion                                       => minion
    }
    )

  lazy val possibleTargets: NonEmptyList[Minion] = NonEmptyList.fromListUnsafe(if (hasTaunts) taunts else minions)

  /**
   * Calculates the possible outcomes from this team attacking the opposing team on the battlefield
   *
   * @return List of possible battlefields states after the damage is dealt but before triggers
   */
  def attack(battlefield: Battlefield): NonEmptyList[Battlefield] = {
    val attackingMinion = nextAttackingMinion.getOrElse(
      throw new Exception(s"Trying to attack but no minions left! Team $side\n${battlefield.prettyPrint()}")
    )
    val battlefieldWithAttacker = battlefield.modifyTeam(side)(_.addAttackingMinion(attackingMinion))
    attackingMinion.attack(battlefieldWithAttacker)
  }

  /**
   * Inserts the maximum amount of minions from the given list into this team,
   * may not insert all minions if team becomes full.
   *
   * @param id Where in the team should minions be inserted
   */
  def insertMax(id: Int, minions: List[Minion]): Team = {
    val (front, back) = this.minions.splitAt(id)
    withMinions(front ++ minions.take(7 - size) ++ back)
  }

  /**
   * This assumes the given team contains all minions of this team, if this is not true,
   * minions in this team that are not in the given team will not be in the returned list.
   *
   * @return A list of new minions from the given team that don't exist in this team.
   */
  def newMinions(team: Team): List[Minion] = {
    val minionsMap = team.minions.map(a => (a.uuid, a)).toMap
    val keys = minionsMap.keySet -- minions.map(_.uuid)
    keys.toList.map(minionsMap)
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Team]

  override def equals(other: Any): Boolean = other match {
    case that: Team =>
      (that canEqual this) &&
        side == that.side &&
        hero == that.hero &&
        minions == that.minions &&
        minionsThatAttacked == that.minionsThatAttacked
    case _          => false
  }

  override def hashCode(): Int = {
    val state: Seq[Any] = Seq(side, hero, minions, minionsThatAttacked)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Team($side, $hero, $minions)"
}
