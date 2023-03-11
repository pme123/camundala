import sbt.url
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import laika.rewrite.link._
import laika.ast.ExternalTarget

import scala.util.Using

lazy val projectVersion =
  Using(scala.io.Source.fromFile("version"))(_.mkString.trim).get
val scala3Version = "3.2.1"
val org = "io.github.pme123"

ThisBuild / versionScheme := Some("early-semver")

lazy val root = project
  .in(file("."))
  .configure(preventPublication)
  .settings(
    name := "camundala"
  )
  .aggregate(
    domain,
    bpmn,
    api,
    dmn,
    camunda,
    camunda8,
    simulation,
    documentation,
    exampleTwitterC7,
    exampleTwitterC8,
    exampleInvoiceC7,
    exampleInvoiceC8,
    exampleDemos
  )

def projectSettings(projName: String): Seq[Def.Setting[_]] = Seq(
  name := s"camundala-$projName",
  organization := org,
  scalaVersion := scala3Version,
  version := projectVersion,
  scalacOptions ++= Seq(
    "-Xmax-inlines",
    "50" // is declared as erased, but is in fact used
  )
)

lazy val domain = project
  .in(file("./domain"))
  .configure(publicationSettings)
  .settings(projectSettings("domain"))
  .settings(
    libraryDependencies ++= tapirDependencies
  )

lazy val bpmn = project
  .in(file("./bpmn"))
  .configure(publicationSettings)
  .settings(projectSettings("bpmn"))
  .settings(
    libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.8.1" // dangerous library - in domain this caused 'geny.Generator$ already has a symbol'
  )
  .dependsOn(domain)

lazy val api = project
  .in(file("./api"))
  .configure(publicationSettings)
  .settings(projectSettings("api"))
  .settings(
    libraryDependencies ++=
      Seq(
        "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
        "com.novocode" % "junit-interface" % "0.11" % Test
      )
  )
  .dependsOn(bpmn)

lazy val camunda = project
  .in(file("./camunda"))
  .configure(preventPublication)
  .settings(projectSettings("camunda"))
  .settings(
    libraryDependencies +=
      "org.camunda.bpm" % "camunda-engine" % camundaVersion
  )
  .dependsOn(bpmn)

lazy val camunda8 = project
  .in(file("./camunda8"))
  .configure(preventPublication)
  .settings(projectSettings("camunda8"))
  .settings(
    libraryDependencies ++= zeebeDependencies
  )
  .dependsOn(bpmn)

lazy val dmn = project
  .in(file("./dmn"))
  .configure(publicationSettings)
  .settings(projectSettings("dmn"))
  .settings(
    libraryDependencies ++= Seq(
      sttpDependency,
      "io.github.pme123" %% "camunda-dmn-tester-shared" % "0.18.0-SNAPSHOT"
    )
  )
  .dependsOn(bpmn)

lazy val simulation = project
  .in(file("./simulation"))
  .configure(publicationSettings)
  .settings(projectSettings("simulation"))
  .settings(
    libraryDependencies ++= Seq(
      sttpDependency,
      "org.scala-sbt" % "test-interface" % "1.0"
    )
  )
  .dependsOn(api)

lazy val documentation = (project in file("./documentation"))
  .configure(preventPublication)
  .settings(projectSettings("documentation"))
  .settings(
    laikaConfig := LaikaConfig.defaults
      .withConfigValue(
        LinkConfig(
          targets = Seq(
            TargetDefinition(
              "bpmn specification",
              ExternalTarget("https://www.bpmn.org")
            ),
            TargetDefinition("camunda", ExternalTarget("https://camunda.com"))
          )
        )
      )
      //  .withConfigValue(LinkConfig(excludeFromValidation = Seq(Root)))
      .withRawContent
    //  .failOnMessages(MessageFilter.None)
    //  .renderMessages(MessageFilter.None)
    ,
    laikaSite / target := baseDirectory.value / ".." / "docs",
    laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting)
  )
  .enablePlugins(LaikaPlugin)

