package com.easywritten.allowancechart.adapter.in

import scalatags.Text._
import scalatags.Text.all._

object ExamplePage {
  def apply(): String = frag.toString

  val frag: TypedTag[String] =
    html(
      head(
        script("some script")
      ),
      body(
        h1("This is my title"),
        div(
          p("This is my first paragraph"),
          p("This is my second paragraph")
        )
      )
    )
}
