package com.easywritten.allowancechart

package object domain {
  type MoneyAmount = BigDecimal

  val ZeroAmount: MoneyAmount = BigDecimal(0)

  type Ticker = String
}
