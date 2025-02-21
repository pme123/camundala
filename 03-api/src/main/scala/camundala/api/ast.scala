package camundala
package api

import camundala.bpmn.*
import camundala.domain.*
import sttp.tapir.EndpointIO
import sttp.tapir.json.circe.*

import scala.annotation.targetName
import scala.reflect.ClassTag
import scala.util.Random

case class ApiDoc(apis: List[CApi]):
  lazy val groupTags: List[ApiTag] = apis.flatMap(_.groupTag)
end ApiDoc

case class ApiTag(name: String, description: String, `x-displayName`: String)
object ApiTag:
  given InOutCodec[ApiTag] = deriveInOutCodec
//  given ApiSchema[ApiTag] = deriveApiSchema // problem with magnolia

sealed trait CApi:
  def name: String
  def groupTag: Option[ApiTag] = None

sealed trait GroupedApi extends CApi:
  def name: String
  def apis: List[? <: InOutApi[?, ?]]
  def withApis(apis: List[? <: InOutApi[?, ?]]): GroupedApi
end GroupedApi

sealed trait InOutApi[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
] extends CApi:
  def inOut: InOut[In, Out, ?]
  def apiExamples: ApiExamples[In, Out]
  lazy val inOutDescr: InOutDescr[In, Out] = inOut.inOutDescr
  lazy val id: String                      = inOutDescr.id
  lazy val descr: String                   = inOut.descr.getOrElse("")
  lazy val typeName: String                = inOut.typeName
  lazy val inOutType: InOutType            = inOut.inOutType

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

  // this function needs to be here as circe does not find the JsonEncoder in the extension method
  lazy val inMapper: EndpointIO.Body[String, In] = jsonBody[In]

  // this function needs to be here as circe does not find the JsonEncoder in the extension method
  lazy val outMapper: EndpointIO.Body[String, Out] = jsonBody[Out]

  lazy val inJson: Option[Json] = inOut.in match
    case _: NoInput => None
    case _          => Some(inOut.in.asJson.deepDropNullValues)

  lazy val outJson: Option[Json] = inOut.out match
    case _: NoInput => None
    case _          => Some(inOut.out.asJson.deepDropNullValues)

  lazy val variableNamesIn: List[String] =
    inOut.in.productElementNames.toList

  lazy val variableNamesOut: List[String] =
    inOut.out.productElementNames.toList

  def apiDescription(companyName: String): String =
    s"""$descr
       |
       |- Input:  `${inOut.in.getClass.getName.replace("$", " > ")}`
       |- Output: `${inOut.out.getClass.getName
        .replace("$", " > ")}`""".stripMargin

  protected def diagramName: Option[String] = None

  protected def diagramFrame(companyName: String): String =
    val postfix         = if typeName == "Process" then "bpmn" else "dmn"
    val postfixUpper    = postfix.head.toUpper + postfix.tail
    val pureDiagramName = diagramName.getOrElse(id)
    val name            = pureDiagramName.replaceFirst(s"$companyName-", "")
    val fileName        = s"$name.$postfix"
    val randomPostfix   = Random.nextInt(100000)
    s"""
       |<div class="diagramCanvas">
       |  <div class="diagram" id="$name-$randomPostfix">
       |    <img onLoad="openFromUrl('$fileName', new ${postfixUpper}JS({ container: $$('#$name-$randomPostfix'), height: '95%', width: '95%' }));" src="data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==" />
       |  </div>
       |</div>
       |
       |Download: [$fileName](diagrams/$fileName)
       |
       |
       |<div>
       |  <button onclick="downloadSVG('$name-$randomPostfix')">Download Diagram as SVG</button>
       |</div>
       |""".stripMargin
  end diagramFrame

end InOutApi

