package camundala.worker.c8zio

import camundala.bpmn.{*, given}
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.Decoder.Result
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.`type`.{PrimitiveValueType, ValueType}
import org.camunda.bpm.engine.variable.value.TypedValue
import zio.*

object CamundaHelper:

  def variableTypedOpt(
      varKey: String | InputParams
  ): HelperContext[Option[TypedValue]] =
    Option(summon[ExternalTask].getVariableTyped(varKey.toString))

  /** Returns the Variable in the Bag. If there is none it return `null`. It returns whatever
    * datatype the variable contains.
    */
  def variableOpt[A: InOutDecoder](
      varKey: String | InputParams
  ): HelperContext[IO[BadVariableError, Option[A]]] =
    for
      maybeJson <- jsonVariableOpt(varKey)
      obj       <- ZIO.fromEither(
                     maybeJson
                       .map(_.as[Option[A]])
                       .getOrElse(Right(None))
                       .left
                       .map(err =>
                         BadVariableError(
                           s"Problem decoding Json to ${nameOfType[A]}: ${err.getMessage}"
                         )
                       )
                   )
    yield obj

  def jsonVariableOpt(
      varKey: String | InputParams
  ): HelperContext[IO[BadVariableError, Option[Json]]] =
    variableTypedOpt(varKey)
      .map {
        case typedValue if typedValue.getType == ValueType.NULL =>
          ZIO.succeed(None) // k -> null as Camunda Expressions need them
        case typedValue                                         =>
          extractValue(typedValue)
            .map(v => Some(v))
      }
      .getOrElse(ZIO.succeed(None))

  // used for input variables you can define with Array of Strings or a comma-separated String
  // if not set it returns an empty Seq
  def extractSeqFromArrayOrString(
      varKey: String | InputParams
  ): HelperContext[IO[BadVariableError, Seq[String]]] =
    extractSeqFromArrayOrString(varKey, Seq.empty)

  // used for input variables you can define with Array of Strings or a comma-separated String
  // if not set it returns an empty Seq
  def extractSeqFromArrayOrString(
      varKey: String | InputParams,
      defaultSeq: Seq[String | ErrorCodes] = Seq.empty
  ): HelperContext[IO[BadVariableError, Seq[String]]] =
    jsonVariableOpt(varKey)
      .flatMap {
        case Some(value) if value.isArray  =>
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
        case _                             =>
          ZIO.succeed(defaultSeq.map(_.toString))
      }

  /** Analog `variable(String vari)`. You can define a Value that is returned if there is no
    * Variable with this name.
    */
  def variable[A: InOutDecoder](
      varKey: String | InputParams,
      defaultObj: A
  ): HelperContext[IO[BadVariableError, A]] =
    variableOpt[A](varKey).map(_.getOrElse(defaultObj))

  /** Returns the Variable in the Bag. B if there is no Variable with that identifier.
    */
  def variable[T: InOutDecoder](
      varKey: String | InputParams
  ): HelperContext[IO[BadVariableError, T]] =
    variableOpt(varKey)
      .flatMap(maybeVar =>
        ZIO.fromOption(
          maybeVar
        ).mapError(_ =>
          BadVariableError(
            s"The Variable '$varKey' is required! But does  not exist in your Process"
          )
        )
      )
  end variable

  def topicName: HelperContext[String] =
    summon[ExternalTask].getTopicName

  extension [T](option: Option[T])

    def toZIO(msg: String): HelperContext[IO[BadVariableError, T]] =
      toZIO(BadVariableError(errorMsg = msg))

    def toZIO[E <: CamundalaWorkerError](
        error: E
    ): HelperContext[IO[E, T]] =
      option
        .map(ZIO.succeed(_))
        .getOrElse(
          ZIO.fail(error)
        )

  end extension // Option

  def extractValue(typedValue: TypedValue): IO[BadVariableError, Json] =
    typedValue.getType match
      case pt: PrimitiveValueType if pt.getName == "json" =>
        val jsonStr = typedValue.getValue.toString
        ZIO.fromEither(io.circe.parser
          .parse(jsonStr)
          .left
          .map(ex => BadVariableError(s"Input is not valid: $ex")))

      case _: PrimitiveValueType =>
        typedValue.getValue match
          case vt: DmnValueSimple     =>
            ZIO.succeed(vt.asJson)
          case en: scala.reflect.Enum =>
            ZIO.succeed(Json.fromString(en.toString))
          case other                  =>
            ZIO.fail(
              BadVariableError(
                s"Input is not valid: Unexpected PrimitiveValueType: $other"
              )
            )

      case other =>
        ZIO.fail(
          BadVariableError(
            s"Unexpected ValueType ${other.getName} - but is ${typedValue.getType}"
          )
        )

  end extractValue

  private def extractFromSeq(
      variableKeys: Result[Seq[String]]
  ): HelperContext[IO[BadVariableError, Seq[String]]] =
    ZIO.fromEither(variableKeys)
      .map: varKeys =>
        varKeys.map(_.trim).filter(_.nonEmpty)
      .mapError : error =>
        error.printStackTrace()
        BadVariableError(
          s"Could not extract Seq for an Array or comma-separated String: ${error.getMessage}"
        )

end CamundaHelper