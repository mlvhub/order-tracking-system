package ordertrackerweb

import zio.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

import ordertrackerweb.users.UserEndpoints
import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.metrics.MetricsEndpoints
import ordertrackerweb.auth.AuthEndpoints

class Endpoints(
    metricsEndpoint: MetricsEndpoints,
    userEndpoints: UserEndpoints,
    authEndpoints: AuthEndpoints
):
  private val uiEndpoints =
    userEndpoints.uiEndpoints ++ authEndpoints.uiEndpoints

  private val apiEndpoints =
    userEndpoints.apiEndpoints ++ authEndpoints.apiEndpoints

  private def docsEndpoints(
      apiEndpoints: List[ZServerEndpoint[Any, Any]]
  ): List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter()
      .fromServerEndpoints[Task](apiEndpoints, "order-tracking-system", "0.0.1")

  val endpoints: List[ZServerEndpoint[Any, Any]] =
    apiEndpoints ++ uiEndpoints ++ metricsEndpoint.endpoints ++ docsEndpoints(
      apiEndpoints
    )

  val options: ZioHttpServerOptions[Any] =
    ZioHttpServerOptions.customiseInterceptors
      .metricsInterceptor(metricsEndpoint.metricsInterceptor)
      .options

object Endpoints:
  val live: ZLayer[
    UserEndpoints & MetricsEndpoints & AuthEndpoints,
    Nothing,
    Endpoints
  ] =
    ZLayer.fromFunction(new Endpoints(_, _, _))
