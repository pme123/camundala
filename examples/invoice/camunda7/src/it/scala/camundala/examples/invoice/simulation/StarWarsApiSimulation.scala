package camundala.examples.invoice
package simulation

import StarWarsRestApi.*
import camundala.domain.MockedServiceResponse
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceC7/It/testOnly *StarWarsApiSimulation
class StarWarsApiSimulation extends CustomSimulation:

  simulate(
    scenario(
      `Star Wars Api People Detail`
    ),
    scenario(
      `Star Wars Api People Detail defaultMock`
    ),
    scenario(
      `Star Wars Api People Detail outputMock`
    ),
    scenario(
      `Star Wars Api People Detail outputServiceMock`
    )
  )

  override implicit def config =
    super.config
      .withPort(8034)
  //.withLogLevel(LogLevel.DEBUG)

  private lazy val `Star Wars Api People Detail` = example
  private lazy val `Star Wars Api People Detail defaultMock` =
    StarWarsRestApi.example
      .mockServices
  private lazy val `Star Wars Api People Detail outputMock` =
    StarWarsRestApi.example
      .mockWith(Out(People("Pascal Starrider")))
      .withOut(Out(People("Pascal Starrider")))
  private lazy val `Star Wars Api People Detail outputServiceMock` =
    StarWarsRestApi.example
      .mockServiceWith(MockedServiceResponse.success200(People("Pascal Starrider")))
      .withOut(Out(People("Pascal Starrider")))


end StarWarsApiSimulation
