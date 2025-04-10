package camundala.examples.demos.newWorker

import camundala.domain.*
import camundala.worker.*
import camundala.worker.c7zio.{C7Context, C7Worker, C8Context, C8Worker}

import scala.reflect.ClassTag

trait CompanyWorkerHandler[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
] extends C7Worker[In, Out], C8Worker[In, Out]:
  override protected def c7Context: C7Context = new C7Context {}
  override protected def c8Context: C8Context = new C8Context {}
end CompanyWorkerHandler

trait CompanyValidationWorkerDsl[
    In <: Product: InOutCodec
] extends CompanyWorkerHandler[In, NoOutput], ValidationWorkerDsl[In]

trait CompanyInitWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    InitIn <: Product: InOutCodec,
    InConfig <: Product: InOutCodec
] extends CompanyWorkerHandler[In, Out], InitWorkerDsl[In, Out, InitIn, InConfig]

trait CompanyCustomWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
] extends CompanyWorkerHandler[In, Out], CustomWorkerDsl[In, Out]

trait CompanyServiceWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    ServiceIn: InOutEncoder,
    ServiceOut: {InOutDecoder, ClassTag}
] extends CompanyWorkerHandler[In, Out], ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]
