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
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ]: Conversion[Process[In, Out], ProcessApi[In, Out]] =
    process => ProcessApi(nameOfVariable(process), process)

  given [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag,
      ServiceOut: Encoder: Decoder: Schema
  ]: Conversion[ServiceTask[In, Out, ServiceOut], ServiceWorkerApi[In, Out, ServiceOut]] =
    task => ServiceWorkerApi(nameOfVariable(task), task)

  given [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ]: Conversion[CustomTask[In, Out], CustomWorkerApi[In, Out]] =
    task => CustomWorkerApi(nameOfVariable(task), task)

  given [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ]: Conversion[DecisionDmn[In, Out], DecisionDmnApi[In, Out]] =
    dmn => DecisionDmnApi(nameOfVariable(dmn), dmn)

  given [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ]: Conversion[UserTask[In, Out], ActivityApi[In, Out]] =
    inOut => ActivityApi(nameOfVariable(inOut), inOut)

  given [
      In <: Product: Encoder: Decoder: Schema
  ]: Conversion[ReceiveEvent[In, ?], ActivityApi[In, NoOutput]] =
    inOut => ActivityApi(nameOfVariable(inOut), inOut)

end ApiDsl
