package ordertrackerweb.users.htmx

import java.util.UUID

import zio.*
import zio.http.{endpoint => zendpoint, Root, *}
import sttp.model.HeaderNames

import ordertrackerweb.users.*
import ordertrackerweb.users.templates.*
import fansi.ErrorMode.Throw

class UserRoutes(userService: UserService):

    private val getProfileHandler: (UUID, Request) => ZIO[Any, Nothing, Response] =
        (id: UUID, req: Request) =>
            userService
                .byId(id)
                .map(u => Response.html(Profile(u.toPublic)))
                .catchAll(e =>
                    ZIO.succeed(
                      Response
                          .html(
                            Register(errors = e)
                          )
                          .addHeader("HX-Push-Url", "/register")
                    )
                )

    def optionalField(
        name: String,
        form: Form
    ): ZIO[Any, Nothing, String] =
        // this is fine given we do validation on the service
        ZIO
            .fromOption(form.get(name))
            .flatMap(_.asText)
            .catchAll(_ => ZIO.succeed(""))

    private val postRegisterHandler: Request => ZIO[Any, Nothing, Response] =
        (req: Request) => {
            (for {
                form     <- req.body.asURLEncodedForm
                name     <- optionalField("name", form)
                email    <- optionalField("email", form)
                password <- optionalField("password", form)
                passwordConfirmation <- optionalField(
                  "passwordConfirmation",
                  form
                )
                user <- userService.save(
                  CreateUserForm(
                    name,
                    email,
                    password,
                    passwordConfirmation
                  )
                )
            } yield Response.html(Profile(user.toPublic)).addHeader("HX-Push-Url", s"/profile/${user.id}"))
                .catchAll {
                    case e: List[String] =>
                        ZIO.succeed(Response.html(Register(errors = e)))
                    case e: Throwable =>
                        // TODO: add logging
                        ZIO.succeed(
                          Response.html(Register(errors = List("Unknown error, please contact an administrator.")))
                        )
                }
        }

    val routes: Routes[Any, Nothing] = Routes(
      Method.GET / "register" -> handler(Response.html(Register())),
      // TODO: implement cookie based auth
      Method.GET / "profile" / uuid("id") -> handler(getProfileHandler),
      Method.POST / "register"            -> handler(postRegisterHandler)
    )

object UserRoutes:
    val live: ZLayer[UserService, Nothing, UserRoutes] =
        ZLayer.fromFunction(new UserRoutes(_))
