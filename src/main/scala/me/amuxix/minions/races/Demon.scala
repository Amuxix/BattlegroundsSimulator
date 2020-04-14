package me.amuxix.minions.races

import me.amuxix.{Race, Races}
import me.amuxix.minions.Minion

trait Demon { this: Minion =>
  override protected val race: Option[Race] = Some(Races.Demon)
}
