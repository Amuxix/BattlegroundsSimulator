package me.amuxix

trait Printer[T <: Printer[T]] { this: T =>
  def prettyPrint(indentLevel: Int = 0)(implicit prettyPrinter: PrettyPrinter[T]): String = prettyPrinter.prettyPrint(this)(indentLevel)
}
