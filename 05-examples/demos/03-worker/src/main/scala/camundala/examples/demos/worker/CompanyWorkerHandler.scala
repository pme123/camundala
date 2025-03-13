package camundala.examples.demos.worker

import camundala.domain.*
import camundala.camunda7.worker.C7WorkerHandler
import camundala.worker.{CustomWorkerDsl, InitWorkerDsl, ServiceWorkerDsl}

trait CompanyWorkerHandler[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
] extends C7WorkerHandler[In, Out]

trait CompanyInitWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    InitIn <: Product: InOutEncoder,
    InConfig <: Product: InOutCodec
] extends CompanyWorkerHandler[In, Out], InitWorkerDsl[In, Out, InitIn, InConfig]

trait CompanyCustomWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
] extends CompanyWorkerHandler[In, Out], CustomWorkerDsl[In, Out]

trait CompanyServiceWorkerDsl[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    ServiceIn <: Product: InOutEncoder,
    ServiceOut: InOutDecoder
] extends CompanyWorkerHandler[In, Out], ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]
