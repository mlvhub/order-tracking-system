package ordertrackerweb.users

import java.util.UUID

import zio.json.*
import zio.schema.*
import zio.prelude.Validation
import io.getquill.*

final case class CreateUserForm(
    name: String,
    email: String,
    password: String,
    passwordConfirmation: String
)
final case class InsertUser(
    id: UUID,
    name: String,
    email: String,
    password: String
)
final case class PublicUser(id: UUID, name: String, email: String)
final case class PrivateUser(
    id: UUID,
    name: String,
    email: String,
    password: String
):
    def toPublic: PublicUser =
        PublicUser(id, name, email)

object CreateUserForm:
    given JsonEncoder[CreateUserForm] = DeriveJsonEncoder.gen[CreateUserForm]
    given JsonDecoder[CreateUserForm] = DeriveJsonDecoder.gen[CreateUserForm]
    given Schema[CreateUserForm] = DeriveSchema.gen[CreateUserForm]

    private def validateName(name: String): Validation[String, String] =
        Validation.fromPredicateWith("Name was empty")(name)(_.nonEmpty)
    private def validateEmail(email: String): Validation[String, String] =
        val regex = """^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$"""
        Validation.fromPredicateWith("Email must be valid")(email)(e =>
            e.nonEmpty && e.matches(regex)
        )
    private def validatePassword(password: String): Validation[String, String] =
        Validation.fromPredicateWith("Password must be at least 8 characters")(
          password
        )(p => p.nonEmpty && p.length >= 8)
    private def validatePasswordConfirmation(
        passwordConfirmation: String,
        password: String
    ): Validation[String, String] =
        Validation.fromPredicateWith(
          "Password confirmation must match password"
        )(
          passwordConfirmation
        )(p => p.nonEmpty && p == password)
    def validate(
        createUser: CreateUserForm
    ): Validation[String, CreateUserForm] =
        val CreateUserForm(name, email, password, passwordConfirmation) =
            createUser
        Validation.validateWith(
          validateName(name),
          validateEmail(email),
          validatePassword(password),
          validatePasswordConfirmation(passwordConfirmation, password)
        )(CreateUserForm.apply)

object PublicUser:
    given JsonEncoder[PublicUser] = DeriveJsonEncoder.gen[PublicUser]
    given JsonDecoder[PublicUser] = DeriveJsonDecoder.gen[PublicUser]
    given Schema[PublicUser] = DeriveSchema.gen[PublicUser]

object PrivateUser:
    given JsonEncoder[PrivateUser] = DeriveJsonEncoder.gen[PrivateUser]
    given JsonDecoder[PrivateUser] = DeriveJsonDecoder.gen[PrivateUser]
    given Schema[PrivateUser] = DeriveSchema.gen[PrivateUser]
