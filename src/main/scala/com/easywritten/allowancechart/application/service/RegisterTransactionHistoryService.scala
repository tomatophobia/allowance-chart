package com.easywritten.allowancechart.application.service

import com.easywritten.allowancechart.application.port.in.{RegisterTransactionHistory, TransactionHistory}
import com.easywritten.allowancechart.domain.account.AccountName
import zio.{IO, ULayer, ZLayer}

object RegisterTransactionHistoryService {
  def layer: ULayer[RegisterTransactionHistory] = ZLayer.succeed(new RegisterTransactionHistory.Service {
    override def initializeAccountUsingTransactionHistory(
        name: AccountName,
        transactionHistories: List[TransactionHistory]
    ): IO[ServiceError, Unit] = ???
  })
}
