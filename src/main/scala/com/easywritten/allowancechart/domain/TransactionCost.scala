package com.easywritten.allowancechart.domain

// 세금과 수수료를 분리할 필요가 있는가?
final case class TransactionCost(buy: BigDecimal, sell: BigDecimal)

object TransactionCost {
  val zero: TransactionCost = TransactionCost(0, 0)
}
