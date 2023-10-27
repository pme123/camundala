package camundala
package camunda7.worker

import worker.*
import org.camunda.bpm.client.variable.impl.value.JsonValueImpl


final case class Camunda7Context() extends EngineContext :

  protected def toEngineObject: Json => Any =
    json => new JsonValueImpl(json.toString)

end Camunda7Context

