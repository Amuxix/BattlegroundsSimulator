package me.amuxix.minions.races

import me.amuxix.{Race, Races}
import me.amuxix.minions.Minion

trait Beast { this: Minion =>
  override protected val race: Option[Race] = Some(Races.Beast)
}
