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

given Schema[DmnConfig] = Schema.derived
given Encoder[DmnConfig] = deriveEncoder
given Decoder[DmnConfig] = deriveDecoder

given Schema[TesterData] = Schema.derived
given Encoder[TesterData] = deriveEncoder
given Decoder[TesterData] = deriveDecoder

given Schema[TesterInput] = Schema.derived
given Encoder[TesterInput] = deriveEncoder
given Decoder[TesterInput] = deriveDecoder

