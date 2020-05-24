package pme123.camundala.config

import eu.timepit.refined.auto._
import pme123.camundala.config.appConfig.{AppConf, CamundaConf, ServicesConf}
import pme123.camundala.model.deploy.{CamundaEndpoint, Sensitive}
import zio.test.Assertion.equalTo
import zio.test._

object AppConfigSuites
  extends DefaultRunnableSpec {

  private val expectedConf =
    AppConf(".", ServicesConf("localhost", 8889))//, CamundaConf(CamundaEndpoint("http://localhost:10001/rest", "kermit", Sensitive("kermit"))))

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("AppConfigSuites")(
      testM("the Config is correct")(
        assertM(appConfig.get())(
          equalTo(expectedConf))
      ).provideCustomLayer(ConfigLayers.appConfigLayer.mapError(TestFailure.fail))
    )
}
