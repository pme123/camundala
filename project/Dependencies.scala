import sbt.*
import sbt.Keys.*

object Dependencies:

  // dependency Versions
  // 00-documentation
  // - Laika Plugin
  // 04-helper
  val osLibVersion = "0.10.5"
  // 01-domain
  val openapiCirceVersion = "0.11.3"
  val tapirVersion = "1.11.8"
  val ironCirceVersion = "2.6.0"
  val mUnitVersion = "1.0.1"
  // 02-bpmn
  // -> domain
  // - osLib
  // 03-api
  // -> bpmn
  val typesafeConfigVersion = "1.4.3"
  val scalaXmlVersion = "2.3.0"
  // - mUnitVersion
  // 03-dmn
  // -> bpmn
  val sttpClient3Version = "3.9.8"
  val dmnTesterVersion = "0.17.9"
  // - mUnitVersion
  // 03-simulation
  // -> bpmn
  val testInterfaceVersion = "1.0"
  // - sttpClient3
  // 03-worker
  // -> bpmn
  // -mUnitVersion
  val chimneyVersion = "1.4.0"
  // --- Implementations
  // 04-worker-c7spring
  // -> worker
  val camundaVersion = "7.21.0" // external task client
  val jaxbApiVersion = "4.0.2" // needed by the camunda client 7.21?!
  // - sttpClient3

  // --- Experiments
  // 04-c7-spring
  // -> bpmn
  val camundaSpinVersion = "1.23.0"
  // camunda // server spring-boot
  // 04-c8-spring
  // -> bpmn
  val scalaJacksonVersion = "2.17.2"
  val zeebeVersion = "8.5.10"
  val springBootVersion = "3.3.3"
  val swaggerOpenAPIVersion = "2.1.22"
  // examples
  val h2Version = "2.3.232"
  val twitter4jVersion = "4.1.2"
  val groovyVersion = "3.0.22"

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

  val zeebeDependencies = Seq(
    "org.springframework.boot" % "spring-boot-starter" % springBootVersion,
    "org.springframework.boot" % "spring-boot-starter-webflux" % springBootVersion,
    "io.camunda.spring" % "spring-boot-starter-camunda" % zeebeVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % scalaJacksonVersion
  ).map(_.exclude("org.slf4j", "slf4j-api"))

  // examples
  val camundaDependencies = Seq(
    "org.springframework.boot" % "spring-boot-starter-web" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
    "org.springframework.boot" % "spring-boot-starter-jdbc" % springBootVersion exclude ("org.slf4j", "slf4j-api"),
    "io.netty" % "netty-all" % "4.1.110.Final", // needed for Spring Boot Version > 2.5.*
    "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-rest" % camundaVersion,
    "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaVersion,
    // json support
    "org.camunda.bpm" % "camunda-engine-plugin-spin" % camundaVersion,
    "org.camunda.spin" % "camunda-spin-dataformat-json-jackson" % camundaSpinVersion,
    // groovy support
    "org.codehaus.groovy" % "groovy-jsr223" % groovyVersion,
    "com.h2database" % "h2" % h2Version
  ) // .map(_.exclude("org.slf4j", "slf4j-api"))
end Dependencies
