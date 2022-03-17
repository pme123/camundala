package camundala.examples.demos.service

import camundala.examples.demos.MappingDomain.AddressServiceOut
import org.camunda.bpm.engine.delegate.{DelegateExecution, ExecutionListener, JavaDelegate}
import org.camunda.bpm.engine.variable.value.IntegerValue
import io.circe.syntax.*
import io.circe.generic.auto.*
import org.camunda.spin.Spin

class AddressService extends JavaDelegate :

  val addresses: Map[Int, AddressServiceOut] = Map(
    1234 -> AddressServiceOut()
  )

  @throws[Exception]
  def execute(execution: DelegateExecution): Unit =
    val clientId = execution.getVariableTyped[IntegerValue]("customer").getValue
    println(s"Called AddressService for $clientId")
    addresses.get(clientId).foreach {address =>
      execution.setVariable("address", Spin.JSON(address.asJson.toString))
      execution.setVariable("street", address.street)
      execution.setVariable("streetNr", address.streetNr)
      execution.setVariable("zipcode", address.zipcode)
      execution.setVariable("place", address.place)
    }
