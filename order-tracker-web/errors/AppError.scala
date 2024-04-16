package ordertrackerweb.errors

import zio.http.Status.Forbidden
import zio.json.*

sealed trait AppError

object AppError:
  final case class Unauthorized(message: String) extends AppError
  final case class BadRequest(message: String) extends AppError
  final case class NotFound(message: String) extends AppError
  final case class InternalServerError(message: String) extends AppError

  given unauthorizedEncoder: JsonEncoder[Unauthorized] =
    DeriveJsonEncoder.gen[Unauthorized]
  given unauthorizedDecoder: JsonDecoder[Unauthorized] =
    DeriveJsonDecoder.gen[Unauthorized]

  given badRequestEncoder: JsonEncoder[BadRequest] =
    DeriveJsonEncoder.gen[BadRequest]
  given badRequestDecoder: JsonDecoder[BadRequest] =
    DeriveJsonDecoder.gen[BadRequest]

  given notFoundEncoder: JsonEncoder[NotFound] =
    DeriveJsonEncoder.gen[NotFound]
  given notFoundDecoder: JsonDecoder[NotFound] =
    DeriveJsonDecoder.gen[NotFound]

  given internalServerErrorEncoder: JsonEncoder[InternalServerError] =
    DeriveJsonEncoder.gen[InternalServerError]
  given internalServerErrorDecoder: JsonDecoder[InternalServerError] =
    DeriveJsonDecoder.gen[InternalServerError]
