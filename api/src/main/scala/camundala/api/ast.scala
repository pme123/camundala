package camundala
package api

import camundala.bpmn.*
import camundala.domain.*
import sttp.tapir.EndpointIO
import sttp.tapir.json.circe.*

import scala.annotation.targetName
import scala.reflect.ClassTag
import scala.util.Random

case class ApiDoc(apis: List[CApi])

sealed trait CApi:
  def name: String

sealed trait GroupedApi extends CApi:
  def name: String
  def apis: List[_ <: CApi]
  def withApis(apis: List[_ <: CApi]): GroupedApi

sealed trait InOutApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
] extends CApi:
  def inOut: InOut[In, Out, ?]
  def apiExamples: ApiExamples[In, Out]
  lazy val inOutDescr: InOutDescr[In, Out] = inOut.inOutDescr
  lazy val id: String = inOutDescr.id
  lazy val descr: String = inOut.descr.getOrElse("")
  lazy val typeName: String = inOut.typeName

  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out]

  def addInExample(label: String, example: In): InOutApi[In, Out] =
    withExamples(
      apiExamples.copy(inputExamples = apiExamples.inputExamples :+ (label, example))
    )

  def addOutExample(label: String, example: Out): InOutApi[In, Out] =
    withExamples(
      apiExamples.copy(outputExamples = apiExamples.outputExamples :+ (label, example))
    )

  // this function needs to be here as circe does not find the Encoder in the extension method
  lazy val inMapper: EndpointIO.Body[String, In] = jsonBody[In]

  // this function needs to be here as circe does not find the Encoder in the extension method
  lazy val outMapper: EndpointIO.Body[String, Out] = jsonBody[Out]

  lazy val inJson: Option[Json] = inOut.in match
    case _: NoInput => None
    case _ => Some(inOut.in.asJson)

  lazy val outJson: Option[Json] = inOut.out match
    case _: NoInput => None
    case _ => Some(inOut.out.asJson)

  lazy val variableNamesIn: List[String] =
    inOut.in.productElementNames.toList

  lazy val variableNamesOut: List[String] =
    inOut.out.productElementNames.toList

  def apiDescription(
      diagramDownloadPath: Option[String],
      diagramNameAdjuster: Option[String => String]
  ): String =
    s"""$descr
       |
       |- Input:  `${inOut.in.getClass.getName.replace("$", " > ")}`
       |- Output: `${inOut.out.getClass.getName
        .replace("$", " > ")}`""".stripMargin

  protected def diagramName: Option[String] = None

  protected def diagramFrame(
      diagramDownloadPath: String,
      diagramNameAdjuster: Option[String => String]
  ): String =
    val postfix = if (typeName == "Process") "bpmn" else "dmn"
    val postfixUpper = postfix.head.toUpper + postfix.tail
    val pureDiagramName = diagramName.getOrElse(id)
    val name =
      diagramNameAdjuster.map(_(pureDiagramName)).getOrElse(pureDiagramName)
    val fileName = s"$name.$postfix"
    val randomPostfix = Random.nextInt(100000)
    s"""
       |<div class="diagramCanvas">
       |  <div class="diagram" id="$name-$randomPostfix">
       |    <img onLoad="openFromUrl('$fileName', new ${postfixUpper}JS({ container: $$('#$name-$randomPostfix'), height: '95%', width: '95%' }));" src="data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==" />
       |  </div>
       |</div>
       |
       |Download: [$fileName]($diagramDownloadPath/$fileName)
       |""".stripMargin
  end diagramFrame

end InOutApi

case class ProcessApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    name: String,
    inOut: Process[In, Out],
    apiExamples: ApiExamples[In, Out],
    apis: List[CApi] = List.empty,
    override val diagramName: Option[String] = None
) extends InOutApi[In, Out],
      GroupedApi:

  def withApis(apis: List[CApi]): ProcessApi[In, Out] = copy(apis = apis)
  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)

  override def apiDescription(
      diagramDownloadPath: Option[String],
      diagramNameAdjuster: Option[String => String]
  ): String =
    s"""${super.apiDescription(diagramDownloadPath, diagramNameAdjuster)}
       |
       |${inOut.in match
        case _: GenericServiceIn => "" // no diagram if generic
        case _ =>
          diagramDownloadPath
            .map(diagramFrame(_, diagramNameAdjuster))
            .getOrElse("")
      }
       |${generalVariablesDescr(inOut.out, "")}""".stripMargin

object ProcessApi:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](name: String, inOut: Process[In, Out]): ProcessApi[In, Out] =
    ProcessApi(name, inOut, ApiExamples(name, inOut))

end ProcessApi

