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
    gateway,
    api,
    dmn,
    simulation,
    worker,
    helper,
    // implementations
    gatewayZio,
    camunda7Worker,
    camunda8Worker,
    camunda7ZioWorker,
    // experiments
    camunda,  // not in use
    camunda8, // not in use
    // examples
    // invoice
    exampleInvoiceBpmn,
    exampleInvoiceApi,
    exampleInvoiceDmn,
    exampleInvoiceSimulation,
    exampleInvoiceWorker,
    exampleInvoiceC7,
    exampleInvoiceC8,
    // twitter
    exampleTwitterBpmn,
    exampleTwitterApi,
    exampleTwitterSimulation,
    exampleTwitterC7,
    exampleTwitterC8,
    // demos
    exampleDemosBpmn,
    exampleDemosApi,
    exampleDemosDmn,
    exampleDemosSimulation,
    exampleDemosWorker,
    exampleDemosC7,
    // myCompany
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
lazy val domain  = project
  .in(file("./01-domain"))
  .configure(publicationSettings)
  .settings(projectSettings("domain"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= tapirDependencies ++ Seq(
      osLib,
      chimney // mapping
    ),
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
lazy val gateway = project
  .in(file("./02-gateway"))
  .configure(publicationSettings)
  .settings(projectSettings("gateway"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      zioDependency
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
  .dependsOn(gateway)

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
  .dependsOn(gateway)

lazy val worker = project
  .in(file("./03-worker"))
  .configure(publicationSettings)
  .settings(
    projectSettings("worker"),
    unitTestSettings,
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      scaffeineDependency,
      zioDependency,
      zioSlf4jDependency
    )
  )
  .dependsOn(gateway)

lazy val gatewayZio = project
  .in(file("./03-gateway-zio"))
  .configure(publicationSettings)
  .settings(projectSettings("gateway-zio"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++= Seq(
      sttpDependency,
      scaffeineDependency,
      zioDependency,
      zioSlf4jDependency
    )
  )
  .dependsOn(gateway)

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
  .dependsOn(gateway)

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
      jaxbApiDependency
    ) ++ camunda7workerDependencies ++ zioTestDependencies
  )
  .dependsOn(worker)

lazy val camunda7ZioWorker = project
  .in(file("./04-worker-c7zio"))
  .configure(publicationSettings)
  .settings(projectSettings("camunda7-zio-worker"))
  .settings(unitTestSettings)
  .settings(
    autoImportSetting,
    libraryDependencies ++=
      camunda7ZioWorkerDependencies ++ zioTestDependencies
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
      zeebeJavaClientDependency
    ) ++ zioTestDependencies
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
  .dependsOn(gateway)

lazy val camunda8 = project
  .in(file("./04-c8-spring"))
  .configure(preventPublication)
  .settings(projectSettings("camunda8"))
  .settings(
    autoImportSetting,
    libraryDependencies ++= zeebeDependencies
  )
  .dependsOn(gateway)
// end not in use

// EXAMPLES
// INVOICE
lazy val exampleInvoiceBpmn = project
  .in(file("./05-examples/invoice/02-bpmn"))
  .settings(projectSettings("example-invoice-bpmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(gateway)

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
  .dependsOn(gateway, exampleInvoiceBpmn, camunda)

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
lazy val exampleTwitterBpmn = project
  .in(file("./05-examples/twitter/02-bpmn"))
  .settings(projectSettings("example-twitter-bpmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(gateway)

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
  .dependsOn(gateway, exampleTwitterBpmn, camunda)

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
  .dependsOn(gateway, api, /*exampleTwitterBpmn,*/ camunda8)

// DEMO
lazy val exampleDemosBpmn = project
  .in(file("./05-examples/demos/02-bpmn"))
  .settings(projectSettings("example-demos-bpmn"))
  .configure(preventPublication)
  .settings(autoImportSetting)
  .dependsOn(gateway)

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
  .dependsOn(gateway, exampleDemosBpmn, camunda)

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
