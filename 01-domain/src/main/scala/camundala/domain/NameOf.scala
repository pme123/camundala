package camundala.domain

import scala.quoted.{Expr, Quotes, Type}

object NameOf:

  def nameOfVariable(x: Expr[Any])(using Quotes): Expr[String] =
    val name = x.show.split("""\.""").last
      .replace(")", "") // attributes _.name -> returns name)
    println(s"Variable name: $name")
    Expr(name)
  end nameOfVariable

  def nameOfType[A](using Type[A], Quotes): Expr[String] =
    Expr(Type.show[A])
end NameOf
