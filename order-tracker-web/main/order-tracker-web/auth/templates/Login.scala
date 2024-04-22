package ordertrackerweb.auth.templates

import zio.http.template._

import ordertrackerweb.templates.Layout
import ordertrackerweb.templates.CustomDom.*

object Login:

  def apply(errors: List[String] = List.empty): Html =
    Layout(
      div(
        h1("Login"),
        errorList(errors),
        form(
          Dom.attr("hx-post", "/auth/login"),
          classAttr := "mt-2 inline-flex flex-col space-y-2",
          inputFor("email", "email"),
          inputFor("password", "password"),
          button(
            "Login",
            classAttr := "rounded-md bg-indigo-600 px-2.5 py-1.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          )
        )
      )
    )
