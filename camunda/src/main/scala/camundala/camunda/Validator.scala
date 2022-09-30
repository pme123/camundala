package camundala.camunda

import camundala.bpmn.*
import camundala.domain.*

import io.circe.generic.auto.*
import io.circe.syntax.*
import org.camunda.bpm.engine.delegate.{BpmnError, DelegateExecution, ExecutionListener}
import org.camunda.bpm.engine.variable.`type`.{FileValueType, PrimitiveValueType, SerializableValueType}
import org.camunda.bpm.engine.variable.impl.`type`.PrimitiveValueTypeImpl.StringTypeImpl
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl
import org.camunda.bpm.engine.variable.value.TypedValue
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

abstract class Validator[T <: Product : Encoder : Decoder]
  extends ExecutionListener :

  def product: T

  @throws[Exception]
  override def notify(execution: DelegateExecution): Unit =
    val ir = product.productElementNames.toSeq
      .map { k =>
        val typedValue: TypedValue = execution.getVariableTyped(k)
        if (typedValue == null)
          println(s"NOT SET: $k")
          "NOT_SET"
        else
          val value = extractValue(typedValue)
          s""""$k": $value"""
      }.filterNot(_ == "NOT_SET")
      .mkString("{", ",", "}")
    println(s"VALIDATOR JSON: ${toJson(ir)}")
    toJson(ir).as[T] match
      case Right(_) =>
        println(s"Validation Succeeded (${product.getClass.getSimpleName})")
      case Left(ex) =>
        println(s"Validation Failed $ex (${product.getClass.getSimpleName})")
        throw new IllegalArgumentException(s"Validation Error: Input is not valid: $ex")


  private def extractValue(typedValue: TypedValue) =
    typedValue.getType match
      case _: PrimitiveValueType =>
        typedValue.getValue match
          case vt: DmnValueType =>
            vt.asJson
          case other =>
            println(s"UneXPECTED: $other")
            other
      case _: SerializableValueType => typedValue.getValue
      case _: FileValueType =>
        typedValue match
          case f: FileValueImpl =>
            FileInOut(
              f.getFilename,
              f.getByteArray.take(10), // just take a few
              Option(f.getMimeType)
            ).asJson
          case o =>
            throwErr(s"Must be a FileValueImpl - but is ${o.getClass}")
