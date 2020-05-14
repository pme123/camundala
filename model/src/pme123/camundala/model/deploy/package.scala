package pme123.camundala.model

import eu.timepit.refined.api.Refined
import pme123.camundala.model.bpmn.IdRegex

package object deploy {

   type DeployId = String Refined IdRegex
}
