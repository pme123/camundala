package pme123.camundala.camunda

import pme123.camundala.camunda.xml.ValidateWarnings
import pme123.camundala.model.bpmn.{BpmnId, CamundalaException, FileName}

case class DeployRequest(bpmnId: BpmnId,
                         enableDuplicateFilterung: Boolean = false,
                         deployChangedOnly: Boolean = false,
                         source: Option[String] = None,
                         tenantId: Option[String] = None,
                         deployFiles: Set[DeployFile] = Set.empty)

object DeployRequest {
  val DeploymentName = "deployment-name"
  val EnableDuplicateFiltering = "enable-duplicate-filtering"
  val DeployChangedOnly = "deploy-changed-only"
  val DeploymentSource = "deployment-source"
  val tenantId = "tenant-id"

  val ReservedKeywords = Set(
    DeploymentName,
    EnableDuplicateFiltering,
    DeployChangedOnly,
    DeploymentSource,
    tenantId
  )
}

case class DeployFile(filename: FileName, file: Vector[Byte])

case class DeployResult(id: String,
                        name: String,
                        deploymentTime: String,
                        source: Option[String] = None,
                        tenantId: Option[String] = None,
                        validateWarnings: ValidateWarnings = ValidateWarnings.none
                       )

case class DeploymentException(msg: String) extends CamundalaException
