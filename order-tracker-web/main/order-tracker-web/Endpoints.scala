package ordertrackerweb

import zio.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import sttp.tapir.server.ziohttp.ZioHttpInterpreter

import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.metrics.MetricsEndpoints
import ordertrackerweb.auth.api.AuthApiEndpoints
import ordertrackerweb.users.api.UserApiEndpoints
import zio.http.HttpApp

class Endpoints(
    metricsEndpoint: MetricsEndpoints,
    userApiEndpoints: UserApiEndpoints,
    authApiEndpoints: AuthApiEndpoints
):
  private val apiEndpoints =
    userApiEndpoints.endpoints ++ authApiEndpoints.endpoints

  private def docsEndpoints(
      apiEndpoints: List[ZServerEndpoint[Any, Any]]
  ): List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter()
      .fromServerEndpoints[Task](apiEndpoints, "order-tracking-system", "0.0.1")

  private val endpoints: List[ZServerEndpoint[Any, Any]] =
    apiEndpoints ++ metricsEndpoint.endpoints ++ docsEndpoints(
      apiEndpoints
    )

  private val options: ZioHttpServerOptions[Any] =
    ZioHttpServerOptions.customiseInterceptors
      .metricsInterceptor(metricsEndpoint.metricsInterceptor)
      .options

  val httpApp: HttpApp[Any] = ZioHttpInterpreter(options).toHttp(endpoints)

object Endpoints:
  val live: ZLayer[
    MetricsEndpoints & UserApiEndpoints & AuthApiEndpoints,
    Nothing,
    Endpoints
  ] =
    ZLayer.fromFunction(new Endpoints(_, _, _))
