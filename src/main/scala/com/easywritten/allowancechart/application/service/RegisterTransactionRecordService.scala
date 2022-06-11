package com.easywritten.allowancechart.application.service

import com.easywritten.allowancechart.application.port.in.{RegisterTransactionRecordPort, TransactionRecord}
import com.easywritten.allowancechart.domain.{Asset, SecuritiesCompany}
import com.easywritten.allowancechart.domain.account.AccountName
import zio._

final case class RegisterTransactionRecordService(asset: Asset) extends RegisterTransactionRecordPort {
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
  val layer: URLayer[Has[Asset], Has[RegisterTransactionRecordPort]] = (RegisterTransactionRecordService(_)).toLayer
}
