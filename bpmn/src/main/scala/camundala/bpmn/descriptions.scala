package camundala
package bpmn

import domain.*

// Experiments with Descriptions
case class ProcessInDescr[In <: Product: CirceCodec, Out <: Product: CirceCodec](
    @description(handledErrorsDescr)
    handledErrors: Option[String] = Some("400,404"),
    @description(regexHandledErrorsDescr)
    regexHandledErrors: Option[Seq[String]] = None,
    outputMock: Option[Out] = None,
    @description(servicesMockedDescr)
    servicesMocked: Boolean = false
)
case class ServiceInDescr[
    InS <: Product: CirceCodec,
    ServiceOut <: Product: CirceCodec
](
    // @description(serviceNameDescr(serviceName))
    serviceName: String,
    defaultServiceMock: ServiceOut,
    outputServiceMock: Option[MockedServiceResponse[ServiceOut]]
)
