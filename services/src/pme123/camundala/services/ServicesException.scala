package pme123.camundala.services

import pme123.camundala.model.bpmn.CamundalaException

sealed trait ServicesException
  extends CamundalaException

case class InvalidRequestException(msg: String)
  extends ServicesException


