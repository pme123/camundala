package camundala
package simulation

import api.CamundaProperty
import bpmn.*
import bpmn.CamundaVariable.{CFile, CFileValueInfo, CInteger, CJson}
import domain.*
import io.gatling.core.Predef.*
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*
import io.circe.parser.*

trait SimulationHelper:

  implicit def config: SimulationConfig = SimulationConfig()

  extension (builder: HttpRequestBuilder)
    def auth(): HttpRequestBuilder =
      config.authHeader(builder)

  def loadVariable(variableName: String): ChainBuilder =
    exec(
      http(s"Load Variable '$variableName'")
        .get(
          s"/history/variable-instance?variableName=$variableName&processInstanceIdIn=#{processInstanceId}&deserializeValues=false"
        )
        .auth()
        .check(checkMaxCount)
        .check(
          extractJson("$[*].value", variableName)
        )
    ).exitHereIfFailed

  def checkMaxCount =
    val maxCount = config.maxCount
    bodyString
      .transformWithSession { (_: String, session: Session) =>
        if (session.attributes.contains("retryCount"))
          assert(
            session("retryCount").as[Int] <= maxCount,
            s"!!! The retryCount reached the maximum of $maxCount"
          )
      }

  def statusCondition(status: Int*): Session => Boolean = session => {
    println("<<< lastStatus: " + session("lastStatus").as[Int])
    println("<<< retryCount: " + session("retryCount").as[Int])
    val lastStatus = session("lastStatus").as[Int]
    !status.contains(lastStatus)
  }

  def taskCondition(): Session => Boolean = session => {
    println("<<< retryCount taskCondition: " + session("retryCount").as[Int])
    session.attributes.get("taskId").contains(null)
  }

  def processInstanceCondition(): Session => Boolean = session => {
    println(
      "<<< retryCount processInstanceCondition: " + session("retryCount")
        .as[Int]
    )
    session.attributes.get("processInstanceId").contains(null)
  }

  // check if the process is  not active
  def processFinishedCondition: Session => Boolean = session =>
    val status = session.attributes.get("processState")
    status.contains("ACTIVE")

  // check if there is a variable in the process with a certain value
  def processReadyCondition(key: String, value: Any): Session => Boolean =
    session =>
      val variable = session.attributes.get(key)
      println(
        s"<<< processReadyCondition: ${variable.getClass} - ${value.getClass} - ${!variable
          .contains(value.toString)}"
      )
      !variable.contains(value)

  // check if there is an incident in the session that contains the expected Message
  def incidentReadyCondition(errorMsg: String): Session => Boolean =
    session =>
      val variable = session.attributes.get("errorMsg")
      println(
        s"<<< incidentReadyCondition: ${variable
          .exists(_.asInstanceOf[String].contains(errorMsg))} - $errorMsg in: $variable"
      )
      variable != null && !variable.contains(null) && !variable.exists(
        _.asInstanceOf[String].contains(errorMsg)
      )

  def extractJson(path: String, key: String) =
    jsonPath(path)
      .ofType[Any]
      .transform { v =>
        println(s"<<< Extracted $key: $v"); v
      } // save the data
      .saveAs(key)

  def extractJsonOptional(path: String, key: String) =
    jsonPath(path)
      .ofType[Any]
      .transform { v =>
        println(s"<<< Extracted optional $key: $v"); v
      }
      .optional
      .saveAs(key)

  val printBody =
    bodyString.transform { b => println(s"<<< Response Body: $b") }

  val printSession: ChainBuilder =
    exec { session =>
      println(s"<<< Session: " + session)
      session
    }

  def checkProps(
      withOverrides: WithTestOverrides[_],
      result: Seq[CamundaProperty]
  ): Boolean =
    withOverrides.testOverrides match
      case Some(TestOverrides(overrides)) =>
        checkO(overrides, result)
      case _ =>
        checkP(withOverrides.camundaToCheckMap, result)

  private def checkO(
      overrides: Seq[TestOverride],
      result: Seq[CamundaProperty]
  ) =
    import TestOverrideType.*
    overrides
      .map {
        case TestOverride(Some(k), Exists, _) =>
          val matches = result.exists(_.key == k)
          if (!matches)
            println(s"!!! $k did NOT exist in $result")
          matches
        case TestOverride(Some(k), NotExists, _) =>
          val matches = !result.exists(_.key == k)
          if (!matches)
            println(s"!!! $k did EXIST in $result")
          matches
        case TestOverride(Some(k), IsEquals, Some(v)) =>
          val r = result.find(_.key == k)
          val matches = r.nonEmpty && r.exists(_.value == v)
          if (!matches)
            println(s"!!! $v ($k) is NOT equal in $r")
          matches
        case TestOverride(Some(k), HasSize, Some(value)) =>
          val r = result.find(_.key == k)
          val matches = r.exists {
            _.value match
              case CJson(j, _) =>
                (toJson(j).asArray, value) match
                  case (Some(vector), CInteger(s, _)) =>
                    vector.size == s
                  case _ =>
                    false
              case _ => false
          }
          if (!matches)
            println(s"!!! $k has NOT Size $value in $r")
          matches
        case _ =>
          println(
            s"!!! Only ${TestOverrideType.values.mkString(", ")} for TestOverrides supported."
          )
          false
      }
      .forall(_ == true)

  def checkOForCollection(
                      overrides: Seq[TestOverride],
                      result: Seq[CamundaVariable | Map[String, CamundaVariable]]
                    ) =
    import TestOverrideType.*
    overrides
      .map {
        case TestOverride(None, HasSize, Some(CInteger(size, _))) =>
          val matches = result.size == size
          if (!matches)
            println(s"!!! Size '${result.size}' of collection is NOT equal to $size in $result")
          matches
        case TestOverride(None, Contains, Some(expected)) =>
          val exp = expected match
            case CJson(jsonStr, _) =>
              parse(jsonStr) match
                case Right(json) =>
                  CamundaVariable.jsonToCamundaValue(json)
                case Left(ex) =>
                  throwErr(s"Problem parsing Json: $jsonStr\n$ex")
            case other => other
          println(s"EXPECTED: $exp")
          val matches = result.contains(exp)
          if (!matches)
            println(s"!!! Result '$result' of collection does NOT contain to $expected")
          matches
        case _ =>
          println(
            s"!!! Only ${TestOverrideType.values.mkString(", ")} for TestOverrides supported."
          )
          false
      }
      .forall(_ == true)

  private def checkP[T <: Product](
      camundaVariableMap: Map[String, CamundaVariable],
      result: Seq[CamundaProperty]
  ): Boolean =
    camundaVariableMap
      .map { case key -> expectedValue =>
        result
          .find(_.key == key)
          .map {
            case CamundaProperty(
                  _,
                  cValue @ CFile(
                    _,
                    cFileValueInfo @ CFileValueInfo(cFileName, _),
                    _
                  )
                ) =>
              val matches = expectedValue match
                case CFile(_, CFileValueInfo(pFileName, _), _) =>
                  cFileName == pFileName
                case o =>
                  false
              if (!matches)
                println(
                  s"<<< cFile: ${cValue.getClass} / expectedFile: ${expectedValue.getClass}"
                )
                println(
                  s"!!! The expected File value '${expectedValue}'\n of $key does not match the result variable: '$cFileValueInfo'."
                )
              matches
            case CamundaProperty(_, CJson(cValue, _)) =>
              val cJson = toJson(cValue)
              val pJson = toJson(expectedValue.value.toString)
              val setCJson = cJson.as[Set[Json]].toOption.getOrElse(cJson)
              val setPJson = pJson.as[Set[Json]].toOption.getOrElse(pJson)
              val matches: Boolean = setCJson == setPJson
              if (!matches)
                println(
                  s"<<< cJson: ${setCJson.getClass} / expectedJson: ${setPJson.getClass}"
                )
                println(
                  s"!!! The expected Json value '$setPJson' of $key does not match the result variable cJson: '$setCJson'."
                )
              matches
            case CamundaProperty(_, cValue) =>
              val matches: Boolean = cValue.value == expectedValue.value
              if (!matches)
                println(
                  s"<<< cValue: ${cValue.getClass} / expectedValue ${expectedValue.getClass}"
                )
                println(
                  s"!!! The expected value '$expectedValue' of $key does not match the result variable '${cValue}'.\n $result"
                )
              matches
          }
          .getOrElse {
            println(
              s"!!! $key does not exist in the result variables.\n $result"
            )
            false
          }
      }
      .forall(_ == true)

  def retryOrFail(
      chainBuilder: ChainBuilder,
      condition: Session => Boolean = statusCondition(200)
  ) = {
    exec {
      _.set("lastStatus", -1)
        .set("retryCount", 0)
    }.doWhile(condition(_)) {
      exec()
        .pause(1.second)
        .exec(chainBuilder)
        .exec { session =>
          if (session("lastStatus").asOption[Int].nonEmpty)
            session.set("lastStatus", session("lastStatus").as[Int])
          else
            session
        }
        .exec(session =>
          session.set("retryCount", 1 + session("retryCount").as[Int])
        )
    }.exitHereIfFailed
  }

  protected def checkIncident(errorMsg: String): Seq[ChainBuilder] =
    Seq(
      exec(_.remove("errorMsg")),
      exec(_.remove("rootCauseIncidentId")),
      retryOrFail(
        getIncident(errorMsg),
        incidentReadyCondition(errorMsg)
      )
    )
  private def getIncident(errorMsg: String): ChainBuilder =
    exec(
      http(s"Check Incident '$errorMsg'")
        .get(
          s"/incident?processInstanceId=#{processInstanceId}"
        )
        .auth()
        .check(checkMaxCount)
        .check(
          extractJsonOptional("$[*].incidentMessage", "errorMsg")
        )
        .check(
          extractJsonOptional("$[*].rootCauseIncidentId", "rootCauseIncidentId")
        )
    ).doIf(session => session.attributes.get("errorMsg").contains(null)) {
      getRootIncident(errorMsg)
    }.exitHereIfFailed

  private def getRootIncident(errorMsg: String): ChainBuilder =
    doIf("#{rootCauseIncidentId.exists()}")(
      exec(session => {
        println(s"We are in the loop: ${session.attributes.get("rootCauseIncidentId")}")
        session
      })
        .exec(
          http(s"Check Root Incident '$errorMsg'")
            .get(
              s"/incident?rootCauseIncidentId=#{rootCauseIncidentId}"
            )
            .auth()
            .check(checkMaxCount)
            .check(
              extractJsonOptional("$[*].incidentMessage", "errorMsg")
            )
        ).exitHereIfFailed
    )
