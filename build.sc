import mill._
import mill.define.BasePath
import mill.scalalib._

object Version {
  val scalaVersion = "2.13.1"

  val spring = "2.2.4.RELEASE"
  val camundaSpringBoot = "3.3.7"
  val h2 = "1.4.200"
  val postgres = "42.2.8"

  val twitter4s = "6.2"
  val zio = "1.0.0-RC18-2"
  val zioConfig = "1.0.0-RC16"
}

object Libs {
  val spring = ivy"org.springframework.boot:spring-boot-starter-web:${Version.spring}"
  val springJdbc = ivy"org.springframework.boot:spring-boot-starter-jdbc:${Version.spring}"
  val camunda = ivy"org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:${Version.camundaSpringBoot}"
  val h2 = ivy"com.h2database:h2:${Version.h2}"
  val postgres = ivy"org.postgresql:postgresql:${Version.postgres}"

  val twitter4s = ivy"com.danielasfregola::twitter4s:${Version.twitter4s}"

  val zio = ivy"dev.zio::zio:${Version.zio}"
  val zioConfig = ivy"dev.zio::zio-config:${Version.zioConfig}"
  val zioConfigTypesafe = ivy"dev.zio::zio-config-typesafe:${Version.zioConfig}"

  val zioTest = ivy"dev.zio::zio-test:${Version.zio}"
  val zioTestSbt = ivy"dev.zio::zio-test-sbt:${Version.zio}"
}

trait MyModule extends ScalaModule {
  val scalaVersion = Version.scalaVersion


  override def scalacOptions =
    defaultScalaOpts

  val defaultScalaOpts = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8", // Specify character encoding used by source files.
    "-language:higherKinds", // Allow higher-kinded types
    "-language:postfixOps", // Allows operator syntax in postfix position (deprecated since Scala 2.10)
    "-feature" // Emit warning and location for usages of features that should be imported explicitly.
    //  "-Ypartial-unification",      // Enable partial unification in type constructor inference
    //  "-Xfatal-warnings"            // Fail the compilation if there are any warnings
  )

}

trait MyModuleWithTests extends MyModule {

  object test extends Tests {
    override def moduleDeps = super.moduleDeps

    override def ivyDeps = Agg(
      Libs.zioTest,
      Libs.zioTestSbt
    )

    def testOne(args: String*) = T.command {
      super.runMain("org.scalatest.run", args: _*)
    }

    def testFrameworks =
      Seq("zio.test.sbt.ZTestFramework")
  }

}
object model extends MyModuleWithTests {

}

object camunda extends MyModuleWithTests {

  override def moduleDeps = Seq(model)

  override def ivyDeps = {
    Agg(
      Libs.spring,
      Libs.springJdbc,
      Libs.camunda,
      Libs.h2
      // Libs.postgres,
    )
  }
}

object examples extends mill.Module {


  object twitter extends MyModuleWithTests {

    override def moduleDeps = Seq(camunda, twitterApi)

    override def mainClass = Some("pme123.camundala.examples.twitter.TwitterApp")

    object twitterApi extends MyModuleWithTests {

      override def ivyDeps = {
        Agg(
          Libs.twitter4s,
          Libs.zio,
          Libs.zioConfig,
          Libs.zioConfigTypesafe
        )
      }
    }

  }

}
