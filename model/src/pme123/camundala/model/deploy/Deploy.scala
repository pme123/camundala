package pme123.camundala.model.deploy

import pme123.camundala.model.bpmn.Bpmn

case class Deploys(value: Set[Deploy])
/**
  *
  * @param id The Id of the deployment - must be unique in the deployRegistry
  * @param bpmns Bpmns you want to have in this Deployment (send together to Camunda)
  */
case class Deploy(id: String,
                  bpmns: Set[Bpmn])
