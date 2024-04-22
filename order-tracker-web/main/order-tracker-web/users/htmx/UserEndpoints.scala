package ordertrackerweb.users.htmx

import java.util.UUID

import zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}
import sttp.model.HeaderNames

import ordertrackerweb.users.*
import ordertrackerweb.users.templates.*

class UserEndpoints(userService: UserService):
  // TODO: implement cookie based auth
  private val profilePageUiEndpoint: ZServerEndpoint[Any, Any] =
    endpoint.get
      .in("profile")
      .out(stringBody)
      .out(header(HeaderNames.ContentType, "text/html"))
      // .zServerLogic(_ => ZIO.succeed(Profile(user.toPublic).encode.toString))
      .zServerLogic(_ => ZIO.succeed("TODO"))

  private val registerPageUiEndpoint: ZServerEndpoint[Any, Any] =
    endpoint.get
      .in("register")
      .out(stringBody)
      .out(header(HeaderNames.ContentType, "text/html"))
      .zServerLogic(_ => ZIO.succeed(Register().encode.toString))

  private val createUserUiEndpoint: ZServerEndpoint[Any, Any] =
    endpoint.post
      .in("users")
      .in(formBody[CreateUserForm])
      .out(stringBody)
      .out(header(HeaderNames.ContentType, "text/html"))
      .zServerLogic(userForm =>
        userService
          .save(userForm)
          .map(u => Profile(u.toPublic).encode.toString)
          .catchAll(e =>
            ZIO.succeed(
              Register(
                userForm = Some(userForm),
                errors = e
              ).encode.toString
            )
          )
      )

  val endpoints: List[ZServerEndpoint[Any, Any]] =
    List(profilePageUiEndpoint, registerPageUiEndpoint, createUserUiEndpoint)

object UserEndpoints:
  val live: ZLayer[UserService, Nothing, UserEndpoints] =
    ZLayer.fromFunction(new UserEndpoints(_))
