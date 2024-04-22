package ordertrackerweb.auth.api

import zio.*
import at.favre.lib.crypto.bcrypt.BCrypt
import ordertrackerweb.users.PublicUser
import ordertrackerweb.users.api.UserApiService
import ordertrackerweb.errors.AppError
import java.util.concurrent.Flow.Publisher
import ordertrackerweb.errors.AppError.Unauthorized
import ordertrackerweb.auth.*

trait AuthApiService:
  def login(
      email: String,
      password: String
  ): ZIO[Any, AppError, (String, PublicUser)]

class AuthApiServiceImpl(
    hashService: HashService,
    tokenService: TokenService,
    userService: UserApiService
) extends AuthApiService:

  private val unauthorized: AppError =
    AppError.Unauthorized("Invalid email or password")

  def login(
      email: String,
      password: String
  ): ZIO[Any, AppError, (String, PublicUser)] =
    for {
      user <- userService.byEmail(email).mapError {
        case AppError.Unauthorized(_) => unauthorized

        case e => e
      }
      _ <- hashService
        .verifyHash(password, user.password)
        .mapError(e => AppError.InternalServerError(e.getMessage))
        .flatMap {
          case true  => ZIO.succeed(())
          case false => ZIO.fail(unauthorized)
        }
      token <- tokenService
        .createToken(user.email)
        .mapError(e => AppError.InternalServerError(e.getMessage))
    } yield (token, user.toPublic)

object AuthApiService:
  val live: ZLayer[
    HashService & TokenService & UserApiService,
    Nothing,
    AuthApiService
  ] =
    ZLayer.fromFunction(new AuthApiServiceImpl(_, _, _))
