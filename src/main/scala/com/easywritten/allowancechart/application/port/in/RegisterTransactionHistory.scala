package com.easywritten.allowancechart.application.port.in

import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.account.AccountName
import zio.IO

object RegisterTransactionHistory {
  trait Service {
    def initializeAccountUsingTransactionHistory(name: AccountName, transactionHistories: List[TransactionHistory]): IO[ServiceError, Unit]
  }
}
