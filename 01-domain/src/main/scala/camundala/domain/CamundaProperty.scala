package camundala.domain

import io.circe.HCursor

@description(
  """A JSON object containing a property for each variable returned. The key is the variable name,
    |the value is a JSON object with the following properties:
    |```
    |{
    |  "amount": {
    |    "type": "Double",
    |    "value": 300.0,
    |    "valueInfo": {}
    |  }
    |}
    |```
    |""".stripMargin
)
type FormVariables = Map[String, CamundaVariable]

@description(
  """Output for /history/variable-instance?processInstanceIdIn=#{processInstanceId}
    |```
    |  {
    |    "type": "Boolean",
    |    "value": true,
    |    "valueInfo": {},
    |    "id": "0f99b629-6b9a-11ec-8318-6a9c8e2a273d",
    |    "name": "clarified",
    |    "processDefinitionKey": "ReviewInvoiceProcess",
    |    "processDefinitionId": "ReviewInvoiceProcess:6:88718f1d-6297-11ec-8d87-6a9c8e2a273d",
    |    "processInstanceId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d",
    |    "executionId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d",
    |    "activityInstanceId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d",
    |    "caseDefinitionKey": null,
    |    "caseDefinitionId": null,
    |    "caseInstanceId": null,
    |    "caseExecutionId": null,
    |    "taskId": null,
    |    "errorMessage": null,
    |    "tenantId": null,
    |    "state": "CREATED",
    |    "createTime": "2022-01-02T08:03:17.251+0100",
    |    "removalTime": null,
    |    "rootProcessInstanceId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d"
    |  }
    |```
    |""".stripMargin
)
case class CamundaProperty(
    key: String,
    value: CamundaVariable
)

object CamundaProperty:

  given InOutDecoder[CamundaProperty] =
    (c: HCursor) =>
      for
        name <- c.downField("name").as[String]
        valueType <- c.downField("type").as[String]
        anyValue = c.downField("value")
        value <- CamundaVariable.decodeValue(
          valueType,
          anyValue,
          c.downField("valueInfo")
        )
      yield new CamundaProperty(name, value)

  def from(vars: FormVariables): Seq[CamundaProperty] =
    vars.map { case k -> c =>
      CamundaProperty(k, c)
    }.toSeq

end CamundaProperty
