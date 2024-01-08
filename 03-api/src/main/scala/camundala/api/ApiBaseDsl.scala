package camundala
package api

import domain.*
import bpmn.*

import scala.reflect.ClassTag

trait ApiBaseDsl:

  def group(name: String)(apis: CApi*): CApiGroup =
    CApiGroup(name, apis.toList)

  def api[
    In <: Product :InOutEncoder: InOutDecoder : Schema,
    Out <: Product :InOutEncoder: InOutDecoder : Schema : ClassTag,
    T <: InOutApi[In, Out]
  ](pApi: T): T =
    pApi

  def api[
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema: ClassTag
  ](pApi: ProcessApi[In, Out])(body: CApi*): ProcessApi[In, Out] =
    pApi.withApis(body.toList)

  extension[
    In <: Product :InOutEncoder: InOutDecoder : Schema,
    Out <: Product :InOutEncoder: InOutDecoder : Schema : ClassTag,
    T <: InOutApi[In, Out]
  ] (inOutApi: T)

    inline def withExample(inline example: InOut[In, Out, ?]): T =
      withExample(nameOfVariable(example), example)

    inline def withInExample(inline example: In): T =
      withInExample(nameOfVariable(example), example)

    inline def withOutExample(inline example: Out): T =
      withOutExample(nameOfVariable(example), example)

    def withExample(label: String, example: InOut[In, Out, ?]): T =
      withInExample(label, example.in)
        .withOutExample(label, example.out)

    def withInExample(label: String, example: In): T =
      inOutApi.addInExample(label, example).asInstanceOf[T]

    def withOutExample(label: String, example: Out): T =
      inOutApi.addOutExample(label, example).asInstanceOf[T]
  end extension

  extension[
    In <: Product :InOutEncoder: InOutDecoder : Schema,
    Out <: Product :InOutEncoder: InOutDecoder : Schema : ClassTag,
    T <: DecisionDmnApi[In, Out]
  ] (decApi: T)

    def withDiagramName(diagramName: String): DecisionDmnApi[In, Out] = decApi.copy(diagramName = Some(diagramName))
  end extension

  extension[
    In <: Product :InOutEncoder: InOutDecoder : Schema,
    Out <: Product :InOutEncoder: InOutDecoder : Schema : ClassTag,
    T <: ProcessApi[In, Out]
  ] (processApi: T)

    def withDiagramName(diagramName: String): ProcessApi[In, Out] = processApi.copy(diagramName = Some(diagramName))
  end extension
end ApiBaseDsl
