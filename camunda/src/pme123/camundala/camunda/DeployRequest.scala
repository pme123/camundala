package pme123.camundala.camunda

import eu.timepit.refined.auto._
import pme123.camundala.camunda.xml.ValidateWarnings
import pme123.camundala.model.bpmn.{BpmnId, CamundalaException, FilePath, PropKey}

case class DeployRequest(bpmnId: BpmnId,
                         enableDuplicateFilterung: Boolean = false,
                         deployChangedOnly: Boolean = false,
                         source: Option[String] = None,
                         tenantId: Option[String] = None,
                         deployFiles: Set[DeployFile] = Set.empty)

object DeployRequest {
  val DeploymentName: PropKey = "deployment-name"
  val EnableDuplicateFiltering: PropKey = "enable-duplicate-filtering"
  val DeployChangedOnly: PropKey = "deploy-changed-only"
  val DeploymentSource: PropKey = "deployment-source"
  val tenantId: PropKey = "tenant-id"

  val ReservedKeywords = Set(
    DeploymentName.value,
    EnableDuplicateFiltering.value,
    DeployChangedOnly.value,
    DeploymentSource.value,
    tenantId.value
  )
}

case class DeployFile(filePath: FilePath, file: Vector[Byte])

case class DeployResult(id: String,
                        name: Option[String],
                        deploymentTime: String,
                        source: Option[String] = None,
                        tenantId: Option[String] = None,
                        validateWarnings: ValidateWarnings = ValidateWarnings.none
                       ) {
  def withWarnings(validateWarnings: ValidateWarnings): DeployResult =
    copy(validateWarnings = validateWarnings)
}

case class DeploymentException(msg: String) extends CamundalaException
