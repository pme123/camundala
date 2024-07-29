package camundala
package api

import camundala.bpmn.*
import camundala.domain.*

import scala.reflect.ClassTag

trait ApiDsl extends ApiBaseDsl:

  protected def nameOfVariable(inOut: InOut[?, ?, ?]): String =
    inOut.id

  // converters
  given [
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema: ClassTag
  ]: Conversion[Process[In, Out, In], ProcessApi[In, Out]] =
    process => ProcessApi(nameOfVariable(process), process)

  given [
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema: ClassTag,
      ServiceIn: InOutEncoder: InOutDecoder: Schema,
      ServiceOut: InOutEncoder: InOutDecoder: Schema
  ]: Conversion[ServiceTask[In, Out, ServiceIn, ServiceOut], ServiceWorkerApi[
    In,
    Out,
    ServiceIn,
    ServiceOut
  ]] =
    task => ServiceWorkerApi(nameOfVariable(task), task)

  given [
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema: ClassTag
  ]: Conversion[CustomTask[In, Out], CustomWorkerApi[In, Out]] =
    task => CustomWorkerApi(nameOfVariable(task), task)

  given [
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema: ClassTag
  ]: Conversion[DecisionDmn[In, Out], DecisionDmnApi[In, Out]] =
    dmn => DecisionDmnApi(nameOfVariable(dmn), dmn)

  given [
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema: ClassTag
  ]: Conversion[UserTask[In, Out], ActivityApi[In, Out]] =
    inOut => ActivityApi(nameOfVariable(inOut), inOut)

  given [
      In <: Product: InOutEncoder: InOutDecoder: Schema
  ]: Conversion[ReceiveEvent[In, ?], ActivityApi[In, NoOutput]] =
    inOut => ActivityApi(nameOfVariable(inOut), inOut)

end ApiDsl
