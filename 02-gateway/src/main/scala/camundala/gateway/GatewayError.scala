package camundala.gateway

import camundala.domain.ErrorCodes

sealed trait GatewayError extends Throwable:
  def errorCode: ErrorCodes
  def errorMsg: String

  def causeMsg                    = s"$errorCode: $errorMsg"
  override def toString(): String = causeMsg
end GatewayError

object GatewayError:
  case class DecodingError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`gateway-decoding-error`
  ) extends GatewayError

  case class EncodingError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`gateway-encoding-error`
  ) extends GatewayError

  case class ProcessError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`gateway-process-error`
  ) extends GatewayError

  case class DmnError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`gateway-dmn-error`
  ) extends GatewayError

  case class WorkerError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`gateway-worker-error`
  ) extends GatewayError

  case class UnexpectedError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`error-unexpected`
  ) extends GatewayError

  case class ServiceError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`gateway-service-error`
  ) extends GatewayError
end GatewayError
