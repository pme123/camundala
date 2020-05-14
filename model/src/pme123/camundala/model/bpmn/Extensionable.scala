package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.Prop

trait Extensionable {
  def extensions: Extensions

}

sealed trait Extensions {
  def properties: Seq[Prop]

  def inOuts: InputOutputs

}

object Extensions {

  case class PropExtensions(properties: Seq[Prop] = Seq.empty) extends Extensions {
    final val inOuts: InputOutputs = InputOutputs.none
  }

  object PropExtensions {
    val none: PropExtensions = PropExtensions()
  }

  case class PropInOutExtensions(properties: Seq[Prop] = Seq.empty, inOuts: InputOutputs = InputOutputs.none) extends Extensions

  object PropInOutExtensions {
    val none: PropInOutExtensions = PropInOutExtensions()
  }

  case class Prop(key: PropKey, value: String)

}

case class InputOutputs(inputs: Seq[InputOutput] = Nil, outputs: Seq[InputOutput] = Nil) {
  val inputMap: Map[PropKey, ConditionExpression] = inputs.map(in => in.key -> in.expression).toMap
  val outputMap: Map[PropKey, ConditionExpression] = outputs.map(out => out.key -> out.expression).toMap
}

object InputOutputs {
  def none: InputOutputs = InputOutputs()
}

case class InputOutput(key: PropKey, expression: ConditionExpression)

