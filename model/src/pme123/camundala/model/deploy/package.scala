package pme123.camundala.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined._
import eu.timepit.refined.boolean.And
import eu.timepit.refined.string.Trimmed
import pme123.camundala.model.bpmn.IdRegex

package object deploy {

  type DeployId = String Refined IdRegex
  type ProjectName = String Refined IdRegex

  type Url = String Refined string.Url
  type Port = Int Refined numeric.Greater[0]

  type TrimmedNonEmpty = Trimmed And collection.NonEmpty
  type Username = String Refined TrimmedNonEmpty
  type Password = String Refined TrimmedNonEmpty

  case class Sensitive(secret: Password) {
    def value: String = secret.value
    override def toString: String = "*" * 10
  }


}