def generalVariablesDescr[Out <: Product: Encoder](
    out: Out,
    serviceMock: String
) =
  s"""<p/>
     |
     |<details>
     |<summary>
     |<b><i>General Variable(s)</i></b>
     |</summary>
     |
     |<p>
     |
     |**outputVariables**:
     |
     |Just take the variable you need in your process!
     |```json
     |...
     |"outputVariables": "${out.productElementNames.mkString(",")}",
     |...
     |```
     |
     |**outputMock**:
     |
     |```json
     |...
     |"outputMock": ${out.asJson},
     |...
     |```
     |$serviceMock
     |</p>
     |</details>
     |</p>
      """.stripMargin
end generalVariablesDescr

sealed trait ExternalTaskApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
] extends InOutApi[In, Out]:
  def inOut: ExternalTask[In, Out, ?]

  def processName: String = inOut.processName
  lazy val topicName = inOut.topicName

  override def apiDescription(
      diagramDownloadPath: Option[String],
      diagramNameAdjuster: Option[String => String]
  ): String =
    s"""
       |**Topic:** `$topicName` (to define in the _**Topic**_ of the _**External Task**_ > _Service Task_ of type _External_)
       |
       |${super.apiDescription(diagramDownloadPath, diagramNameAdjuster)}
       |
       |You can test this worker using the generic process _**$GenericExternalTaskProcessName**_ (e.g. with Postman).
       |""".stripMargin

case class ServiceWorkerApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag,
    ServiceOut: Encoder: Decoder: Schema
](
    name: String,
    inOut: ServiceTask[In, Out, ServiceOut],
    apiExamples: ApiExamples[In, Out]
) extends ExternalTaskApi[In, Out]:

  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)

  override def apiDescription(
      diagramDownloadPath: Option[String],
      diagramNameAdjuster: Option[String => String]
  ): String =
    s"""
       |
       |${super.apiDescription(diagramDownloadPath, diagramNameAdjuster)}
       |- ServiceOut:  `$serviceOutDescr`
       |${generalVariablesDescr(
        inOut.out,
        s"""
       |**outputServiceMock**:
       |```json
       |...
       |"outputServiceMock": ${MockedServiceResponse
            .success200(inOut.defaultServiceOutMock)
            .asJson},
       |...
       |```"""
      )}
    """.stripMargin

  private def serviceOutDescr =
    inOut.defaultServiceOutMock match
      case seq: Seq[?] =>
        s"Seq[${seq.head.getClass.getName.replace("$", " > ")}]"
      case other => other.getClass.getName.replace("$", " > ")

object ServiceWorkerApi:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag,
      ServiceOut: Encoder: Decoder: Schema
  ](
      name: String,
      inOut: ServiceTask[In, Out, ServiceOut]
  ): ServiceWorkerApi[In, Out, ServiceOut] =
    ServiceWorkerApi(name, inOut, ApiExamples(name, inOut))

end ServiceWorkerApi

case class CustomWorkerApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    name: String,
    inOut: CustomTask[In, Out],
    apiExamples: ApiExamples[In, Out]
) extends ExternalTaskApi[In, Out]:

  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)

object CustomWorkerApi:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](
      name: String,
      inOut: CustomTask[In, Out]
  ): CustomWorkerApi[In, Out] =
    CustomWorkerApi(name, inOut, ApiExamples(name, inOut))

end CustomWorkerApi

case class DecisionDmnApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    name: String,
    inOut: DecisionDmn[In, Out],
    apiExamples: ApiExamples[In, Out],
    override val diagramName: Option[String] = None
) extends InOutApi[In, Out]:

  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)

  def toActivityApi: ActivityApi[In, Out] =
    ActivityApi(name, inOut)

  override def apiDescription(
      diagramDownloadPath: Option[String],
      diagramNameAdjuster: Option[String => String]
  ): String =
    s"""${super.apiDescription(diagramDownloadPath, diagramNameAdjuster)}
       |
       |${diagramDownloadPath
        .map(diagramFrame(_, diagramNameAdjuster))
        .getOrElse("")}
       |""".stripMargin

object DecisionDmnApi:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](name: String, inOut: DecisionDmn[In, Out]): DecisionDmnApi[In, Out] =
    DecisionDmnApi(name, inOut, ApiExamples(name, inOut))

end DecisionDmnApi

case class CApiGroup(
    name: String,
    apis: List[CApi]
) extends GroupedApi:

  def withApis(apis: List[CApi]): CApiGroup = copy(apis = apis)

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
    examples: Seq[InOutExample[T]]
):
  @targetName("add")
  def :+(label: String, example: T): InOutExamples[T] =
    copy(examples = examples :+ InOutExample(label, example))

  lazy val fetchExamples: Seq[InOutExample[T]] =
    examples

object InOutExamples:

  def apply[T <: Product: Encoder: Decoder: Schema](
      name: String,
      inOut: T
  ): InOutExamples[T] =
    InOutExamples(Seq(InOutExample(name, inOut)))

case class InOutExample[T <: Product: Encoder: Decoder: Schema](
    name: String,
    example: T
):
  // this function needs to be here as circe does not find the Encoder in the extension method
  def toCamunda: FormVariables = CamundaVariable.toCamunda(example)
