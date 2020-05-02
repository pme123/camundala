package pme123.camundala.model.deploy

import pme123.camundala.model.bpmn.Bpmn

case class Deploy(id: String, bpmns: Set[Bpmn])
