package camundala
package test

import bpmn.*
import camundala.bpmn.{DecisionDmn, Process, ProcessNode}
import org.camunda.bpm.dmn.engine.{DmnDecision, DmnDecisionResult, DmnEngine}
import org.camunda.bpm.dmn.engine.test.DmnEngineRule
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{assertThat, repositoryService, runtimeService, task}
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Assert.{assertEquals, fail}
import org.junit.{Before, Rule}
import org.mockito.MockitoAnnotations
import os.{Path, ResourcePath}
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.model.dmn.DmnModelInstance

import java.time.{LocalDateTime, ZonedDateTime}
import java.util.Date
import scala.collection.immutable
import scala.jdk.CollectionConverters.*
import scala.jdk.CollectionConverters.*
trait DmnTestRunner:

  def dmnPath: ResourcePath

  @Rule
  lazy val dmnEngineRule = new DmnEngineRule()

  lazy val dmnEngine: DmnEngine = dmnEngineRule.getDmnEngine

  lazy val dmnInputStream =
    new java.io.ByteArrayInputStream(os.read.bytes(dmnPath))

  def test[
      In <: Product: Encoder: Decoder,
      Out <: Product: Encoder: Decoder
  ](decisionDmn: => DecisionDmn[In, Out]): Unit =
    val variables: VariableMap = Variables.createVariables
    for (k, v) <- decisionDmn.inOutDescr.in.asDmnVars()
    yield variables.putValue(k, v)

    val cDecision: DmnDecision =
      dmnEngine.parseDecision(decisionDmn.decisionDefinitionKey, dmnInputStream)
    val result = dmnEngine.evaluateDecisionTable(cDecision, variables)

    decisionDmn.out match
      case o: SingleEntry[?] => // SingleEntry
        val resultEntry: Any = result.getSingleEntry
        val expResult = o.result match
          case e: scala.reflect.Enum => e.toString
          case ldt : LocalDateTime =>
            import java.time.ZoneId
            Date.from(ldt.atZone(ZoneId.systemDefault).toInstant)
          case zdt: ZonedDateTime =>
            Date.from(zdt.toInstant)
          case o => o
        assert(resultEntry == expResult, s"SingleEntry: $resultEntry == $expResult")
      case o: SingleResult[?] => // SingleResult
        val resultEntryMap = result.getSingleResult.getEntryMap.asScala

        val expResult: Seq[(String, Any)] =
          o.result.productElementNames
                .zip(o.result.productIterator.toSeq map {
                  case e: scala.reflect.Enum => e.toString
                  case o => o
                })
                .toSeq
        assert(expResult.size == resultEntryMap.size)
        for (key, value) <- expResult
        yield
          if(!resultEntryMap.contains(key))
            throw IllegalArgumentException(s"Your output Object ($expResult) has a key ($key) that does not exist in the actual Decision Output ($resultEntryMap).")
          assert(resultEntryMap(key) == value, s"SingleResult $key: ${resultEntryMap(key)} == $value")
      case o: CollectEntries[?] => // CollectEntries
        val resultList = result.getResultList.asScala
        val expResultDmn =
          o.result.map {
                case e: scala.reflect.Enum => e.toString
                case v => v
              }

        assert(expResultDmn.size == resultList.size)
        val sortedResult = resultList.flatMap(_.values.asScala.toSeq).sortBy(_.toString)
        val sortedExpected = expResultDmn.sortBy(_.toString)
        for i <- expResultDmn.indices
        yield
          val result = sortedResult(i)
          val expected = sortedExpected(i)
          assert(result == expected, s"$result == $expected (expected)")

      case o: ResultList[?] => // ResultList
        val resultList = result.getResultList.asScala

        val expResultDmn = o.result.map {
              case prod: Product =>
                prod.productElementNames
                  .zip(prod.productIterator)
                  .map {
                    case (k, e: scala.reflect.Enum) => k -> e.toString
                    case k -> o => k -> o
                  }
                  .toMap
            }
        assert(expResultDmn.size == resultList.size, s"${expResultDmn.size} == ${resultList.size} \n$resultList == $expResultDmn (expected)")
        for (expMap, resMap) <- expResultDmn.zip(resultList)
        yield
          assert(expMap == resMap.asScala, s"$expMap == $resMap")
