package camundala.examples.demos.newWorker

import camundala.camunda7.worker.C7WorkerHandler
import camundala.domain.*
import camundala.worker.*
import camundala.worker.c8zio.C8Worker

import scala.reflect.ClassTag

trait CompanyWorkerHandler[
  In <: Product: InOutCodec,
  Out <: Product: InOutCodec
] extends C7WorkerHandler, C8Worker[In, Out]

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
    ServiceOut: InOutDecoder: ClassTag
] extends CompanyWorkerHandler[In, Out], ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]