val tapirVersion = "1.2.4"
lazy val tapirDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % "0.3.1",
  //"com.softwaremill.quicklens" %% "quicklens" % "1.7.5", // simple modifying case classes
  "org.latestbit" %% "circe-tagged-adt-codec" % "0.10.1" // to encode enums
)
lazy val sttpDependency = "com.softwaremill.sttp.client3" %% "circe" % "3.8.10"
val camundaVersion = "7.18.0"
/* NOT IN USE
lazy val camundaTestDependencies = Seq(
  // provide Camunda interaction
  "org.camunda.bpm" % "camunda-engine" % camundaVersion,
  "org.camunda.bpm" % "camunda-engine-plugin-spin" % camundaVersion,
  "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % "1.13.1",
  "org.codehaus.groovy" % "groovy-jsr223" % "3.0.10",
  //
  //"org.camunda.bpm.dmn" % "camunda-engine-dmn" % camundaVersion % Provided,
  // provide test helper
  "org.camunda.bpm.assert" % "camunda-bpm-assert" % "10.0.0",
  "org.assertj" % "assertj-core" % "3.19.0",
  "org.camunda.bpm.extension" % "camunda-bpm-assert-scenario" % "1.1.1",
  "org.camunda.bpm.extension.mockito" % "camunda-bpm-mockito" % "5.16.0",
  // dmn testing
  //("org.camunda.bpm.extension.dmn.scala" % "dmn-engine" % "1.7.2-SNAPSHOT").cross(CrossVersion.for3Use2_13),
  "de.odysseus.juel" % "juel" % "2.1.3",
  //     "org.scalactic" %% "scalactic" % "3.2.9",
  //     "org.scalatest" %% "scalatest" % "3.2.9",
  //     "org.mockito" % "mockito-scala-scalatest_2.13" % "1.16.37",
  "org.mockito" % "mockito-core" % "3.1.0",
  "com.novocode" % "junit-interface" % "0.11"
)
 */

// EXAMPLES
lazy val exampleInvoiceC7 = project
  .in(file("./examples/invoice/camunda7"))
  .settings(projectSettings("example-invoice-c7"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    //Test / parallelExecution := false,
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(dmn, camunda, simulation)

lazy val exampleInvoiceC8 = project
  .in(file("./examples/invoice/camunda8"))
  .settings(projectSettings("example-invoice-c8"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    Test / parallelExecution := false,
    libraryDependencies ++= zeebeDependencies
  )
  .dependsOn(camunda8, api, simulation)

lazy val exampleTwitterC7 = project
  .in(file("./examples/twitter/camunda7"))
  .settings(projectSettings("example-twitter-c7"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    libraryDependencies ++= camundaDependencies :+
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(api, simulation)

lazy val exampleTwitterC8 = project
  .in(file("./examples/twitter/camunda8"))
  .settings(projectSettings("example-twitter-c8"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    libraryDependencies +=
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(camunda8, api, simulation)

lazy val exampleDemos = project
  .in(file("./examples/demos"))
  .settings(projectSettings("example-demos"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    libraryDependencies ++= camundaDependencies
    //   libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "it",

  )
  .dependsOn(dmn, camunda, simulation)

val springBootVersion = "2.7.6"
val h2Version = "2.1.214"
// Twitter
val twitter4jVersion = "4.1.2"
val camundaDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
  "org.springframework.boot" % "spring-boot-starter-jdbc" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
  "io.netty" % "netty-all" % "4.1.73.Final", // needed for Spring Boot Version > 2.5.*
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-rest" % camundaVersion,
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaVersion,
  // json support
  "org.camunda.bpm" % "camunda-engine-plugin-spin" % camundaVersion,
  "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % "1.17.0",
  // groovy support
  "org.codehaus.groovy" % "groovy-jsr223" % "3.0.13",
  "com.h2database" % "h2" % h2Version
) //.map(_.exclude("org.slf4j", "slf4j-api"))

val zeebeVersion = "1.3.4"
val zeebeDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter" % springBootVersion,
  "org.springframework.boot" % "spring-boot-starter-webflux" % springBootVersion,
  "io.camunda" % "spring-zeebe-starter" % zeebeVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.3"
  //"io.camunda" % "spring-zeebe-test" % zeebeVersion % Test,
).map(_.exclude("org.slf4j", "slf4j-api"))

lazy val developerList = List(
  Developer(
    id = "pme123",
    name = "Pascal Mengelt",
    email = "pascal.mengelt@gmail.com",
    url = url("https://github.com/pme123")
  )
)

lazy val publicationSettings: Project => Project = _.settings(
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/pme123/camundala")),
  startYear := Some(2021),
  // logLevel := Level.Debug,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/pme123/camundala"),
      "scm:git:github.com:/pme123/camundala"
    )
  ),
  developers := developerList
)

lazy val preventPublication: Project => Project =
  _.settings(
    publish := {},
    publishTo := Some(
      Resolver
        .file("Unused transient repository", target.value / "fakepublish")
    ),
    publishArtifact := false,
    publishLocal := {},
    packagedArtifacts := Map.empty
  ) // doesn't work - https://github.com/sbt/sbt-pgp/issues/42

lazy val integrationTests: Project => Project =
  _.configs(IntegrationTest)
    .settings(
      Defaults.itSettings,
      testFrameworks += new TestFramework(
        "camundala.simulation.custom.SimulationTestFramework"
      )
    )
