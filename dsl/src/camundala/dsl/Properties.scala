package camundala.dsl

// Camunda Extension
case class Property(key: Identifier, value: String)

case class Properties(properties: Seq[Property] = Seq.empty) {
}

object Properties {
  val none: Properties = Properties()
}
