package camundala.simulation
package custom

import camundala.bpmn.*
import io.circe.*
import sttp.client3.*

import scala.concurrent.Future

trait DmnScenarioExtensions extends SScenarioExtensions:
  extension (scenario: DmnScenario)
    def run(): Future[ResultType] =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data

        evaluate()
      }

    def evaluate()(using data: ScenarioData): ResultType =
      val dmn  = scenario.inOut
      val body = EvaluateDecisionIn(
        dmn.camundaInMap
      ).asJson.deepDropNullValues.toString
      val uri  = config.tenantId match
        case Some(tenantId) =>
          uri"${config.endpoint}/decision-definition/key/${dmn.decisionDefinitionKey}/tenant-id/$tenantId/evaluate"
        case None           =>
          uri"${config.endpoint}/decision-definition/key/${dmn.decisionDefinitionKey}/evaluate"

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)

      runRequest(request, s"Process '${scenario.name}' startProcess")((body, data) =>
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
    end evaluate
    private def evaluateDmn(resultSeq: Seq[Map[String, CamundaVariable]])(using
        data: ScenarioData
    ): ResultType =
      val result                         = resultSeq
      val decisionDmn: DecisionDmn[?, ?] = scenario.inOut
      val check: ResultType              = decisionDmn.out match
        case expected: SingleEntry[?]    =>
          for
            given ScenarioData <-
              if result.size == 1 && result.head.size == 1 then
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"A ${expected.decisionResultType} is expected, but it was: $result"
                  )
                )
            given ScenarioData <-
              if result.head.head._2 == expected.toCamunda then
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: ${result.head.head._2}"
                  )
                )
          yield summon[ScenarioData]
        case expected: SingleResult[?]   =>
          for
            given ScenarioData <-
              if
                result.size == 1 &&
                result.head.size > 1
              then
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"A ${expected.decisionResultType} is expected, but it was: $result"
                  )
                )
            given ScenarioData <-
              if result.head == expected.toCamunda then Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: ${result.head}"
                  )
                )
          yield summon[ScenarioData]
        case expected: CollectEntries[?] =>
          val resultValues = result.map(_.values.head)
          scenario.testOverrides match
            case None                =>
              if
                (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size == 1 &&
                  resultValues.toSet == expected.toCamunda.toSet)
              then
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: $resultValues"
                  )
                )
            case Some(testOverrides) =>
              val overrides = testOverrides.overrides
              if checkOForCollection(overrides, resultValues) then
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} with TestOverrides did not match, \n- expected: $overrides \n- result: $resultValues"
                  )
                )
              end if
          end match

        case expected: ResultList[?] =>
          scenario.testOverrides match
            case None                =>
              if
                (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size > 1 &&
                  result.toSet == expected.toCamunda.toSet)
              then
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: $result"
                  )
                )
            case Some(testOverrides) =>
              val overrides = testOverrides.overrides
              if checkOForCollection(overrides, result) then
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} with TestOverrides did not match, \n- expected: $overrides \n- result: $result"
                  )
                )
              end if
        case _                       =>
          Left(
            summon[ScenarioData].error(
              s"Unknown Type ${decisionDmn.out.getClass}: ${decisionDmn.out}"
            )
          )

      check.map(data =>
        data.info(s"Dmn Evaluation was successful for ${scenario.name}")
      )
    end evaluateDmn
  end extension

end DmnScenarioExtensions
