package camundala.bpmn

import camundala.domain.*

trait BpmnWorkerDsl extends BpmnDsl:

  def descr: String
  def path: String
  def serviceLabel: String
  def serviceVersion: String
  def additionalText: String = ""
  def topicName: String

  def serviceTaskExample[
      In <: Product: InOutCodec: ApiSchema,
      Out <: Product: InOutCodec: ApiSchema,
      ServiceIn: InOutEncoder: InOutDecoder,
      ServiceOut: InOutEncoder: InOutDecoder
  ](
      in: In,
      out: Out,
      defaultServiceOutMock: MockedServiceResponse[ServiceOut],
      serviceInExample: ServiceIn
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    serviceTask(
      topicName,
      in,
      out,
      defaultServiceOutMock,
      serviceInExample,
      description(serviceInExample, defaultServiceOutMock)
    )

  private def description[ServiceIn: InOutEncoder, ServiceOut: InOutEncoder](
      serviceInExample: ServiceIn,
      defaultServiceOutMock: MockedServiceResponse[ServiceOut]
  ): String =
    s"""|$descr
        |
        |---
        |
        |$additionalText
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
end BpmnWorkerDsl
object BpmnWorkerDsl