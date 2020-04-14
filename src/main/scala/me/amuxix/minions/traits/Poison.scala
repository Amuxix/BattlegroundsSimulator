package me.amuxix.minions.traits

import me.amuxix.minions.Minion

trait Poison { this: Minion =>
  override val hasPoison: Boolean = true
}
