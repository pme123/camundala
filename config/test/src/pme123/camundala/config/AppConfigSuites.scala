package pme123.camundala.config

import pme123.camundala.config.appConfig.{AppConf, ServicesConf}
import zio.test.Assertion.equalTo
import zio.test._

object AppConfigSuites
  extends DefaultRunnableSpec {

  private val expectedConf =
    AppConf(ServicesConf(8889))

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("AppConfigSuites")(
      testM("the Config is correct") {
        assertM(appConfig.get())(
          equalTo(expectedConf))
      }.provideCustomLayer(appConfig.live.mapError(TestFailure.fail))
    )
}
