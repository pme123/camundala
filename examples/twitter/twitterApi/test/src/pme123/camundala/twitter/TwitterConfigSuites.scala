package pme123.camundala.twitter

import twitterConfig.{Token, TwitterAuth}
import zio.test.Assertion.equalTo
import zio.test._

object TwitterConfigSuites
  extends DefaultRunnableSpec {
    private val expectedAuth =
      TwitterAuth(Token("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS"),
        Token("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say"))

    def spec: ZSpec[environment.TestEnvironment, Any] =
      suite("TwitterConfigSuites")(
        testM("the Config is correct") {
          assertM(twitterConfig.auth())(
            equalTo(expectedAuth))
        }.provideCustomLayer(twitterConfig.live.mapError(TestFailure.fail))
      )
  }
