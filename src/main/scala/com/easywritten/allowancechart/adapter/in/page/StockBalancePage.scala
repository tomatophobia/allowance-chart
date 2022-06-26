package com.easywritten.allowancechart.adapter.in.page

import scalatags.Text._
import scalatags.Text.all._

object StockBalancePage extends Base {
  def html: String = frag.toString

  val frag: TypedTag[String] = layout("주식잔고", Menu.StockBalance)(

  )
}
