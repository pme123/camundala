import sbt.url

import scala.util.Using

lazy val projectVersion =
  Using(scala.io.Source.fromFile("version"))(_.mkString.trim).get
val scala3Version = "3.1.2"
val org = "io.github.pme123"

ThisBuild / versionScheme := Some("early-semver")

lazy val root = project
  .in(file("."))
  .configure(preventPublication)
  .settings(
    name := "camundala"
  )
  .aggregate(
    bpmn,
    api,
    dmn,
    camunda,
    camunda8,
    test,
    simulation,
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
  version := projectVersion
)

lazy val bpmn = project
  .in(file("./bpmn"))
  .configure(publicationSettings)
  .settings(projectSettings("bpmn"))
  .settings(
    libraryDependencies ++= tapirDependencies,
    scalacOptions ++= Seq(
      "-Xmax-inlines",
      "50" // is declared as erased, but is in fact used
    )
  )

lazy val api = project
  .in(file("./api"))
  .configure(publicationSettings)
  .settings(projectSettings("api"))
  .settings(
    libraryDependencies ++=
        Seq(
          "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
          "com.novocode" % "junit-interface" % "0.11" % Test
        ),
    scalacOptions ++= Seq(
      "-Xmax-inlines",
      "50" // is declared as erased, but is in fact used
    )
  )
  .dependsOn(bpmn)

lazy val camunda = project
  .in(file("./camunda"))
  .configure(preventPublication)
  .settings(projectSettings("camunda"))
  .settings(
    libraryDependencies +=
      "org.camunda.bpm" % "camunda-engine" % camundaVersion,
    scalacOptions ++= Seq(
      "-Xmax-inlines",
      "50" // is declared as erased, but is in fact used
    )
  )
  .dependsOn(bpmn)

lazy val camunda8 = project
  .in(file("./camunda8"))
  .configure(preventPublication)
  .settings(projectSettings("camunda8"))
  .settings(
    libraryDependencies ++= zeebeDependencies,
    scalacOptions ++= Seq(
      "-Xmax-inlines",
      "50" // is declared as erased, but is in fact used
    )
  )
  .dependsOn(bpmn)

lazy val dmn = project
  .in(file("./dmn"))
  .configure(preventPublication)
  .settings(projectSettings("dmn"))
  .dependsOn(bpmn)

lazy val test = project
  .in(file("./test"))
  .configure(preventPublication)
  .settings(projectSettings("test"))
  .settings(
    libraryDependencies ++=
      camundaTestDependencies
  )
  .dependsOn(bpmn, dmn)

lazy val simulation = project
  .in(file("./simulation"))
  .configure(publicationSettings)
  .settings(projectSettings("simulation"))
  .settings(
    libraryDependencies ++=
      gatlingDependencies,
    scalacOptions ++= Seq(
      "-Xmax-inlines",
      "50" // is declared as erased, but is in fact used
    )
  )
  .dependsOn(api)

val tapirVersion = "0.20.2"
lazy val tapirDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  //"com.softwaremill.quicklens" %% "quicklens" % "1.7.5", // simple modifying case classes
  "org.latestbit" %% "circe-tagged-adt-codec" % "0.10.1", // to encode enums
  "com.lihaoyi" %% "os-lib" % "0.8.1"
)
val camundaVersion = "7.16.0"
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

lazy val gatlingDependencies = Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.7.5",
  "io.gatling" % "gatling-test-framework" % "3.7.5"
)

// EXAMPLES
lazy val exampleInvoiceC7 = project
  .in(file("./examples/invoice/camunda7"))
  .settings(projectSettings("example-invoice-c7"))
  .configure(preventPublication)
  .settings(
    Test / parallelExecution := false,
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(dmn, camunda, test, simulation)
  .enablePlugins(GatlingPlugin)

lazy val exampleInvoiceC8 = project
  .in(file("./examples/invoice/camunda8"))
  .settings(projectSettings("example-invoice-c8"))
  .configure(preventPublication)
  .settings(
    Test / parallelExecution := false,
    libraryDependencies ++= zeebeDependencies
  )
  .dependsOn(api, simulation)
  .enablePlugins(GatlingPlugin)

lazy val exampleTwitterC7 = project
  .in(file("./examples/twitter/camunda7"))
  .settings(projectSettings("example-twitter-c7"))
  .configure(preventPublication)
  .settings(
    libraryDependencies ++= camundaDependencies :+
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(api, test, simulation)
  .enablePlugins(GatlingPlugin)

lazy val exampleTwitterC8 = project
  .in(file("./examples/twitter/camunda8"))
  .settings(projectSettings("example-twitter-c8"))
  .configure(preventPublication)
  .settings(
    libraryDependencies +=
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(camunda8, api, simulation)
  .enablePlugins(GatlingPlugin)

lazy val exampleDemos = project
  .in(file("./examples/demos"))
  .settings(projectSettings("example-demos"))
  .configure(preventPublication)
  .settings(
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(camunda, test, simulation)
  .enablePlugins(GatlingPlugin)

val springBootVersion = "2.6.1"
val h2Version = "1.4.200"
// Twitter
val twitter4jVersion = "4.0.7"
val camundaDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
  "org.springframework.boot" % "spring-boot-starter-jdbc" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
  "io.netty" % "netty-all" % "4.1.73.Final", // needed for Spring Boot Version > 2.5.*
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-rest" % camundaVersion,
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaVersion,
  "com.h2database" % "h2" % h2Version
).map(_.exclude("org.slf4j", "slf4j-api"))

val zeebeVersion = "1.3.4"
val zeebeDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter" % springBootVersion,
  "org.springframework.boot" % "spring-boot-starter-webflux" % springBootVersion,
  "io.camunda" % "spring-zeebe-starter" % zeebeVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2"
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
