package pme123.camundala.camunda

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.StaticFile
import zio.test.Assertion._
import zio.test._

object StreamHelperSuite extends DefaultRunnableSpec {

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("StreamHelperSuite")(
      testM("Load Resource that exists") {
        for {
          xml <- StreamHelper.xml(StaticFile("TwitterDemoProcess.bpmn", "bpmn"))
        } yield
          assert(xml.toString)(containsString("TwitterDemoProcess"))
      },testM("Load Resource that does not exists") {
        for {
          xml <- StreamHelper.xml(StaticFile("TwitterDemoProcessBAD.bpmn", "bpmn")).flip
        } yield
          assert(xml.getMessage)(equalTo("There is a Problem loading bpmn/TwitterDemoProcessBAD.bpmn"))
      }
    )
}



