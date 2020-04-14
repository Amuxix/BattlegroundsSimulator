package me.amuxix

import me.amuxix.minions.Minion

trait Named {
  lazy val name: String = Named.className(getClass)
}

object Named {
  def className(clazz: Class[_]): String = clazz.getSimpleName.split("\\$").last.replaceAll("([a-z])([A-Z])", "$1 $2")
}
