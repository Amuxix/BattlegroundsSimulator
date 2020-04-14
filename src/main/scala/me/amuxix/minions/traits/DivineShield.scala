package me.amuxix.minions.traits

import me.amuxix.minions.Minion

trait DivineShield { this: Minion =>
  override val hasDivineShield: Boolean = true
}