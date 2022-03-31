package com.easywritten.allowancechart.application.port.in

import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.SecuritiesCompany
import com.easywritten.allowancechart.domain.account.AccountName
import zio._

trait RegisterTransactionHistoryPort {
  def registerTransactionHistory(
      name: AccountName,
      company: SecuritiesCompany,
      transactionHistories: List[TransactionHistory]
  ): IO[ServiceError, Unit]
}

object RegisterTransactionHistoryPort {
  def registerTransactionHistory(
      name: AccountName,
      company: SecuritiesCompany,
      transactionHistories: List[TransactionHistory]
  ): ZIO[Has[RegisterTransactionHistoryPort], ServiceError, Unit] =
    ZIO.serviceWith[RegisterTransactionHistoryPort](
      _.registerTransactionHistory(name, company, transactionHistories)
    )
}
