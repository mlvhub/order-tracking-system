package ordertrackerweb.users.htmx

import java.util.UUID

import zio.{ZIO, ZLayer}

import ordertrackerweb.uuid.UUIDService
import ordertrackerweb.auth.HashService
import ordertrackerweb.users.*

trait UserService {
  def save(user: CreateUserForm): ZIO[Any, List[String], PrivateUser]
  def byEmail(email: String): ZIO[Any, List[String], PrivateUser]
  def byId(id: UUID): ZIO[Any, List[String], PrivateUser]
}

class UserServiceImpl(
    userRepo: UserRepo,
    uuidService: UUIDService,
    hashService: HashService
) extends UserService {
  def save(createUser: CreateUserForm): ZIO[Any, List[String], PrivateUser] =
    for {
      _ <- ZIO
        .fromEither(
          CreateUserForm
            .validate(createUser)
            .toEither
        )
        .mapError(_.toList)
      id <- uuidService.random
      hashedPassword <- hashService
        .hash(createUser.password)
        .mapError(e => List(e.getMessage))
      data = createUser.copy(password = hashedPassword)
      user <- userRepo
        .save(id, data)
        .mapError(e => List(e.getMessage))
    } yield user

  def byEmail(email: String): ZIO[Any, List[String], PrivateUser] =
    userRepo
      .byEmail(email)
      .mapError(e => List(e.getMessage))
      .flatMap {
        case Some(user) => ZIO.succeed(user)
        case None       => ZIO.fail(List("User not found"))
      }

  def byId(id: UUID): ZIO[Any, List[String], PrivateUser] =
    userRepo
      .byId(id)
      .mapError(e => List(e.getMessage))
      .flatMap {
        case Some(user) => ZIO.succeed(user)
        case None       => ZIO.fail(List("User not found"))
      }
}

object UserService {
  val live: ZLayer[UserRepo & UUIDService & HashService, Nothing, UserService] =
    ZLayer.fromFunction(new UserServiceImpl(_, _, _))
}
