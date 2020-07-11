package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.TestData
import pme123.camundala.model.bpmn.ConditionExpression.{Expression, GroovyJsonExpression}
import pme123.camundala.model.bpmn.InputOutput.{InputOutputExpression, InputOutputMap}
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{suite, _}

object InputOutputSuite extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("InputOutputSuite")(
      suite("InputOutputExpression")(
        test("Input from Json") {
          val jsonPath: JsonPath = Seq("existingAddress", "street")
          assert(
            InputOutputExpression("existingAddress__street", Expression(s"""$${existingAddress.prop("street").stringValue()}"""))
          )(
            equalTo(InputOutputExpression.inputStringFromJsonPath("existingAddress__street", jsonPath)))
        },
        test("Input String from Json Path") {
          assert(
            Seq(InputOutputExpression("existingAddress__street", Expression(s"""$${existingAddress.prop("street")}""")),
              InputOutputExpression("existingAddress__zipCode", Expression(s"""$${existingAddress.prop("zipCode")}""")),
              InputOutputExpression("existingAddress__city", Expression(s"""$${existingAddress.prop("city")}""")),
              InputOutputExpression("existingAddress__countryIso", Expression(s"""$${existingAddress.prop("countryIso")}""")))
          )(
            equalTo(InputOutputExpression.inputFromJson("existingAddress", TestData.addressChangeForm)))
        },
        test("Input from Map") {
          assert(
            Seq(InputOutputExpression("existingAddress__street", Expression(s"""$${existingAddress.get("street")}""")),
              InputOutputExpression("existingAddress__zipCode", Expression(s"""$${existingAddress.get("zipCode")}""")),
              InputOutputExpression("existingAddress__city", Expression(s"""$${existingAddress.get("city")}""")),
              InputOutputExpression("existingAddress__countryIso", Expression(s"""$${existingAddress.get("countryIso")}""")))
          )(
            equalTo(InputOutputExpression.inputFromMap("existingAddress", TestData.addressChangeForm)))
        }
      ),
      suite("InputOutputMap")(
        test("Output to Map") {
          assert(
            InputOutputMap("existingAddress", Map("street" -> s"$${existingAddress__street}", "zipCode" -> s"$${existingAddress__zipCode}", "city" -> s"$${existingAddress__city}", "countryIso" -> s"$${existingAddress__countryIso}"))
          )(
            equalTo(InputOutputMap.outputToMap("existingAddress", TestData.addressChangeForm)))
        },
        test("Output to Json") {
          assert(
            InputOutputExpression("existingAddress", GroovyJsonExpression(
              s"""["street": existingAddress__street,
                 |"zipCode": existingAddress__zipCode,
                 |"city": existingAddress__city,
                 |"countryIso": existingAddress__countryIso]""".stripMargin)))(equalTo(
            InputOutputExpression.outputToJson("existingAddress", TestData.addressChangeForm)))
        }
      )
    )
}
