package camundala.camunda

import camundala.api.CamundaVariable
import camundala.bpmn.throwErr
import io.circe.Json
import io.circe.Json.JBoolean
import camundala.bpmn.*

import scala.quoted.*

object CamundaMapperMacros:

  def toOutMapper[
    In <: Product,
    Out <: Product,
  ](
      bpmnInOut: Expr[BpmnInOut[In,Out]],
      varName: Expr[PathEntry],
      mapType: Expr[MapType],
      path: Expr[Seq[PathEntry]],
      isOut: Expr[Boolean]
  )(using
      Quotes, Type[In], Type[Out]
  ): Expr[BpmnInOut[In,Out]] =
    '{
      if($isOut)
        ${ bpmnInOut }.withOutMapper(
          ${ pathMapper(varName, mapType, '{$path.toList}) }
        )
      else
        ${ bpmnInOut }.withInMapper(
          ${ pathMapper(varName, mapType, '{$path.toList}) }
        )
    }

  private def pathMapper(
      varName: Expr[PathEntry],
      mapType: Expr[MapType],
      path: Expr[List[PathEntry]]
  )(using
      Quotes
  ): Expr[PathMapper] =
    '{
      println(s"PATH: ${$path}")
      if (${ path }.isEmpty)
        throwErr("The first element must be a PathElem in a Mapper.")
      $varName match
        case PathEntry.PathElem(name) =>
          PathMapper(name, $mapType, $path)
        case other =>
          throwErr("Only one field is supported for the Target path")
    }

  def mapImpl[In <: Product, Out <: Product, S, A, T](
      bpmnInOut: Expr[BpmnInOut[In,Out]],
      sourcePath: Expr[S => A],
      targetField: Expr[T => A],
      isOut: Expr[Boolean]
  )(using Quotes, Type[In], Type[Out], Type[S], Type[A], Type[T]) =
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

    def toPath(tree: Tree): List[PathSymbol] = {
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
          List.empty
        case _ =>
          report.throwError(unsupportedShapeInfo(tree))
      }
    }

    def path(focusTree: Tree): List[Expr[PathEntry]] =
      println(s"FOCUSTREE: ${focusTree}")
      val path = focusTree match
      /** Single inlined path */
      case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
        toPath(p)
      case _ =>
        report.throwError(unsupportedShapeInfo(focusTree))

      path.map {
        case PathSymbol.Field(name) =>
          val n = Expr(name)
          '{ PathEntry.PathElem($n) }
        case _: PathSymbol.FunctionDelegate =>
          '{ PathEntry.OptionalPath }
      }

    path(sourcePath.asTerm)
    val str = Expr(Type.show[A])
    val mapType = '{ MapType($str) }
    toOutMapper(
      bpmnInOut,
      path(targetField.asTerm).head,
      mapType,
      Varargs(path(sourcePath.asTerm)),
      isOut
    )
