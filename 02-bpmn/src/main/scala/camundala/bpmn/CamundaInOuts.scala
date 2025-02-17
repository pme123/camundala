package camundala
package bpmn

import domain.*

@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class StartProcessIn(
    // use the description of the object
    variables: Map[String, CamundaVariable],
    @description("The business key of the process instance.")
    businessKey: Option[String] = Some("example-businesskey"),
    @description("Set to false will not return the Process Variables.")
    withVariablesInReturn: Boolean = true
)
object StartProcessIn:
  given ApiSchema[StartProcessIn]  = deriveApiSchema
  given InOutCodec[StartProcessIn] = deriveCodec

case class CorrelateMessageIn(
    messageName: String,
    businessKey: Option[String] = None,
    tenantId: Option[String] = None,
    withoutTenantId: Option[Boolean] = None,
    processInstanceId: Option[String] = None,
    correlationKeys: Option[FormVariables] = None,
    localCorrelationKeys: Option[FormVariables] = None,
    processVariables: Option[FormVariables] = None,
    processVariablesLocal: Option[FormVariables] = None,
    all: Boolean = false,
    resultEnabled: Boolean = true,
    variablesInResultEnabled: Boolean = true
)
object CorrelateMessageIn:
  given ApiSchema[CorrelateMessageIn]  = deriveApiSchema
  given InOutCodec[CorrelateMessageIn] = deriveCodec

case class SendSignalIn(
    @description("The name of the signal to deliver.")
    name: String,
    @description(
      """
Specifies a tenant to deliver the signal. The signal can only be received on executions or process definitions which belongs to the given tenant.

Note: Cannot be used in combination with executionId.
"""
    )
    tenantId: Option[String] = None,
    withoutTenantId: Option[Boolean] = None,
    @description("""
Optionally specifies a single execution which is notified by the signal.

Note: If no execution id is defined the signal is broadcasted to all subscribed handlers.
""")
    executionId: Option[String] = None,
    @description(
      """A JSON object containing variable key-value pairs. Each key is a variable name and each value a JSON variable value object."""
    )
    variables: Option[Map[String, CamundaVariable]] = None
)

object SendSignalIn:
  given ApiSchema[SendSignalIn]  = deriveApiSchema
  given InOutCodec[SendSignalIn] = deriveCodec

@description(
  "A JSON object with the following properties:"
)
case class EvaluateDecisionIn(
    // use the description of the object
    variables: Map[String, CamundaVariable]
)
object EvaluateDecisionIn:
  given ApiSchema[EvaluateDecisionIn]  = deriveApiSchema
  given InOutCodec[EvaluateDecisionIn] = deriveCodec

case class CompleteTaskOut(
    @description(
      "The Process Variables - Be aware that returns everything stored in the Process."
    )
    variables: Map[String, CamundaVariable]
)
object CompleteTaskOut:
  given ApiSchema[CompleteTaskOut]  = deriveApiSchema
  given InOutCodec[CompleteTaskOut] = deriveCodec
end CompleteTaskOut