case class ProcessApi[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag},
    InitIn <: Product: {InOutEncoder , InOutDecoder , Schema}
](
    name: String,
    inOut: Process[In, Out, InitIn],
    apiExamples: ApiExamples[In, Out],
    apis: List[InOutApi[?, ?]] = List.empty,
    override val diagramName: Option[String] = None
) extends InOutApi[In, Out],
      GroupedApi:

  def withApis(apis: List[InOutApi[?, ?]]): ProcessApi[In, Out, InitIn] = copy(apis = apis)
  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)

  override def apiDescription(companyName:String): String =
    s"""${super.apiDescription(companyName)}
       |
       |${inOut.in match
        case _: GenericServiceIn => "" // no diagram if generic
        case _                   =>
          diagramFrame(companyName)
      }
       |${generalVariablesDescr(inOut.out, "")}""".stripMargin

      // this function needs to be here as circe does not find the JsonEncoder in the extension method
  lazy val initInMapper: EndpointIO.Body[String, InitIn] = jsonBody[InitIn]

end ProcessApi

object ProcessApi:
  def apply[
      In <: Product: {InOutEncoder , InOutDecoder , Schema},
      Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag},
      InitIn <: Product: {InOutEncoder , InOutDecoder , Schema}
  ](name: String, inOut: Process[In, Out, InitIn]): ProcessApi[In, Out, InitIn] =
    ProcessApi(name, inOut, ApiExamples(name, inOut))

end ProcessApi

