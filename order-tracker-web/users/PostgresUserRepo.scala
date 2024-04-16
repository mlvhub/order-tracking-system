package ordertrackerweb.users

import java.util.UUID

import zio.*
import io.getquill.*
import io.getquill.jdbczio.*

import ordertrackerweb.users.User

case class PostgresUserRepo(quill: Quill.Postgres[SnakeCase])
    extends UserRepo:
  import quill.*

   private inline def queryInsertUser = quote(
    querySchema[InsertUser](entity = "users")
  )

  private inline def queryUser = quote(
    querySchema[User](entity = "users")
  )

  def save(id: UUID, user: CreateUserForm): ZIO[Any, Throwable, User] =
    run(
    queryInsertUser
      .insert(
        _.id -> lift(id),
        _.name -> lift(user.name),
        _.email -> lift(user.email),
        _.password -> lift(user.password)
      )
      .returning(r => (r.id, r.name, r.email, r.password))
  )
    .map((id, name, email, _) => User(id, name, email))
  
  def byEmail(email: String): ZIO[Any, Throwable, Option[User]] =
    run(queryUser.filter(_.email == lift(email)))
      .map(_.headOption)

  def byId(id: UUID): ZIO[Any, Throwable, Option[User]] =
    run(queryUser.filter(_.id == lift(id)))
      .map(_.headOption)

object PostgresUserRepo {
  val live: ZLayer[Quill.Postgres[SnakeCase], Nothing, PostgresUserRepo] = ZLayer.fromFunction(PostgresUserRepo(_))
}

