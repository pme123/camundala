package camundala.camunda7.worker

import camundala.worker.CamundalaWorkerError.CamundaBpmnError
import camundala.worker.CamundalaWorkerError.UnexpectedError
import camundala.bpmn.*
import camundala.worker.CamundalaWorkerError.*

class C7WorkerHandlerTest extends munit.FunSuite:

  test("isErrorHandled"):
    assertEquals(
      handler.isErrorHandled(
        UnexpectedError("blabla"),
        Seq(ErrorCodes.`error-unexpected`.toString)
      ),
      true
    )

  test("isErrorHandled not"):
    assertEquals(
      handler.isErrorHandled(UnexpectedError("blabla"), Seq(ErrorCodes.`mapping-error`.toString)),
      false
    )

  test("isErrorHandled mocked"):
    assertEquals(
      handler.isErrorHandled(MockedOutput(Map.empty), Seq(ErrorCodes.`mapping-error`.toString)),
      true
    )

  test("isErrorHandled catch all"):
    assertEquals(handler.isErrorHandled(UnexpectedError("blabla"), Seq("CatchAll")), true)

  private lazy val handler = new C7WorkerHandler:
    def worker = ???
    def topic = "test-topic"
  end handler

end C7WorkerHandlerTest
