import Dependencies.*
import Settings.*
import laika.ast.Path.Root
import laika.config.*
import laika.format.Markdown.GitHubFlavor
import laika.helium.Helium
import laika.helium.config.{Favicon, HeliumIcon, IconLink}

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / evictionErrorLevel := Level.Warn

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
    exampleInvoice,
    exampleTwitter,
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
              SourceLinks(
                baseUri =
                  "https://github.com/pme123/camundala/tree/master/05-examples/invoice/camunda7/src/main/scala/",
                suffix = "scala"
              )
            )
        )
        .withRawContent
      // .failOnMessages(MessageFilter.None)
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
    autoImportSetting
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
      "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-external-task-client" % camundaVersion,
      "javax.xml.bind" % "jaxb-api" % jaxbApiVersion
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
      "org.camunda.bpm" % "camunda-engine-spring-6" % camundaVersion, // listeners
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

// EXAMPLES
// INVOICE
lazy val exampleInvoice = project
  .in(file("./05-examples/invoice"))
  .settings(projectSettings("example-invoice"))
  .configure(preventPublication)
  .aggregate(
    exampleInvoiceBpmn,
    exampleInvoiceApi,
    exampleInvoiceDmn,
    exampleInvoiceSimulation,
    exampleInvoiceWorker,
    exampleInvoiceC7,
    exampleInvoiceC8
  )

lazy val exampleInvoiceBpmn = project
  .in(file("./05-examples/invoice/02-bpmn"))
  .settings(projectSettings("example-invoice-bpmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(bpmn)

lazy val exampleInvoiceApi = project
  .in(file("./05-examples/invoice/03-api"))
  .settings(projectSettings("example-invoice-api"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(api, exampleInvoiceBpmn)

lazy val exampleInvoiceDmn = project
  .in(file("./05-examples/invoice/03-dmn"))
  .settings(projectSettings("example-invoice-dmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(dmn, exampleInvoiceBpmn)

lazy val exampleInvoiceSimulation = project
  .in(file("./05-examples/invoice/03-simulation"))
  .settings(projectSettings("example-invoice-simulation"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .settings(testFrameworks += new TestFramework(
    "camundala.simulation.custom.SimulationTestFramework"
  ))
  .dependsOn(simulation, exampleInvoiceBpmn)

lazy val exampleInvoiceWorker = project
  .in(file("./05-examples/invoice/03-worker"))
  .settings(projectSettings("example-invoice-worker"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(worker, camunda7Worker, exampleInvoiceBpmn)

lazy val exampleInvoiceC7 = project
  .in(file("./05-examples/invoice/04-c7-spring"))
  .settings(projectSettings("example-invoice-c7"))
  .configure(preventPublication)
  .settings(
    autoImportSetting,
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(bpmn, exampleInvoiceBpmn, camunda)

// not in use
lazy val exampleInvoiceC8 = project
  .in(file("./05-examples/invoice/04-c8-spring"))
  .settings(projectSettings("example-invoice-c8"))
  .configure(preventPublication)
  .settings(
    autoImportSetting
  )
  .dependsOn(bpmn, api, /*exampleInvoiceBpmn,*/ camunda8)

// TWITTER
lazy val exampleTwitter = project
  .in(file("./05-examples/twitter"))
  .settings(projectSettings("example-twitter"))
  .configure(preventPublication)
  .aggregate(
    exampleTwitterBpmn,
    exampleTwitterApi,
    exampleTwitterSimulation,
    exampleTwitterC7,
    exampleTwitterC8
  )

lazy val exampleTwitterBpmn = project
  .in(file("./05-examples/twitter/02-bpmn"))
  .settings(projectSettings("example-twitter-bpmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(bpmn)

lazy val exampleTwitterApi = project
  .in(file("./05-examples/twitter/03-api"))
  .settings(projectSettings("example-twitter-api"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(api, exampleTwitterBpmn)

lazy val exampleTwitterSimulation = project
  .in(file("./05-examples/twitter/03-simulation"))
  .settings(projectSettings("example-twitter-simulation"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .settings(testFrameworks += new TestFramework(
    "camundala.simulation.custom.SimulationTestFramework"
  ))
  .dependsOn(simulation, exampleTwitterBpmn)

lazy val exampleTwitterC7 = project
  .in(file("./05-examples/twitter/04-c7-spring"))
  .settings(projectSettings("example-twitter-c7"))
  .configure(preventPublication)
  .settings(
    autoImportSetting,
    libraryDependencies ++= camundaDependencies :+
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion

)
  .dependsOn(bpmn, exampleTwitterBpmn, camunda)

// not in use
lazy val exampleTwitterC8 = project
  .in(file("./05-examples/twitter/04-c8-spring"))
  .settings(projectSettings("example-twitter-c8"))
  .configure(preventPublication)
  .settings(
    autoImportSetting,
    libraryDependencies +=
      "org.twitter4j" % "twitter4j-core" % twitter4jVersion
  )
  .dependsOn(bpmn, api, /*exampleTwitterBpmn,*/ camunda8)

lazy val exampleDemos = project
  .in(file("./05-examples/demos"))
  .settings(projectSettings("example-demos"))
  .configure(preventPublication)
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

