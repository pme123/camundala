package camundala
package api

import bpmn.*
import domain.*
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.matching.*

trait ApiDsl:

  class ApiBuilder:
    private val ib = ListBuffer.empty[GroupedApi]

    def pushApi(x: GroupedApi): Unit = ib.append(x)

    def mkApiLists: List[GroupedApi] = ib.toList
    def mkBlock: ApiDoc = ApiDoc(ib.toList)

  type ApiConstr = ApiBuilder ?=> Unit

  extension (api: GroupedApi)
    private[api] def stage: ApiConstr =
      (bldr: ApiBuilder) ?=> bldr.pushApi(api)

  def group(name: String)(body: ApiConstr): ApiConstr =
      val sb = ApiBuilder()
      body(using sb)
      val apis = sb.mkApiLists
      CApiGroup(name, apis).stage

  def api[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](pApi: ProcessApi[In, Out]): ApiConstr =
    api(pApi)()

  def api[
    In <: Product : Encoder : Decoder : Schema,
    Out <: Product : Encoder : Decoder : Schema : ClassTag
  ](pApi: DecisionDmnApi[In, Out]): ApiConstr =
    pApi.stage

  def api[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](pApi: ProcessApi[In, Out])(body: CApi*): ApiConstr =
    pApi.withApis(body.toList).stage

  implicit inline def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](inline process: Process[In, Out]): ProcessApi[In, Out] =
    ProcessApi(nameOfVariable(process), process)

  implicit inline def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](inline dmn: DecisionDmn[In, Out]): DecisionDmnApi[In, Out] =
    DecisionDmnApi(nameOfVariable(dmn), dmn)

  implicit inline def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](inline inOut: Activity[In, Out, ?]): ActivityApi[In, Out] =
    ActivityApi(nameOfVariable(inOut), inOut)

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag,
      T <: InOutApi[In, Out]
  ](inOutApi: T)

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

end ApiDsl
