package ordertrackerweb.auth.api

import zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}
import sttp.model.HeaderNames

import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.errors.AppError
import ordertrackerweb.auth.models.{LoginRequest, LoginResponse}
import ordertrackerweb.users.api.UserApiService
import ordertrackerweb.auth.templates.*

class AuthApiEndpoints(
    baseEndpoints: BaseEndpoints,
    authService: AuthApiService
):

  private val loginApiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.post
      .in("api" / "auth" / "login")
      .in(jsonBody[LoginRequest])
      .out(jsonBody[LoginResponse])
      .zServerLogic { case LoginRequest(email, password) =>
        authService
          .login(email, password)
          .map { case (token, user) =>
            LoginResponse(token, user)
          }
      }

  val endpoints: List[ZServerEndpoint[Any, Any]] = List(loginApiEndpoint)

object AuthApiEndpoints:
  val live: ZLayer[
    BaseEndpoints & AuthApiService,
    Nothing,
    AuthApiEndpoints
  ] =
    ZLayer.fromFunction(new AuthApiEndpoints(_, _))
