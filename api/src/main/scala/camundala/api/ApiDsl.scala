package camundala
package api

import camundala.bpmn.*

import scala.language.implicitConversions
import scala.reflect.ClassTag

trait ApiDsl extends ApiBaseDsl:

  protected def nameOfVariable(inOut: InOut[?, ?, ?]): String =
    inOut.id

  // converters
  implicit def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](process: Process[In, Out]): ProcessApi[In, Out] =
    ProcessApi(nameOfVariable(process), process)

  implicit def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag,
      ServiceOut: Encoder: Decoder: Schema
  ](
      process: ServiceTask[In, Out, ServiceOut]
  ): ServiceWorkerApi[In, Out, ServiceOut] =
    ServiceWorkerApi(nameOfVariable(process), process)

  implicit def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](
      task: CustomTask[In, Out]
  ): CustomWorkerApi[In, Out] =
    CustomWorkerApi(nameOfVariable(task), task)

  implicit def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](dmn: DecisionDmn[In, Out]): DecisionDmnApi[In, Out] =
    DecisionDmnApi(nameOfVariable(dmn), dmn)

  implicit def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](inOut: Activity[In, Out, ?]): ActivityApi[In, Out] =
    ActivityApi(nameOfVariable(inOut), inOut)

end ApiDsl
