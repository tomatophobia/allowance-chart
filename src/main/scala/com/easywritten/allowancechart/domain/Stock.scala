package com.easywritten.allowancechart.domain

import cats.kernel.Eq

final case class Stock(symbol: Ticker, nation: Nation)

object Stock {
  implicit val eqStock: Eq[Stock] = Eq.fromUniversalEquals
}
