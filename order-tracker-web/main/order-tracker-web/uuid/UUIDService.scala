package ordertrackerweb.uuid

import java.util.UUID

import zio.{ZIO, ZLayer}

trait UUIDService {
  def random: ZIO[Any, Nothing, UUID]
}

class UUIDServiceImpl() extends UUIDService {
  def random: ZIO[Any, Nothing, UUID] = ZIO.succeed(UUID.randomUUID())
}

object UUIDService {
  val live: ZLayer[Any, Nothing, UUIDService] = ZLayer.succeed(new UUIDServiceImpl())
}

