package camundala
package bpmn

import domain.*

trait ProcessDescr[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec
]

trait ServiceDescr[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec
] extends ProcessDescr[In, Out]