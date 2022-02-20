package camundala.gatling

import io.gatling.core.structure.ChainBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

case class SimulationConfig(
                             // define tenant if you have one
                             tenantId: Option[String] = None,
                             // the Camunda Port
                             // there are Requests that wait until the process is ready - like getTask.
                             // the Simulation waits 1 second between the Requests.
                             // so with a timeout of 10 sec it will try 10 times (retryDuration = 1.second)
                             maxCount: Int = 10,
                             // the number of parallel execution of a simulation.
                             // for example run the process 3 times (userAtOnce = 3)
                             userAtOnce: Int = 1,
                             preRequests: Seq[ChainBuilder] = Nil,
                             // REST endpoint of Camunda
                             endpoint: String = "http://localhost:8080/engine-rest",
                             // you can add authentication with this - default there is none.
                             // see BasicSimulationRunner / OAuthSimulationRunner for examples
                             authHeader: HttpRequestBuilder => HttpRequestBuilder = b => b
                           ):

  def withTenantId(tenantId: String): SimulationConfig =
    copy(tenantId = Some(tenantId))

  def withMaxCount(maxCount: Int): SimulationConfig =
    copy(maxCount = maxCount)

  def withTenantId(userAtOnce: Int = 1): SimulationConfig =
    copy(userAtOnce = userAtOnce)

  def withPreRequests(preRequests: ChainBuilder*): SimulationConfig =
    copy(preRequests = preRequests)

  def withAuthHeader(authHeader: HttpRequestBuilder => HttpRequestBuilder = b => b): SimulationConfig =
    copy(authHeader = authHeader)

  def withPort(port: Int): SimulationConfig =
    copy(endpoint = s"http://localhost:$port/engine-rest")
