package pme123.camundala.config

import pme123.camundala.config.appConfig.{AppConf, CamundaConf, RestHost, ServicesConf}
import zio.test.Assertion.equalTo
import zio.test._

object AppConfigSuites
  extends DefaultRunnableSpec {

  private val expectedConf =
    AppConf(".", ServicesConf("localhost", 8889), CamundaConf(RestHost("http://localhost:10001/rest", "kermit", "kermit")))

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("AppConfigSuites")(
      testM("the Config is correct")(
        assertM(appConfig.get())(
          equalTo(expectedConf))
      ).provideCustomLayer(ConfigLayers.appConfigLayer.mapError(TestFailure.fail))
    )
}
