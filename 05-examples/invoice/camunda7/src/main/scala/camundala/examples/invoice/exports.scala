package camundala.examples.invoice

import camundala.domain.*

enum InvoiceCategory derives ConfiguredEnumCodec:
  case `Travel Expenses`, Misc, `Software License Costs`

object InvoiceCategory:
  given Schema[InvoiceCategory] = Schema.derived
end InvoiceCategory
