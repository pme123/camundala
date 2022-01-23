package camundala.camunda

import camundala.api.CamundaVariable
import camundala.bpmn.throwErr
import io.circe.Json
import io.circe.Json.JBoolean

import scala.quoted.*

object CamundaMapperMacros:

  def toMapper(
      varName: Expr[String],
      mapType: Expr[MapType],
      path: Expr[Seq[PathEntry]]
  )(using
      Quotes
  ): Expr[PathMapper] =
    '{
      println(s"PATH: ${$path}")
      if (${ path }.isEmpty)
        throwErr("The first element must be a PathElem in a Mapper.")
      PathMapper($varName, $mapType, $path)
    }
  /*
  def mapImpl[S, A](focus: Expr[S => A])(using Quotes, Type[S], Type[A]) =
    import quotes.reflect.*
    val a: Type[A] = summon[Type[A]]
    println(s"TYPE A = ${Type.show[A]}")
    def unsupportedShapeInfo(tree: Tree) =
      s"Unsupported path element. Path must have shape: _.field1.field2.each.field3.(...), got: ${tree.show}"

    def methodSupported(method: String) =
      Seq("at", "each", "eachWhere", "eachRight", "eachLeft", "atOrElse",
        "index", "when").contains(method)

    enum PathSymbol:
      case Field(name: String)
      case FunctionDelegate(
          name: String,
          givn: Term,
          typeTree: TypeTree,
          args: List[Term]
      )

    def toPath(tree: Tree): Seq[PathSymbol] = {
      tree match {
        /** Field access */
        case Select(deep, ident) =>
          toPath(deep) :+ PathSymbol.Field(ident)
        /** Method call with arguments and using clause */
        case Apply(
              Apply(Apply(TypeApply(Ident(s), typeTrees), idents), args),
              List(givn)
            ) if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(
            s,
            givn,
            typeTrees.last,
            args
          )
        /** Method call with no arguments and using clause */
        case Apply(Apply(TypeApply(Ident(s), typeTrees), idents), List(givn))
            if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(
            s,
            givn,
            typeTrees.last,
            List.empty
          )
        /** Method call with one type parameter and using clause */
        case a @ Apply(
              TypeApply(Apply(TypeApply(Ident(s), _), idents), typeTrees),
              List(givn)
            ) if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(
            s,
            givn,
            typeTrees.last,
            List.empty
          )
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

    val path: Seq[PathSymbol] = focusTree match {
      /** Single inlined path */
      case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
        toPath(p)
      case _ =>
        report.throwError(unsupportedShapeInfo(focusTree))
    }
    val mapperEntries = path.map {
      case PathSymbol.Field(name) =>
        PathEntry.PathElem(name)
      case _: PathSymbol.FunctionDelegate =>
        PathEntry.OptionalPath
    }
    given ToExpr[PathEntry] with {
      def apply(x: PathEntry)(using Quotes): Expr[PathEntry] =
        x match {
          case PathEntry.PathElem(n) =>
            val name = Expr(n)
            '{ PathEntry.PathElem($name) }
          case _: PathEntry.OptionalPath.type =>
            '{ PathEntry.OptionalPath }
        }
    }

    /*      x match {
            case x: PathEntry.PathElem =>
              Expr{PathEntry.PathElem(x.name)}
            case _: PathEntry.OptionalPath.type =>
              Expr(PathEntry.OptionalPath)
          }*/

    println(s"toMapper: $mapperEntries")

    toMapper(
      Expr(MapType(Type.show[A]))
        Varargs(mapperEntries.map(Expr(_))),
    )
   */
  def mapImpl2[S, A, T, B](
      sourcePath: Expr[S => A],
      targetName: Expr[String]
  )(using Quotes, Type[S], Type[A]) =
    import quotes.reflect.*

    def unsupportedShapeInfo(tree: Tree) =
      s"Unsupported path element. Path must have shape: _.field1.field2.each.field3.(...), got: ${tree.show}"

    def methodSupported(method: String) =
      Seq("at", "each", "eachWhere", "eachRight", "eachLeft", "atOrElse",
        "index", "when").contains(method)

    enum PathSymbol:
      case Field(name: String)
      case FunctionDelegate(
          name: String,
          givn: Term,
          typeTree: TypeTree,
          args: List[Term]
      )

    def toPath(tree: Tree): Seq[PathSymbol] = {
      tree match {
        /** Field access */
        case Select(deep, ident) =>
          toPath(deep) :+ PathSymbol.Field(ident)
        /** Method call with arguments and using clause */
        case Apply(
              Apply(Apply(TypeApply(Ident(s), typeTrees), idents), args),
              List(givn)
            ) if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(
            s,
            givn,
            typeTrees.last,
            args
          )
        /** Method call with no arguments and using clause */
        case Apply(Apply(TypeApply(Ident(s), typeTrees), idents), List(givn))
            if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(
            s,
            givn,
            typeTrees.last,
            List.empty
          )
        /** Method call with one type parameter and using clause */
        case a @ Apply(
              TypeApply(Apply(TypeApply(Ident(s), _), idents), typeTrees),
              List(givn)
            ) if methodSupported(s) =>
          idents.flatMap(toPath) :+ PathSymbol.FunctionDelegate(
            s,
            givn,
            typeTrees.last,
            List.empty
          )
        /** Field access */
        case Apply(deep, idents) =>
          toPath(deep) ++ idents.flatMap(toPath)
        /** Wild card from path */
        case i: Ident if i.name.startsWith("_") =>
          Seq.empty
        case _ =>
          report.throwError(unsupportedShapeInfo(sourcePath.asTerm))
      }
    }

    val focusTree: Tree = sourcePath.asTerm
    println(s"FocusTree2: ${focusTree.getClass} $focusTree")

    val path: Seq[PathSymbol] = focusTree match {
      /** Single inlined path */
      case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
        toPath(p)
      case _ =>
        report.throwError(unsupportedShapeInfo(focusTree))
    }
    val mapperEntries = path.map {
      case PathSymbol.Field(name) =>
        val n = Expr(name)
        '{ PathEntry.PathElem($n) }
      case _: PathSymbol.FunctionDelegate =>
        '{ PathEntry.OptionalPath }
    }
    /*   given ToExpr[PathEntry] with {
       def apply(x: PathEntry)(using Quotes): Expr[PathEntry] =
         x match {
           case PathEntry.PathElem(n) =>
             val name = Expr(n)
             '{ PathEntry.PathElem($name) }
           case _: PathEntry.OptionalPath.type =>
             '{ PathEntry.OptionalPath }
         }
     }

         x match {
             case x: PathEntry.PathElem =>
               Expr{PathEntry.PathElem(x.name)}
             case _: PathEntry.OptionalPath.type =>
               Expr(PathEntry.OptionalPath)
           }*/

    val str = Expr(Type.show[A])
    val mapType = '{ MapType($str)}
    toMapper(
      targetName,
      mapType,
      Varargs(mapperEntries)
    )
