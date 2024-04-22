package ordertrackerweb.users.templates

import zio.http.template._

import ordertrackerweb.templates.Layout
import ordertrackerweb.templates.CustomDom.*
import ordertrackerweb.users.CreateUserForm

object Register:

  def apply(
      userForm: Option[CreateUserForm] = None,
      errors: List[String] = List.empty
  ): Html =
    Layout(
      div(
        id := "register-page",
        h1("Register"),
        errorList(errors),
        form(
          Dom.attr("hx-post", "/users"),
          Dom.attr("hx-target", "#register-page"),
          Dom.attr("hx-swap", "outerHtml"),
          classAttr := "mt-2 inline-flex flex-col space-y-2",
          inputFor("name", defaultValue = userForm.map(_.name)),
          inputFor("email", "email", defaultValue = userForm.map(_.email)),
          inputFor(
            "password",
            "password"
          ),
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
