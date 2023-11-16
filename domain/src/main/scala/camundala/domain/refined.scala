package camundala.domain

import io.github.iltotore.iron.constraint.all.*

type DayInIsoFormat = DescribedAs[
  Match["""^([0-9]{4})-?(1[0-2]|0[1-9])-?(3[01]|0[1-9]|[12][0-9])$"""],
  "This must be a correct Day in ISO Format, like `yyyy-MM-dd` for example `2023-12-23`"
]
val dayInIsoFormatDescr = "Day in ISO Format, like `yyyy-MM-dd` for example `2003-12-23`"
