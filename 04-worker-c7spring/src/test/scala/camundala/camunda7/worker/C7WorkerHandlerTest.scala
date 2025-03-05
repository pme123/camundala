package camundala.camunda7.worker

import camundala.bpmn.{ErrorCodes, GeneralVariables, InOutDescr, Process, ProcessLabels}
import camundala.camunda7.worker.context.DefaultCamunda7Context
import camundala.domain.{NoInput, NoOutput}
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.*
import zio.*
import org.camunda.bpm.client.task as camunda
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import zio.test.*
import zio.test.Assertion.*

import java.util
import java.util.Date

object C7WorkerHandlerTest extends ZIOSpecDefault, C7WorkerHandler[NoInput, NoOutput]:
  engineContext = DefaultCamunda7Context()
  def spec =
    suite("C7WorkerSpec")(
      test("isErrorHandled should return true for handledErrors") {
        val error  = CamundalaWorkerError.MappingError("error")
        val result = externalTaskService.isErrorHandled(error, handledErrors)
        assert(result)(isTrue)
      },
      test("isErrorHandled should return true for MockedOutput") {
        val error  = CamundalaWorkerError.MockedOutput(Map.empty)
        val result = externalTaskService.isErrorHandled(error, handledErrors)
        assert(result)(isTrue)
      },
      test("isErrorHandled should return true for catchAll") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = externalTaskService.isErrorHandled(error, Seq("CatchAll"))
        assert(result)(isTrue)
      },
      test("isErrorHandled should return false for any other Error") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = externalTaskService.isErrorHandled(error, handledErrors)
        assert(result)(isFalse)
      },
      test("handleSuccess should return false for any other Error") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = externalTaskService.handleSuccess(Map.empty, true)
        assertZIO(result)(equalTo(()))
      },
      test("handleSuccess should fail with an UnsupportedError if ") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = TestExternalTaskService(throw IllegalAccessError("camunda not working"))
          .handleSuccess(Map.empty, true)
        assertZIO(result.flip)(equalTo(UnexpectedError("There is an unexpected Error from completing a successful Worker to C7: camunda not working.")))
      },
      test("handleBpmnError should return false for any other Error") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = externalTaskService.handleBpmnError(error, Map.empty[String, Any])
        assertZIO(result)(equalTo(error))
        assert(externalTask.getRetries)(equalTo(3))
      },
      test("handleBpmnError should fail with an UnsupportedError if ") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = TestExternalTaskService(throw IllegalAccessError("camunda not working"))
          .handleBpmnError(error, Map.empty[String, Any])
        assertZIO(result)(equalTo(UnexpectedError("Problem handling BpmnError to C7: camunda not working.")))
      },
      test("handleFailure should return false for any other Error") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = externalTaskService.handleFailure(error)
        assertZIO(result)(equalTo(error))
        assert(externalTask.getRetries)(equalTo(3))
      },
      test("handleFailure should fail with an UnsupportedError if ") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = TestExternalTaskService(throw IllegalAccessError("camunda not working"))
          .handleFailure(error)
        assertZIO(result)(equalTo(UnexpectedError("Problem handling Failure to C7: camunda not working.")))
      },
      test("checkError should fail with an unhandled Error") {
        val error  = CamundalaWorkerError.CustomError("error")
        val result = TestExternalTaskService()
          .checkError(error, generalVariables)
        assertZIO(result)(equalTo(error))
      },
      test("checkError should fail with an handled Error") {
        val error  = CamundalaWorkerError.MappingError("error")
        val result = TestExternalTaskService()
          .checkError(error, generalVariables.copy(regexHandledErrors = Seq("error")))
        assertZIO(result)(equalTo(error))
      },
      test("checkError should fail with an handled Error bad regex") {
        val error  = CamundalaWorkerError.MappingError("error")
        val result = TestExternalTaskService()
          .checkError(error, generalVariables.copy(regexHandledErrors = Seq("errror")))
        assertZIO(result)(equalTo(HandledRegexNotMatchedError(error)))
      },
      test("checkError should fail with a MockedOutput") {
        val error  = CamundalaWorkerError.MockedOutput(Map.empty)
        val result = TestExternalTaskService()
          .checkError(error, generalVariables)
        assertZIO(result)(equalTo(error))
      },
      test("handleError expected Error") {
        val error = CamundalaWorkerError.CustomError("error")
        for
          outError <- externalTaskService.handleError(error, generalVariables)
        yield assertTrue(error == outError)
      },
      test("handleError UnexpectedError") {
        val error = UnexpectedError("unexpected error")
        for
          outError <- externalTaskService.handleError(error, generalVariables)
        yield assertTrue(error == outError)
      }
    )

  lazy val handledErrors: Seq[String]         = Seq(ErrorCodes.`mapping-error`.toString)
  lazy val generalVariables: GeneralVariables =
    GeneralVariables(handledErrors = handledErrors)

  lazy val externalTaskService = TestExternalTaskService()
  given externalTask: camunda.ExternalTask   = TestExternalTask()

  override def worker: Worker[NoInput, NoOutput, ?] =
    InitWorker[NoInput, NoOutput, NoInput](
      Process(
        InOutDescr(
          "dummy Worker",
        NoInput(), NoOutput()
        ),
        processLabels = ProcessLabels.none
      )
  )

  def topic: String = ???

