package camundala
package api

import bpmn.*
import domain.*
import sttp.apispec.openapi.circe.yaml.*
import sttp.apispec.openapi.*

import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.matching.*

trait ApiDsl:

  class ApiBuilder:
    private val ib = ListBuffer.empty[CApi]

    def pushApi(x: CApi): Unit = ib.append(x)

    def mkApiLists: List[CApi] = ib.toList
    def mkBlock: ApiDoc = ApiDoc(ib.toList)

  def group(name: String)(apis: CApi*): CApiGroup =
    CApiGroup(name, apis.toList)

  def api[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](pApi: ProcessApi[In, Out])(body: CApi*): ProcessApi[In, Out] =
    pApi.withApis(body.toList)

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
