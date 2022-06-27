package camundala
package api

import camundala.bpmn.*
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.EndpointIO.Example
import sttp.tapir.json.circe.*

import scala.annotation.targetName
import scala.reflect.ClassTag


case class ApiDoc(apis: List[GroupedApi])

sealed trait CApi:
  def name: String

sealed trait GroupedApi extends CApi:
  def name: String
  def apis: List[_ <: CApi]

sealed trait InOutApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
] extends CApi:
  def inOut: InOut[In, Out, ?]
  def apiExamples: ApiExamples[In, Out]
  lazy val inOutDescr: InOutDescr[In, Out] = inOut.inOutDescr
  lazy val id: String = inOutDescr.id
  lazy val descr: String = inOut.maybeDescr.getOrElse("-")
  lazy val typeName: String = inOut.typeName

  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out]

  def addInExample(label: String, example: In): InOutApi[In, Out] =
    withExamples(
      apiExamples.copy(inputExamples =
        apiExamples.inputExamples :+ (label, example)
      )
    )

  def addOutExample(label: String, example: Out): InOutApi[In, Out] =
    withExamples(
      apiExamples.copy(outputExamples =
        apiExamples.outputExamples :+ (label, example)
      )
    )

  // this function needs to be here as circe does not find the Encoder in the extension method
  lazy val inMapper: EndpointIO.Body[String, In] = jsonBody[In]

  // this function needs to be here as circe does not find the Encoder in the extension method
  lazy val outMapper: EndpointIO.Body[String, Out] = jsonBody[Out]

end InOutApi

case class ProcessApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    name: String,
    inOut: Process[In, Out],
    apiExamples: ApiExamples[In, Out],
    apis: List[CApi] = List.empty
) extends InOutApi[In, Out],
      GroupedApi:

  def withApis(apis: List[CApi]): ProcessApi[In, Out] = copy(apis = apis)
  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)

object ProcessApi:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](name: String, inOut: Process[In, Out]): ProcessApi[In, Out] =
    ProcessApi(name, inOut, ApiExamples(name, inOut))

end ProcessApi

case class CApiGroup(
    name: String,
    apis: List[GroupedApi]
) extends GroupedApi:

  def withApis(apis: List[GroupedApi]): GroupedApi = copy(apis = apis)

end CApiGroup

case class ActivityApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    name: String,
    inOut: Activity[In, Out, ?],
    apiExamples: ApiExamples[In, Out]
) extends InOutApi[In, Out]:

  def withExamples(
      examples: ApiExamples[In, Out]
  ): ActivityApi[In, Out] =
    copy(apiExamples = examples)

object ActivityApi:

  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](name: String, inOut: Activity[In, Out, ?]): ActivityApi[In, Out] =
    ActivityApi(name, inOut, ApiExamples(name, inOut))

end ActivityApi
case class ApiExamples[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    inputExamples: InOutExamples[In],
    outputExamples: InOutExamples[Out]
)

object ApiExamples:

  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](name: String, inOut: InOut[In, Out, ?]): ApiExamples[In, Out] =
    ApiExamples(InOutExamples(name, inOut.in), InOutExamples(name, inOut.out))

end ApiExamples

case class InOutExamples[T <: Product: Encoder: Decoder: Schema](
    defaultExample: InOutExample[T],
    examples: Option[Seq[InOutExample[T]]] = None
):
  @targetName("add")
  def :+(label: String, example: T): InOutExamples[T] =
    copy(examples =
      Some(examples.getOrElse(Seq.empty) :+ InOutExample(label, example))
    )

  lazy val fetchExamples: Seq[InOutExample[T]] =
    examples.getOrElse(Seq(defaultExample))

object InOutExamples:

  def apply[T <: Product: Encoder: Decoder: Schema](
      name: String,
      inOut: T
  ): InOutExamples[T] =
    InOutExamples(InOutExample(name, inOut))

case class InOutExample[T <: Product: Encoder: Decoder: Schema](
    name: String,
    example: T
):
  // this function needs to be here as circe does not find the Encoder in the extension method
  def toCamunda: FormVariables = CamundaVariable.toCamunda(example)
