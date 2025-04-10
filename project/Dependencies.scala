import sbt.*
import sbt.Keys.*

object Dependencies:

  // dependency Versions
  // 00-docs
  // - Laika Plugin
  // 04-helper
  val osLibVersion          = "0.11.4"
  // 01-domain
  val openapiCirceVersion   = "0.11.7"
  val tapirVersion          = "1.11.15"
  val ironCirceVersion      = "2.6.0"
  val mUnitVersion          = "1.1.0"
  val scala3MockVersion     = "0.6.6"
  // 02-bpmn
  // -> domain
  // - osLib
  // 03-api
  // -> bpmn
  val typesafeConfigVersion = "1.4.3"
  val scalaXmlVersion       = "2.3.0"
  // - mUnitVersion
  // 03-dmn
  // -> bpmn
  val sttpClient3Version    = "3.10.3"
  val dmnTesterVersion      = "0.17.9"
  // - mUnitVersion
  // 03-simulation
  // -> bpmn
  val testInterfaceVersion  = "1.0"
  // - sttpClient3
  // 03-worker
  // -> bpmn
  // -mUnitVersion
  val chimneyVersion        = "1.7.3"
  // --- Implementations
  // 04-helper
  val sardineWebDavVersion  = "5.13"
  // 04-worker-c7spring
  // -> worker
  val camundaVersion        = "7.22.0" // external task client
  val jaxbApiVersion        = "4.0.2"  // needed by the camunda client 7.21?!
  val scaffeineV            = "5.3.0"  // caching
  val zioVersion            = "2.1.16" // zio
  val logbackVersion        = "1.4.11" // logging
  // - sttpClient3

  // --- Experiments
  // 04-c7-spring
  // -> bpmn
  val camundaSpinVersion    = "7.22.0"
  // camunda // server spring-boot
  // 04-c8-spring
  // -> bpmn
  val scalaJacksonVersion   = "2.18.3"
  val camunda8Version       = "8.5.17"
  val springBootVersion     = "3.3.9"
  val nettyVersion          = "4.1.119.Final"
  val swaggerOpenAPIVersion = "2.1.25"
  // examples
  val h2Version             = "2.3.232"
  val twitter4jVersion      = "4.1.2"
  val groovyVersion         = "3.0.24"

  lazy val jaxbApiDependency   = "jakarta.xml.bind" % "jakarta.xml.bind-api" % jaxbApiVersion
  lazy val scaffeineDependency = // token caching
    "com.github.blemale" %% "scaffeine" % scaffeineV // caching

  lazy val tapirDependencies = Seq(
    "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs" % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"   % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-redoc-bundle" % tapirVersion,
    "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % openapiCirceVersion,
    // "io.circe" %% "circe-generic" % circeVersion,
    "io.github.iltotore"            %% "iron-circe"         % ironCirceVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-iron"         % tapirVersion
  )

  lazy val camunda7workerDependencies    = Seq(
    "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-external-task-client" % camundaVersion,
    "org.springframework.boot"   % "spring-boot-starter-oauth2-client"                    % springBootVersion,
    "org.springframework.boot"   % "spring-boot-starter-security"                         % springBootVersion,
    "org.springframework.boot"   % "spring-boot-starter-web"                              % springBootVersion
  )
  lazy val camunda7ZioWorkerDependencies = Seq(
    "org.camunda.bpm" % "camunda-external-task-client" % camundaVersion
  )
  lazy val sttpDependency                = "com.softwaremill.sttp.client3" %% "circe" % sttpClient3Version

  val zeebeDependencies = Seq(
    "org.springframework.boot"      % "spring-boot-starter"         % springBootVersion,
    "org.springframework.boot"      % "spring-boot-starter-webflux" % springBootVersion,
    "io.camunda.spring"             % "spring-boot-starter-camunda" % camunda8Version,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"        % scalaJacksonVersion
  ) // .map(_.exclude("org.slf4j", "slf4j-api"))

  val zeebeJavaClientDependency =
    "io.camunda" % "zeebe-client-java" % "8.7.0-alpha2" // TODO camunda8Version,
  val zioDependency =
    "dev.zio" %% "zio" % zioVersion
  val zioTestDependencies =
    Seq(
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    )

  val zioSlf4jDependency = "dev.zio"       %% "zio-logging-slf4j" % zioVersion
  val logbackDependency  = "ch.qos.logback" % "logback-classic"   % logbackVersion % Runtime

  // examples
  val camundaDependencies = Seq(
    "org.springframework.boot"   % "spring-boot-starter-web"                % springBootVersion exclude (
      "org.slf4j",
      "slf4j-api"
    ),
    "org.springframework.boot"   % "spring-boot-starter-jdbc"               % springBootVersion exclude (
      "org.slf4j",
      "slf4j-api"
    ),
    "io.netty"                   % "netty-all"                              % nettyVersion, // needed for Spring Boot Version > 2.5.*
    "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-rest"   % camundaVersion,
    "org.camunda.bpm.springboot" % "camunda-bpm-spring-boot-starter-webapp" % camundaVersion,
    // json support
    "org.camunda.bpm"            % "camunda-engine-plugin-spin"             % camundaVersion,
    "org.camunda.spin"           % "camunda-spin-dataformat-json-jackson"   % camundaSpinVersion,
    jaxbApiDependency,
    // groovy support
    "org.codehaus.groovy"        % "groovy-jsr223"                          % groovyVersion,
    "com.h2database"             % "h2"                                     % h2Version
  ) // .map(_.exclude("org.slf4j", "slf4j-api"))

  lazy val osLib          = "com.lihaoyi"         %% "os-lib"         % osLibVersion
  lazy val chimney        = "io.scalaland"        %% "chimney"        % chimneyVersion
  lazy val swaggerOpenAPI = "io.swagger.parser.v3" % "swagger-parser" % swaggerOpenAPIVersion
  lazy val sardineWebDav  = "com.github.lookfirst" % "sardine"        % sardineWebDavVersion
end Dependencies
