package ordertrackerweb.auth

import zio.*

trait AuthService:
  def hashPassword(password: String): Task[String]

class AuthServiceImpl extends AuthService:
  // TODO: implement password hashing
  def hashPassword(password: String): Task[String] = ZIO.succeed(password)

object AuthService:
  val live: ZLayer[Any, Nothing, AuthService] = ZLayer.succeed(new AuthServiceImpl())

