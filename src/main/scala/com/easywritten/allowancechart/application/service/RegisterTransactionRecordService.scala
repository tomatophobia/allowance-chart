package com.easywritten.allowancechart.application.service

import com.easywritten.allowancechart.application.port.in.{RegisterTransactionRecordPort, TransactionRecord}
import com.easywritten.allowancechart.domain.SecuritiesCompany
import com.easywritten.allowancechart.domain.account.AccountName
import zio._

final case class RegisterTransactionRecordService() extends RegisterTransactionRecordPort {
  override def registerTransactionRecord(
      name: AccountName,
      company: SecuritiesCompany,
      transactionHistories: List[TransactionRecord]
  ): IO[ServiceError, Unit] = {
    // 여기서 account 인터페이스의 메소드를 호출한다
    ???
  }
}

object RegisterTransactionRecordService {
  // about lift case class toLayer https://discord.com/channels/629491597070827530/630498701860929559/848905356414287912
  val layer: ULayer[Has[RegisterTransactionRecordPort]] = (RegisterTransactionRecordService.apply _).toLayer
}
