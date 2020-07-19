package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{suite, _}

object VariableDefSuite extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("VariableDefSuite")(
      suite("toString")(
        test("VariableType.Json") {
          assert(
            VariableDef("existingAddr", VariableType.Json).toString
          )(
            equalTo("""existingAddr_json = execution.getVariableTyped('existingAddr')
                      |if(existingAddr_json == null){
                      |  throw new Exception("The JSON Variable 'existingAddr' is not set!")
                      |} else { existingAddr = existingAddr_json}.getValue()""".stripMargin)
          )
        },
        test("VariableType.String") { // others
          assert(
            VariableDef("street", VariableType.String).toString
          )(
            equalTo("""street = execution.getVariable('street')""")
          )
        },
        test("VariableType.BusinessKey") {
          assert(
            VariableDef("bKey", VariableType.BusinessKey).toString
          )(
            equalTo("""bKey = execution.getBusinessKey()""")

          )
        },
        test("VariableType.String") { // others
          assert(
            VariableDef("street", VariableType.String).toString
          )(
            equalTo("""street = execution.getVariable('street')""")
          )
        },
        test("VariableType.Long with defaultValue") { // others
          assert(
            VariableDef("street", VariableType.Long, Some("12")).toString
          )(
            equalTo(
              """street = execution.getVariable('street')
                |if(street == null) street = 12""".stripMargin)
          )
        },
        test("VariableType.String with defaultValue") { // others
          assert(
            VariableDef("street", VariableType.String, Some("Sonnenweg")).toString
          )(
            equalTo(
              """street = execution.getVariable('street')
                |if(street == null) street = "Sonnenweg"""".stripMargin)
          )
        }
      )
    )
}
