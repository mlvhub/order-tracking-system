package ordertrackerweb.auth.htmx

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

  // TODO: implement cookie-based auth
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
          .catchAll(e => ZIO.succeed(Login(e).encode.toString))
      }

  val endpoints: List[ZServerEndpoint[Any, Any]] =
    List(loginPageUiEndpoint, userLoginUiEndpoint)

object AuthEndpoints:
  val live: ZLayer[
    BaseEndpoints & AuthService,
    Nothing,
    AuthEndpoints
  ] =
    ZLayer.fromFunction(new AuthEndpoints(_, _))
