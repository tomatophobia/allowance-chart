package com.easywritten.allowancechart.adapter.in.page

import scalatags.Text._
import scalatags.Text.all._

object RegisterTransactionHistory extends Base {
  def apply(): String = frag.toString

  val frag: TypedTag[String] = layout("Register Transaction History")()
}
