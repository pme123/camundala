import laika.ast.ExternalTarget
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import laika.rewrite.link.*
import sbt.url

import scala.util.Using

lazy val projectVersion =
  Using(scala.io.Source.fromFile("version"))(_.mkString.trim).get
val scala3Version = "3.3.0"
val org = "io.github.pme123"
val dmnTesterVersion = "0.17.9"

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

lazy val root = project
  .in(file("."))
  .configure(preventPublication)
  .settings(
    name := "camundala",
    organization := org,
  )
  .aggregate(
    domain,
    bpmn,
    api,
    dmn,
    camunda,
    camunda7Worker,
    camunda8,
    simulation,
    helper,
    documentation,
    exampleTwitterC7,
    exampleTwitterC8,
    exampleInvoiceC7,
    exampleInvoiceWorkerC7,
    exampleInvoiceC8,
    exampleDemos,
    exampleMyCompany
  )

def projectSettings(projName: String) = Seq(
  name := s"camundala-$projName",
  organization := org,
  scalaVersion := scala3Version,
  // version := projectVersion,
  scalacOptions ++= Seq(
    //   "-Xmax-inlines:50", // is declared as erased, but is in fact used
    //   "-Wunused:imports"
  ),
)
lazy val autoImportSetting =
  scalacOptions +=
    Seq(
      "java.lang",
      "scala",
      "scala.Predef",
      "io.circe",
      "io.circe.generic.semiauto",
      "io.circe.derivation",
      "io.circe.syntax",
      "sttp.tapir",
      "sttp.tapir.json.circe"
    ).mkString(start = "-Yimports:", sep = ",", end = "")

lazy val domain = project
  .in(file("./domain"))
  .configure(publicationSettings)
  .settings(projectSettings("domain"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= (tapirDependencies :+
      "com.novocode" % "junit-interface" % "0.11" % Test)
  )

val osLibDependency = "com.lihaoyi" %% "os-lib" % "0.9.1"
lazy val bpmn = project
  .in(file("./bpmn"))
  .configure(publicationSettings)
  .settings(projectSettings("bpmn"))
  .settings(
    autoImportSetting,
    libraryDependencies += osLibDependency
  )
  .dependsOn(domain)

lazy val api = project
  .in(file("./api"))
  .configure(publicationSettings)
  .settings(projectSettings("api"))
  .settings(
    autoImportSetting,
    libraryDependencies ++=
      Seq(
        "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
        "com.typesafe" % "config" % "1.4.2",
        "com.novocode" % "junit-interface" % "0.11" % Test
      )
  )
  .dependsOn(bpmn)

lazy val camunda = project
  .in(file("./camunda"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      "org.camunda.bpm" % "camunda-engine" % camundaVersion, // listeners
      "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-external-task-client" % camundaVersion,
      "org.camunda.bpm" % "camunda-engine-plugin-spin" % camundaVersion,
      "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % "1.18.1"
    )
  )
  .dependsOn(bpmn)

lazy val camunda7Worker = project
  .in(file("./camunda7/worker"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda7-worker"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-external-task-client" % camundaVersion
    )
  )
  .dependsOn(bpmn)

lazy val camunda8 = project
  .in(file("./camunda8"))
  .configure(preventPublication)
  .settings(projectSettings("camunda8"))
  .settings(
    autoImportSetting,
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
      "io.github.pme123" %% "camunda-dmn-tester-shared" % dmnTesterVersion
    )
  )
  .dependsOn(bpmn)

lazy val simulation = project
  .in(file("./simulation"))
  .configure(publicationSettings)
  .settings(projectSettings("simulation"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      "org.scala-sbt" % "test-interface" % "1.0"
    )
  )
  .dependsOn(bpmn)

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

val tapirVersion = "1.2.10"
// can be removed when tapir upgraded
val circeVersion = "0.14.5"
lazy val tapirDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % "0.3.2",
  "io.circe" %% "circe-generic" % circeVersion
)
lazy val sttpDependency = "com.softwaremill.sttp.client3" %% "circe" % "3.8.13"
val camundaVersion = "7.19.0"
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

lazy val helper = project
  .in(file("./helper"))
  .configure(publicationSettings)
  .settings(projectSettings("helper"))
  .settings(
    libraryDependencies += osLibDependency
  )

// EXAMPLES
lazy val exampleInvoiceC7 = project
  .in(file("./examples/invoice/camunda7"))
  .settings(projectSettings("example-invoice-c7"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    autoImportSetting,
    //Test / parallelExecution := false,
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(api, dmn, camunda, simulation)

lazy val exampleInvoiceWorkerC7 = project
  .in(file("./examples/invoice/camunda7Worker"))
  .settings(projectSettings("example-invoice-c7"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(camunda7Worker, exampleInvoiceC7)

lazy val exampleInvoiceC8 = project
  .in(file("./examples/invoice/camunda8"))
  .settings(projectSettings("example-invoice-c8"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    autoImportSetting,
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
    autoImportSetting,
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
    autoImportSetting,
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
    autoImportSetting,
    libraryDependencies ++= camundaDependencies
    //   libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "it",
  )
  .dependsOn(api, dmn, camunda, simulation)

// start company documentation example
import com.typesafe.config.ConfigFactory
import laika.ast.Path.Root
import laika.helium.Helium
import laika.helium.config.*
import laika.rewrite.{Version, Versions}
import laika.rewrite.link.LinkConfig

import scala.jdk.CollectionConverters.*

val config = ConfigFactory.parseFile(new File("examples/myCompany/CONFIG.conf"))
val currentVersion = config.getString("release.tag")
val released = config.getBoolean("released")
val olderVersions = config.getList("releases.older").asScala
val versions = Versions(
  currentVersion = Version(
    currentVersion,
    currentVersion,
    label = Some(if (released) "Stable" else "Dev")
  ),
  olderVersions =
    olderVersions.map(_.unwrapped().toString).map(v => Version(v, v)),
  newerVersions = Seq()
)
lazy val exampleMyCompany = project
  .in(file("./examples/myCompany"))
  .settings(projectSettings("example-exampleDemos"))
  .settings(
    laikaConfig := LaikaConfig.defaults
      .withConfigValue(LinkConfig(excludeFromValidation = Seq(Root)))
      .withRawContent
    //  .failOnMessages(MessageFilter.None)
    //  .renderMessages(MessageFilter.None)
    ,
    laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting),
    laikaTheme := Helium.defaults.site
      .topNavigationBar(
        homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home)
      )
      .site
      .favIcons(
        Favicon.internal(Root / "favicon.ico", sizes = "32x32")
      )
      .site
      .versions(versions)
      .build,
    buildInfoKeys := Seq[BuildInfoKey](
      organization,
      name,
      version,
      scalaVersion,
      sbtVersion
    ),
    buildInfoPackage := "camundala.examples.myCompany"
  )
  .enablePlugins(LaikaPlugin, BuildInfoPlugin)
  .configure(preventPublication)
  .dependsOn(api)

val springBootVersion = "2.7.15"
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
  "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % "1.18.1",
  // groovy support
  "org.codehaus.groovy" % "groovy-jsr223" % "3.0.16",
  "com.h2database" % "h2" % h2Version
) //.map(_.exclude("org.slf4j", "slf4j-api"))

val zeebeVersion = "8.2.4"
val zeebeDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter" % springBootVersion,
  "org.springframework.boot" % "spring-boot-starter-webflux" % springBootVersion,
  "io.camunda.spring" % "spring-boot-starter-camunda" % zeebeVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2"
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
  // publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
  /*  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype_credentials"),
   */ licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
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
