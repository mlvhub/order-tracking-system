package ordertrackerweb.metrics

import zio.*
import zio.metrics.*
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.http.{endpoint => zendpoint, *}
import sttp.tapir.ztapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.model.StatusCode
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import ordertrackerweb.endpoints.BaseEndpoints

class MetricsEndpoint(
  baseEndpoints: BaseEndpoints,
  prometheusPublisher: PrometheusPublisher
):

  val endpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint
    .get
    .in("metrics")
    .out(stringBody)
    .zServerLogic(_ => prometheusPublisher.get)


object MetricsEndpoint:
  val live: ZLayer[
    PrometheusPublisher & BaseEndpoints,
    Nothing,
    MetricsEndpoint
  ] =
    ZLayer.fromFunction(MetricsEndpoint(_, _))
