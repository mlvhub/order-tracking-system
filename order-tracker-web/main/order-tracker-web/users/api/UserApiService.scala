package ordertrackerweb.users.api

import java.util.UUID

import zio.{ZIO, ZLayer}

import ordertrackerweb.uuid.UUIDService
import ordertrackerweb.errors.AppError
import ordertrackerweb.auth.HashService

import ordertrackerweb.users.*

trait UserApiService {
  def save(user: CreateUserForm): ZIO[Any, AppError, PrivateUser]
  def byEmail(email: String): ZIO[Any, AppError, PrivateUser]
  def byId(id: UUID): ZIO[Any, AppError, PrivateUser]
}

class UserApiServiceImpl(
    userRepo: UserRepo,
    uuidService: UUIDService,
    hashService: HashService
) extends UserApiService {
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

object UserApiService {
  val live
      : ZLayer[UserRepo & UUIDService & HashService, Nothing, UserApiService] =
    ZLayer.fromFunction(new UserApiServiceImpl(_, _, _))
}
