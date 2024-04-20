package ordertrackerweb.auth.models

import zio.json.*
import zio.prelude.Validation

import ordertrackerweb.users.PublicUser

final case class LoginResponse(token: String, user: PublicUser)

object LoginResponse:
  given JsonEncoder[LoginResponse] = DeriveJsonEncoder.gen[LoginResponse]
  given JsonDecoder[LoginResponse] = DeriveJsonDecoder.gen[LoginResponse]
