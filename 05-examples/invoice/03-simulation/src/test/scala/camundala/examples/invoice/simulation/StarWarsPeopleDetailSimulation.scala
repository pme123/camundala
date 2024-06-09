package camundala.examples.invoice
package simulation

import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.bpmn.{ErrorCodes, InputParams}
import camundala.domain.MockedServiceResponse
import camundala.examples.invoice.bpmn.*
import camundala.examples.invoice.bpmn.StarWarsPeopleDetail.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceSimulation/test
// exampleInvoiceSimulation/testOnly *StarWarsPeopleDetailSimulation
class StarWarsPeopleDetailSimulation extends CustomSimulation:

  simulate(
    scenario(
      `Star Wars Api People Detail real`
    ),
    incidentScenario(
      `Star Wars Api People Detail real failed`,
      """"detail":"Not found""""
    ),
    serviceScenario(
      `Star Wars Api People Detail`,
      Out.Success(People("Pascal Starrider")),
      People("Pascal Starrider"),
      Map("fromHeader" -> "okidoki")
    ),
    scenario(
      `Star Wars Api People Detail defaultMock 2`
    ),
    scenario(
      `Star Wars Api People Detail outputMock 2`
    ),
    scenario(
      `Star Wars Api People Detail outputServiceMock 2`
    ),
    scenario(`Star Wars Api People Detail outputServiceMock handled 404`),
    incidentScenario(
      `Star Wars Api People Detail outputServiceMock failure`,
      "People Not found"
    ),
    scenario(`Star Wars Api People Detail outputMock handled`),
    scenario(`Star Wars Api People Detail outputServiceMock handled - not possible`),
    scenario(`Star Wars Api People Detail outputServiceMock handled 400`),
    scenario(`Star Wars Api People Detail validation-failed handled`)
  )

  override implicit def config =
    super.config
      .withPort(8034)
    //  .withLogLevel(LogLevel.DEBUG)

  private lazy val `Star Wars Api People Detail` = example

  private lazy val `Star Wars Api People Detail real` = example
    .withOut(Out.Success(fromHeader = "---")) // no header
  private lazy val `Star Wars Api People Detail real failed` = example
    .withIn(In(90923, Some("skywalker")))
  private lazy val `Star Wars Api People Detail defaultMock 2` =
    StarWarsPeopleDetail.example.mockServicesWithDefault
  private lazy val `Star Wars Api People Detail outputMock 2` =
    StarWarsPeopleDetail.example
      .mockWith(Out.Success(People("Pascal Starrider")))
      .withOut(Out.Success(People("Pascal Starrider")))
  private lazy val `Star Wars Api People Detail outputMock handled` =
    StarWarsPeopleDetail.example
      .handleError(ErrorCodes.`output-mocked`)
      .mockWith(Out.Success())
      .withOut(Out.Failure(ProcessStatus.`output-mocked`))

  private lazy val `Star Wars Api People Detail outputServiceMock 2` =
    StarWarsPeopleDetail.example
      .mockServiceWith(
        MockedServiceResponse
          .success200(People("Peter Starrider"))
          .withHeader("fromHeader", "okidoki")
      )
      .withOut(Out.Success(People("Peter Starrider")))

  // it can not be handled - so if you want to handle mocking, you need to take outputMock:
  // - see `Star Wars Api People Detail outputMock handled`
  private lazy val `Star Wars Api People Detail outputServiceMock handled - not possible` =
    StarWarsPeopleDetail.example
      .handleError(ErrorCodes.`output-mocked`)
      .mockServiceWith(
        MockedServiceResponse
          .success200(People("Peter Starrider"))
          .withHeader("fromHeader", "okidoki")
      )
      .withOut(Out.Success(People("Peter Starrider")))

  private lazy val `Star Wars Api People Detail validation-failed handled` =
    StarWarsPeopleDetail.example
      .handleError(ErrorCodes.`validation-failed`)
      .withIn(In(-12))
      .withOut(Out.Failure(ProcessStatus.`validation-failed`))

  private lazy val `Star Wars Api People Detail outputServiceMock failure` =
    StarWarsPeopleDetail.example.mockServicesWithDefault
      .mockServiceWith(MockedServiceResponse.error(404, Json.fromString("People Not found")))

  private lazy val `Star Wars Api People Detail outputServiceMock handled 404` =
    StarWarsPeopleDetail.example
      .handleError(404)
      .withOut(Out.Failure())
      .mockServiceWith(MockedServiceResponse.error(404))

  private lazy val `Star Wars Api People Detail outputServiceMock handled 400` =
    StarWarsPeopleDetail.example
      .handleError(400)
      .withOut(Out.Failure(ProcessStatus.`400`))
      .mockServiceWith(MockedServiceResponse.error(400))

end StarWarsPeopleDetailSimulation
