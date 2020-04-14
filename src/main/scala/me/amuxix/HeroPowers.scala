package me.amuxix

import enumeratum.{EnumEntry, Enum}

sealed trait Hero extends EnumEntry

object HeroPowers extends Enum[Hero] {
  override def values: IndexedSeq[Hero] = findValues

  case object Illidan extends Hero
  case object Deathwing extends Hero
  case object Nefarian extends Hero
}