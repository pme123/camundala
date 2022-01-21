package camundala.camunda

import scala.quoted.*

object CamundaMapperMacros   :

  def mapImpl[S, A](focus: Expr[S => A])(using Quotes, Type[S], Type[A]) =
    import quotes.reflect.*

    def unsupportedShapeInfo(tree: Tree) =
      s"Unsupported path element. Path must have shape: _.field1.field2.each.field3.(...), got: ${tree.show}"

    def methodSupported(method: String) =
      Seq("at", "each", "eachWhere", "eachRight", "eachLeft", "atOrElse", "index", "when").contains(method)

    enum PathSymbol:
      case Field(name: String)
      case FunctionDelegate(name: String, givn: Term, typeTree: TypeTree, args: List[Term])

    def toPath(tree: Tree): Seq[PathSymbol] = {
      tree match {
        /** Field access */
        case Select(deep, ident) =>
          toPath(deep) :+ PathSymbol.Field(ident)
        /** Method call with arguments and using clause */
        case Apply(Apply(Apply(TypeApply(Ident(s), typeTrees), idents), args), List(givn)) if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(s, givn, typeTrees.last, args)
        /** Method call with no arguments and using clause */
        case Apply(Apply(TypeApply(Ident(s), typeTrees), idents), List(givn)) if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(s, givn, typeTrees.last, List.empty)
        /** Method call with one type parameter and using clause */
        case a @ Apply(TypeApply(Apply(TypeApply(Ident(s), _), idents), typeTrees), List(givn)) if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(s, givn, typeTrees.last, List.empty)
        /** Field access */
        case Apply(deep, idents) =>
          toPath(deep) ++ idents.flatMap(toPath)
        /** Wild card from path */
        case i: Ident if i.name.startsWith("_") =>
          Seq.empty
        case _ =>
          report.throwError(unsupportedShapeInfo(focus.asTerm))
      }
    }

    val focusTree: Tree = focus.asTerm
    println(s"FocusTree2: ${focusTree.getClass} $focusTree")

    val path = focusTree match {
      /** Single inlined path */
      case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
        println(s"Inlined: $p")
        toPath(p)
      case _ =>
        report.throwError(unsupportedShapeInfo(focusTree))
    }

    Expr(s"FocusTree2: $focusTree \n- " + path.toString)

