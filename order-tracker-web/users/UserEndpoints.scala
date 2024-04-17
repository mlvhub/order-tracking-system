package ordertrackerweb.users

import java.util.UUID

import zio.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}

import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.errors.AppError.*

class UserEndpoints(baseEndpoints: BaseEndpoints, userService: UserService):
  val createUserEndpoint: ZServerEndpoint[Any, Any] =
    baseEndpoints.publicEndpoint.post
      .in("api" / "users")
      .in(jsonBody[CreateUserForm])
      .out(jsonBody[PublicUser])
      .zServerLogic(userService.save(_).map(_.toPublic))

  // TODO: allow admins to query for anyone
  val byIdEndpoint: ZServerEndpoint[Any, Any] =
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
  val byEmailEndpoint: ZServerEndpoint[Any, Any] =
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

  val endpoints: List[ZServerEndpoint[Any, Any]] =
    List(createUserEndpoint, byIdEndpoint, byEmailEndpoint)

object UserEndpoints {
  val live: ZLayer[BaseEndpoints & UserService, Nothing, UserEndpoints] =
    ZLayer.fromFunction(new UserEndpoints(_, _))
}
