package camundala.camunda7.worker

import camundala.camunda7.worker.context.DefaultCamunda7Context
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.*
import zio.*
import org.camunda.bpm.client.task as camunda
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.value.TypedValue
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

import java.util
import java.util.Date

object C7WorkerHandlerSpec extends ZIOSpecDefault, C7WorkerHandler[NoInput, NoOutput]:
  engineContext = DefaultCamunda7Context()
  def spec =
    suite("C7WorkerSpec")(
      suite("isErrorHandled")(
        test("should return true for handledErrors") {
          val error  = CamundalaWorkerError.MappingError("error")
          val result = externalTaskService.isErrorHandled(error, handledErrors)
          assertTrue(result)
        },
        test("should return true for MockedOutput") {
          val error  = CamundalaWorkerError.MockedOutput(Map.empty)
          val result = externalTaskService.isErrorHandled(error, handledErrors)
          assertTrue(result)
        },
        test("should return true for catchAll") {
          val error  = CamundalaWorkerError.CustomError("error")
          val result = externalTaskService.isErrorHandled(error, Seq("CatchAll"))
          assertTrue(result)
        },
        test("should return false for any other Error") {
          val error  = CamundalaWorkerError.CustomError("error")
          val result = externalTaskService.isErrorHandled(error, handledErrors)
          assertTrue(!result)
        },
        test("should return true for a causeError") {
          val error  = CamundalaWorkerError.CustomError(
            "error",
            causeError = Some(CamundalaWorkerError.MappingError("mapping error"))
          )
          val result = externalTaskService.isErrorHandled(error, handledErrors)
          assertTrue(result)
        }
      ),
      suite("handleSuccess")(
        test("should return unit for any error"):
          val error  = CamundalaWorkerError.CustomError("error")
          val result = externalTaskService.handleSuccess(Map.empty, true)
          assertZIO(result)(equalTo(()))
        ,
        test("should succeed if Camunda throws an exception, as it already did the retries."):
          val error  = CamundalaWorkerError.CustomError("error")
          val result = TestExternalTaskService(throw IllegalAccessError("camunda not working"))
            .handleSuccess(Map.empty, true)
          assertZIO(result)(equalTo(()))
        ,
        test("should return unit for any error"):
          val error  = CamundalaWorkerError.CustomError("error")
          val result = externalTaskService.handleSuccess(Map.empty, true)
          assertZIO(result)(equalTo(()))
        ,
        test("should succeed if Camunda throws an exception, as it already did the retries."):
          val error  = CamundalaWorkerError.CustomError("error")
          val result = TestExternalTaskService(throw IllegalAccessError("camunda not working"))
            .handleSuccess(Map.empty, true)
          assertZIO(result)(equalTo(()))
      ),
      suite("handleBpmnError")(
        test("should return the error") {
          val error  = CamundalaWorkerError.CustomError("error")
          val result = externalTaskService.handleBpmnError(error, Map.empty[String, Any])
          assertZIO(result)(equalTo(())) &&
          assertTrue(externalTask.getRetries == 0)
        },
        test("should fail with an UnsupportedError if Camunda throws an exception") {
          val error  = CamundalaWorkerError.CustomError("error")
          val result = TestExternalTaskService(throw IllegalAccessError("camunda not working"))
            .handleBpmnError(error, Map.empty[String, Any])

          assertZIO(result)(equalTo(()))
        }
      ),
      suite("handleFailure")(
        test("should return the error") {
          val error  = CamundalaWorkerError.CustomError("error")
          val result = externalTaskService.handleFailure(error)
          assertZIO(result)(equalTo(())) &&
          assertTrue(externalTask.getRetries == 0)
        },
        test("should return unit if Camunda throws an exception") {
          val error  = CamundalaWorkerError.CustomError("error")
          val result = TestExternalTaskService(throw IllegalAccessError("camunda not working"))
            .handleFailure(error)
          assertZIO(result)(equalTo(()))
        }
      ),
      suite("checkError")(
        test("should fail with an unhandled Error") {
          val error  = CamundalaWorkerError.CustomError("error")
          val result = TestExternalTaskService()
            .checkError(error, generalVariables)
          assertZIO(result)(equalTo(error))
        },
        test("should fail with an handled Error"):
          val error  = CamundalaWorkerError.MappingError("error")
          val result = TestExternalTaskService()
            .checkError(error, generalVariables.copy(regexHandledErrors = Seq("error")))
          assertZIO(result)(equalTo(AlreadyHandledError))
        ,
        test("should fail with an handled Error bad regex"):
          val error  = CamundalaWorkerError.MappingError("error")
          val result = TestExternalTaskService()
            .checkError(error, generalVariables.copy(regexHandledErrors = Seq("errror")))
          assertZIO(result)(equalTo(HandledRegexNotMatchedError(error)))
        ,
        test("should fail with a MockedOutput"):
          val error  = CamundalaWorkerError.MockedOutput(Map.empty)
          val result = TestExternalTaskService()
            .checkError(error, generalVariables)
          assertZIO(result)(equalTo(AlreadyHandledError))
      ),
      suite("handleError")(
        test("CustomError should return unit"):
          val error  = CamundalaWorkerError.CustomError("error")
          val result = externalTaskService.handleError(error, generalVariables)
          assertZIO(result)(equalTo(()))
        ,
        test("UnexpectedError should return unit"):
          val error  = UnexpectedError("unexpected error")
          val result = externalTaskService.handleError(error, generalVariables)
          assertZIO(result)(equalTo(()))
      ),
      suite("calcRetries")(
        test("should return 2 when retries <= 0 and error message contains a retry pattern"):
          given externalTask: camunda.ExternalTask = TestExternalTask(retries = 0)

          val error  = CamundalaWorkerError.CustomError(
            "Entity was updated by another transaction concurrently"
          )
          val result = externalTaskService.calcRetries(error)

          assertTrue(result == 2)
        ,
        test("should return 2 when retries < 0 and error message contains a retry pattern"):
          given externalTask: camunda.ExternalTask = TestExternalTask(retries = -1)

          val error  =
            CamundalaWorkerError.CustomError("An exception occurred in the persistence layer")
          val result = externalTaskService.calcRetries(error)

          assertTrue(result == 2)
        ,
        test("should decrement retries by 1 when retries > 0 regardless of error message"):
          given externalTask: camunda.ExternalTask = TestExternalTask(retries = 5)

          val error  = CamundalaWorkerError.CustomError(
            "Entity was updated by another transaction concurrently"
          )
          val result = externalTaskService.calcRetries(error)

          assertTrue(result == 4)
        ,
        test(
          "should decrement retries by 1 when retries > 0 and error doesn't match retry patterns"
        ):
          given externalTask: camunda.ExternalTask = TestExternalTask(retries = 2)

          val error  = CamundalaWorkerError.CustomError("Some other error message")
          val result = externalTaskService.calcRetries(error)

          assertTrue(result == 1)
        ,
        test("should decrement retries to 0 when retries = 1"):
          given externalTask: camunda.ExternalTask = TestExternalTask(retries = 1)

          val error  = CamundalaWorkerError.CustomError("Some error message")
          val result = externalTaskService.calcRetries(error)

          assertTrue(result == 0)
        ,
        test("should return 0 when retries = 0 and error doesn't match retry patterns"):
          given externalTask: camunda.ExternalTask = TestExternalTask(retries = 0)

          val error  =
            CamundalaWorkerError.CustomError("Some error that doesn't match retry patterns")
          val result = externalTaskService.calcRetries(error)

          assertTrue(result == -1)
      ),
      suite("filteredOutput"):
        test("should return all outputs when output variables is empty"):
          val outputs = Map("key1" -> "value1", "key2" -> "value2")
          val result  = externalTaskService.filteredOutput(Seq.empty, outputs)
          assertTrue(result == outputs)
      ,
      test("should filter outputs based on output variables"):
        val outputs    = Map("key1" -> "value1", "key2" -> "value2", "key3" -> "value3")
        val filterVars = Seq("key1", "key3")
        val expected   = Map("key1" -> "value1", "key3" -> "value3")
        val result     = externalTaskService.filteredOutput(filterVars, outputs)
        assertTrue(result == expected)
      ,
      test("should return empty map when no outputs match filter"):
        val outputs    = Map("key1" -> "value1", "key2" -> "value2")
        val filterVars = Seq("key3", "key4")
        val result     = externalTaskService.filteredOutput(filterVars, outputs)
        assertTrue(result == Map.empty)
    )

  lazy val handledErrors: Seq[String]         = Seq(ErrorCodes.`mapping-error`.toString)
  lazy val generalVariables: GeneralVariables =
    GeneralVariables(handledErrors = handledErrors)

  lazy val externalTaskService             = TestExternalTaskService()
  given externalTask: camunda.ExternalTask = TestExternalTask()

  override def worker: Worker[NoInput, NoOutput, ?] =
    InitWorker[NoInput, NoOutput, NoInput](
      Process(
        InOutDescr(
          "dummy Worker",
          NoInput(),
          NoOutput()
        ),
        processLabels = ProcessLabels.none
      ),
      validationHandler = ValidationHandler(Right(_))
    )

  def topic: String = ???

end C7WorkerHandlerSpec

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
    retries: Integer = 0,
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

  def getProcessInstanceId = "213423-1234-1234"

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

  def getBusinessKey = businessKey

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

  override def handleFailure(
      externalTaskId: String,
      errorMessage: String,
      errorDetails: String,
      retries: RuntimeFlags,
      retryTimeout: Long
  ): Unit =
    completeFunct

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
