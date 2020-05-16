package pme123.camundala.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined.{numeric, string}
import pme123.camundala.model.bpmn.IdRegex

package object deploy {

   type DeployId = String Refined IdRegex
   type ProjectName = String Refined IdRegex

   type Url = String Refined string.Url
   type Port = Int Refined numeric.Greater[0]
}
