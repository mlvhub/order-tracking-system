package ordertrackerweb

import zio.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint

import ordertrackerweb.users.UserEndpoints
import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.metrics.MetricsEndpoint

class Endpoints(metricsEndpoint: MetricsEndpoint, userEndpoints: UserEndpoints):
  private val apiEndpoints = userEndpoints.endpoints

  val endpoints: List[ZServerEndpoint[Any, Any]] = apiEndpoints ++ List(metricsEndpoint.endpoint)
  // private def docsEndpoints(apiEndpoints: List[ZServerEndpoint[Any, Any]]): List[ZServerEndpoint[Any, Any]] = 
    // SwaggerInterpreter().fromServerEndpoints[Task](apiEndpoints, "order-tracking-system", "0.0.1")

object Endpoints:
  val live: ZLayer[UserEndpoints & MetricsEndpoint, Nothing, Endpoints] = ZLayer.fromFunction(new Endpoints(_, _))

