package ordertrackerweb

import zio.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpServerOptions

import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.metrics.MetricsEndpoints
import ordertrackerweb.auth.api.AuthApiEndpoints
import ordertrackerweb.users.api.UserApiEndpoints
import ordertrackerweb.users.htmx.UserEndpoints
import ordertrackerweb.auth.htmx.AuthEndpoints

class Endpoints(
    metricsEndpoint: MetricsEndpoints,
    userEndpoints: UserEndpoints,
    authEndpoints: AuthEndpoints,
    userApiEndpoints: UserApiEndpoints,
    authApiEndpoints: AuthApiEndpoints
):
  private val uiEndpoints =
    userEndpoints.endpoints ++ authEndpoints.endpoints

  private val apiEndpoints =
    userApiEndpoints.endpoints ++ authApiEndpoints.endpoints

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
    UserEndpoints & MetricsEndpoints & AuthEndpoints & UserApiEndpoints &
      AuthApiEndpoints,
    Nothing,
    Endpoints
  ] =
    ZLayer.fromFunction(new Endpoints(_, _, _, _, _))
