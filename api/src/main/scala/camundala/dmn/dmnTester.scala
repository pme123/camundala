package camundala
package dmn

import bpmn.*

case class DmnConfig(
    decisionId: String,
    data: TesterData,
    dmnPath: List[String] = List.empty,
    isActive: Boolean = false,
    testUnit: Boolean = false,
) 

case class TesterData(
    inputs: List[TesterInput],
    // simple input-, output-variables used in the DMN
    variables: List[TesterInput] = List.empty,
)

case class TesterInput(
    key: String,
    nullValue: Boolean,
    values: List[String]
)

implicit lazy val DmnConfigSchema: Schema[DmnConfig] = Schema.derived
implicit lazy val DmnConfigEncoder: Encoder[DmnConfig] = deriveEncoder
implicit lazy val DmnConfigDecoder: Decoder[DmnConfig] = deriveDecoder

implicit lazy val TesterDataSchema: Schema[TesterData] = Schema.derived
implicit lazy val TesterDataEncoder: Encoder[TesterData] = deriveEncoder
implicit lazy val TesterDataDecoder: Decoder[TesterData] = deriveDecoder

implicit lazy val TesterInputSchema: Schema[TesterInput] = Schema.derived
implicit lazy val TesterInputEncoder: Encoder[TesterInput] = deriveEncoder
implicit lazy val TesterInputDecoder: Decoder[TesterInput] = deriveDecoder

