package me.amuxix

trait PrettyPrinter[F] {
  protected def print(f: F)(indentLevel: Int = 0): String

  def prettyPrint(f: F)(indentLevel: Int = 0): String = PrettyPrinter.indentation(indentLevel) + print(f)(indentLevel)
}

object PrettyPrinter {
  final def indentation(indentLevel: Int): String = "  " * indentLevel
}
