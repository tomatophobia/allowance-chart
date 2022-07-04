package com.easywritten.allowancechart.adapter.in.page

import scalatags.Text._
import scalatags.Text.all._

object StockBalancePage extends Base {
  def html: String = frag.toString

  val frag: TypedTag[String] =
    layout("주식잔고", Menu.StockBalance, Seq("stock-balance.js"))(
      div(cls := "row")(
        div(cls := "col-12")(
          div(cls := "card")(
            div(cls := "card-header")(
              h3(cls := "card-title")("DataTable with default features")
            ),
            div(cls := "card-body")(
              table(id := "example1", cls := "table table-bordered table-striped")(
                thead(
                  tr(
                    th("1"),
                    th("2"),
                    th("3")
                  )
                ),
                tbody(
                  tr(
                    td("1"),
                    td("2"),
                    td("3")
                  )
                )
              )
            )
          )
        )
      )
    )
}
