package ordertrackerweb.users.templates

import zio.http.template._

import ordertrackerweb.templates.Layout
import ordertrackerweb.templates.CustomDom.*

object Register:

  def apply(errors: List[String] = List.empty): Html =
    Layout(
      div(
        h1("Register"),
        ul(errors.map(li(_))),
        form(
          Dom.attr("hx-post", "/users"),
          classAttr := "mt-2 inline-flex flex-col space-y-2",
          inputFor("name"),
          inputFor("email", "email"),
          inputFor("password", "password"),
          inputFor(
            "passwordConfirmation",
            "password",
            Some("Password Confirmation")
          ),
          button(
            "Register",
            classAttr := "rounded-md bg-indigo-600 px-2.5 py-1.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          )
        )
      )
    )
