package camundala.api

import camundala.bpmn.{GeneralVariables, InOutDescr}
import camundala.domain.*

final case class ModelerTemplGenerator(
    apiVersion: String,
    config: ModelerTemplateConfig,
    projectName: Option[String]
) extends App:

  lazy val version = apiVersion.split("\\.").head.toInt

  def generate(apis: List[InOutApi[?, ?]]): Unit =
    os.makeDir.all(config.templatePath)
    apis.foreach:
      case api: ExternalTaskApi[?, ?] =>
        println(s"ExternalTaskApi supported for Modeler Template: ${api.id}")
        generateTempl(api)
      case api: ProcessApi[?, ?] if !api.inOut.in.isInstanceOf[GenericServiceIn] =>
        println(s"ProcessApi supported for Modeler Template: ${api.id} - ${api.name}")
        generateTempl(api)
      case api =>
        println(s"API NOT supported for Modeler Template: ${api.getClass.getSimpleName} - ${api.id}")
  end generate

  private def generateTempl(
      inOut: InOutDescr[?, ?],
      appliesTo: Seq[AppliesTo],
      elementType: ElementType,
      properties: Seq[TemplProp]
  ): Unit =
    val mapProps = mappings(
      inOut.in,
      if elementType == ElementType.callActivity then PropType.`camunda:in`
      else PropType.`camunda:inputParameter`
    ) ++
      mappings(
        inOut.out,
        if elementType == ElementType.callActivity then PropType.`camunda:out`
        else PropType.`camunda:outputParameter`
      )
    val templ = MTemplate(
      inOut.id,
      inOut.id,
      inOut.descr.getOrElse("").split("---").head,
      version,
      appliesTo,
      elementType,
      mapProps ++ properties :+ TemplProp.businessKey :+ TemplProp.name(inOut.niceName),
      config.schema
    )
    os.write.over(
      config.templatePath / s"${inOut.id}.json",
      templ.asJson.deepDropNullValues.toString
    )
  end generateTempl

  private def generateTempl(inOut: ProcessApi[?, ?]): Unit =
    generateTempl(
      inOut.inOutDescr,
      AppliesTo.activity,
      ElementType.callActivity,
      Seq(
        TemplProp.calledElement(inOut.id),
        TemplProp.calledElementBinding
      ) ++ generalVariables(isCallActivity = true, GeneralVariables.processVariables)
    )
  end generateTempl

  private def generateTempl(inOut: ExternalTaskApi[?, ?]): Unit =
    val vars = inOut match
      case _: ServiceWorkerApi[?, ?, ?, ?] => GeneralVariables.serviceWorkerVariables
      case _ => GeneralVariables.customWorkerVariables
    generateTempl(
      inOut.inOutDescr,
      AppliesTo.activity,
      ElementType.serviceTask,
      Seq(
        TemplProp.serviceTaskTopic(inOut.id),
        TemplProp.serviceTaskType
      ) ++ generalVariables(isCallActivity = false, vars)
    )
  end generateTempl

  private def mappings[T <: Product](prod: T, propType: PropType): Seq[TemplProp] =
    prod.productElementNames.toSeq
      .filterNot:
        _ == "inConfig" // don't show configuration
      .map: k =>
        TemplProp(
          `type` = TemplType.Hidden,
          label = k,
          value = if PropType.`camunda:inputParameter` == propType then s"#{$k}" else k,
          binding = propType match
            case PropType.`camunda:in` => PropBinding.`camunda:in`(
                `type` = propType,
                target = k
              )
            case PropType.`camunda:out` => PropBinding.`camunda:out`(
                `type` = propType,
                source = k
              )
            case PropType.`camunda:inputParameter` => PropBinding.`camunda:inputParameter`(
                `type` = propType,
                name = k
              )
            case PropType.`camunda:outputParameter` => PropBinding.`camunda:outputParameter`(
                `type` = propType,
                source = s"#{$k}"
              )
            case _ =>
              throw new IllegalArgumentException(s"PropType not expected for mappings: $propType")
        )
  end mappings

  private def generalVariables(isCallActivity: Boolean, vars: Seq[String]) =
    if config.generateGeneralVariables then
      vars
        .map: k =>
          TemplProp(
            `type` = TemplType.Hidden,
            label = k,
            value = if isCallActivity then k else s"#{null}",
            binding = if isCallActivity then
              PropBinding.`camunda:in`(
                target = k
              )
            else
              PropBinding.`camunda:inputParameter`(
                name = k
              )
          )
    else
      Seq.empty
  end generalVariables

end ModelerTemplGenerator

final case class MTemplate(
    name: String,
    id: String,
    description: String,
    version: Int,
    appliesTo: Seq[AppliesTo],
    elementType: ElementType,
    properties: Seq[TemplProp],
    $schema: String,
    groups: Seq[PropGroup] = Seq.empty,
    entriesVisible: Boolean = true
)

object MTemplate:
  given InOutCodec[MTemplate] = deriveInOutCodec
  given ApiSchema[MTemplate] = deriveApiSchema

