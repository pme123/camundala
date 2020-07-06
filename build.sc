import coursier.maven.MavenRepository
import mill._
import mill.scalalib._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import $ivy.`com.lihaoyi::mill-contrib-buildinfo:$MILL_VERSION`
import mill.api.Loose
import mill.contrib.buildinfo.BuildInfo
import mill.define.Target

import scala.io.Source

object Version {
  val versionSource = Source.fromFile("./version")

  val projectVersion = versionSource.getLines().next()

  val scalaVersion = "2.13.2"

  // model
  val zio = "1.0.0-RC20" //+99-bb2ded5f-SNAPSHOT"//+119-559be413-SNAPSHOT" //"
  //val zioMacros = "1.0.0-RC18-2+99-bb2ded5f-SNAPSHOT"//"
  val zioLogging = "0.3.0"
  val refined = "0.9.14"
  val quicklens = "1.6.0"

  // config
  val zioConfig = "1.0.0-RC20"

  // services
  val zioCats = "2.1.3.0-RC15"
  val http4s = "0.21.3"
  val circe = "0.13.0"
  val sttp = "2.0.6"
  val tapir = "0.15.3"

  // camunda
  val spring = "2.2.4.RELEASE"
  val camunda = "7.12.0"
  val camundaSpringBoot = "3.4.2"
  val camundaSpinJackson = "1.9.0"
  val camundaAssert = "5.0.0"
  val assertJ = "3.13.2"

  val h2 = "1.4.200"
  val postgres = "42.2.8"
  val scalaXml = "1.3.0"
  val groovy = "3.0.3"

  // cli
  val decline = "1.2.0"

  // example apps
  // twitter
  val twitter4s = "6.2"

}

object Libs {
  // model
  val zio = ivy"dev.zio::zio:${Version.zio}"
  val zioLogging = ivy"dev.zio::zio-logging-slf4j:${Version.zioLogging}"
  val refinded = ivy"eu.timepit::refined:${Version.refined}"
  // val quicklens = ivy"com.softwaremill.quicklens::quicklens:${Version.quicklens}"
  // config
  val zioConfig = ivy"dev.zio::zio-config:${Version.zioConfig}"
  val zioConfigTypesafe = ivy"dev.zio::zio-config-typesafe:${Version.zioConfig}"
  val zioConfigRefined = ivy"dev.zio::zio-config-refined:${Version.zioConfig}"
  //val zioMacros = ivy"dev.zio::zio-macros:${Version.zioMacros}"

  // app
  val sttpCore = ivy"com.softwaremill.sttp.client::core:${Version.sttp}"
  val sttpClient = ivy"com.softwaremill.sttp.client::async-http-client-backend-zio:${Version.sttp}"

  // camunda
  val spring = ivy"org.springframework.boot:spring-boot-starter-web:${Version.spring}"
  val springJdbc = ivy"org.springframework.boot:spring-boot-starter-jdbc:${Version.spring}"
  val camundaWeb = ivy"org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:${Version.camundaSpringBoot}"
  val camundaRest = ivy"org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest:${Version.camundaSpringBoot}"
  val camundaSpin = ivy"org.camunda.bpm:camunda-engine-plugin-spin:${Version.camunda}"
  val camundaSpinJackson = ivy"org.camunda.spin:camunda-spin-dataformat-json-jackson:${Version.camundaSpinJackson}"
  val camundaAssert = ivy"org.camunda.bpm.assert:camunda-bpm-assert:${Version.camundaAssert}"
  val assertJ = ivy"org.assertj:assertj-core:${Version.assertJ}"

  val h2 = ivy"com.h2database:h2:${Version.h2}"
  val postgres = ivy"org.postgresql:postgresql:${Version.postgres}"
  val scalaXml = ivy"org.scala-lang.modules::scala-xml:${Version.scalaXml}"
  val sttpCirce = ivy"com.softwaremill.sttp.client::circe::${Version.sttp}"
  val circe = ivy"io.circe::circe-generic:${Version.circe}"
  val circeParser = ivy"io.circe::circe-parser:${Version.circe}"
  val circeRefined = ivy"io.circe::circe-refined:${Version.circe}"
  val groovy = ivy"org.codehaus.groovy:groovy-all:${Version.groovy}"

