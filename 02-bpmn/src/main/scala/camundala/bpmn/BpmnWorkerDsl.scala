package camundala.bpmn

import camundala.domain.*

trait BpmnWorkerDsl extends BpmnDsl:

  def topicName: String
      
trait BpmnCustomWorkerDsl extends BpmnWorkerDsl:
  def customTask[
    In <: Product : InOutEncoder : InOutDecoder : Schema,
    Out <: Product : InOutEncoder : InOutDecoder : Schema
  ](
     in: In = NoInput(),
     out: Out = NoOutput(),
   ): CustomTask[In, Out] =
    CustomTask(
      InOutDescr(topicName, in, out, Some(descr))
    )
    
trait BpmnServiceWorkerDsl extends BpmnWorkerDsl:

  def path: String
  def serviceLabel: String
  def serviceVersion: String

  def serviceTask[
      In <: Product: InOutCodec: ApiSchema,
      Out <: Product: InOutCodec: ApiSchema,
      ServiceIn: InOutEncoder: InOutDecoder,
      ServiceOut: InOutEncoder: InOutDecoder
  ](
      in: In,
      out: Out,
      defaultServiceOutMock: MockedServiceResponse[ServiceOut],
      serviceInExample: ServiceIn,
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    ServiceTask(
      InOutDescr(topicName, in, out, Some(description(serviceInExample, defaultServiceOutMock))),
      defaultServiceOutMock,
      serviceInExample
    )

  private def description[ServiceIn: InOutEncoder, ServiceOut: InOutEncoder](
      serviceInExample: ServiceIn,
      defaultServiceOutMock: MockedServiceResponse[ServiceOut]
  ): String =
    s"""|$descr
        |
        |---
        |
        |$companyDescr
        |
        |<details>
        |<summary><b>Wrapped Service:</b>
        |
        |($serviceLabel - v$serviceVersion - $path)
        |</summary>
        |<p>
        |Input Body: `${serviceInExample.getClass.getName}`:
        |${
         if serviceInExample.isInstanceOf[NoInput] then ""
         else
           s"""
              |```json
              |${serviceInExample.asJson}
              |```
              |""".stripMargin
       }
        |
        |Example Response: `${defaultServiceOutMock.unsafeBody.getClass.getName}`:
        |
        |```json
        |${defaultServiceOutMock.asJson}
        |```
        |</p>
        |</details>
        |</p>
        |
        |---
        |""".stripMargin
end BpmnServiceWorkerDsl


