package ordertrackerweb

import zio.*
import zio.http.*

import ordertrackerweb.users.htmx.UserRoutes

class AppRoutes(
    userRoutes: UserRoutes
):
    private val routes = userRoutes.routes

    val httpApp: HttpApp[Any] = routes.toHttpApp

object AppRoutes:
    val live: ZLayer[UserRoutes, Nothing, AppRoutes] =
        ZLayer.fromFunction(new AppRoutes(_))
