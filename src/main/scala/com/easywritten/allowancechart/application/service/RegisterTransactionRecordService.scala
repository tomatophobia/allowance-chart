package com.easywritten.allowancechart.application.service

import com.easywritten.allowancechart.application.port.in.{RegisterTransactionRecordPort, TransactionRecord}
import com.easywritten.allowancechart.domain.{Asset, SecuritiesCompany, TransactionCost}
import com.easywritten.allowancechart.domain.account.{Account, AccountName}
import zio._

import java.time.{Instant, LocalDate, ZoneId, ZoneOffset, ZonedDateTime}

final case class RegisterTransactionRecordService(asset: Asset) extends RegisterTransactionRecordPort {
  override def registerTransactionRecord(
      name: AccountName,
      company: SecuritiesCompany,
      transactionRecords: List[TransactionRecord]
  ): IO[ServiceError, Unit] = {
    // 여기서 account 인터페이스의 메소드를 호출한다
    val account: Account = asset.accounts(name)
    (
      for {
        _ <- account.initialize(company) // TODO 이미 존재하는 계좌에 추가로 거래내역을 입력하는 경우 개발 필요
        _ <- ZIO.foreach_(transactionRecords) {
          case TransactionRecord.Deposit(date, transactionClass, amount, briefName) =>
            account.deposit(amount, localDateToSeoulNoonInstant(date))
          case TransactionRecord.Dividend(date, transactionClass, amount, stock, briefName, tax) =>
            account.dividendPaid(stock, amount, tax, localDateToSeoulNoonInstant(date))
          case TransactionRecord.ForeignExchangeBuy(date, transactionClass, fx, exchangeRate, briefName) =>
            account.foreignExchangeBuy(fx, exchangeRate, localDateToSeoulNoonInstant(date))
          case TransactionRecord.Buy(date, transactionClass, totalPrice, holding, briefName, fee) =>
            account.buy(holding.stock, holding.unitPrice, holding.quantity, localDateToSeoulNoonInstant(date))
          case TransactionRecord.Sell(date, transactionClass, totalPrice, holding, briefName, fee, tax) =>
            account.sell(holding.stock, holding.unitPrice, holding.quantity, localDateToSeoulNoonInstant(date))
        }
      } yield ()
    ).mapError(e => ServiceError.InternalServerError("Account 엔티티 거래내역 입력 중 에러 발생", Some(e)))
  }

  // TODO 서울 12시로 바꾸는 것이 아니라 바깥쪽에서 거래내역 파싱할 때부터 Instant로 바뀌어야 함
  private def localDateToSeoulNoonInstant(date: LocalDate): Instant =
    date.atTime(12, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant
}

object RegisterTransactionRecordService {
  val layer: URLayer[Has[Asset], Has[RegisterTransactionRecordPort]] = (RegisterTransactionRecordService(_)).toLayer
}
