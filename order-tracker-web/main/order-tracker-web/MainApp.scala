package ordertrackerweb

import sttp.tapir.*
import sttp.tapir.ztapir.{endpoint, stringBody, plainBody, RichZEndpoint}
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}
import zio.*
import zio.metrics.*
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.metrics.connectors.{MetricsConfig, prometheus}
import zio.metrics.jvm.DefaultJvmMetrics

import ordertrackerweb.metrics.MetricsEndpoints
import ordertrackerweb.users.PostgresUserRepo
import ordertrackerweb.users.api.UserApiService
import ordertrackerweb.users.api.UserApiEndpoints
import ordertrackerweb.users.htmx.UserService
import ordertrackerweb.users.htmx.UserEndpoints
import ordertrackerweb.uuid.UUIDService
import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.db.Db
import ordertrackerweb.config.Configuration
import ordertrackerweb.auth.HashService
import ordertrackerweb.auth.api.AuthApiEndpoints
import ordertrackerweb.auth.api.AuthApiService
import ordertrackerweb.auth.htmx.AuthEndpoints
import ordertrackerweb.auth.htmx.AuthService
import ordertrackerweb.auth.TokenService
import ordertrackerweb.db.Migrator

object MainApp extends ZIOAppDefault {

  val httpServer = for {
    migrator <- ZIO.service[Migrator]
    _ <- migrator.migrate()
    endpoints <- ZIO.service[Endpoints]
    _ <- ZIO.logInfo(s"endpoints: ${endpoints.endpoints}")
    httpApp = ZioHttpInterpreter(endpoints.options).toHttp(endpoints.endpoints)
    _ <- ZIO.logInfo("Starting server on port 8080")
    _ <- Server.install(httpApp)
    _ <- ZIO.never
  } yield ()

  override def run: ZIO[Environment & ZIOAppArgs & Scope, Any, Any] =
    httpServer
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
        ZLayer.succeed(Clock.ClockLive),
        // App layers
        Configuration.live,
        Db.dataSourceLive,
        Db.quillLive,
        Migrator.live,
        UUIDService.live,
        BaseEndpoints.live,
        MetricsEndpoints.live,
        PostgresUserRepo.live,
        UserService.live,
        UserEndpoints.live,
        UserApiService.live,
        UserApiEndpoints.live,
        HashService.live,
        TokenService.live,
        AuthService.live,
        AuthEndpoints.live,
        AuthApiService.live,
        AuthApiEndpoints.live,
        Endpoints.live
      )
}