  // services
  val zioCats = ivy"dev.zio::zio-interop-cats:${Version.zioCats}"
  val http4sBlazeServer =
    ivy"org.http4s::http4s-blaze-server:${Version.http4s}"
  val http4sBlazeClient =
    ivy"org.http4s::http4s-blaze-client:${Version.http4s}"
  val http4sCirce = ivy"org.http4s::http4s-circe:${Version.http4s}"
  val http4sDsl = ivy"org.http4s::http4s-dsl:${Version.http4s}"
  val scalaCompiler = ivy"org.scala-lang:scala-compiler:${Version.scalaVersion}"
  val tapir = ivy"com.softwaremill.sttp.tapir::tapir-zio-http4s-server:${Version.tapir}"
  val tapirCirce = ivy"com.softwaremill.sttp.tapir::tapir-json-circe:${Version.tapir}"
  val tapirDocs = ivy"com.softwaremill.sttp.tapir::tapir-openapi-docs:${Version.tapir}"
  val tapirDocsCirce = ivy"com.softwaremill.sttp.tapir::tapir-openapi-circe-yaml:${Version.tapir}"
  val tapirSwagger = ivy"com.softwaremill.sttp.tapir::tapir-swagger-ui-http4s:${Version.tapir}"
  val tapirRedoc = ivy"com.softwaremill.sttp.tapir::tapir-redoc-http4s:${Version.tapir}"

  // cli
  val decline = ivy"com.monovore::decline-effect:${Version.decline}"
  val declineRefined = ivy"com.monovore::decline-refined:${Version.decline}"

  // examples
  // twitter
  val twitter4s = ivy"com.danielasfregola::twitter4s:${Version.twitter4s}"

  // test
  val zioTest = ivy"dev.zio::zio-test:${Version.zio}"
  val zioTestJunit = ivy"dev.zio::zio-test-junit:${Version.zio}"
  val zioTestSbt = ivy"dev.zio::zio-test-sbt:${Version.zio}"
}

trait CamundalaModule
  extends ScalaModule
    with PublishModule {
  val scalaVersion = Version.scalaVersion

  def publishVersion = Version.projectVersion


  override def artifactName = s"camundala-${super.artifactName()}"

  override def scalacOptions =
    defaultScalaOpts

  val defaultScalaOpts = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8", // Specify character encoding used by source files.
    "-Ymacro-annotations", // ZIO Macros: add the macro annotation compiler options.
    "-language:higherKinds", // Allow higher-kinded types
    "-language:postfixOps", // Allows operator syntax in postfix position (deprecated since Scala 2.10)

    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings -> check https://alexn.org/blog/2020/05/26/scala-fatal-warnings.html
    "-Wconf:any:warning-verbose",
    "-Wunused",
  // Warnings as errors! -> check https://alexn.org/blog/2020/05/26/scala-fatal-warnings.html
  "-Xfatal-warnings",

  // Linting options
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-override",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused:implicits",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-Wvalue-discard"
  )

  def pomSettings = PomSettings(
    description = "Doing Camunda with Scala.",
    organization = "pme123",
    url = "https://github.com/pme123/camundala",
    licenses = Seq(License.MIT),
    versionControl = VersionControl.github("pme123", "camundala"),
    developers = Seq(
      Developer("pme123", "Pascal Mengelt", "https://github.com/pme123")
    )
  )
}

trait ModuleWithTests extends CamundalaModule {

  // needed for ZIO nightly
  override def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )

  object test extends Tests {
    override def moduleDeps = super.moduleDeps

    override def runIvyDeps: Target[Loose.Agg[Dep]] =
      Agg(
        Libs.scalaCompiler
      )

    override def ivyDeps = Agg(
      Libs.zioTest,
      Libs.zioTestJunit,
      Libs.zioTestSbt,
      Libs.camundaAssert,
      Libs.assertJ
    )

    def testOne(args: String*) = T.command {
      super.runMain("org.scalatest.run", args: _*)
    }

    def testFrameworks =
      Seq("zio.test.sbt.ZTestFramework")
  }

}

