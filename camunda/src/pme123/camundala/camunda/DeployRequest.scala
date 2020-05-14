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
  val DEPLOYMENT_NAME = "deployment-name"
  val ENABLE_DUPLICATE_FILTERING = "enable-duplicate-filtering"
  val DEPLOY_CHANGED_ONLY = "deploy-changed-only"
  val DEPLOYMENT_SOURCE = "deployment-source"
  val TENANT_ID = "tenant-id"

  val RESERVED_KEYWORDS = Set(
    DEPLOYMENT_NAME,
    ENABLE_DUPLICATE_FILTERING,
    DEPLOY_CHANGED_ONLY,
    DEPLOYMENT_SOURCE,
    TENANT_ID
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
