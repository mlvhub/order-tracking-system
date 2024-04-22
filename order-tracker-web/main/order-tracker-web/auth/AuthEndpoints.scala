package ordertrackerweb.auth

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
import ordertrackerweb.users.UserService
import ordertrackerweb.auth.templates.*

class AuthEndpoints(
    baseEndpoints: BaseEndpoints,
    authService: AuthService
):

  private val loginPageUiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.get
      .in("login")
      .out(stringBody)
      .out(header(HeaderNames.ContentType, "text/html"))
      .zServerLogic(_ => ZIO.succeed(Login().encode.toString))

  // TODO: set cookie in htmx
  private val userLoginUiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.post
      .in("auth" / "login")
      .in(formBody[LoginRequest])
      .out(stringBody)
      .out(header(HeaderNames.SetCookie, "token"))
      .out(header("HX-Redirect", "/profile"))
      .zServerLogic { case LoginRequest(email, password) =>
        authService
          .login(email, password)
          .map { case (token, _) =>
            token
          }
      }

  val uiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(loginPageUiEndpoint, userLoginUiEndpoint)

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

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] = List(loginApiEndpoint)

object AuthEndpoints:
  val live: ZLayer[
    BaseEndpoints & AuthService,
    Nothing,
    AuthEndpoints
  ] =
    ZLayer.fromFunction(new AuthEndpoints(_, _))
