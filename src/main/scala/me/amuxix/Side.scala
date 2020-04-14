package me.amuxix

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable.IndexedSeq

sealed trait Side extends EnumEntry {
  def opposite: Side
}

object Sides extends Enum[Side] {
  override def values: IndexedSeq[Side] = findValues

  case object A extends Side {
    override val opposite: Side = B
  }
  case object B extends Side {
    override val opposite: Side = A
  }
}
