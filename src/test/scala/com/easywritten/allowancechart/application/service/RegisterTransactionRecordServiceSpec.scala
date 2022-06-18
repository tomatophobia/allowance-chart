package com.easywritten.allowancechart.application.service

import com.easywritten.allowancechart.application.port.in.TransactionRecord
import com.easywritten.allowancechart.domain.{
  Asset,
  Currency,
  Holding,
  Money,
  MoneyBag,
  Nation,
  SecuritiesCompany,
  Stock,
  TestAsset
}
import com.easywritten.allowancechart.domain.account.AccountName
import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

import java.time.LocalDate

object RegisterTransactionRecordServiceSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Any] =
    suite("RegisterTransactionRecordServiceSpec")(
      testM("register transaction record data and publish account event") {
        val name = AccountName("account1")
        val company = SecuritiesCompany.Daishin
        val transactionRecords = List(
          TransactionRecord.Deposit(LocalDate.of(2020, 10, 12), "입금", Money.krw(500000), "개별상품대체입금"),
          TransactionRecord.ForeignExchangeBuy(
            LocalDate.of(2020, 10, 12),
            "입금",
            MoneyBag.fromMoneys(Money.krw(-499995), Money.usd(434.62)),
            1150.42,
            "외화매수환전"
          ),
          TransactionRecord.Buy(
            LocalDate.of(2020, 11, 4),
            "해외증권장내매매",
            Money.usd(325),
            Holding(Stock("IVV", Nation.USA), Money.usd(325), 1),
            "현금매수",
            Money.usd(0.26)
          ),
          TransactionRecord.Deposit(LocalDate.of(2020, 12, 9), "입금", Money.krw(1000000), "개별상품대체입금"),
          TransactionRecord.ForeignExchangeBuy(
            LocalDate.of(2020, 12, 9),
            "입금",
            MoneyBag.fromMoneys(Money.krw(-1000000), Money.usd(920.65)),
            1086.19,
            "외화매수환전"
          ),
          TransactionRecord.Dividend(
            LocalDate.of(2020, 12, 22),
            "입금",
            Money(Currency.USD, 1.61),
            Stock("IVV", Nation.USA),
            "배당금",
            Money(Currency.USD, 0.24)
          ),
          TransactionRecord.Buy(
            LocalDate.of(2021, 2, 19),
            "해외증권장내매매",
            Money.usd(333),
            Holding(Stock("DDOG", Nation.USA), Money.usd(111), 3),
            "현금매수",
            Money.usd(0.27)
          ),
          TransactionRecord.Sell(
            LocalDate.of(2021, 3, 15),
            "해외증권장내매매",
            Money.usd(249.79),
            Holding(Stock("DDOG", Nation.USA), Money.usd(83.26), 3),
            "현금매도",
            Money.usd(0.2),
            Money.usd(0.01)
          ),
          TransactionRecord.Deposit(LocalDate.of(2021, 4, 11), "입금", Money(Currency.KRW, 5), "예탁금이용료")
        )

        for {
          asset <- ZIO.service[Asset]
          appService = RegisterTransactionRecordService(asset)
          _ <- appService.registerTransactionRecord(name, company, transactionRecords)
        } yield assertCompletes // 에러 없이 돌아가는 것만 확인하는 테스트
        // TODO stub asset을 만든 다음에 asset 내부의 계좌가 적절한 메소드 호출을 받았는지 확인해야 한다.
        // TODO 더 자세하게 하면 최종 계좌 상태까지 확인할 수 있지만 그건 AccountSpec이 따로 있으니까 굳이...?
      }
    ).provideCustomLayer(TestAsset.layer)
}
