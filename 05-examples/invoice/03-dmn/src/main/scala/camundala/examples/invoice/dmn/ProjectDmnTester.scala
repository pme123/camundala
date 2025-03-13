package camundala
package examples.invoice
package dmn

import camundala.bpmn.*
import camundala.domain.*
import camundala.dmn.*
import bpmn.InvoiceReceipt.{InvoiceAssignApproverDMN, InvoiceAssignApproverDmnUnit}

object ProjectDmnTester
    extends DmnTesterConfigCreator:
  val processName = "NOT USED"
  val descr       = ""

  private lazy val localDmnConfigPath: os.Path =
    os.pwd / "05-examples" / "invoice" / "03-dmn" / "src" / "main" / "resources" / "dmnConfigs"

  private lazy val localDmnPath =
    os.pwd / "05-examples" / "invoice" / "04-c7-spring" / "src" / "main" / "resources"

  override def starterConfig: DmnTesterStarterConfig =
    DmnTesterStarterConfig(
      companyName = "camundala",
      dmnPaths = Seq(localDmnPath),
      dmnConfigPaths = Seq(localDmnConfigPath)
    )

  createDmnConfigs(
    InvoiceAssignApproverDMN.example
      .dmnPath("example-invoice-c7-assignApprover")
      .testValues(_.amount, 249, 250, 999, 1000, 1001),
    // for demonstration - created unit test - acceptMissingRules just for demo
    InvoiceAssignApproverDmnUnit.example
      .acceptMissingRules
      .testUnit
      .dmnPath("example-invoice-c7-assignApprover")
    //  .inTestMode
  )

end ProjectDmnTester
