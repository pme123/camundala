package camundala

import camundala.dsl.{IdRegex, IdentifiableNode, Identifier}
import eu.timepit.refined.refineV
import zio.ZIO

package object tools {

  def identifierFromStr(str: String): ZIO[Any, DslException, Identifier] =
    ZIO
      .fromEither(refineV[IdRegex](str.split("/").last))
      .mapError(ex => DslException(s"'$str' is not a valid Identifier.\n $ex"))

  def asList(commaSeparatedString: String): Seq[String] =
    commaSeparatedString.split(",").toList.map(_.trim).filter(_.nonEmpty)

  def toMap[A <: IdentifiableNode](
                                            processNodes: Seq[A]
                                          ): Map[Identifier, A] =
    processNodes.map(pn => pn.id -> pn).toMap

}