def generalVariablesDescr[Out <: Product: InOutEncoder](
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
     |"outputMock": ${out.asJson.deepDropNullValues},
     |...
     |```
     |$serviceMock
     |</p>
     |</details>
     |</p>
      """.stripMargin
end generalVariablesDescr

sealed trait ExternalTaskApi[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
] extends InOutApi[In, Out]:
  def inOut: ExternalTask[In, Out, ?]

  def processName: String = inOut.processName
  lazy val topicName      = inOut.topicName

  override def apiDescription(companyName: String): String =
    s"""
       |**Topic:** `$topicName` (to define in the _**Topic**_ of the _**External Task**_ > _Service Task_ of type _External_)
       |
       |${super.apiDescription(companyName)}
       |
       |You can test this worker using the generic process _**$GenericExternalTaskProcessName**_ (e.g. with Postman).
       |""".stripMargin
end ExternalTaskApi

case class ServiceWorkerApi[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag},
    ServiceIn: {InOutEncoder , InOutDecoder , Schema},
    ServiceOut: {InOutEncoder , InOutDecoder , Schema}
](
    name: String,
    inOut: ServiceTask[In, Out, ServiceIn, ServiceOut],
    apiExamples: ApiExamples[In, Out]
) extends ExternalTaskApi[In, Out]:

  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)

  override def apiDescription(companyName: String): String =
    s"""
       |
       |${super.apiDescription(companyName)}
       |- ServiceOut:  `$serviceOutDescr`
       |${generalVariablesDescr(
        inOut.out,
        s"""
       |**outputServiceMock**:
       |```json
       |...
       |"outputServiceMock": ${MockedServiceResponse
            .success200(inOut.defaultServiceOutMock)
            .asJson.deepDropNullValues},
       |...
       |```"""
      )}
    """.stripMargin

  private def serviceOutDescr =
    inOut.defaultServiceOutMock match
      case seq: Seq[?] =>
        s"Seq[${seq.head.getClass.getName.replace("$", " > ")}]"
      case other       => other.getClass.getName.replace("$", " > ")
end ServiceWorkerApi

object ServiceWorkerApi:
  def apply[
      In <: Product: {InOutEncoder , InOutDecoder , Schema},
      Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag},
      ServiceIn: {InOutEncoder , InOutDecoder , Schema},
      ServiceOut: {InOutEncoder , InOutDecoder , Schema}
  ](
      name: String,
      inOut: ServiceTask[In, Out, ServiceIn, ServiceOut]
  ): ServiceWorkerApi[In, Out, ServiceIn, ServiceOut] =
    ServiceWorkerApi(name, inOut, ApiExamples(name, inOut))

end ServiceWorkerApi

case class CustomWorkerApi[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
](
    name: String,
    inOut: CustomTask[In, Out],
    apiExamples: ApiExamples[In, Out]
) extends ExternalTaskApi[In, Out]:

  def withExamples(
      examples: ApiExamples[In, Out]
  ): InOutApi[In, Out] =
    copy(apiExamples = examples)
end CustomWorkerApi

object CustomWorkerApi:
  def apply[
      In <: Product: {InOutEncoder , InOutDecoder , Schema},
      Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
  ](
      name: String,
      inOut: CustomTask[In, Out]
  ): CustomWorkerApi[In, Out] =
    CustomWorkerApi(name, inOut, ApiExamples(name, inOut))

end CustomWorkerApi

case class DecisionDmnApi[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
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

  override def apiDescription(companyName: String): String =
    s"""${super.apiDescription(companyName)}
       |${diagramFrame(companyName: String)}""".stripMargin
end DecisionDmnApi

object DecisionDmnApi:
  def apply[
      In <: Product: {InOutEncoder , InOutDecoder , Schema},
      Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
  ](name: String, inOut: DecisionDmn[In, Out]): DecisionDmnApi[In, Out] =
    DecisionDmnApi(name, inOut, ApiExamples(name, inOut))

end DecisionDmnApi

case class CApiGroup(
    name: String,
    description: String,
    apis: List[InOutApi[?, ?]]
) extends GroupedApi:

  def withApis(apis: List[InOutApi[?, ?]]): CApiGroup = copy(apis = apis)
  override def groupTag: Option[ApiTag]               = Some(ApiTag(name, description, name))

end CApiGroup

case class ActivityApi[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
](
    name: String,
    inOut: Activity[In, Out, ?],
    apiExamples: ApiExamples[In, Out]
) extends InOutApi[In, Out]:

  def withExamples(
      examples: ApiExamples[In, Out]
  ): ActivityApi[In, Out] =
    copy(apiExamples = examples)
end ActivityApi

object ActivityApi:

  def apply[
      In <: Product: {InOutEncoder , InOutDecoder , Schema},
      Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
  ](name: String, inOut: Activity[In, Out, ?]): ActivityApi[In, Out] =
    ActivityApi(name, inOut, ApiExamples(name, inOut))

end ActivityApi
case class ApiExamples[
    In <: Product: {InOutEncoder , InOutDecoder , Schema},
    Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
](
    inputExamples: InOutExamples[In],
    outputExamples: InOutExamples[Out]
)

object ApiExamples:

  def apply[
      In <: Product: {InOutEncoder , InOutDecoder , Schema},
      Out <: Product: {InOutEncoder , InOutDecoder , Schema, ClassTag}
  ](name: String, inOut: InOut[In, Out, ?]): ApiExamples[In, Out] =
    val enumInExamples = inOut.otherEnumInExamples
      .map: examples =>
        InOutExample(inOut.in) +:
          examples.map: ex =>
            InOutExample(ex)
      .getOrElse:
        Seq(InOutExample(name, inOut.in))

    val enumOutExamples = inOut.otherEnumOutExamples
      .map: examples =>
        InOutExample(inOut.out) +:
          examples.map: ex =>
            InOutExample(ex)
      .getOrElse:
        Seq(InOutExample(name, inOut.out))

    ApiExamples(
      InOutExamples(enumInExamples),
      InOutExamples(enumOutExamples)
    )
  end apply

end ApiExamples

case class InOutExamples[T <: Product: {InOutEncoder , InOutDecoder , Schema}](
    examples: Seq[InOutExample[T]]
):
  @targetName("add")
  def :+(label: String, example: T): InOutExamples[T] =
    copy(examples = examples :+ InOutExample(label, example))

  lazy val fetchExamples: Seq[InOutExample[T]] =
    examples
end InOutExamples

case class InOutExample[T <: Product: {InOutEncoder , InOutDecoder , Schema}](
    name: String,
    example: T
):
  // this function needs to be here as circe does not find theInOutEncoderin the extension method
  def toCamunda: FormVariables = CamundaVariable.toCamunda(example)
end InOutExample

object InOutExample:

  def apply[T <: Product: {InOutEncoder , InOutDecoder , Schema}](inOut: T): InOutExample[T] =
    val name = inOut.getClass.getName.replace("$", " > ").split('.').last
    InOutExample(name, inOut)

end InOutExample
