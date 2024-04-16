package ordertrackerweb.users

import java.util.UUID

import zio.ZLayer
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{endpoint => zendpoint, *}

import ordertrackerweb.endpoints.BaseEndpoints
import ordertrackerweb.errors.AppError

class UserEndpoints(baseEndpoints: BaseEndpoints, userService: UserService):
  val createUserEndpoint: ZServerEndpoint[Any, Any] = 
    baseEndpoints.publicEndpoint
    .post.in("api" / "users")
    .in(jsonBody[CreateUserForm])
    .out(jsonBody[User])
    .zServerLogic(userService.save)

  val byIdEndpoint: ZServerEndpoint[Any, Any] = baseEndpoints.publicEndpoint
    .get.in("api" / "users" / path[UUID])
    .out(jsonBody[User])
    .zServerLogic(userService.byId)

  val byEmailEndpoint: ZServerEndpoint[Any, Any] = baseEndpoints.publicEndpoint
    .get.in("api" / "users" / "email" / path[String])
    .out(jsonBody[User])
    .zServerLogic(userService.byEmail)

  val endpoints: List[ZServerEndpoint[Any, Any]] = List(createUserEndpoint, byIdEndpoint, byEmailEndpoint)

object UserEndpoints {
  val live: ZLayer[BaseEndpoints & UserService, Nothing, UserEndpoints] = 
    ZLayer.fromFunction(new UserEndpoints(_, _))
}

