package camundala.simulation
package custom

import camundala.api.*
import camundala.bpmn.*
import io.circe.*
import io.circe.syntax.*
import sttp.client3.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DmnScenarioExtensions extends SScenarioExtensions:
  extension (scenario: DmnScenario)
    def run(): Future[ResultType] =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data

        evaluate()
      }

    def evaluate()(using data: ScenarioData): ResultType = {
      val dmn = scenario.inOut
      val body = EvaluateDecisionIn(
        dmn.camundaInMap
      ).asJson.toString
      val uri =
        uri"${config.endpoint}/decision-definition/key/${dmn.decisionDefinitionKey}${config.tenantPath}/evaluate"

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)

      runRequest(request, s"Process '${scenario.name}' startProcess")(
        (body, data) =>
          body
            .as[Seq[Map[String, CamundaVariable]]]
            .left
            .map { ex =>
              summon[ScenarioData]
                .error(
                  s"Problem extracting Seq[Map[String, CamundaVariable] from $body\n $ex"
                )
            }
            .flatMap { values =>
              evaluateDmn(values)
            }
      )
    }
    private def evaluateDmn(resultSeq: Seq[Map[String, CamundaVariable]])(using
        data: ScenarioData
    ): ResultType =
      val result = resultSeq.map(
        _.filter(_._2 != CamundaVariable.CNull)
      )
      val decisionDmn: DecisionDmn[_, _] = scenario.inOut
      val check: ResultType = decisionDmn.out match
        case expected: SingleEntry[_] =>
          for {
            given ScenarioData <-
              if (result.size == 1 && result.head.size == 1)
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"A ${expected.decisionResultType} is expected, but it was: $result"
                  )
                )
            given ScenarioData <-
              if (result.head.head._2 == expected.toCamunda)
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: ${result.head.head._2}"
                  )
                )
          } yield summon[ScenarioData]
        case expected: SingleResult[_] =>
          for {
            given ScenarioData <-
              if (
                result.size == 1 &&
                result.head.size > 1
              )
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"A ${expected.decisionResultType} is expected, but it was: $result"
                  )
                )
            given ScenarioData <-
              if (result.head == expected.toCamunda) Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: ${result.head}"
                  )
                )
          } yield summon[ScenarioData]
        case expected: CollectEntries[_] =>
          val resultValues = result.map(_.values.head)
          scenario.testOverrides match
            case None =>
              if (
                (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size == 1 &&
                  resultValues.toSet == expected.toCamunda.toSet)
              )
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: $resultValues"
                  )
                )
            case Some(testOverrides) =>
              val overrides = testOverrides.overrides
              if (checkOForCollection(overrides, resultValues))
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} with TestOverrides did not match, \n- expected: $overrides \n- result: $resultValues"
                  )
                )

        case expected: ResultList[_] =>
          scenario.testOverrides match
            case None =>
              if (
                (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size > 1 &&
                  result.toSet == expected.toCamunda.toSet)
              )
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: $result"
                  )
                )
            case Some(testOverrides) =>
              val overrides = testOverrides.overrides
              if (checkOForCollection(overrides, result))
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} with TestOverrides did not match, \n- expected: $overrides \n- result: $result"
                  )
                )
        case _ =>
          Left(
            summon[ScenarioData].error(
              s"Unknown Type ${decisionDmn.out.getClass}: ${decisionDmn.out}"
            )
          )

      check.map(data =>
        data.info(s"Dmn Evaluation was successful for ${scenario.name}")
      )
  end extension

end DmnScenarioExtensions
