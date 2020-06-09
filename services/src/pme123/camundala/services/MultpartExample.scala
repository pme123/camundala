package pme123.camundala.services

import java.io.{File, PrintWriter}

import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.client.{NothingT, SttpBackend, basicRequest, multipart, _}
import sttp.model._
import sttp.tapir.Endpoint
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import zio.duration._
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.{Task, ZEnv, _}

/**
  * Working example for Multipart Form
  * - problem is that file part name is dynamic in Camunda Modeler
  *
  * THIS IS NOT USED CODE
  *
  */
object MultipartFormUploadAkkaServer extends zio.App {

  // the class representing the multipart data
  //
  // parts can be referenced directly; if part metadata is needed, we define the type wrapped with Part[_].
  //
  // note that for binary parts need to be buffered either in-memory or in the filesystem anyway (the whole request
  // has to be read to find out what are the parts), so handling multipart requests in a purely streaming fashion is
  // not possible
  case class UserProfile(`na-me`: String, hobby: Option[String], age: Int, photo: Part[File])

  //implicit def conf: Configuration = Configuration.default.withKebabCaseMemberNames


  // corresponds to: POST /user/profile [multipart form data with fields name, hobby, age, photo]
  val setProfile: Endpoint[UserProfile, Unit, String, Nothing] =
    endpoint.post.in("user" / "profile")
      .in(multipartBody[UserProfile])
      .out(stringBody)

  val setDummy: Endpoint[Unit, Unit, String, Nothing] =
    endpoint.get.in("user").out(stringBody)


  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val setProfileRoute: HttpRoutes[Task] = setProfile.toRoutes { data =>
    val response = s"Received: ${data.`na-me`} / ${data.hobby} / ${data.age} / ${data.photo.fileName} (${data.photo.body.length()})"
    data.photo.body.delete()
    UIO(response)
  }

  val setDommyRoute = setDummy.toRoutes { _ =>
    UIO("hello")
  }

  // starting the server
  lazy val server =
    ZIO.runtime[Any]
      .flatMap {
        implicit rts =>

          import org.http4s.implicits._
          BlazeServerBuilder[Task]
            .bindHttp(8080, "localhost")
            .withHttpApp((setDommyRoute <+> setProfileRoute).orNotFound)
            .serve
            .compile
            .drain
      }
  lazy val client = ZIO {
    println("Server up: ")

    val testFile = File.createTempFile("user-123", ".jpg")
    val pw = new PrintWriter(testFile);
    pw.write("This is not a photo");
    pw.close()
    println("Got testFile: " + testFile)

    // testing
    implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
    val result = try {
      basicRequest
        .response(asStringAlways)
        .post(uri"http://localhost:8080/user/profile")
        .multipartBody(multipart("na-me", "Frodo"), multipart("hobby", "hiking"), multipart("age", "33"), multipartFile("photo", testFile))
        .send()
        .body
    }
    catch {
      case ex => ex.printStackTrace()
    }
    println("Got result: " + result)

    assert(result == s"Received: Frodo / Some(hiking) / 33 / Some(${testFile.getName}) (19)")
  }.mapError(es => UIO(es.printStackTrace()) *> zio.console.putStrLn(es.toString))


  def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    (
      (for {_ <- server.forever.fork
            _ <- zio.console.putStrLn("START")
            _ <- clock.sleep(3.seconds)
            _ <- client
            _ <- zio.console.putStrLn("MIDDLE")
            //  _ <- clock.sleep(30.seconds)
            _ <- zio.console.putStrLn("END")

            } yield ())
        .fold(_ => ExitCode.failure,
          _ => ExitCode.success)
      )

}
