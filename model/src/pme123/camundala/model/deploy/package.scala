package pme123.camundala.model

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.Trimmed
import pme123.camundala.model.bpmn.{IdRegex, ModelException}
import zio.ZIO

package object deploy {

  type DeployId = String Refined IdRegex
  type ProjectName = String Refined IdRegex

  type Url = String Refined string.Url
  type Port = Int Refined numeric.Greater[0]

  type TrimmedNonEmpty = Trimmed And NonEmpty
  //TODO problem in zio-config with And
  type Username = String Refined  TrimmedNonEmpty
  type Password = String Refined  TrimmedNonEmpty

  case class Sensitive(secret: Password) {
    def value: String = secret.value
    override def toString: String = "*" * 10
  }

  def urlFromStr(url: String): ZIO[Any, ModelException, Url] =
    ZIO.fromEither(refineV[string.Url](url))
      .mapError(ex => ModelException(s"Could not create Url $url:\n $ex"))

  def passwordFromStr(password: String): ZIO[Any, ModelException, Password] =
    ZIO.fromEither(refineV[TrimmedNonEmpty](password))
      .mapError(ex => ModelException(s"Could not create Password $password:\n $ex"))

  def usernameFromStr(username: String): ZIO[Any, ModelException, Username] =
    ZIO.fromEither(refineV[TrimmedNonEmpty](username))
      .mapError(ex => ModelException(s"Could not create Username $username:\n $ex"))

}
