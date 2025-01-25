import Dependencies.*
import Settings.*
import laika.ast.Path.Root
import laika.config.*
import laika.format.Markdown.GitHubFlavor
import laika.helium.Helium
import laika.helium.config.{Favicon, HeliumIcon, IconLink}

ThisBuild / versionScheme          := Some("early-semver")
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / evictionErrorLevel     := Level.Warn
//Problems in Scala 3.5.0: ThisBuild / usePipelining := true

lazy val root = project
  .in(file("."))
  .configure(preventPublication)
  .settings(
    name          := "camundala",
    organization  := org,
    sourcesInBase := false
  )
  .aggregate(
    docs,
    domain,
    bpmn,
    api,
    dmn,
    simulation,
    worker,
    helper,
    // implementations
    camunda7Worker,
    camunda8Worker,
    camunda7ZioWorker,
    // experiments
    camunda,  // not in use
    camunda8, // not in use
    // examples
    exampleInvoice,
    exampleTwitter,
    exampleDemos,
    exampleMyCompany
  )

// general independent
lazy val docs =
  (project in file("./00-docs"))
    .configure(preventPublication)
    .settings(
      projectSettings("docs"),
      autoImportSetting,
      laikaSettings,
      mdocSettings
    )
    .enablePlugins(LaikaPlugin, MdocPlugin)
    .dependsOn(helper)

// layer 01
lazy val domain = project
  .in(file("./01-domain"))
  .configure(publicationSettings)
  .settings(projectSettings("domain"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= tapirDependencies,
    buildInfoPackage := "camundala",
    buildInfoKeys    := Seq[BuildInfoKey](
      organization,
      name,
      version,
      scalaVersion,
      sbtVersion,
      BuildInfoKey("camundaVersion", camundaVersion),
      BuildInfoKey("springBootVersion", springBootVersion),
      BuildInfoKey("jaxbApiVersion", jaxbApiVersion),
      BuildInfoKey("osLibVersion", osLibVersion),
      BuildInfoKey("mUnitVersion", mUnitVersion),
      BuildInfoKey("dmnTesterVersion", dmnTesterVersion)
    )
  ).enablePlugins(BuildInfoPlugin)
// layer 02
lazy val bpmn   = project
  .in(file("./02-bpmn"))
  .configure(publicationSettings)
  .settings(projectSettings("bpmn"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      osLib,
      chimney // mapping
    )
  )
  .dependsOn(domain)

// layer 03
lazy val api = project
  .in(file("./03-api"))
  .configure(publicationSettings)
  .settings(projectSettings("api"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++=
      Seq(
        "org.scala-lang.modules" %% "scala-xml" % scalaXmlVersion,
        "com.typesafe"            % "config"    % typesafeConfigVersion
      )
  )
  .dependsOn(bpmn)

lazy val dmn = project
  .in(file("./03-dmn"))
  .configure(publicationSettings)
  .settings(projectSettings("dmn"))
  .settings(unitTestSettings)
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
  .settings(
    projectSettings("worker"),
    unitTestSettings,
    autoImportSetting,
    libraryDependencies ++= Seq(
      zioDependency,
      zioSlf4jDependency
    )
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
lazy val helper = project
  .in(file("./04-helper"))
  .configure(publicationSettings)
  .settings(projectSettings("helper"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(osLib, swaggerOpenAPI, sardineWebDav)
  ).dependsOn(api, simulation, worker)

lazy val camunda7Worker = project
  .in(file("./04-worker-c7spring"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda7-worker"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      jaxbApiDependency,
      scaffeineDependency
    ) ++ camunda7workerDependencies
  )
  .dependsOn(worker)

lazy val camunda7ZioWorker = project
  .in(file("./04-worker-c7zio"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda7-zio-worker"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      scaffeineDependency
    ) ++ camunda7ZioWorkerDependencies
  )
  .dependsOn(worker)
lazy val camunda8Worker    = project
  .in(file("./04-worker-c8zio"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda8-worker"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      zeebeJavaClientDependency
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
      "org.camunda.bpm"            % "camunda-engine-spring-6"                              % camundaVersion, // listeners
      "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-external-task-client" % camundaVersion,
      "org.camunda.bpm"            % "camunda-engine-plugin-spin"                           % camundaVersion,
      "org.camunda.spin"           % "camunda-spin-dataformat-json-jackson"                 % camundaSpinVersion
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
  .dependsOn(api, exampleInvoiceBpmn, camunda8)

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

// INVOICE
lazy val exampleDemos = project
  .in(file("./05-examples/demos"))
  .settings(projectSettings("example-demos"))
  .configure(preventPublication)
  .aggregate(
    exampleDemosBpmn,
    exampleDemosApi,
    exampleDemosDmn,
    exampleDemosSimulation,
    exampleDemosWorker,
    exampleDemosC7
  )

lazy val exampleDemosBpmn = project
  .in(file("./05-examples/demos/02-bpmn"))
  .settings(projectSettings("example-demos-bpmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(bpmn)

lazy val exampleDemosApi = project
  .in(file("./05-examples/demos/03-api"))
  .settings(projectSettings("example-demos-api"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(api, exampleDemosBpmn)

lazy val exampleDemosDmn = project
  .in(file("./05-examples/demos/03-dmn"))
  .settings(projectSettings("example-demos-dmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(dmn, exampleDemosBpmn)

lazy val exampleDemosSimulation = project
  .in(file("./05-examples/demos/03-simulation"))
  .settings(projectSettings("example-demos-simulation"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .settings(testFrameworks += new TestFramework(
    "camundala.simulation.custom.SimulationTestFramework"
  ))
  .dependsOn(simulation, exampleDemosBpmn)

lazy val exampleDemosWorker = project
  .in(file("./05-examples/demos/03-worker"))
  .settings(projectSettings("example-demos-worker"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(worker, camunda7Worker, camunda8Worker, camunda7ZioWorker, exampleDemosBpmn)

lazy val exampleDemosC7 = project
  .in(file("./05-examples/demos/04-c7-spring"))
  .settings(projectSettings("example-demos-c7"))
  .configure(preventPublication)
  .settings(
    autoImportSetting,
    libraryDependencies ++= camundaDependencies
  )
  .dependsOn(bpmn, exampleDemosBpmn, camunda)

// start company docs example
import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

val config                = ConfigFactory.parseFile(new File("05-examples/myCompany/CONFIG.conf"))
val currentVersion        = config.getString("release.tag")
val released              = config.getBoolean("released")
val olderVersions         = config.getList("releases.older").asScala
val versions              = Versions
  .forCurrentVersion(
    Version(currentVersion, currentVersion)
      .withLabel(if (released) "Stable" else "Dev")
  ).withOlderVersions(olderVersions.map(_.unwrapped().toString).map(v => Version(v, v)) *)
lazy val exampleMyCompany = project
  .in(file("./05-examples/myCompany"))
  .settings(projectSettings("example-exampleDemos"))
  .settings(
    laikaConfig      := LaikaConfig.defaults
      .withConfigValue("projectVersion", projectVersion)
      .withConfigValue(LaikaKeys.excludeFromNavigation, Seq(Root))
      .withRawContent
    //  .failOnMessages(MessageFilter.None)
    //  .renderMessages(MessageFilter.None)
    ,
    laikaExtensions  := Seq(GitHubFlavor, SyntaxHighlighting),
    laikaTheme       := Helium.defaults.site
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
    buildInfoKeys    := Seq[BuildInfoKey](
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
  .dependsOn(api, helper)
