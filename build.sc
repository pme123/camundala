import coursier.maven.MavenRepository
import mill._
import mill.define.BasePath
import mill.scalalib._

object Version {
  val scalaVersion = "2.13.1"

  // model
  val scalaXml = "1.3.0"
  val zio = "1.0.0-RC18-2"//+99-bb2ded5f-SNAPSHOT"//+119-559be413-SNAPSHOT"
  val zioMacros = "1.0.0-RC18-2+99-bb2ded5f-SNAPSHOT"
  val zioLogging = "0.2.7"

  // config
  val zioConfig = "1.0.0-RC16"

  // services
  val zioCats = "2.0.0.0-RC12"
  val http4s = "0.21.3"
  val circe = "0.13.0"

  // camunda
  val spring = "2.2.4.RELEASE"
  val camundaSpringBoot = "3.4.2"
  val h2 = "1.4.200"
  val postgres = "42.2.8"

  // example apps
  // twitter
  val twitter4s = "6.2"


}

object Libs {
  // model
  val scalaXml = ivy"org.scala-lang.modules::scala-xml:${Version.scalaXml}"
  val zio = ivy"dev.zio::zio:${Version.zio}"
  val zioLogging = ivy"dev.zio::zio-logging:${Version.zioLogging}"

  // config
  val zioConfig = ivy"dev.zio::zio-config:${Version.zioConfig}"
  val zioConfigTypesafe = ivy"dev.zio::zio-config-typesafe:${Version.zioConfig}"
  val zioMacros = ivy"dev.zio::zio-macros:${Version.zioMacros}"

  // camunda
  val spring = ivy"org.springframework.boot:spring-boot-starter-web:${Version.spring}"
  val springJdbc = ivy"org.springframework.boot:spring-boot-starter-jdbc:${Version.spring}"
  val camundaWeb = ivy"org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:${Version.camundaSpringBoot}"
  val camundaRest = ivy"org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest:${Version.camundaSpringBoot}"
  val h2 = ivy"com.h2database:h2:${Version.h2}"
  val postgres = ivy"org.postgresql:postgresql:${Version.postgres}"

  // services
  val zioCats = ivy"dev.zio::zio-interop-cats:${Version.zioCats}"
  val http4sBlazeServer =
    ivy"org.http4s::http4s-blaze-server:${Version.http4s}"
  val http4sBlazeClient =
    ivy"org.http4s::http4s-blaze-client:${Version.http4s}"
  val http4sCirce = ivy"org.http4s::http4s-circe:${Version.http4s}"
  val http4sDsl = ivy"org.http4s::http4s-dsl:${Version.http4s}"
  val circe = ivy"io.circe::circe-generic:${Version.circe}"

  // examples
  // twitter
  val twitter4s = ivy"com.danielasfregola::twitter4s:${Version.twitter4s}"

  // test
  val zioTest = ivy"dev.zio::zio-test:${Version.zio}"
  val zioTestJunit = ivy"dev.zio::zio-test-junit:${Version.zio}"
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
    "-Ymacro-annotations", // ZIO Macros: add the macro annotation compiler options.
    "-language:higherKinds", // Allow higher-kinded types
    "-language:postfixOps", // Allows operator syntax in postfix position (deprecated since Scala 2.10)
    "-feature" // Emit warning and location for usages of features that should be imported explicitly.
    //  "-Ypartial-unification",      // Enable partial unification in type constructor inference
    //  "-Xfatal-warnings"            // Fail the compilation if there are any warnings
  )

}

trait MyModuleWithTests extends MyModule {

  // needed for ZIO nightly
  override def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )

  object test extends Tests {
    override def moduleDeps = super.moduleDeps

    override def ivyDeps = Agg(
      Libs.zioTest,
      Libs.zioTestJunit,
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

  override def ivyDeps = {
    Agg(
      Libs.zio,
      Libs.zioMacros,
      Libs.scalaXml,
      Libs.zioLogging
    )
  }
}

object config extends MyModuleWithTests {

  override def ivyDeps = {
    Agg(
      Libs.zio,
      Libs.zioConfig,
      Libs.zioConfigTypesafe,
      Libs.zioLogging
    )
  }
}

object camunda extends MyModuleWithTests {

  override def moduleDeps = Seq(model, config)

  override def ivyDeps = {
    Agg(
      Libs.spring,
      Libs.springJdbc,
      Libs.camundaWeb,
      Libs.camundaRest,
      Libs.h2,
      Libs.postgres
    )
  }
}

object services extends MyModuleWithTests {

  override def moduleDeps = Seq(config, camunda, model)

  override def ivyDeps = {
    Agg(
      Libs.circe,
      Libs.http4sBlazeServer,
      Libs.http4sBlazeClient,
      Libs.http4sDsl,
      Libs.http4sCirce,
      Libs.zioCats
    )
  }
}

object examples extends mill.Module {


  object twitter extends MyModuleWithTests {

    override def moduleDeps = Seq(camunda, services, twitterApi)

    override def mainClass = Some("pme123.camundala.examples.twitter.TwitterApp")

    object twitterApi extends MyModuleWithTests {

      override def ivyDeps = {
        Agg(
          Libs.twitter4s,
          Libs.zioConfig,
          Libs.zioConfigTypesafe
        )
      }
    }

  }

  object rest extends MyModuleWithTests {

    override def moduleDeps = Seq(camunda)

    override def mainClass = Some("pme123.camundala.examples.rest.RestApp")

  }

}