end C7WorkerHandlerTest

case class TestExternalTask(
    activityId: String = "defaultActivityId",
    activityInstanceId: String = "defaultActivityInstanceId",
    errorMessage: String = "defaultErrorMessage",
    errorDetails: String = "defaultErrorDetails",
    executionId: String = "defaultExecutionId",
    id: String = "defaultId",
    lockExpirationTime: Date = new Date(),
    createTime: Date = new Date(),
    processDefinitionId: String = "defaultProcessDefinitionId",
    processDefinitionKey: String = "defaultProcessDefinitionKey",
    processDefinitionVersionTag: String = "defaultProcessDefinitionVersionTag",
    processInstanceId: String = "defaultProcessInstanceId",
    retries: Integer = 3,
    workerId: String = "defaultWorkerId",
    topicName: String = "defaultTopicName",
    tenantId: String = "defaultTenantId",
    priority: Long = 0L,
    businessKey: String = "defaultBusinessKey",
    extensionProperties: Map[String, String] = Map.empty,
    allVariables: Map[String, AnyRef] = Map.empty,
    allVariablesTyped: VariableMap = null
) extends camunda.ExternalTask:
  def getActivityId = ???

  def getActivityInstanceId = ???

  def getErrorMessage = ???

  def getErrorDetails = ???

  def getExecutionId = ???

  def getId = id

  def getLockExpirationTime = ???

  def getCreateTime = ???

  def getProcessDefinitionId = ???

  def getProcessDefinitionKey = ???

  def getProcessDefinitionVersionTag = ???

  def getProcessInstanceId = ???

  def getRetries = retries

  def getWorkerId = ???

  def getTopicName = ???

  def getTenantId = ???

  def getPriority = ???

  def getVariable[T](variableName: String) = ???

  def getVariableTyped[T <: TypedValue](variableName: String) = ???

  def getVariableTyped[T <: TypedValue](variableName: String, deserializeObjectValue: Boolean) = ???

  def getAllVariables = ???

  def getAllVariablesTyped = ???

  def getAllVariablesTyped(deserializeObjectValues: Boolean) = ???

  def getBusinessKey = ???

  def getExtensionProperty(propertyKey: String) = ???

  def getExtensionProperties = ???
end TestExternalTask

class TestExternalTaskService(completeFunct: => Unit = ()) extends camunda.ExternalTaskService:
  override def lock(externalTaskId: String, lockDuration: Long): Unit = ???

  override def lock(externalTask: ExternalTask, lockDuration: Long): Unit = ???

  override def unlock(externalTask: ExternalTask): Unit = ???

  override def complete(externalTask: ExternalTask): Unit = ???

  override def setVariables(processInstanceId: String, variables: util.Map[String, AnyRef]): Unit =
    ???

  override def setVariables(externalTask: ExternalTask, variables: util.Map[String, AnyRef]): Unit =
    ???

  override def complete(externalTask: ExternalTask, variables: util.Map[String, AnyRef]): Unit =
    completeFunct

  override def complete(
      externalTask: ExternalTask,
      variables: util.Map[String, AnyRef],
      localVariables: util.Map[String, AnyRef]
  ): Unit = completeFunct

  override def complete(
      externalTaskId: String,
      variables: util.Map[String, AnyRef],
      localVariables: util.Map[String, AnyRef]
  ): Unit = completeFunct

  override def handleFailure(
      externalTask: ExternalTask,
      errorMessage: String,
      errorDetails: String,
      retries: RuntimeFlags,
      retryTimeout: Long
  ): Unit =
    completeFunct
  end handleFailure

  override def handleFailure(
      externalTaskId: String,
      errorMessage: String,
      errorDetails: String,
      retries: RuntimeFlags,
      retryTimeout: Long
  ): Unit =
    completeFunct
  end handleFailure

  override def handleFailure(
      externalTaskId: String,
      errorMessage: String,
      errorDetails: String,
      retries: RuntimeFlags,
      retryTimeout: Long,
      variables: util.Map[String, AnyRef],
      localVariables: util.Map[String, AnyRef]
  ): Unit =
    completeFunct

  override def handleBpmnError(externalTask: ExternalTask, errorCode: String): Unit = completeFunct

  override def handleBpmnError(
      externalTask: ExternalTask,
      errorCode: String,
      errorMessage: String
  ): Unit =
    completeFunct

  override def handleBpmnError(
      externalTask: ExternalTask,
      errorCode: String,
      errorMessage: String,
      variables: util.Map[String, AnyRef]
  ): Unit = completeFunct

  override def handleBpmnError(
      externalTaskId: String,
      errorCode: String,
      errorMessage: String,
      variables: util.Map[String, AnyRef]
  ): Unit = completeFunct

  override def extendLock(externalTask: ExternalTask, newDuration: Long): Unit = ???

  override def extendLock(externalTaskId: String, newDuration: Long): Unit = ???
end TestExternalTaskService
