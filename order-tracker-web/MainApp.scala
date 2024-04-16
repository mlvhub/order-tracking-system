package ordertrackerweb

import sttp.tapir.*
import sttp.tapir.ztapir.{endpoint, stringBody, plainBody, RichZEndpoint}
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}
import zio.*
import zio.metrics.*
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.metrics.connectors.{MetricsConfig, prometheus}
import zio.metrics.connectors.{MetricsConfig, prometheus}
import zio.metrics.jvm.DefaultJvmMetrics

import ordertrackerweb.metrics.MetricsEndpoint
import ordertrackerweb.users.PostgresUserRepo
import ordertrackerweb.users.UserService
import ordertrackerweb.users.UserEndpoints
import ordertrackerweb.uuid.UUIDService
import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.db.Db
import ordertrackerweb.config.Configuration
import ordertrackerweb.auth.AuthService

object MainApp extends ZIOAppDefault {

  def countCharacters(s: String): ZIO[Any, Nothing, Int] =
    ZIO.succeed(s.length)

  val countCharactersEndpoint: PublicEndpoint[String, Unit, Int, Any] =
    endpoint.get.in("count").in(stringBody).out(plainBody[Int])

  val countCharactersHttp: HttpApp[Any] =
    ZioHttpInterpreter().toHttp(
      countCharactersEndpoint.zServerLogic(countCharacters)
    )

  val httpServer = for {
    endpoints <- ZIO.service[Endpoints]
    _ <- ZIO.logInfo(s"endpoints: ${endpoints.endpoints}")
    httpApp = ZioHttpInterpreter().toHttp(endpoints.endpoints)
    _ <- ZIO.logInfo("Starting server on port 8080")
    _ <- Server.install(httpApp)
    _ <- ZIO.never
  } yield ()

  override def run = httpServer
    .provide(
      // ZIO Http default server layer, default port: 8080
      Server.default,
      // The prometheus reporting layer
      prometheus.prometheusLayer,
      prometheus.publisherLayer,
      // Interval for polling metrics
      ZLayer.succeed(MetricsConfig(5.seconds)),
      // Default JVM Metrics
      DefaultJvmMetrics.live.unit,
      // App layers
      Configuration.live,
      Db.dataSourceLive,
      Db.quillLive,
      UUIDService.live,
      AuthService.live,
      BaseEndpoints.live,
      MetricsEndpoint.live,
      PostgresUserRepo.live,
      UserService.live,
      UserEndpoints.live,
      Endpoints.live
    )
}