object model extends ModuleWithTests {

  override def ivyDeps = {
    Agg(
      Libs.zio,
      //   Libs.zioMacros,
      Libs.zioLogging,
      Libs.refinded,
      Libs.circe,
      Libs.circeParser
    )
  }
}

object config extends ModuleWithTests {
  override def moduleDeps = Seq(model)

  override def ivyDeps = {
    Agg(
      Libs.zioConfig,
      Libs.zioConfigRefined,
      Libs.zioConfigTypesafe
    )
  }
}

object app extends CamundalaModule {

  override def moduleDeps = Seq()

  override def ivyDeps = {
    Agg(
      Libs.zio,
      Libs.sttpClient,
      Libs.sttpCore
    )
  }
}

object camunda
  extends ModuleWithTests
    with BuildInfo {

  override def moduleDeps = Seq(model, config, app)

  override def ivyDeps = {
    Agg(
      Libs.spring,
      Libs.springJdbc,
      Libs.camundaWeb,
      Libs.camundaRest,
      Libs.camundaSpin,
      Libs.camundaSpinJackson,
      Libs.groovy,
      Libs.h2,
      Libs.postgres,
      Libs.scalaXml,
      Libs.sttpCirce,
      Libs.circe,
      Libs.circeRefined
    )
  }

  override def buildInfoPackageName: Option[String] = Some("pme123.camundala")

  override def buildInfoMembers: T[Map[String, String]] = T {
    Map(
      "name" -> "camundala",
      "organization" -> pomSettings().organization,
      "license" -> pomSettings().licenses.head.id,
      "version" -> publishVersion(),
      "url" -> pomSettings().url,
      "scalaVersion" -> scalaVersion(),
      "camundaVersion" -> Version.camunda
    )
  }
}

object services extends ModuleWithTests {

  override def moduleDeps = Seq(camunda)


  override def ivyDeps = {
    Agg(
      Libs.http4sBlazeServer,
      Libs.http4sBlazeClient,
      Libs.http4sDsl,
      Libs.http4sCirce,
      Libs.zioCats,
      Libs.tapir,
      Libs.tapirCirce,
      Libs.tapirDocs,
      Libs.tapirDocsCirce,
      Libs.tapirSwagger,
      Libs.tapirRedoc,
      Libs.scalaCompiler
    )
  }
}

object cli extends ModuleWithTests {

  override def moduleDeps = Seq(services)

  override def mainClass = Some("pme123.camundala.cli.CliApp")

  override def ivyDeps = {
    Agg(
      Libs.decline,
      Libs.declineRefined,
      Libs.zioCats
    )
  }
}

object examples extends mill.Module {

  trait ExampleModule extends ModuleWithTests {
    override def moduleDeps: Seq[JavaModule with PublishModule] = Seq(services, cli)
  }

  object common extends ExampleModule {

  }

  object twitter extends ExampleModule {

    override def moduleDeps: Seq[JavaModule with PublishModule] = super.moduleDeps ++ Seq(common, twitterApi)

    override def mainClass = Some("pme123.camundala.examples.twitter.TwitterApp")

    object twitterApi extends ModuleWithTests {

      override def ivyDeps = {
        Agg(
          Libs.twitter4s,
          Libs.zioConfig,
          Libs.zioLogging,
          Libs.zioConfigTypesafe
        )
      }
    }

  }

  object rest extends ExampleModule {

    override def mainClass = Some("pme123.camundala.examples.rest.RestApp")

  }

  object playground extends ExampleModule {
    override def moduleDeps: Seq[JavaModule with PublishModule] = super.moduleDeps ++ Seq(common)

    override def mainClass = Some("pme123.camundala.examples.playground.PlaygroundApp")

  }

}
