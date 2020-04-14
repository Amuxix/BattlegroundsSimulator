package me.amuxix.minions.traits

import me.amuxix.minions.Minion

trait WindFury { this: Minion =>
  override val hasWindFury: Boolean = true
}
