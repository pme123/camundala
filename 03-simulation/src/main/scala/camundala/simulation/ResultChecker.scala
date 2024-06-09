package camundala
package simulation

import camundala.bpmn.*
import camundala.bpmn.CamundaVariable.*
import camundala.domain.*
import camundala.simulation.TestOverrideType.*
import io.circe.*
import io.circe.parser.*

import scala.collection.mutable.ListBuffer
import scala.deriving.Mirror.Sum

trait ResultChecker:

  def checkProps(
      withOverrides: WithTestOverrides[?],
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
    overrides
      .map {
        case TestOverride(Some(k), Exists, _) =>
          val matches = result.exists(_.key == k)
          if !matches then
            println(s"!!! $k did NOT exist in $result")
          matches
        case TestOverride(Some(k), NotExists, _) =>
          val matches = !result.exists(_.key == k)
          if !matches then
            println(s"!!! $k did EXIST in $result")
          matches
        case TestOverride(Some(k), IsEquals, Some(v)) =>
          val r = result.find(_.key == k)
          val matches = r.nonEmpty && r.exists(_.value == v)
          if !matches then
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
          if !matches then
            println(s"!!! $k has NOT Size $value in $r")
          matches
        case TestOverride(Some(k), Contains, Some(value)) =>
          val r = result.find(_.key == k)
          val matches = r.exists {
            _.value match
              case CJson(j, _) =>
                toJson(j).asArray match
                  case Some(vector) =>
                    vector
                      .exists(x =>
                        value match
                          case CString(v, _) =>
                            x.asString.contains(v)
                          case CInteger(v, _) => x.asNumber.contains(v)
                          case CBoolean(v, _) => x.asBoolean.contains(v)
                          case _ => x.toString == value.value.toString
                      )
                  case _ =>
                    false
              case _ => false
          }
          if !matches then
            println(s"!!! $k does NOT contains $value in $r")
          matches
        case _ =>
          println(
            s"!!! Only ${TestOverrideType.values.mkString(", ")} for TestOverrides supported."
          )
          false
      }
      .forall(_ == true)

  // DMN
  def checkOForCollection(
      overrides: Seq[TestOverride],
      result: Seq[CamundaVariable | Map[String, CamundaVariable]]
  ) =
    overrides
      .map {
        case TestOverride(None, HasSize, Some(CInteger(size, _))) =>
          val matches = result.size == size
          if !matches then
            println(
              s"!!! Size '${result.size}' of collection is NOT equal to $size in $result"
            )
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
          val matches = result.contains(exp)
          if !matches then
            println(
              s"!!! Result '$result' of collection does NOT contain to $expected"
            )
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
      .map {
        case key -> CNull => // must not be in the result
          result
            .find(p =>
              p.key == key && p.value != CNull
            ) // it is only ok, if the value is null
            .map(p =>
              println(
                s"!!! The variable '$key' (value: '${p.value.value}') exists in the result - but is NOT expected."
              )
            )
            .isEmpty
        case key -> expectedValue =>
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
                if !matches then
                  println(
                    s"<<< cFile: ${cValue.getClass} / expectedFile: ${expectedValue.getClass}"
                  )
                  println(
                    s"!!! The expected File value '${expectedValue}'\n of $key does not match the result variable: '$cFileValueInfo'."
                  )
                end if
                matches
              case CamundaProperty(key, CJson(cValue, _)) =>
                val resultJson = toJson(cValue)
                val expectedJson = toJson(expectedValue.value.toString)
                checkJson(expectedJson, resultJson, key)
              case CamundaProperty(_, cValue) =>
                val matches: Boolean = cValue.value == expectedValue.value
                if !matches then
                  println(
                    s"<<< cValue: ${cValue.getClass} / expectedValue ${expectedValue.getClass}"
                  )
                  println(
                    s"!!! The expected value '$expectedValue' of $key does not match the result variable '${cValue}'.\n $result"
                  )
                end if
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

  end checkP

  private def checkJson(
      expectedJson: io.circe.Json,
      resultJson: io.circe.Json,
      key: String
  ): Boolean =
    val diffs: ListBuffer[String] = ListBuffer()
    def compareJsons(
        expJson: io.circe.Json,
        resJson: io.circe.Json,
        path: String
    ): Unit =
      if expJson != resJson then
        (expJson, resJson) match
          case _ if expJson.isArray && resJson.isArray =>
            val expJsonArray = expJson.asArray.toList.flatten
            val resJsonArray = resJson.asArray.toList.flatten
            for
              (expJson, resJson) <- expJsonArray.zipAll(
                resJsonArray,
                Json.Null,
                Json.Null
              )
            do
              compareJsons(
                expJson,
                resJson,
                s"$path[${expJsonArray.indexOf(expJson)}]"
              )
            end for
          case _ if expJson.isObject && resJson.isObject =>
            val expJsonObj = expJson.asObject.get
            val resJsonObj = resJson.asObject.get
            val expKeys = expJsonObj.keys.toSeq
            val resKeys = resJsonObj.keys.toSeq
            val commonKeys = expKeys.intersect(resKeys).toSet
            val uniqueKeys = (expKeys ++ resKeys).toSet.diff(commonKeys)
            for key <- commonKeys do
              compareJsons(
                expJsonObj(key).get,
                resJsonObj(key).get,
                s"$path.$key"
              )
            end for
            for key <- uniqueKeys do
              if expKeys.contains(key) then
                expJsonObj(key).foreach { json =>
                  diffs += s"$path.$key: ${json.noSpaces} (expected field not in result)"
                }
              else
                resJsonObj(key).foreach { json =>
                  diffs += s"$path.$key: ${json.noSpaces} (field in result not expected)"
                }
            end for
          case _ =>
            diffs += s"$path: ${expJson.noSpaces} (expected) != ${resJson.noSpaces} (result)"

    compareJsons(expectedJson, resultJson, "")

    if diffs.nonEmpty then
      println(
        s"!!! The JSON variable $key have the following different fields:"
      )
      for diff <- diffs do
        println(diff)
    end if
    diffs.isEmpty
  end checkJson

end ResultChecker
