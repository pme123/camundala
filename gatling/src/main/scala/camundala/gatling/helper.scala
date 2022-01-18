package camundala
package gatling

import camundala.api.{CamundaProperty, CamundaVariable}
import camundala.api.CamundaVariable.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import camundala.bpmn.*
import camundala.domain.*
import camundala.gatling.TestOverrideType.*
import io.circe.{Decoder, Encoder}
import io.circe.Json.JArray
import sttp.tapir.Schema
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.jdk.CollectionConverters.*

case class TestOverride(
    key: String,
    overrideType: TestOverrideType, // problem with encoding?! derives JsonTaggedAdt.PureEncoder
    value: Option[CamundaVariable] = None
)

case class TestOverrides(overrides: Seq[TestOverride]) //Seq[TestOverride])

enum TestOverrideType:
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
  println("<<< retryCount: " + session("retryCount").as[Int])
  session.attributes.get("taskId").contains(null)
}

// check if the process is  not active
def processCondition: Session => Boolean = session =>
  println("<<< retryCount: " + session("retryCount").as[Int])
  val status = session.attributes.get("processState")
  status.contains("ACTIVE")

def extractJson(path: String, key: String) =
  jsonPath(path)
    .ofType[String]
    .transform { v =>
      println(s"<<< Extracted $key: $v"); v
    } // save the data
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
      check(overrides, result)
    case product =>
      check(product, result)

private def check(overrides: Seq[TestOverride], result: Seq[CamundaProperty]) =
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

private def check[T <: Product: Encoder](
    product: T,
    result: Seq[CamundaProperty]
): Boolean =
  CamundaVariable
    .toCamunda(product)
    .map { case key -> pValue =>
      result
        .find(_.key == key)
        .map {
          case CamundaProperty(_, cValue @ CFile(_, cFileValueInfo @ CFileValueInfo(cFileName, _), _)) =>
            val matches = pValue match
              case CFile(_, CFileValueInfo(pFileName, _), _) =>
                cFileName == pFileName
              case o =>
                false
            if (!matches)
              println(
                s"cFile: ${cValue.getClass} / pFile: ${pValue.getClass}"
              )
              println(
                s"!!! The File value '${pValue}'\n of $key does not match the result variable: '$cFileValueInfo'."
              )
            matches
          case CamundaProperty(_, CJson(cValue, _)) =>
            val cJson = toJson(cValue).deepDropNullValues
            val pJson = toJson(pValue.value.toString).deepDropNullValues
            val setCJson = cJson.as[Set[Json]].toOption.getOrElse(cJson)
            val setPJson = pJson.as[Set[Json]].toOption.getOrElse(pJson)
            val matches: Boolean = setCJson == setPJson
            if (!matches)
              println(s"cJson: ${cValue.getClass} / pJson: ${pValue.value.getClass}")
              println(
                s"!!! The pJson value '${toJson(pValue.value.toString)}' of $key does not match the result variable cJson: '${toJson(cValue)}'."
              )
            matches
          case CamundaProperty(_, cValue) =>
            val matches: Boolean = cValue.value == pValue.value
            if (!matches)
              println(s"cValue: ${cValue.getClass} / pValue ${pValue.getClass}")
              println(
                s"!!! The value '$pValue' of $key does not match the result variable '${cValue}'.\n $result"
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
