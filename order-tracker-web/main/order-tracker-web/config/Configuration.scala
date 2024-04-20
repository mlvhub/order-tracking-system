package ordertrackerweb.config

import zio.ZLayer
import zio._

final case class RootConfig(
    config: AppConfig
)
final case class AppConfig(
    system: SystemConfig,
    db: DbConfig
)

final case class SystemConfig(
    jwtSecret: String
)

final case class DbConfig(
    databaseUrl: String,
    databaseUser: String,
    databasePassword: String,
    databaseName: String
)

object Configuration:

  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(
      for {
        jwtSecret <- ZIO.config(Config.string("JWT_SECRET"))
        databaseUrl <- ZIO.config(Config.string("DATABASE_URL"))
        databaseUser <- ZIO.config(Config.string("DATABASE_USER"))
        databasePassword <- ZIO.config(Config.string("DATABASE_PASSWORD"))
        databaseName <- ZIO.config(Config.string("DATABASE_NAME"))
      } yield AppConfig(
        system = SystemConfig(jwtSecret = jwtSecret),
        db = DbConfig(
          databaseUrl = databaseUrl,
          databaseUser = databaseUser,
          databasePassword = databasePassword,
          databaseName = databaseName
        )
      )
    )
