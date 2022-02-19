package com.easywritten.allowancechart.domain.account

import com.easywritten.allowancechart.domain.{Money, TickerSymbol}

import java.time.Instant

sealed trait AccountEvent

// companion object에 넣어 네임스페이스를 통해 AccountEvent 중 하나라는 것을 명시적으로 표현
object AccountEvent {
  final case class Initialize(fee: BigDecimal) extends AccountEvent

  final case class Deposit(money: Money) extends AccountEvent

  final case class Withdrawal(money: Money) extends AccountEvent

  final case class Buy(symbol: TickerSymbol, averagePrice: Money, quantity: Int, contractedAt: Instant)
      extends AccountEvent

  final case class Sell(symbol: TickerSymbol, contractPrice: Money, quantity: Int, contractedAt: Instant)
      extends AccountEvent
}
