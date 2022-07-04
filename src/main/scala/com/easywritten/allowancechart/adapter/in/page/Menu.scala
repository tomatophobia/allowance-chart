package com.easywritten.allowancechart.adapter.in.page

import cats.kernel.Eq
import enumeratum._

sealed abstract class Menu(val name: String, val link: String) extends EnumEntry with Product with Serializable

object Menu extends Enum[Menu] {
  case object ManageTransactionRecord extends Menu("거래내역 관리", "/transaction-record/register-page")

  case object StockBalance extends Menu("주식잔고", "/stock/balance")

  override def values: IndexedSeq[Menu] = findValues

  implicit val eqMenu: Eq[Menu] = Eq.fromUniversalEquals
}
