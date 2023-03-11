package camundala.examples.invoice

import camundala.domain.*

object Beverages:
  case class In(InputClause_1acmlkd: String, InputClause_0bo3uen: String)
  object In:
    given Schema[In] = Schema.derived
    given Encoder[In] = deriveEncoder
    given Decoder[In] = deriveDecoder
  end In
  case class Out()
  object Out:
    given Schema[Out] = Schema.derived
    given Encoder[Out] = deriveEncoder
    given Decoder[Out] = deriveDecoder
  end Out
end Beverages


object DesiredDish:
  case class In(inputClause_0bbq1z8:String, inputClause_0pcbpc9:String)
  object In:
    given Schema[In] = Schema.derived
    given Encoder[In] = deriveEncoder
    given Decoder[In] = deriveDecoder
  end In
  case class Out()
  object Out:
    given Schema[Out] = Schema.derived
    given Encoder[Out] = deriveEncoder
    given Decoder[Out] = deriveDecoder
  end Out
end DesiredDish