package camundala
package gatling

import scala.quoted.{Expr, Quotes}

object NameFromVariable:

  def nameOfVariable(x: Expr[Any])(using Quotes): Expr[String] =
    val name = x.show.split("""\.""").last
    println(s"Variable name: $name")
    Expr(name)
