package camundala.tools

import camundala.dsl._
import zio.{IO, Task, ZIO}

import scala.xml.Elem

object BpmnFromXml {

  def createBpmn(
      bpmnId: Identifier,
      basePath: Seq[String] = Seq.empty
  ): Task[Bpmn] =
    for {
      bpmn <- StreamHelper(basePath).xml(Seq("bpmn", s"$bpmnId.bpmn"))
      processes <- createBpmn(bpmn)
    } yield Bpmn(bpmnId, processes)

  def createBpmn(bpmnXml: Elem): Task[List[BpmnProcess]] =
    bpmnXml.createBpmn()

  def xmlSource(
      bpmnId: Identifier,
      basePath: Seq[String] = Seq.empty
  ): Task[Elem] = {
    StreamHelper(basePath).xml(Seq("bpmn", s"$bpmnId.bpmn"))
  }

  implicit class ElemExtractor(xmlElem: Elem) {

    def createBpmn(): Task[List[BpmnProcess]] =
      Task.foreach(
        (xmlElem \ "process")
          .filter(_ \@ "isExecutable" == "true")
      ) { case e: Elem => e.createProcess() }

    def createProcess(): Task[BpmnProcess] =
      for {
        processId <- xmlElem.elemId
        uTasks <- Task.foreach(xmlElem.userTasks)(_.createUserTask())
        sTasks <- Task.foreach(xmlElem.serviceTasks)(_.createServiceTask())
        bRuleTasks <- Task.foreach(xmlElem.businessRuleTasks)(
          _.createBusinessRuleTask()
        )
        sendTasks <- Task.foreach(xmlElem.sendTasks)(_.createSendTask())
        callActivities <- Task.foreach(xmlElem.callActivities)(
          _.createCallActivity()
        )
        startEvents <- Task.foreach(xmlElem.startEvents)(_.createStartEvent())
        endEvents <- Task.foreach(xmlElem.endEvents)(_.createEndEvent())
        exGateways <- Task.foreach(xmlElem.exclusiveGateways)(
          _.createExclusiveGateway()
        )
        pGateways <- Task.foreach(xmlElem.parallelGateways)(
          _.createParallelGateway()
        )
        seqFlows <- Task.foreach(xmlElem.sequenceFlows)(_.createSequenceFlow())
      } yield BpmnProcess(
        processId,
        CandidateGroups.none,
        CandidateUsers.none,
        startEvents,
        uTasks,
        sTasks,
        bRuleTasks,
        sendTasks,
        callActivities,
        endEvents,
        seqFlows,
        exGateways,
        pGateways
      )

    def createUserTask(): IO[DslException, UserTask] =
      createElemWithInOut((nodeId, inFlows, outFlows) =>
        UserTask(
          nodeId,
          inFlows = inFlows,
          outFlows = outFlows
        )
      )

    def createSendTask(): IO[DslException, SendTask] =
      createElemWithInOut((nodeId, inFlows, outFlows) =>
        SendTask(
          nodeId,
          inFlows = inFlows,
          outFlows = outFlows
        )
      )

    def createServiceTask(): IO[DslException, ServiceTask] =
      createElemWithInOut((nodeId, inFlows, outFlows) =>
        ServiceTask(
          nodeId,
          inFlows = inFlows,
          outFlows = outFlows
        )
      )

    def createBusinessRuleTask(): IO[DslException, BusinessRuleTask] =
      createElemWithInOut((nodeId, inFlows, outFlows) =>
        BusinessRuleTask(
          nodeId,
          inFlows = inFlows,
          outFlows = outFlows
        )
      )

    def createCallActivity(): IO[DslException, CallActivity] =
      createElemWithInOut((nodeId, inFlows, outFlows) =>
        CallActivity(
          nodeId,
          inFlows = inFlows,
          outFlows = outFlows
        )
      )

    def createStartEvent(): IO[DslException, StartEvent] =
      for {
        nodeId <- xmlElem.elemId
        outFlows <- xmlElem.outgoingFlows
      } yield StartEvent(
        nodeId,
        outFlows = outFlows
      )

    def createEndEvent(): IO[DslException, EndEvent] =
      for {
        nodeId <- xmlElem.elemId
        inFlows <- xmlElem.incomingFlows
      } yield EndEvent(
        nodeId,
        inFlows = inFlows
      )

    def createExclusiveGateway(): IO[DslException, ExclusiveGateway] =
      createElemWithInOut((nodeId, inFlows, outFlows) =>
        ExclusiveGateway(
          nodeId,
          inFlows = inFlows,
          outFlows = outFlows
        )
      )

    def createParallelGateway(): IO[DslException, ParallelGateway] =
      createElemWithInOut((nodeId, inFlows, outFlows) =>
        ParallelGateway(
          nodeId,
          inFlows = inFlows,
          outFlows = outFlows
        )
      )

    def createSequenceFlow(): IO[DslException, SequenceFlow] =
      for {
        nodeId <- xmlElem.elemId
      } yield SequenceFlow(
        nodeId
      )

    private def createElemWithInOut[A](
        constr: (Identifier, List[SequenceFlow], List[SequenceFlow]) => A
    ): IO[DslException, A] =
      for {
        nodeId <- xmlElem.elemId
        inFlows <- xmlElem.incomingFlows
        outFlows <- xmlElem.outgoingFlows
      } yield constr(
        nodeId,
        inFlows,
        outFlows
      )

    val userTasks: Seq[Elem] =
      (xmlElem \ "userTask").map { case e: Elem => e }

    val serviceTasks: Seq[Elem] =
      (xmlElem \ "serviceTask").map { case e: Elem => e }

    val businessRuleTasks: Seq[Elem] =
      (xmlElem \ "businessRuleTask").map { case e: Elem => e }

    val sendTasks: Seq[Elem] =
      (xmlElem \ "sendTask").map { case e: Elem => e }

    val callActivities: Seq[Elem] =
      (xmlElem \ "callActivity").map { case e: Elem => e }

    val startEvents: Seq[Elem] =
      (xmlElem \ "startEvent").map { case e: Elem => e }

    val endEvents: Seq[Elem] =
      (xmlElem \ "endEvent").map { case e: Elem => e }

    val exclusiveGateways: Seq[Elem] =
      (xmlElem \ "exclusiveGateway").map { case e: Elem => e }

    val parallelGateways: Seq[Elem] =
      (xmlElem \ "parallelGateway").map { case e: Elem => e }

    val sequenceFlows: Seq[Elem] =
      (xmlElem \ "sequenceFlow").map { case e: Elem => e }

    def elemId: IO[DslException, Identifier] =
      identifierFromStr(xmlElem \@ "id")

    def incomingFlows: IO[DslException, List[SequenceFlow]] =
      ZIO.foreach(xmlElem \ "incoming") {
        case e: Elem =>
          identifierFromStr(e.text).map(SequenceFlow(_))
      }

    def outgoingFlows: IO[DslException, List[SequenceFlow]] =
      ZIO.foreach(xmlElem \ "outgoing") {
        case e: Elem =>
          identifierFromStr(e.text).map(SequenceFlow(_))
      }

  }

}