final case class ElementType(
    value: AppliesTo
)

object ElementType:
  lazy val callActivity: ElementType = ElementType(AppliesTo.`bpmn:CallActivity`)
  lazy val serviceTask: ElementType = ElementType(AppliesTo.`bpmn:ServiceTask`)

  given InOutCodec[ElementType] = deriveInOutCodec
  given ApiSchema[ElementType] = deriveApiSchema
end ElementType

final case class TemplProp(
    value: String,
    `type`: TemplType,
    binding: PropBinding,
    label: String = "",
    description: String = "",
    group: Option[PropGroupId] = None
)

object TemplProp:
  lazy val businessKey = TemplProp(
    `type` = TemplType.Hidden,
    value = "#{execution.processBusinessKey}",
    //  group = Some(PropGroup.callActivity.id),
    binding = PropBinding.`camunda:in:businessKey`()
  )
  def name(value: String) = TemplProp(
    `type` = TemplType.Hidden,
    value = value,
    //  group = Some(PropGroup.callActivity.id),
    binding = PropBinding.property(name = "name")
  )
  def calledElement(value: String) = TemplProp(
    `type` = TemplType.Hidden,
    value = value,
    //  group = Some(PropGroup.callActivity.id),
    binding = PropBinding.property(name = "calledElement")
  )
  lazy val calledElementBinding = TemplProp(
    `type` = TemplType.Hidden,
    value = "deployment",
    //  group = Some(PropGroup.callActivity.id),
    binding = PropBinding.property(name = "camunda:calledElementBinding")
  )
  // Service Task
  def serviceTaskTopic(value: String) = TemplProp(
    `type` = TemplType.Hidden,
    value = value,
    binding = PropBinding.property(name = "camunda:topic")
  )
  lazy val serviceTaskType = TemplProp(
    `type` = TemplType.Hidden,
    value = "external",
    binding = PropBinding.property(name = "camunda:type")
  )

  given InOutCodec[TemplProp] = deriveInOutCodec
  given ApiSchema[TemplProp] = deriveApiSchema
end TemplProp

enum PropBinding:
  case property(
      `type`: PropType = PropType.property,
      name: String
  )
  case `camunda:in:businessKey`(
      `type`: PropType = PropType.`camunda:in:businessKey`
  )
  case `camunda:in`(
      `type`: PropType = PropType.`camunda:in`,
      target: String,
      expression: Boolean = false
  )
  case `camunda:out`(
      `type`: PropType = PropType.`camunda:out`,
      source: String,
      expression: Boolean = false
  )
  case `camunda:inputParameter`(
      `type`: PropType = PropType.`camunda:inputParameter`,
      name: String
  )
  case `camunda:outputParameter`(
      `type`: PropType = PropType.`camunda:outputParameter`,
      source: String
  )
end PropBinding

object PropBinding:
  given InOutCodec[PropBinding] = deriveInOutCodec
  given ApiSchema[PropBinding] = deriveApiSchema

final case class PropGroup(
    id: PropGroupId,
    label: String
)

object PropGroup:
  val callActivity = PropGroup(
    PropGroupId.calledProcess,
    "Called Process"
  )
  val inMappings = PropGroup(
    PropGroupId.inMappings,
    "In Mappings"
  )
  val outMapping = PropGroup(
    PropGroupId.outMappings,
    "Out Mappings"
  )
  val inputs = PropGroup(
    PropGroupId.inputs,
    "Inputs"
  )
  val outputs = PropGroup(
    PropGroupId.outputs,
    "Outputs"
  )

  given InOutCodec[PropGroup] = deriveInOutCodec
  given ApiSchema[PropGroup] = deriveApiSchema
end PropGroup

enum TemplType:
  case String, Text, Boolean, Dropdown, Hidden
object TemplType:
  given InOutCodec[TemplType] = deriveEnumInOutCodec
  given ApiSchema[TemplType] = deriveApiSchema
enum PropType:
  case property, `camunda:in:businessKey`, `camunda:in`, `camunda:out`, `camunda:inputParameter`,
    `camunda:outputParameter`
object PropType:
  given InOutCodec[PropType] = deriveEnumInOutCodec
  given ApiSchema[PropType] = deriveApiSchema

enum PropGroupId:
  case calledProcess, inMappings, outMappings, inputs, outputs
object PropGroupId:
  given InOutCodec[PropGroupId] = deriveEnumInOutCodec
  given ApiSchema[PropGroupId] = deriveApiSchema

enum AppliesTo:
  case `bpmn:Activity`, `bpmn:CallActivity`, `bpmn:ServiceTask`

object AppliesTo:
  lazy val activity = Seq(AppliesTo.`bpmn:Activity`) // all
  lazy val callActivity = Seq(AppliesTo.`bpmn:CallActivity`)
  lazy val serviceTask = Seq(AppliesTo.`bpmn:ServiceTask`)

  given InOutCodec[AppliesTo] = deriveEnumInOutCodec
  given ApiSchema[AppliesTo] = deriveApiSchema
end AppliesTo
