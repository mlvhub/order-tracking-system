package ordertrackerweb.users.templates

import zio.http.template._

import ordertrackerweb.users.PublicUser

object Profile:
  def apply(user: PublicUser): Html =
    div(
      h1(user.name),
      p(user.email)
    )
