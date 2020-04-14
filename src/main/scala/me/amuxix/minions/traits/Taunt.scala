package me.amuxix.minions.traits

import me.amuxix.minions.Minion

trait Taunt { this: Minion =>
  override val hasTaunt: Boolean = true
}
