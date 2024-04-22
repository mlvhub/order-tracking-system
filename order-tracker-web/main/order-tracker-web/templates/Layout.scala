package ordertrackerweb.templates

import zio.http.template._

object Layout:
  def apply(content: Html): Html =
    html(
      head(
        title("Order Tracker"),
        script(
          srcAttr := "https://cdn.tailwindcss.com?plugins=forms,typography,aspect-ratio,line-clamp"
        ),
        script(srcAttr := "https://unpkg.com/htmx.org@1.9.12")
      ),
      body(content)
    )
