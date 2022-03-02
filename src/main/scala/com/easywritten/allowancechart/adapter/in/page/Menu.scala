package com.easywritten.allowancechart.adapter.in.page

import cats.kernel.Eq
import enumeratum._

sealed abstract class Menu(val name: String, val link: String) extends EnumEntry with Product with Serializable

object Menu extends Enum[Menu] {
  case object RegisterTransactionHistory extends Menu("거래내역 관리", "/transaction-history/register")

  override def values: IndexedSeq[Menu] = findValues

  implicit val eqMenu: Eq[Menu] = Eq.fromUniversalEquals
}