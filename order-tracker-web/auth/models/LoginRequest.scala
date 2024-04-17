package ordertrackerweb.auth.models

import zio.json.*
import zio.prelude.Validation

final case class LoginRequest(email: String, password: String)

object LoginRequest:
  given JsonEncoder[LoginRequest] = DeriveJsonEncoder.gen[LoginRequest]
  given JsonDecoder[LoginRequest] = DeriveJsonDecoder.gen[LoginRequest]
