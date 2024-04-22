package ordertrackerweb.templates

import zio.http.template._

object CustomDom:

  def errorList(errors: List[String]): Html =
    div(
      classAttr := (if (errors.nonEmpty) "visible" else "invisible"),
      h3(classAttr := "text-red-500 font-bold", "Errors:"),
      ul(
        classAttr := "text-red-500 list-disc list-inside",
        errors.map(e => li(e, classAttr := "list-disc"))
      )
    )

  def inputFor(
      name: String,
      `type`: String = "text",
      labelText: Option[String] = None,
      defaultValue: Option[String] = None
  ): Html =
    div(
      label(s"${labelText.getOrElse(name.capitalize)}: "),
      input(
        classAttr := "block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6",
        typeAttr := `type`,
        nameAttr := name,
        valueAttr := defaultValue.getOrElse("")
      )
    )
