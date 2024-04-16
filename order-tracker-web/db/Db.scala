package ordertrackerweb.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.*
import io.getquill.jdbczio.*
import zio.{ZIO, ZLayer}

import javax.sql.DataSource
import ordertrackerweb.config.AppConfig

object Db:
  private def create(config: AppConfig): HikariDataSource = {
    val poolConfig = new HikariConfig()
    poolConfig.setJdbcUrl(config.db.databaseUrl)
    poolConfig.setUsername(config.db.databaseUser)
    poolConfig.setPassword(config.db.databasePassword)
    poolConfig.addDataSourceProperty("databaseName", config.db.databaseName)
    poolConfig.setDataSourceClassName("org.postgresql.ds.PGSimpleDataSource")
    new HikariDataSource(poolConfig)
  }

  // Used for migration and executing queries.
  val dataSourceLive: ZLayer[AppConfig, Nothing, DataSource] =
    ZLayer.scoped {
      ZIO.fromAutoCloseable {
        for {
          config <- ZIO.service[AppConfig]
          dataSource <- ZIO.succeed(create(config))
        } yield dataSource
      }
    }

  // Quill framework object used for specifying sql queries.
  val quillLive: ZLayer[DataSource, Nothing, Quill.Postgres[SnakeCase]] =
    Quill.Postgres.fromNamingStrategy(SnakeCase)
