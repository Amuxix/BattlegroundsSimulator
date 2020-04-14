package me.amuxix

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable.IndexedSeq

sealed trait Race extends EnumEntry

object Races extends Enum[Race] {
  override def values: IndexedSeq[Race] = findValues

  case object Beast extends Race
  case object Demon extends Race
  case object Dragon extends Race
  case object Mech extends Race
  case object Murloc extends Race
}
