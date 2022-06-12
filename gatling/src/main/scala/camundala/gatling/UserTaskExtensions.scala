package camundala
package gatling

import camundala.bpmn.CamundaVariable.CInteger
import camundala.api.*
import camundala.bpmn.*
import io.circe.parser.parse
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.language.implicitConversions

trait UserTaskExtensions:

  implicit def defaultUserTaskSteps[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](userTask: UserTask[In, Out]): WithConfig[Seq[ChainBuilder]] =
    userTask.getAndComplete()

  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](userTask: UserTask[In, Out])

    def getAndComplete(): WithConfig[Seq[ChainBuilder]] = {
      Seq(
        exec(_.set("taskId", null)),
        retryOrFail(
          exec(task()).exitHereIfFailed,
          taskCondition()
        ),
        exec(checkForm()).exitHereIfFailed,
        exec(completeTask()).exitHereIfFailed
      )
    }

    private def task(): WithConfig[HttpRequestBuilder] =
      http(s"Get Tasks ${userTask.id}")
        .get("/task?processInstanceId=#{processInstanceId}")
        .auth()
        .check(checkMaxCount)
        .check(
          extractJsonOptional("$[*].id","taskId")
        )

    private def checkForm(): WithConfig[HttpRequestBuilder] =
      http(s"Check Form ${userTask.id}")
        .get(
          "/process-instance/#{processInstanceId}/variables?deserializeValues=false"
        )
        // Removed as Jsons were returned with type String?! Check History 8.1.22 19:00h
        // .get("/task/#{taskId}/form-variables?deserializeValues=false")
        .auth()
        .check(
          bodyString
            .transform { body =>
              parse(body)
                .flatMap(_.as[FormVariables]) match {
                case Right(value) =>
                  checkProps(userTask.in, CamundaProperty.from(value))
                case Left(exc) =>
                  s"\n!!! Problem parsing Result Body to a List of FormVariables.\n$exc\n$body"
              }
            }
            .is(true)
        )

    private def completeTask(): WithConfig[HttpRequestBuilder] =
      http(s"Complete Task ${userTask.id}")
        .post(s"/task/#{taskId}/complete")
        .auth()
        .queryParam("deserializeValues", false)
        .body(
          StringBody(
            CompleteTaskOut(
              CamundaVariable.toCamunda(userTask.out)
            ).asJson.deepDropNullValues.toString
          )
        )

    def exists(
        key: String
    ): UserTask[TestOverrides, Out] =
      overrideUserTask(key, TestOverrideType.Exists)

    def notExists(
        key: String
    ): UserTask[TestOverrides, Out] =
      overrideUserTask(key, TestOverrideType.NotExists)

    def isEquals(
        key: String,
        value: Any
    ): UserTask[TestOverrides, Out] =
      overrideUserTask(
        key,
        TestOverrideType.IsEquals,
        Some(CamundaVariable.valueToCamunda(value))
      )

    def hasSize(
        key: String,
        size: Int
    ): UserTask[TestOverrides, Out] =
      overrideUserTask(
        key,
        TestOverrideType.HasSize,
        Some(CInteger(size))
      )

    def overrideUserTask(
        key: String,
        overrideType: TestOverrideType,
        value: Option[CamundaVariable] = None
    ): UserTask[TestOverrides, Out] =
      UserTask(
        InOutDescr(
          userTask.id,
          addOverride(userTask, key, overrideType, value),
          userTask.out,
          userTask.descr
        )
      )
  end extension
