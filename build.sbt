import laika.ast.Path.Root
import laika.config.*
import laika.format.Markdown.GitHubFlavor
import laika.helium.Helium
import laika.helium.config.{Favicon, HeliumIcon, IconLink}
import sbt.url

import scala.util.Using

lazy val projectVersion =
  Using(scala.io.Source.fromFile("version"))(_.mkString.trim).get
val scala3Version = "3.3.0"
val org = "io.github.pme123"

// dependency Versions
// 00-documentation
// - Laika Plugin
// 00-helper
val osLibVersion = "0.9.1"
// 01-domain
val tapirVersion = "1.9.2"
val openapiCirceVersion = "0.7.3"
val ironCirceVersion = "2.3.0"
val junitInterfaceVersion = "0.11"
// 02-bpmn
// -> domain
// - osLib
// 03-api
// -> bpmn
val scalaXmlVersion = "2.1.0"
val typesafeConfigVersion = "1.4.2"
// - junitInterface
// 03-dmn
// -> bpmn
val sttpClient3Version = "3.8.13"
val dmnTesterVersion = "0.17.9"
// 03-simulation
// -> bpmn
val testInterfaceVersion = "1.0"
// - sttpClient3
// 03-worker
// -> bpmn

// --- Implementations
// 04-worker-c7spring
// -> worker
val camundaVersion = "7.19.0" // external task client
// - sttpClient3

// --- Experiments
// 04-c7-spring
// -> bpmn
val camundaSpinVersion = "1.18.1"
// camunda // server spring-boot
// 04-c8-spring
// -> bpmn
val springBootVersion = "2.7.15"
val zeebeVersion = "8.2.4"
val scalaJacksonVersion = "2.14.2"

// --- Examples
val h2Version = "2.1.214"
val twitter4jVersion = "4.1.2"
val groovyVersion = "3.0.16"




ThisBuild / versionScheme := Some("early-semver")
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

lazy val root = project
  .in(file("."))
  .configure(preventPublication)
  .settings(
    name := "camundala",
    organization := org
  )
  .aggregate(
    documentation,
    helper,
    domain,
    bpmn,
    api,
    dmn,
    simulation,
    worker,
    // implementations
    camunda7Worker,
    // experiments
    camunda, // not in use
    camunda8, // not in use
    // examples
    exampleTwitterC7,
    exampleTwitterC8,
    exampleInvoiceC7,
    exampleInvoiceWorkerC7,
    exampleInvoiceC8,
    exampleDemos,
    exampleMyCompany
  )

// general independent
lazy val documentation =
  (project in file("./00-documentation"))
    .configure(preventPublication)
    .settings(projectSettings("documentation"))
    .settings(
      laikaConfig := LaikaConfig.defaults
        .withConfigValue(LaikaKeys.excludeFromNavigation, Seq(Root))
        .withConfigValue("projectVersion", projectVersion)
        .withConfigValue(
          LinkConfig.empty
            .addTargets(
              TargetDefinition.external("bpmn specification", "https://www.bpmn.org"),
              TargetDefinition.external("camunda", "https://camunda.com")
            ).addSourceLinks(
              SourceLinks(baseUri = "https://github.com/pme123/camundala/tree/master/05-examples/invoice/camunda7/src/main/scala/", suffix = "scala")
            )
        )
        .withRawContent
        //.failOnMessages(MessageFilter.None)
      //  .renderMessages(MessageFilter.None)
      ,
      laikaSite / target := baseDirectory.value / ".." / "docs",
      laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting)
    )
    .enablePlugins(LaikaPlugin)

lazy val helper = project
  .in(file("./00-helper"))
  .configure(publicationSettings)
  .settings(projectSettings("helper"))
  .settings(
    libraryDependencies += osLibDependency
  )

