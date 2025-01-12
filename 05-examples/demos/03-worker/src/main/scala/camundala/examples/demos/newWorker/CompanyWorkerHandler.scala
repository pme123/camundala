package camundala.examples.demos.newWorker

import camundala.camunda7.worker.C7WorkerHandler
import camundala.domain.*
import camundala.worker.*
import camundala.worker.c8zio.C8Worker

import scala.reflect.ClassTag

trait CompanyWorkerHandler extends C7WorkerHandler, C8Worker

trait CompanyValidationWorkerDsl[
    In <: Product: InOutCodec
] extends CompanyWorkerHandler, ValidationWorkerDsl[In]

trait CompanyInitWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    InitIn <: Product: InOutCodec,
    InConfig <: Product: InOutCodec
] extends CompanyWorkerHandler, InitWorkerDsl[In, Out, InitIn, InConfig]

trait CompanyCustomWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
] extends CompanyWorkerHandler, CustomWorkerDsl[In, Out]


trait CompanyServiceWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    ServiceIn: InOutEncoder,
    ServiceOut: InOutDecoder: ClassTag
] extends CompanyWorkerHandler, ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]
