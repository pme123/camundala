package camundala
package gatling

import camundala.api.*
import camundala.bpmn.*
import camundala.bpmn.CamundaVariable.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.*
import io.gatling.http.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.circe.{Decoder, Encoder}
import io.circe.Json.JArray
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.CheckBuilder.Validate
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.http.Predef.http
import io.gatling.http.check.HttpCheck
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

// context function def f(using BpmnModelInstance): T
type WithConfig[T] = SimulationConfig ?=> T

case class TestOverride(
    key: String,
    overrideType: TestOverrideType, // problem with encoding?! derives JsonTaggedAdt.PureEncoder
    value: Option[CamundaVariable] = None
)

case class TestOverrides(overrides: Seq[TestOverride]) //Seq[TestOverride])

enum TestOverrideType derives JsonTaggedAdt.PureEncoder:
  case Exists, NotExists, IsEquals, HasSize

def addOverride[
    T <: Product
](
    model: T,
    key: String,
    overrideType: TestOverrideType,
    value: Option[CamundaVariable] = None
): TestOverrides =
  val testOverride = TestOverride(key, overrideType, value)
  val newOverrides: Seq[TestOverride] = model match
    case TestOverrides(overrides) =>
      overrides :+ testOverride
    case other =>
      Seq(testOverride)
  TestOverrides(newOverrides)

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
    "<<< retryCount processInstanceCondition: " + session("retryCount").as[Int]
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
      s"<<< processReadyCondition: ${variable.getClass} - ${value.getClass} - ${variable != null && !variable
        .contains(value)}"
    )
    variable != null && !variable.contains(value)

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

def checkProps[T <: Product: Encoder](
    out: T,
    result: Seq[CamundaProperty]
): Boolean =
  out match
    case TestOverrides(overrides) =>
      checkO(overrides, result)
    case product =>
      checkP(product, result)

private def checkO(overrides: Seq[TestOverride], result: Seq[CamundaProperty]) =
  import TestOverrideType.*
  overrides
    .map {
      case TestOverride(k, Exists, _) =>
        val matches = result.exists(_.key == k)
        if (!matches)
          println(s"!!! $k did NOT exist in $result")
        matches
      case TestOverride(k, NotExists, _) =>
        val matches = !result.exists(_.key == k)
        if (!matches)
          println(s"!!! $k did EXIST in $result")
        matches
      case TestOverride(k, IsEquals, Some(v)) =>
        val r = result.find(_.key == k)
        val matches = r.nonEmpty && r.exists(_.value == v)
        if (!matches)
          println(s"!!! $v ($k) is NOT equal in $r")
        matches
      case TestOverride(k, HasSize, Some(value)) =>
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
      case other =>
        println(
          s"!!! Only ${TestOverrideType.values.mkString(", ")} for TestOverrides supported"
        )
        false
    }
    .forall(_ == true)

private def checkP[T <: Product: Encoder](
    product: T,
    result: Seq[CamundaProperty]
): Boolean =
  CamundaVariable
    .toCamunda(product)
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
                s"<<< cJson: ${cValue.getClass} / expectedJson: ${expectedValue.value.getClass}"
              )
              println(
                s"!!! The expected Json value '${toJson(expectedValue.value.toString)}' of $key does not match the result variable cJson: '${toJson(cValue)}'."
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

def checkMaxCount(using config: SimulationConfig) =
  val maxCount = config.maxCount
  bodyString
    .transformWithSession { (_: String, session: Session) =>
      assert(
        session("retryCount").as[Int] <= maxCount,
        s"!!! The retryCount reached the maximum of $maxCount"
      )
    }

def loadVariable(variableName: String): WithConfig[ChainBuilder] =
  exec(
    http(s"Load Variable '$variableName'")
      .get(
        s"/history/variable-instance?variableName=$variableName&processInstanceIdIn=#{processInstanceId}&deserializeValues=false"
      )
      .auth()
      .check(checkMaxCount)
      .check(
        extractJsonOptional("$[*].value", variableName)
      )
  ).exitHereIfFailed

def getIncident(errorMsg: String): WithConfig[ChainBuilder] =
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

def getRootIncident(errorMsg: String): WithConfig[ChainBuilder] =
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

extension (builder: HttpRequestBuilder)
  def auth(): WithConfig[HttpRequestBuilder] =
    summon[SimulationConfig].authHeader(builder)

object TestOverrides:
  given Schema[TestOverrides] = Schema.derived
  given Encoder[TestOverrides] = deriveEncoder
  given Decoder[TestOverrides] = deriveDecoder

object TestOverride:

  given Schema[TestOverride] = Schema.derived
  given Encoder[TestOverride] = deriveEncoder
  given Decoder[TestOverride] = deriveDecoder

given Schema[TestOverrideType] =
  Schema.derived
given Encoder[TestOverrideType] =
  deriveEncoder
given Decoder[TestOverrideType] =
  deriveDecoder
