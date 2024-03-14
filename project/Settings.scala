import Dependencies.mUnitVersion
import sbt.Keys.*
import sbt.*
import xerial.sbt.Sonatype.autoImport.sonatypeRepository

import scala.util.Using

object Settings {

  lazy val projectVersion =
    Using(scala.io.Source.fromFile("version"))(_.mkString.trim).get
  val scala3Version = "3.3.3"
  val org = "io.github.pme123"

  def projectSettings(projName: String) = Seq(
    name := s"camundala-$projName",
    organization := org,
    scalaVersion := scala3Version,
    version := projectVersion,
    scalacOptions ++= Seq(
      //   "-Xmax-inlines:50", // is declared as erased, but is in fact used
      //   "-Wunused:imports"
    ),
    javacOptions ++= Seq("-source", "17", "-target", "17")
  )
  lazy val unitTestSettings = Seq(
    libraryDependencies +="org.scalameta" %% "munit" % mUnitVersion % Test,
    testFrameworks += new TestFramework("munit.Framework")
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
  lazy val developerList = List(
    Developer(
      id = "pme123",
      name = "Pascal Mengelt",
      email = "pascal.mengelt@gmail.com",
      url = url("https://github.com/pme123")
    )
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

}
