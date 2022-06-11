package camundala
package simulation

import api.*
import bpmn.*

import scala.annotation.targetName

case class SSimulation(scenarios: List[SScenario])

sealed trait WithTestOverrides[T <: WithTestOverrides[T]]:
  def inOut: InOut[_, _, _]
  def testOverrides: Option[TestOverrides]
  def add(testOverride: TestOverride): T
  protected def addOverride(testOverride: TestOverride): Option[TestOverrides] =
    Some(
      testOverrides
        .map(_ :+ testOverride)
        .getOrElse(TestOverrides(Seq(testOverride)))
    )
  lazy val camundaToCheckMap: Map[String, CamundaVariable] = inOut.camundaToCheckMap

sealed trait SScenario:
  def name: String
  def process: Process[_, _]
  def isIgnored: Boolean

case class ProcessScenario(
    name: String,
    process: Process[_, _],
    steps: List[SStep] = List.empty,
    isIgnored: Boolean = false,
    testOverrides: Option[TestOverrides] = None
) extends SScenario,
      WithTestOverrides[ProcessScenario]:
  def inOut: InOut[_, _, _] = process

  def add(testOverride: TestOverride): ProcessScenario =
    copy(testOverrides = addOverride(testOverride))

case class BadScenario(
    name: String,
    process: Process[_, _],
    status: Int,
    errorMsg: Option[String],
    isIgnored: Boolean = false
) extends SScenario

sealed trait SStep extends WithTestOverrides[SStep]:
  def name: String
  lazy val inOutDescr: InOutDescr[_, _] = inOut.inOutDescr
  lazy val id: String = inOutDescr.id
  lazy val descr: Option[String] | String = inOutDescr.descr
  lazy val camundaInMap: Map[String, CamundaVariable] = inOut.camundaInMap
  lazy val camundaOutMap: Map[String, CamundaVariable] = inOut.camundaOutMap

case class SUserTask(
    name: String,
    inOut: UserTask[_, _],
    testOverrides: Option[TestOverrides] = None
) extends SStep:

  def add(testOverride: TestOverride): SUserTask =
    copy(testOverrides = addOverride(testOverride))

case class SSubProcess(
    name: String,
    inOut: Process[_, _],
    steps: List[SStep],
    testOverrides: Option[TestOverrides] = None
) extends SStep:

  def add(testOverride: TestOverride): SSubProcess =
    copy(testOverrides = addOverride(testOverride))
