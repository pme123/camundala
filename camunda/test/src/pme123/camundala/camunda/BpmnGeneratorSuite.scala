package pme123.camundala.camunda

import eu.timepit.refined.auto._
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.bpmn.StaticFile
import pme123.camundala.model.register.bpmnRegister
import zio.test.Assertion._
import zio.test._

object BpmnGeneratorSuite extends DefaultRunnableSpec {

  import TestData._

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("BpmnGeneratorSuite")(

      testM("the BPMN Model is generated") {
        for {
          _ <- bpmnRegister.registerBpmn(bpmn)
          bpmn <- bpmnGenerator.generate(StaticFile("TwitterDemoProcess.bpmn"))
        } yield {
          assert(bpmn.processes.length)(equalTo(3) ?? "number of generated Processes")
        }
      },

    ).provideCustomLayer((CamundaLayers.bpmnGeneratorLayer ++ ModelLayers.bpmnRegisterLayer).mapError(TestFailure.fail))

}
