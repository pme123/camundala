package camundala
package api

import camundala.api.Sample.{SampleOut, standardSample}
import camundala.bpmn.{BpmnDsl, BpmnProcessDsl}
import camundala.domain.*

object TestApiCreator extends DefaultApiCreator, BpmnProcessDsl, App:

  lazy val projectName = "TestApi"

  def title = "Test API"

  def version                            = "1.0"
  lazy val companyProjectVersion: String = "0.1.0"
  lazy val projectDescr: String          = ""

  override val apiConfig: ApiConfig =
    ApiConfig("DemoConfig")
      .withBasePath(os.pwd / "api")

  document(
    api(Sample.testProcess)(
      Sample.testUT
    ),
    testProcess2
  )

  private lazy val testProcess2 =
    process(standardSample, SampleOut())

  val processName: String = "sample-process2"

  val descr: String = ""
end TestApiCreator

object Sample extends BpmnProcessDsl:
  val processName = "sample-process"

  @description("My Sample input object to make the point.")
  case class SampleIn(
      @description("Make sure it reflects the name of the Id.")
      firstName: String = "Peter",
      lastName: String = "Pan",
      dateOfBirth: String = "1734-12-04",
      nationality: String = "CH",
      country: String = "CH",
      address: Address = Address()
  )
  object SampleIn:
    given ApiSchema[SampleIn]  = deriveApiSchema
    given InOutCodec[SampleIn] = deriveCodec
  end SampleIn

  case class Address(
      street: String = "Merkurstrasse",
      houseNumber: Option[String] = Some("16"),
      place: String = "Lenzburg",
      postcode: Int = 5600,
      @description("Country as ISO Code")
      country: String = "CH"
  )
  object Address:
    given ApiSchema[Address]  = deriveApiSchema
    given InOutCodec[Address] = deriveCodec
  end Address

  case class SampleOut(
      @description("Indication if this was a success")
      success: Int = 0,
      outputValue: String = "Just some text"
  )
  object SampleOut:
    given ApiSchema[SampleOut]  = deriveApiSchema
    given InOutCodec[SampleOut] = deriveCodec
  end SampleOut

  lazy val standardSample: SampleIn = SampleIn()
  val descr                         =
    s"""This runs the Sample Process.
       |""".stripMargin

  lazy val testProcess =
    process(standardSample, SampleOut())

  lazy val testUT =
    userTask("myUserTask")
end Sample
/*  .startProcessInstance(
        name,
        name,
        descr,
        Map(
          "standard" -> standardSample,
          "other input" -> SampleIn(firstName = "Heidi")
        ),
        Map(
          "standard" -> SampleOut(),
          "other output" -> SampleOut(success = -1)
        )
      )*/
