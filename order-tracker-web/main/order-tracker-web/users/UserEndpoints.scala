package ordertrackerweb.users

import java.util.UUID

import zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}
import sttp.model.HeaderNames

import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.errors.AppError.*
import ordertrackerweb.users.templates.*

class UserEndpoints(baseEndpoints: BaseEndpoints, userService: UserService):
  // TODO: allow admins to query for anyone
  private val profilePageUiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.secureEndpoint.get
      .in("profile")
      .out(stringBody)
      .out(header(HeaderNames.ContentType, "text/html"))
      .serverLogic(user =>
        _ => ZIO.succeed(Profile(user.toPublic).encode.toString)
      )

  private val registerPageUiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.get
      .in("register")
      .out(stringBody)
      .out(header(HeaderNames.ContentType, "text/html"))
      .zServerLogic(_ => ZIO.succeed(Register().encode.toString))

  private val createUserUiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.post
      .in("users")
      .in(formBody[CreateUserForm])
      .out(header("HX-Redirect", "/login"))
      .zServerLogic(userService.save(_).map(_.toPublic).mapError())

  val uiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(profilePageUiEndpoint, registerPageUiEndpoint, createUserUiEndpoint)

  private val createUserApiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.post
      .in("api" / "users")
      .in(jsonBody[CreateUserForm])
      .out(jsonBody[PublicUser])
      .zServerLogic(userService.save(_).map(_.toPublic))

  // TODO: allow admins to query for anyone
  private val byIdApiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.secureEndpoint.get
      .in("api" / "users" / path[UUID])
      .out(jsonBody[PublicUser])
      .serverLogic(user =>
        _ match {
          case id if id == user.id => userService.byId(id).map(_.toPublic)
          case _                   => ZIO.fail(Unauthorized("Invalid token"))
        }
      )

  // TODO: allow admins to query for anyone
  private val byEmailApiEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.secureEndpoint.get
      .in("api" / "users" / "email" / path[String])
      .out(jsonBody[PublicUser])
      .serverLogic(user =>
        _ match {
          case email if email == user.email =>
            userService.byEmail(email).map(_.toPublic)
          case _ => ZIO.fail(Unauthorized("Invalid token"))
        }
      )

  val apiEndpoints: List[ZServerEndpoint[Any, Any]] =
    List(createUserApiEndpoint, byIdApiEndpoint, byEmailApiEndpoint)

object UserEndpoints:
  val live: ZLayer[BaseEndpoints & UserService, Nothing, UserEndpoints] =
    ZLayer.fromFunction(new UserEndpoints(_, _))
