package com.easywritten.allowancechart.application.service

import com.easywritten.allowancechart.application.port.in.{RegisterTransactionRecordPort, TransactionRecord}
import com.easywritten.allowancechart.domain.{Asset, SecuritiesCompany}
import com.easywritten.allowancechart.domain.account.{Account, AccountName}
import zio._
import zio.logging._

import java.time.{Instant, LocalDate, ZoneId, ZoneOffset, ZonedDateTime}

final case class RegisterTransactionRecordService(asset: Asset.Service, logger: Logger[String])
    extends RegisterTransactionRecordPort {
  override def registerTransactionRecord(
      name: AccountName,
      company: SecuritiesCompany,
      transactionRecords: List[TransactionRecord]
  ): IO[ServiceError, Unit] = {
    // 여기서 account 인터페이스의 메소드를 호출한다
    (
      for {
        _ <- asset.initialize(name, company) // TODO 이미 존재하는 계좌에 추가로 거래내역을 입력하는 경우 개발 필요
        _ <- ZIO.foreach_(transactionRecords) {
          case TransactionRecord.Deposit(date, transactionClass, amount, briefName) =>
            asset.deposit(name, amount, localDateToSeoulNoonInstant(date))
          case TransactionRecord.Dividend(date, transactionClass, amount, stock, briefName, tax) =>
            asset.dividendPaid(name, stock, amount, tax, localDateToSeoulNoonInstant(date))
          case TransactionRecord.ForeignExchangeBuy(date, transactionClass, fx, exchangeRate, briefName) =>
            asset.foreignExchangeBuy(name, fx, exchangeRate, localDateToSeoulNoonInstant(date))
          case TransactionRecord.Buy(date, transactionClass, totalPrice, holding, briefName, fee) =>
            asset.buy(name, holding.stock, holding.unitPrice, holding.quantity, localDateToSeoulNoonInstant(date))
          case TransactionRecord.Sell(date, transactionClass, totalPrice, holding, briefName, fee, tax) =>
            asset.sell(name, holding.stock, holding.unitPrice, holding.quantity, localDateToSeoulNoonInstant(date))
        }
      } yield ()
    ).mapError(e => ServiceError.InternalServerError("Account 엔티티 거래내역 입력 중 에러 발생", Some(e)))
      .tapError(e => logger.error(e.message))
  }

  // TODO 서울 12시로 바꾸는 것이 아니라 바깥쪽에서 거래내역 파싱할 때부터 Instant로 바뀌어야 함
  private def localDateToSeoulNoonInstant(date: LocalDate): Instant =
    date.atTime(12, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant
}

object RegisterTransactionRecordService {
  val layer: URLayer[Has[Asset.Service] with Logging, Has[RegisterTransactionRecordPort]] =
    (RegisterTransactionRecordService(_, _)).toLayer
}
