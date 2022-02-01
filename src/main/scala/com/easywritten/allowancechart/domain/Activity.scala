package com.easywritten.allowancechart.domain

sealed trait Activity
// TODO 어느 계좌에 입금했는지
final case class Deposit(money: Money) extends Activity
