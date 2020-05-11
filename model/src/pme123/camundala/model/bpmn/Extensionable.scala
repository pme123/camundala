package pme123.camundala.model.bpmn

trait Extensionable {
  def extensions: Extensions

}

sealed trait Extensions {
  def properties: Map[String, String]

  def inOuts: InputOutputs

}

object Extensions {

  case class PropExtensions(properties: Map[String, String] = Map.empty) extends Extensions {
    final val inOuts: InputOutputs = InputOutputs.none
  }

  object PropExtensions {
    val none: PropExtensions = PropExtensions()
  }

  case class PropInOutExtensions(properties: Map[String, String] = Map.empty, inOuts: InputOutputs = InputOutputs.none) extends Extensions

  object PropInOutExtensions {
    val none: PropInOutExtensions = PropInOutExtensions()
  }

}

case class InputOutputs(inputs: Seq[InputOutput] = Nil, outputs: Seq[InputOutput] = Nil) {
  val inputMap: Map[String, ConditionExpression] = inputs.map(in => in.key -> in.expression).toMap
  val outputMap: Map[String, ConditionExpression] = outputs.map(out => out.key -> out.expression).toMap
}

object InputOutputs {
  def none: InputOutputs = InputOutputs()
}

case class InputOutput(key: String, expression: ConditionExpression)

