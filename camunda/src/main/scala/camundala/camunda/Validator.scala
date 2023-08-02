package camundala.camunda

import camundala.bpmn.*
import camundala.domain.*

import io.circe.generic.auto.*
import io.circe.syntax.*
import org.camunda.bpm.engine.delegate. DelegateExecution
import org.camunda.bpm.engine.variable.`type`.{FileValueType, PrimitiveValueType, SerializableValueType}
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl
import org.camunda.bpm.engine.variable.value.TypedValue


/**
 * Validator to validate the input variables automatically.
 */
trait Validator[T <: Product : CirceCodec] :

  def prototype: T

  def validate(execution: DelegateExecution): Unit =
    val ir = prototype.productElementNames.toSeq
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

    toJson(ir).as[T] match
      case Right(_) =>
        println(s"Validation Succeeded (${prototype.getClass.getSimpleName})")
      case Left(ex) =>
        println(s"Validation Failed $ex (${prototype.getClass.getSimpleName})")
        throw new IllegalArgumentException(s"Validation Error: Input is not valid: $ex")


  private def extractValue(typedValue: TypedValue): AnyRef =
    typedValue.getType match
      case _: PrimitiveValueType =>
        typedValue.getValue match
          case vt: DmnValueSimple =>
            vt.asJson
          case en: scala.reflect.Enum =>
            en.toString
          case other =>
            println(s"Unexpected: $other")
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
