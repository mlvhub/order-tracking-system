package ordertrackerweb.endpoints

import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.{EndpointOutput, PublicEndpoint, Validator}
import zio.{IO, Task, ZIO, ZLayer}

import ordertrackerweb.errors.AppError
import ordertrackerweb.errors.AppError.*
import ordertrackerweb.auth.TokenService
import ordertrackerweb.users.PrivateUser

class BaseEndpoints(tokenService: TokenService):
  val publicEndpoint: PublicEndpoint[Unit, AppError, Unit, Any] = endpoint
    .errorOut(BaseEndpoints.defaultErrorOutputs)

  private def handleAuth(token: String): ZIO[Any, AppError, PrivateUser] =
    tokenService
      .extractUser(token)
      .mapError(e => InternalServerError(e.getMessage))
      .flatMap {
        case Some(user) => ZIO.succeed(user)
        case None       => ZIO.fail(Unauthorized("Invalid token"))
      }

  val secureEndpoint: ZPartialServerEndpoint[
    Any,
    String,
    PrivateUser,
    Unit,
    AppError,
    Unit,
    Any
  ] = endpoint
    .errorOut(BaseEndpoints.defaultErrorOutputs)
    .securityIn(auth.bearer[String]())
    .zServerSecurityLogic(handleAuth)

object BaseEndpoints:

  val live: ZLayer[TokenService, Nothing, BaseEndpoints] =
    ZLayer.fromFunction(new BaseEndpoints(_))

  private val defaultErrorOutputs: EndpointOutput.OneOf[AppError, AppError] =
    oneOf[AppError](
      oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
      oneOfVariant(
        statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized])
      ),
      oneOfVariant(
        statusCode(StatusCode.InternalServerError).and(
          jsonBody[InternalServerError]
        )
      )
    )
