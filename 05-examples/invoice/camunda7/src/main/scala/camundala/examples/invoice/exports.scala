package camundala.examples.invoice

import camundala.domain.*

enum InvoiceCategory:
  case `Travel Expenses`, Misc, `Software License Costs`

object InvoiceCategory:
  given JsonCodec[InvoiceCategory] = deriveEnumCodec
  given ApiSchema[InvoiceCategory] = deriveEnumSchema
end InvoiceCategory
