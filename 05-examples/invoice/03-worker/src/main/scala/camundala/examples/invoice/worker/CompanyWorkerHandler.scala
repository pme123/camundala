package camundala.examples.invoice.worker

import camundala.domain.*
import camundala.camunda7.worker.C7WorkerHandler
import camundala.worker.{CustomWorkerDsl, InitWorkerDsl, ServiceWorkerDsl}

trait CompanyWorkerHandler extends C7WorkerHandler

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
    ServiceIn <: Product: InOutEncoder,
    ServiceOut: InOutDecoder
] extends CompanyWorkerHandler, ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]
