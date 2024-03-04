package camundala.helper.openApi

import camundala.bpmn.BpmnWorkerDsl
import camundala.simulation.custom.BasicSimulationDsl

case class OpenApiConfig(
    projectName: String,
    subProjectName: Option[String] = None,
    typeMappers: Seq[TypeMapper] = OpenApiConfig.generalTypeMapping,
    openApiFile: os.RelPath = OpenApiConfig.openApiFile,
    outputPath: String => os.Path = OpenApiConfig.outputPath(_),
    superBpmnClass: String = OpenApiConfig.superBpmnClass,
    superSimulationClass: String = OpenApiConfig.superSimulationClass,
    superWorkerClass: String
):
  lazy val basePackage =
    s"${projectName.replace('-', '.')}${subProjectName.map(n => s".$n").getOrElse("")}"
  lazy val bpmnPath: os.Path = basePath(bpmnComp) / "bpmn"
  lazy val schemaPath: os.Path = bpmnPath / "schema"
  lazy val simulationPath: os.Path = basePath(simulationComp) / "simulation"
  def workerPath: os.Path = basePath(workerComp) / "worker"
  lazy val bpmnPackage: String = s"$basePackage.bpmn"
  lazy val schemaPackage: String = s"$bpmnPackage.schema"
  lazy val simulationPackage: String = s"$basePackage.simulation"
  lazy val workerPackage: String = s"$basePackage.worker"
  lazy val projectTopicName: String =
    s"$projectName${subProjectName.map(n => s"-$n").getOrElse("")}"
  lazy val typeMapping =
    typeMappers
      .map:
        case TypeMapper(from, to, _) => from -> to
      .toMap
  lazy val implMapping =
    typeMappers
      .map:
        case TypeMapper(_, to, impl) => to -> impl
      .distinct
      .toMap

  private lazy val bpmnComp = "02-bpmn"
  private lazy val simulationComp = "03-simulation"
  private lazy val workerComp = "03-worker"

  private def basePath(component: String) =
    outputPath(component) / (projectName.split('-') ++ subProjectName.toSeq)

end OpenApiConfig

object OpenApiConfig:
  lazy val openApiFile: os.RelPath = os.rel / "openApi.yml"
  lazy val outputPath: String => os.Path = os.pwd / _ / ".generated"
  lazy val superBpmnClass: String = BpmnWorkerDsl.getClass.getName //"camundala.bpmn.BpmnWorkerDsl"
  lazy val superSimulationClass: String = BasicSimulationDsl.getClass.getName
  lazy val generalTypeMapping = Seq(
    TypeMapper("array", "Seq", _.getOrElse("Seq.empty")),
    TypeMapper("set", "Set", _.getOrElse("Set.empty")),
    TypeMapper("boolean", "Boolean", _.getOrElse("true")),
    TypeMapper("string", "String", _.getOrElse("\"SomeDummyText\"")),
    TypeMapper("int", "Int", _.getOrElse("12")),
    TypeMapper("integer", "Int", _.getOrElse("12")),
    TypeMapper("long", "Long", _.map(ex => s"${ex}L").getOrElse("2345312311231212L")),
    TypeMapper("float", "Float", _.map(ex => s"${ex}f").getOrElse("12.5f")),
    TypeMapper("byte", "Byte", _.getOrElse("2")),
    TypeMapper("short", "Short", _.getOrElse("12")),
    TypeMapper("char", "Char", _.map(e => s"'$e'").getOrElse("'12'")),
    TypeMapper("double", "Double", _.getOrElse("14.5")),
    TypeMapper("object", "Json", jsonObj),
    TypeMapper("file", "File", file),
    TypeMapper("binary", "File", file),
    TypeMapper("number", "Double", _.getOrElse("14.5")),
    TypeMapper("decimal", "BigDecimal", ex => s"new java.math.BigDecimal(${ex.getOrElse("12.6")})"),
    TypeMapper("ByteArray", "Array[Byte]", _.getOrElse("")),
    TypeMapper("AnyType", "Json", jsonObj),
    TypeMapper(
      "date-time",
      "LocalDateTime",
      _.map(e => s"""LocalDateTime.parse($e)""").getOrElse("LocalDateTime.now()")
    ),
    TypeMapper(
      "date",
      "LocalDate",
      _.map(e => s"""LocalDate.parse($e)""").getOrElse("LocalDate.now()")
    )
  )

  lazy val jsonObj: Option[String] => String = _.map(ex =>
    s"""parser.parse(s\"\"\"$ex\"\"\").toOption.getOrElse(Json.obj())"""
  ).getOrElse("Json.obj()")

  private lazy val file: Option[String] => String =
    ex => s"new java.io.File(\"${ex.getOrElse("path/to/file")}\")"

end OpenApiConfig

case class TypeMapper(
    jsonType: String,
    scalaType: String,
    defaultValue: Option[String] => String
)
