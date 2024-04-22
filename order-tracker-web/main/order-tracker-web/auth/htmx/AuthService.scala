package ordertrackerweb.auth.htmx

import zio.*
import at.favre.lib.crypto.bcrypt.BCrypt
import ordertrackerweb.users.PublicUser
import ordertrackerweb.users.htmx.UserService
import ordertrackerweb.auth.*

trait AuthService:
  def login(
      email: String,
      password: String
  ): ZIO[Any, List[String], (String, PublicUser)]

class AuthServiceImpl(
    hashService: HashService,
    tokenService: TokenService,
    userService: UserService
) extends AuthService:

  private val unauthorized: List[String] =
    List("Invalid email or password")

  def login(
      email: String,
      password: String
  ): ZIO[Any, List[String], (String, PublicUser)] =
    for {
      // TODO: test whether we can just accept errors from the user service, probably not
      user <- userService.byEmail(email)
      _ <- hashService
        .verifyHash(password, user.password)
        .mapError(e => List(e.getMessage))
        .flatMap {
          case true  => ZIO.succeed(())
          case false => ZIO.fail(unauthorized)
        }
      token <- tokenService
        .createToken(user.email)
        .mapError(e => List(e.getMessage))
    } yield (token, user.toPublic)

object AuthService:
  val live: ZLayer[
    HashService & TokenService & UserService,
    Nothing,
    AuthService
  ] =
    ZLayer.fromFunction(new AuthServiceImpl(_, _, _))
