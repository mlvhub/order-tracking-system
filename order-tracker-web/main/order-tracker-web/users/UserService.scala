package ordertrackerweb.users

import java.util.UUID

import zio.{ZIO, ZLayer}

import ordertrackerweb.uuid.UUIDService
import ordertrackerweb.errors.AppError
import ordertrackerweb.auth.HashService

trait UserService {
  def save(user: CreateUserForm): ZIO[Any, AppError, PrivateUser]
  def byEmail(email: String): ZIO[Any, AppError, PrivateUser]
  def byId(id: UUID): ZIO[Any, AppError, PrivateUser]
}

class UserServiceImpl(
    userRepo: UserRepo,
    uuidService: UUIDService,
    hashService: HashService
) extends UserService {
  def save(createUser: CreateUserForm): ZIO[Any, AppError, PrivateUser] =
    for {
      _ <- CreateUserForm
        .validate(createUser)
        .toZIO
        .mapError(e => AppError.BadRequest(e))
      id <- uuidService.random
      hashedPassword <- hashService
        .hash(createUser.password)
        .mapError(e => AppError.InternalServerError(e.getMessage))
      data = createUser.copy(password = hashedPassword)
      user <- userRepo
        .save(id, data)
        .mapError(e => AppError.InternalServerError(e.getMessage))
    } yield user

  def byEmail(email: String): ZIO[Any, AppError, PrivateUser] =
    userRepo
      .byEmail(email)
      .mapError(e => AppError.InternalServerError(e.getMessage))
      .flatMap {
        case Some(user) => ZIO.succeed(user)
        case None       => ZIO.fail(AppError.NotFound("User not found"))
      }

  def byId(id: UUID): ZIO[Any, AppError, PrivateUser] =
    userRepo
      .byId(id)
      .mapError(e => AppError.InternalServerError(e.getMessage))
      .flatMap {
        case Some(user) => ZIO.succeed(user)
        case None       => ZIO.fail(AppError.NotFound("User not found"))
      }
}

object UserService {
  val live: ZLayer[UserRepo & UUIDService & HashService, Nothing, UserService] =
    ZLayer.fromFunction(new UserServiceImpl(_, _, _))
}
