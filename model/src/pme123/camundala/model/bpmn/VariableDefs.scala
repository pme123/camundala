package pme123.camundala.model.bpmn

case class VariableDefs(defs: VariableDef*) {
  override def toString: String =
    defs.mkString("\n")

}

object VariableDefs {
  def none: VariableDefs = VariableDefs()
}

/**
  * VariableType.Json:
  * existingAddr_json = execution.getVariableTyped('existingAddress')
  * if(existingAddr_json == null){
  * throw new Exception("The JSON Variable 'existingAddr' is not set!")
  * } else { existingAddr = existingAddr_json}.getValue()
  * Variable.BusinessKey:
  * bKey = execution.getBusinessKey()
  * Others use just toString
  * myKey = execution.getVariable('myKey')
  *
  * defaultValue: if the Variable is not set it sets the default value - if available.
  *
  */
case class VariableDef(key: PropKey,
                       variableType: VariableType = VariableType.String,
                       defaultValue: Option[String] = None) {
  override def toString: String = (variableType match {
    case VariableType.Json =>
      s"""${key}_json = execution.getVariableTyped('$key')
         |if(${key}_json == null){
         |  throw new Exception("The JSON Variable '$key' is not set!")
         |} else { $key = ${key}_json.getValue().toString().replace("\\"", "\\\\\\"")}""".stripMargin

    case VariableType.BusinessKey =>
      s"$key = execution.getBusinessKey()"
    case _ =>
      s"$key = execution.getVariable('$key')"
  }) + defaultValue.map(v => s"""\nif($key == null) $key = ${if (variableType == VariableType.String) "\"" + v + "\"" else v}""").getOrElse("")

}

sealed trait VariableType

object VariableType {

  case object Boolean extends VariableType

  case object Short extends VariableType

  case object Integer extends VariableType

  case object Long extends VariableType

  case object Double extends VariableType

  case object Date extends VariableType

  case object String extends VariableType

  case object Json extends VariableType

  case object BusinessKey extends VariableType

  // XML and File not supported
}

