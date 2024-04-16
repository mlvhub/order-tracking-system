package ordertrackerweb.endpoints

import sttp.model.StatusCode
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.{EndpointOutput, PublicEndpoint, Validator}
import zio.{IO, Task, ZIO, ZLayer}

import ordertrackerweb.errors.AppError
import ordertrackerweb.errors.AppError.*

class BaseEndpoints():
  val publicEndpoint: PublicEndpoint[Unit, AppError, Unit, Any] = endpoint
    .errorOut(BaseEndpoints.defaultErrorOutputs)

object BaseEndpoints:

  val live: ZLayer[Any, Nothing, BaseEndpoints] = ZLayer.succeed(new BaseEndpoints())

  private val defaultErrorOutputs: EndpointOutput.OneOf[AppError, AppError] =
    oneOf[AppError](
      oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
      oneOfVariant(
        statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized])
      ),
      oneOfVariant(
        statusCode(StatusCode.InternalServerError).and(
          jsonBody[InternalServerError]
        )
      )
    )