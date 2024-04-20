package ordertrackerweb.users

import java.util.UUID

import zio.ZIO

trait UserRepo {
  def save(id: UUID, user: CreateUserForm): ZIO[Any, Throwable, PrivateUser]
  def byEmail(email: String): ZIO[Any, Throwable, Option[PrivateUser]]
  def byId(id: UUID): ZIO[Any, Throwable, Option[PrivateUser]]
}
