package camundala.domain

import sttp.tapir.Schema

import scala.deriving.Mirror

// Tapir
export sttp.tapir.Schema.annotations.description
type ApiSchema[T] = Schema[T]

inline def deriveApiSchema[T](using
    m: Mirror.Of[T]
): ApiSchema[T] =
  Schema.derived[T]

inline def deriveEnumApiSchema[T](using
    m: Mirror.SumOf[T]
): ApiSchema[T] =
  Schema.derivedEnumeration[T].defaultStringBased
