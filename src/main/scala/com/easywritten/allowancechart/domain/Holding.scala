package com.easywritten.allowancechart.domain

final case class Holding(symbol: TickerSymbol, unitPrice: Money, quantity: Int)
