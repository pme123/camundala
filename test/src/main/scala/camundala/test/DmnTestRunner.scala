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

    decisionDmn.decisionResultType match
      case DecisionResultType.singleEntry => // SingleEntry
        val resultEntry: Any = result.getSingleEntry
        val expKey = decisionDmn.out.productElementNames.next()
        val expResult = decisionDmn.out.productIterator.next() match
          case e: scala.reflect.Enum => e.toString
          case ldt : LocalDateTime =>
            import java.time.ZoneId
            Date.from(ldt.atZone(ZoneId.systemDefault).toInstant)
          case zdt: ZonedDateTime =>
            Date.from(zdt.toInstant)
          case o => o
        println(s"assert $expKey: $resultEntry == $expResult")
        assert(resultEntry == expResult)
      case DecisionResultType.singleResult => // SingleResult
        val resultEntryMap = result.getSingleResult.getEntryMap.asScala

        val expResult: Seq[(String, Any)] =
          decisionDmn.out.productIterator.next() match
            case p: Product =>
              (p.productElementNames
                .zip(p.productIterator.toSeq map {
                  case e: scala.reflect.Enum => e.toString
                  case o => o
                }))
                .toSeq
            case o => Seq.empty

        assert(expResult.size == resultEntryMap.size)
        for (key, value) <- expResult
        yield
          println(s"assert $key: ${resultEntryMap(key)} == $value")
          assert(resultEntryMap(key) == value)
      case DecisionResultType.collectEntries => // CollectEntries
        val resultList = result.getResultList.asScala
        val expKey = decisionDmn.out.productElementNames.next()
        val expResults = decisionDmn.out.productIterator.next()
        assert(
          expResults.isInstanceOf[Iterable[?]],
          "For DecisionResultType.collectEntries you need to have Iterable[?] object."
        )
        val expResultDmn =
          expResults match
            case seq: Iterable[?] =>
              seq.map {
                case e: scala.reflect.Enum => e.toString
                case v => v
              }.toSeq
            case e => Seq("bad input" -> s"Expected Seq[?], but got $e")

        assert(expResultDmn.size == resultList.size)
        val sortedResult = resultList.flatMap(_.values.asScala.toSeq).sortBy(_.toString)
        val sortedExpected = expResultDmn.sortBy(_.toString)
        for i <- expResultDmn.indices
        yield
          val result = sortedResult(i)
          val expected = sortedExpected(i)
          println(s"assert: $result == $expected (expected)")
          assert(result == expected)

      case DecisionResultType.resultList => // ResultList
        val resultList = result.getResultList.asScala
        val expKey = decisionDmn.out.productElementNames.next()
        val expResults = decisionDmn.out.productIterator.next()

        val expResultDmn = expResults match
          case iterable: Iterable[?] =>
            iterable.map {
              case prod: Product =>
                prod.productElementNames
                  .zip(prod.productIterator)
                  .map {
                    case (k, e: scala.reflect.Enum) => k -> e.toString
                    case k -> o => k -> o
                  }
                  .toMap
              case e => fail(s"Expected Product, but got $e")

            }.toSeq
          case other =>
            fail(
              s"For DecisionResultType.collectEntries you need to have Iterable[?] object. But it was $other"
            )
            Seq.empty

        assert(expResultDmn.size == resultList.size)
        for (expMap, resMap) <- expResultDmn.zip(resultList)
        yield
          println(s"assert $expMap == ${resMap}")
          assert(expMap == resMap.asScala)
