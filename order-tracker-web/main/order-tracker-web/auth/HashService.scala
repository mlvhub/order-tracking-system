package ordertrackerweb.auth

import zio.*
import at.favre.lib.crypto.bcrypt.BCrypt

trait HashService:
  def hash(string: String): Task[String]
  def verifyHash(string: String, hash: String): Task[Boolean]

class HashServiceImpl extends HashService:
  val cost = 12
  def hash(string: String): Task[String] =
    ZIO.from(BCrypt.withDefaults().hashToString(cost, string.toCharArray))

  def verifyHash(string: String, hash: String): Task[Boolean] =
    ZIO.from(BCrypt.verifyer().verify(string.toCharArray, hash).verified)

object HashService:
  val live: ZLayer[Any, Nothing, HashService] =
    ZLayer.succeed(new HashServiceImpl())
