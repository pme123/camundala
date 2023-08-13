package camundala
package bpmn

import domain.*

trait ProcessDescr[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec
]

trait ServiceDescr[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec,
  InB: CirceCodec, // body of service
  OutS: CirceCodec, // output of service
  OutE: CirceCodec // error of service
] extends ProcessDescr[In, Out]