package com.easywritten.allowancechart.domain

final case class Holding(symbol: TickerSymbol, averagePrice: Money, quantity: Int)
