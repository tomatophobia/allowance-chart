package com.easywritten.allowancechart.application.port.in

import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.SecuritiesCompany
import com.easywritten.allowancechart.domain.account.AccountName
import zio._

trait RegisterTransactionRecordPort {
  def registerTransactionRecord(
      name: AccountName,
      company: SecuritiesCompany,
      transactionRecords: List[TransactionRecord]
  ): IO[ServiceError, Unit]
}

object RegisterTransactionRecordPort {
  def registerTransactionRecord(
      name: AccountName,
      company: SecuritiesCompany,
      transactionRecords: List[TransactionRecord]
  ): ZIO[Has[RegisterTransactionRecordPort], ServiceError, Unit] =
    ZIO.serviceWith[RegisterTransactionRecordPort](
      _.registerTransactionRecord(name, company, transactionRecords)
    )
}
