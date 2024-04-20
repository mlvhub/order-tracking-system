package ordertrackerweb.auth

import zio.ZLayer
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}

import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.errors.AppError
import ordertrackerweb.auth.models.{LoginRequest, LoginResponse}
import ordertrackerweb.users.UserService

class AuthEndpoints(
    baseEndpoints: BaseEndpoints,
    authService: AuthService
) {
  private val endpoint: ZServerEndpoint[Any, Any] =
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

  val endpoints: List[ZServerEndpoint[Any, Any]] = List(endpoint)
}

object AuthEndpoints {
  val live: ZLayer[
    BaseEndpoints & AuthService,
    Nothing,
    AuthEndpoints
  ] =
    ZLayer.fromFunction(new AuthEndpoints(_, _))
}
