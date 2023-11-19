package camundala
package camunda7.worker

import camundala.bpmn.{*, given}
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.Decoder.Result
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.`type`.{PrimitiveValueType, ValueType}
import org.camunda.bpm.engine.variable.value.TypedValue

object CamundaHelper:

  def variableTypedOpt(
      varKey: String | InputParams
  ): HelperContext[Option[TypedValue]] =
    Option(summon[ExternalTask].getVariableTyped(varKey.toString))

  /** Returns the Variable in the Bag. If there is none it return `null`. It returns whatever
    * datatype the variable contains.
    */
  def variableOpt[A: Decoder](
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, Option[A]]] =
    for {
      maybeJson <- jsonVariableOpt(varKey)
      obj <- maybeJson
        .map(_.as[Option[A]])
        .getOrElse(Right(None))
        .left
        .map(err =>
          BadVariableError(
            s"Problem decoding Json to ${nameOfType[A]}: ${err.getMessage}"
          )
        )
    } yield obj

  def jsonVariableOpt(
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, Option[Json]]] =
    variableTypedOpt(varKey)
      .map {
        case typedValue if typedValue.getType == ValueType.NULL =>
          Right(None) // k -> null as Camunda Expressions need them
        case typedValue =>
          extractValue(typedValue)
            .map(v => Some(v))
      }
      .getOrElse(Right(None))

  // used for input variables you can define with Array of Strings or a comma-separated String
  // if not set it returns an empty Seq
  def extractSeqFromArrayOrString(
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, Seq[String]]] =
    extractSeqFromArrayOrString(varKey, Seq.empty)

  // used for input variables you can define with Array of Strings or a comma-separated String
  // if not set it returns an empty Seq
  def extractSeqFromArrayOrString(
      varKey: String | InputParams,
      defaultSeq: Seq[String | ErrorCodes]
  ): HelperContext[Either[BadVariableError, Seq[String]]] =
    jsonVariableOpt(varKey)
      .flatMap {
        case Some(value) if value.isArray =>
          extractFromSeq(
            value
              .as[Seq[String]]
          )
        case Some(value) if value.isString =>
          extractFromSeq(
            value
              .as[String]
              .map(_.split(",").toSeq)
          )
        case _ =>
          Right(defaultSeq.map(_.toString))
      }

  /** Analog `variable(String vari)`. You can define a Value that is returned if there is no
    * Variable with this name.
    */
  def variable[A: Decoder](
      varKey: String | InputParams,
      defaultObj: A
  ): HelperContext[Either[BadVariableError, A]] =
    variableOpt[A](varKey).map(_.getOrElse(defaultObj))

  /** Returns the Variable in the Bag. B if there is no Variable with that identifier.
    */
  def variable[T: Decoder](
      varKey: String | InputParams
  ): HelperContext[Either[BadVariableError, T]] =
    variableOpt(varKey)
      .flatMap(
        _.toEither(
          s"The Variable '$varKey' is required! But does  not exist in your Process"
        )
      )
  end variable

  def topicName: HelperContext[String] =
    summon[ExternalTask].getTopicName

  extension [T](option: Option[T])

    def toEither(msg: String): HelperContext[Either[BadVariableError, T]] =
      toEither(BadVariableError(errorMsg = msg))

    def toEither[E <: CamundalaWorkerError](
        error: E
    ): HelperContext[Either[E, T]] =
      option
        .map(Right(_))
        .getOrElse(
          Left(error)
        )

  end extension // Option

  def extractValue(typedValue: TypedValue): Either[BadVariableError, Json] =
    typedValue.getType match
      case pt: PrimitiveValueType if pt.getName == "json" =>
        val jsonStr = typedValue.getValue.toString
        parser
          .parse(jsonStr)
          .left
          .map(ex => BadVariableError(s"Input is not valid: $ex"))

      case _: PrimitiveValueType =>
        typedValue.getValue match
          case vt: DmnValueSimple =>
            Right(vt.asJson)
          case en: scala.reflect.Enum =>
            Right(Json.fromString(en.toString))
          case other =>
            Left(
              BadVariableError(
                s"Input is not valid: Unexpected PrimitiveValueType: $other"
              )
            )

      case other =>
        Left(
          BadVariableError(
            s"Unexpected ValueType ${other.getName} - but is ${typedValue.getType}"
          )
        )

  end extractValue

  private def extractFromSeq(
      variableKeys: Result[Seq[String]]
  ): HelperContext[Either[BadVariableError, Seq[String]]] =
    variableKeys
      .map(_.map(_.trim).filter(_.nonEmpty))
      .left
      .map { error =>
        error.printStackTrace()
        BadVariableError(
          s"Could not extract Seq for an Array or comma-separated String: ${error.getMessage}"
        )
      }

end CamundaHelper
