package ordertrackerweb.users

import java.util.UUID

import zio.json.*
import zio.prelude.Validation
import io.getquill._

final case class CreateUserForm(name: String, email: String, password: String, passwordConfirmation: String)
final case class InsertUser(id: UUID, name: String, email: String, password: String)
final case class User(id: UUID, name: String, email: String)

object CreateUserForm:
  given JsonEncoder[CreateUserForm] = DeriveJsonEncoder.gen[CreateUserForm]
  given JsonDecoder[CreateUserForm] = DeriveJsonDecoder.gen[CreateUserForm]

  private def validateName(name: String): Validation[String, String] =
    Validation.fromPredicateWith("Name was empty")(name)(_.nonEmpty)
  private def validateEmail(email: String): Validation[String, String] =
    val regex = """^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$"""
    Validation.fromPredicateWith("Email must be valid")(email)(e => e.nonEmpty && e.matches(regex))
  private def validatePassword(password: String): Validation[String, String] =
    Validation.fromPredicateWith("Password must be at least 8 characters")(password)(p => p.nonEmpty && p.length >= 8)
  private def validatePasswordConfirmation(passwordConfirmation: String, password: String): Validation[String, String] =
    Validation.fromPredicateWith("Password confirmation must match password")(passwordConfirmation)(p => p.nonEmpty && p == password)
  def validate(createUser: CreateUserForm): Validation[String, CreateUserForm] =
    val CreateUserForm(name, email, password, passwordConfirmation) = createUser
    Validation.validateWith(validateName(name), validateEmail(email), validatePassword(password), validatePasswordConfirmation(passwordConfirmation, password))(CreateUserForm.apply)

object User:
  given JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  given JsonDecoder[User] = DeriveJsonDecoder.gen[User]
