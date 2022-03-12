package com.easywritten.allowancechart.application.service

import com.easywritten.allowancechart.application.port.in.{RegisterTransactionHistoryPort, TransactionHistory}
import com.easywritten.allowancechart.domain.account.AccountName
import zio._

final case class RegisterTransactionHistoryService() extends RegisterTransactionHistoryPort {
  override def registerTransactionHistory(name: AccountName, transactionHistories: List[TransactionHistory]): IO[ServiceError, Unit] = ???
}

object RegisterTransactionHistoryService {
  // about lift case class toLayer https://discord.com/channels/629491597070827530/630498701860929559/848905356414287912
  val layer: ULayer[Has[RegisterTransactionHistoryPort]] = (RegisterTransactionHistoryService.apply _).toLayer
}