package ordertrackerweb.metrics

import zio.*
import zio.metrics.*
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.http.{endpoint => zendpoint, *}
import sttp.tapir.ztapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.model.StatusCode
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import ordertrackerweb.endpoints.BaseEndpoints
import sttp.tapir.server.metrics.zio.ZioMetrics
import sttp.tapir.server.interceptor.metrics.MetricsRequestInterceptor

class MetricsEndpoints(
    baseEndpoints: BaseEndpoints,
    prometheusPublisher: PrometheusPublisher
):

  private val metrics: ZioMetrics[Task] = ZioMetrics.default[Task]()
  val metricsInterceptor: MetricsRequestInterceptor[Task] =
    metrics.metricsInterceptor()

  private val endpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.get
      .in("metrics")
      .out(stringBody)
      .zServerLogic(_ => prometheusPublisher.get)

  val endpoints: List[ZServerEndpoint[Any, Any]] = List(endpoint)

object MetricsEndpoints:
  val live: ZLayer[
    PrometheusPublisher & BaseEndpoints,
    Nothing,
    MetricsEndpoints
  ] =
    ZLayer.fromFunction(MetricsEndpoints(_, _))
