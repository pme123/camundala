package camundala
package api

import bpmn.*
import domain.*

import scala.language.implicitConversions
import scala.reflect.ClassTag

trait ApiInlinedDsl extends ApiBaseDsl:

  implicit inline def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](inline process: Process[In, Out]): ProcessApi[In, Out] =
    ProcessApi(nameOfVariable(process), process)

  implicit inline def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](inline dmn: DecisionDmn[In, Out]): DecisionDmnApi[In, Out] =
    DecisionDmnApi(nameOfVariable(dmn), dmn)

  implicit inline def toApi[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](inline inOut: Activity[In, Out, ?]): ActivityApi[In, Out] =
    ActivityApi(nameOfVariable(inOut), inOut)


end ApiInlinedDsl
