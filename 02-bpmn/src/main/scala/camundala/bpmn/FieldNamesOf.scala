package camundala.bpmn

import scala.quoted.*

object FieldNamesOf:

  def allFieldNames[T: Type](using Quotes): Expr[Seq[String]] =
    import quotes.reflect.*
    val tpe    = TypeRepr.of[T]
    val fields =
      if tpe.typeSymbol.children.isEmpty then
        tpe.typeSymbol.primaryConstructor.paramSymss.flatten.map(_.name)
      else
        tpe.typeSymbol.children.flatMap: child =>
          child.primaryConstructor.paramSymss.flatten.map(_.name)

    Expr(fields)
  end allFieldNames
end FieldNamesOf
