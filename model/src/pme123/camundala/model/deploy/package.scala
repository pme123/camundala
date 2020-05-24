package pme123.camundala.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined._
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.Trimmed
import pme123.camundala.model.bpmn.{IdRegex, ModelException, PropKey}
import zio.ZIO

package object deploy {

  type DeployId = String Refined IdRegex
  type ProjectName = String Refined IdRegex

  type Url = String Refined string.Url
  type Port = Int Refined numeric.Greater[0]

  type TrimmedNonEmpty = Trimmed And NonEmpty
  //TODO problem in zio-config with And
  type Username = String Refined NonEmpty// TrimmedNonEmpty
  type Password = String Refined NonEmpty// TrimmedNonEmpty

  case class Sensitive(secret: Refined[String, NonEmpty]) {
    def value: String = secret.value
    override def toString: String = "*" * 10
  }

  def urlFromStr(url: String): ZIO[Any, ModelException, Url] =
    ZIO.fromEither(refineV[string.Url](url))
      .mapError(ex => ModelException(s"Could not create Url $url:\n $ex"))

}