// layer 01
lazy val domain = project
  .in(file("./01-domain"))
  .configure(publicationSettings)
  .settings(projectSettings("domain"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= (tapirDependencies :+
      "com.novocode" % "junit-interface" % junitInterfaceVersion % Test)
  )
// layer 02
val osLibDependency = "com.lihaoyi" %% "os-lib" % osLibVersion
lazy val bpmn = project
  .in(file("./02-bpmn"))
  .configure(publicationSettings)
  .settings(projectSettings("bpmn"))
  .settings(
    autoImportSetting,
    libraryDependencies += osLibDependency
  )
  .dependsOn(domain)

// layer 03
lazy val api = project
  .in(file("./03-api"))
  .configure(publicationSettings)
  .settings(projectSettings("api"))
  .settings(
    autoImportSetting,
    libraryDependencies ++=
      Seq(
        "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion,
        "com.typesafe" % "config" % typesafeConfigVersion,
        "com.novocode" % "junit-interface" % junitInterfaceVersion % Test
      )
  )
  .dependsOn(bpmn)

lazy val dmn = project
  .in(file("./03-dmn"))
  .configure(publicationSettings)
  .settings(projectSettings("dmn"))
  .settings(
    libraryDependencies ++= Seq(
      sttpDependency,
      "io.github.pme123" %% "camunda-dmn-tester-shared" % dmnTesterVersion
    )
  )
  .dependsOn(bpmn)

lazy val worker = project
  .in(file("./03-worker"))
  .configure(publicationSettings)
  .settings(projectSettings("worker"))
  .settings(
    autoImportSetting,
  )
  .dependsOn(bpmn)

lazy val simulation = project
  .in(file("./03-simulation"))
  .configure(publicationSettings)
  .settings(projectSettings("simulation"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      "org.scala-sbt" % "test-interface" % testInterfaceVersion
    )
  )
  .dependsOn(bpmn)

// layer 04
lazy val camunda7Worker = project
  .in(file("./04-worker-c7spring"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda7-worker"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-external-task-client" % camundaVersion
    )
  )
  .dependsOn(worker)

// just demo
lazy val camunda = project
  .in(file("./04-c7-spring"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      "org.camunda.bpm" % "camunda-engine" % camundaVersion, // listeners
      "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-external-task-client" % camundaVersion,
      "org.camunda.bpm" % "camunda-engine-plugin-spin" % camundaVersion,
      "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % camundaSpinVersion
    )
  )
  .dependsOn(bpmn)

lazy val camunda8 = project
  .in(file("./04-c8-spring"))
  .configure(preventPublication)
  .settings(projectSettings("camunda8"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= zeebeDependencies
  )
  .dependsOn(bpmn)
// end not in use

def projectSettings(projName: String) = Seq(
  name := s"camundala-$projName",
  organization := org,
  scalaVersion := scala3Version,
  version := projectVersion,
  scalacOptions ++= Seq(
    //   "-Xmax-inlines:50", // is declared as erased, but is in fact used
    //   "-Wunused:imports"
  )
)
lazy val autoImportSetting =
  scalacOptions += Seq(
      "java.lang", "scala", "scala.Predef",
      "io.circe.syntax"
    ).mkString(start = "-Yimports:", sep = ",", end = "")


lazy val tapirDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-redoc-bundle" % tapirVersion,
  "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % openapiCirceVersion,
 // "io.circe" %% "circe-generic" % circeVersion,
  "io.github.iltotore" %% "iron-circe" % ironCirceVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-iron" % tapirVersion
)

lazy val sttpDependency = "com.softwaremill.sttp.client3" %% "circe" % sttpClient3Version

// EXAMPLES
lazy val exampleInvoiceC7 = project
  .in(file("./05-examples/invoice/camunda7"))
  .settings(projectSettings("example-invoice-c7"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    autoImportSetting,
    // Test / parallelExecution := false,
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(api, dmn, camunda, simulation)

lazy val exampleInvoiceWorkerC7 = project
  .in(file("./05-examples/invoice/camunda7Worker"))
  .settings(projectSettings("example-invoice-c7"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(camunda7Worker, exampleInvoiceC7)

lazy val exampleInvoiceC8 = project
  .in(file("./05-examples/invoice/camunda8"))
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
  .in(file("./05-examples/twitter/camunda7"))
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
  .in(file("./05-examples/twitter/camunda8"))
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
  .in(file("./05-examples/demos"))
  .settings(projectSettings("example-demos"))
  .configure(preventPublication)
  .configure(integrationTests)
  .settings(
    autoImportSetting,
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(api, dmn, camunda, simulation)

// start company documentation example
import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

val config = ConfigFactory.parseFile(new File("05-examples/myCompany/CONFIG.conf"))
val currentVersion = config.getString("release.tag")
val released = config.getBoolean("released")
val olderVersions = config.getList("releases.older").asScala
val versions = Versions
.forCurrentVersion(Version(currentVersion, currentVersion)
  .withLabel(if (released) "Stable" else "Dev")
).withOlderVersions(olderVersions.map(_.unwrapped().toString).map(v => Version(v, v)) *)
lazy val exampleMyCompany = project
  .in(file("./05-examples/myCompany"))
  .settings(projectSettings("example-exampleDemos"))
  .settings(
    laikaConfig := LaikaConfig.defaults
      .withConfigValue(LaikaKeys.excludeFromNavigation, Seq(Root))
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


val camundaDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
  "org.springframework.boot" % "spring-boot-starter-jdbc" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
  "io.netty" % "netty-all" % "4.1.73.Final", // needed for Spring Boot Version > 2.5.*
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-rest" % camundaVersion,
  "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaVersion,
  // json support
  "org.camunda.bpm" % "camunda-engine-plugin-spin" % camundaVersion,
  "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % camundaSpinVersion,
  // groovy support
  "org.codehaus.groovy" % "groovy-jsr223" % groovyVersion,
  "com.h2database" % "h2" % h2Version
) //.map(_.exclude("org.slf4j", "slf4j-api"))

val zeebeDependencies = Seq(
  "org.springframework.boot" % "spring-boot-starter" % springBootVersion,
  "org.springframework.boot" % "spring-boot-starter-webflux" % springBootVersion,
  "io.camunda.spring" % "spring-boot-starter-camunda" % zeebeVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % scalaJacksonVersion
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
