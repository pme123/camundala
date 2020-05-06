package pme123.camundala.model.deploy

import pme123.camundala.model.bpmn.Bpmn

/**
  *
  * @param id The Id of the deployment - must be unique in the deployRegistry
  * @param project The name of the project this Deploy is defined.
  *                This is the Mill project name, e.g. examples.twitter
  * @param bpmns Bpmns you want to have in this Deployment (send together to Camunda)
  */
case class Deploy(id: String,
                  project: String,
                  source: String,
                  bpmns: Set[Bpmn])
