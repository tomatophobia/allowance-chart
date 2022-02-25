package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Money, Stock, Ticker, TransactionCost}

import java.time.Instant

sealed trait AccountEvent

// companion object에 넣어 네임스페이스를 통해 AccountEvent 중 하나라는 것을 명시적으로 표현
object AccountEvent {
  final case class Initialize(cost: TransactionCost) extends AccountEvent

  final case class Deposit(money: Money) extends AccountEvent

  final case class Withdrawal(money: Money) extends AccountEvent

  final case class Buy(stock: Stock, averagePrice: Money, quantity: Int, contractedAt: Instant) extends AccountEvent

  final case class Sell(stock: Stock, contractPrice: Money, quantity: Int, contractedAt: Instant) extends AccountEvent
}
