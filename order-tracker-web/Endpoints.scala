package ordertrackerweb

import zio.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint

import ordertrackerweb.users.UserEndpoints
import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.metrics.MetricsEndpoints
import ordertrackerweb.auth.AuthEndpoints

class Endpoints(
    metricsEndpoint: MetricsEndpoints,
    userEndpoints: UserEndpoints,
    authEndpoints: AuthEndpoints
):
  private val apiEndpoints =
    userEndpoints.endpoints ++ authEndpoints.endpoints

  val endpoints: List[ZServerEndpoint[Any, Any]] =
    apiEndpoints ++ metricsEndpoint.endpoints
  // private def docsEndpoints(apiEndpoints: List[ZServerEndpoint[Any, Any]]): List[ZServerEndpoint[Any, Any]] =
  // SwaggerInterpreter().fromServerEndpoints[Task](apiEndpoints, "order-tracking-system", "0.0.1")

object Endpoints:
  val live: ZLayer[
    UserEndpoints & MetricsEndpoints & AuthEndpoints,
    Nothing,
    Endpoints
  ] =
    ZLayer.fromFunction(new Endpoints(_, _, _))
