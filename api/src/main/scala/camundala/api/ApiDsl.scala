package camundala
package api

import camundala.api.ast.*
import camundala.bpmn.*
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.matching.*

trait ApiDsl extends ApiCreatorApp:

  class ApiBuilder:
    private val ib = ListBuffer.empty[GroupedApi]

    def pushApi(x: GroupedApi): Unit = ib.append(x)

    def mkBlock: ApiDoc = ApiDoc(ib.toList)

  type ApiConstr = ApiBuilder ?=> Unit

  extension (api: GroupedApi)
    private[api] def stage: ApiConstr =
      (bldr: ApiBuilder) ?=> bldr.pushApi(api)

  def document(body: ApiConstr): Unit =
    val sb = ApiBuilder()
    body(using sb)
    val apiDoc = sb.mkBlock
    println(s"APIDOC: $apiDoc")
    run(apiDoc) // runs Gatling Load Tests

  def group(name: String)(apis: GroupedApi*): ApiConstr =
    CApiGroup(name, apis.toList).stage

  def api[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](pApi: ProcessApi[In, Out]): ApiConstr =
    api(pApi)()

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
  ](inline inOut: Activity[In, Out, ?]): ActivityApi[In, Out] =
    ActivityApi(nameOfVariable(inOut), inOut)

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
    ] (inOutApi: InOutApi[In, Out])

    inline def withInExample(inline example: In): InOutApi[In, Out] =
      inOutApi.withInExample(nameOfVariable(example), example)

    inline def withOutExample(inline example: Out): InOutApi[In, Out] =
      inOutApi.withOutExample(nameOfVariable(example), example)

    def withInExample(label: String, example: In): InOutApi[In, Out] =
      inOutApi.addInExample(label, example)

    def withOutExample(label: String, example: Out): InOutApi[In, Out] =
      inOutApi.addOutExample(label, example)

end ApiDsl
