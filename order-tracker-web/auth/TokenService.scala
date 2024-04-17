package ordertrackerweb.auth

import scala.util.{Success, Try}
import java.util.UUID
import java.time.{Duration => JDuration}

import zio._
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.{JWT, JWTVerifier}
import com.auth0.jwt.interfaces.DecodedJWT

import ordertrackerweb.config.AppConfig
import ordertrackerweb.errors.AppError
import ordertrackerweb.users.UserRepo
import ordertrackerweb.users.PrivateUser

trait TokenService:
  def createToken(email: String): Task[String]
  def extractUser(token: String): Task[Option[PrivateUser]]
  def verifyToken(token: String): Task[Boolean]

class TokenServiceImpl(config: AppConfig, clock: Clock, userRepo: UserRepo)
    extends TokenService:
  private final val Issuer = "OrderTrackerWeb"
  private final val ClaimName = "userEmail"

  private final val algorithm: Algorithm =
    Algorithm.HMAC256(config.system.jwtSecret)
  private final val verifier: JWTVerifier =
    JWT.require(algorithm).withIssuer(Issuer).build()

  override def createToken(email: String): Task[String] =
    for {
      now <- clock.instant
      res <- ZIO.fromTry(
        Try(
          JWT
            .create()
            .withIssuer(Issuer)
            .withClaim(ClaimName, email)
            .withIssuedAt(now)
            .withExpiresAt(now.plus(JDuration.ofHours(1)))
            .withJWTId(UUID.randomUUID().toString)
            .sign(algorithm)
        )
      )
    } yield res

  override def extractUser(token: String): Task[Option[PrivateUser]] = Try(
    verifier.verify(token)
  ) match {
    case Success(decodedJwt: DecodedJWT) =>
      val email = decodedJwt.getClaim(ClaimName).asString()
      userRepo.byEmail(email)
    case _ => ZIO.succeed(None)
  }

  override def verifyToken(token: String): Task[Boolean] =
    extractUser(token).map(_.isDefined)

object TokenService:
  val live: ZLayer[AppConfig & Clock & UserRepo, Nothing, TokenService] =
    ZLayer.fromFunction(new TokenServiceImpl(_, _, _))
