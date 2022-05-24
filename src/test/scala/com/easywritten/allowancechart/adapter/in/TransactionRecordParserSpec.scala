package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.port.in.TransactionRecord
import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.{Holding, Money, MoneyBag, Nation, SecuritiesCompany, Stock}
import zio._
import zio.test._
import zio.test.Assertion._

import java.nio.file.Paths
import java.time.LocalDate

object TransactionRecordParserSpec extends DefaultRunnableSpec {
  import TransactionRecordParser._
  override def spec: ZSpec[Environment, Failure] = {
    suite("TransactionRecordParserSpec")(
      suite("parse data using schema")(
        testM("Daishin") {
          ZIO.foldLeft(daishinFixture)(assertCompletes) { case (acc, (data, expected)) =>
            val record = parseDaishin(daishinSchema, data)
            assertM(record)(equalTo(expected)).map(_ && acc)
          }
        }
      ),
      suite("parse transaction record file")(
        testM("Daishin") {
          for {
            file <- ZIO.effect(Paths.get(getClass.getResource("/transaction-files/creon-test1.csv").toURI).toFile)
            transactionRecords <- fromFile(file, SecuritiesCompany.Daishin)
          } yield assert(transactionRecords)(equalTo(daishinFixture.values.toList))
        }
      ) @@ TestAspect.ignore // TODO
    )
  }

  val daishinSchema: List[String] = List(
    "거래일",
    "거래구분",
    "통화",
    "거래금액",
    "질권일",
    "입금환율",
    "종목코드",
    "수량",
    "유가잔고",
    "국내세",
    "제미납금",
    "외화결제금액",
    "거래상대명",
    "순번",
    "적요명",
    "환전",
    "환전금액",
    "상환금액",
    "출금환율",
    "종목명",
    "단가",
    "수수료",
    "현지세",
    "연체/신용이자",
    "외화예수금",
    "원화예수금"
  )

  val daishinFixture: Map[Seq[String], TransactionRecord] = Map(
    List(
      "2020.10.12",
      "입금",
      "",
      "500,000",
      "",
      "",
      "",
      "",
      "0",
      "",
      "",
      "",
      "종합투자상품",
      "1",
      "개별상품대체입금",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "0",
      "500,000"
    ) -> TransactionRecord.Deposit(LocalDate.of(2020, 10, 12), "입금", Money.krw(500000), "개별상품대체입금"),
    List(
      "2020.10.12",
      "입금",
      "KRW",
      "499,995",
      "",
      "1,150.42",
      "",
      "",
      "0",
      "",
      "",
      "",
      "",
      "2",
      "외화매수환전",
      "USD",
      "434.62",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "434.62",
      "5"
    )
      -> TransactionRecord.ForeignExchangeBuy(
        LocalDate.of(2020, 10, 12),
        "입금",
        MoneyBag.fromMoneys(Money.krw(-499995), Money.usd(434.62)),
        1150.42,
        "외화매수환전"
      ),
    List(
      "2020.11.4",
      "해외증권장내매매",
      "USD",
      "325",
      "",
      "",
      "IVV",
      "1",
      "1",
      "",
      "",
      "",
      "",
      "1",
      "현금매수",
      "",
      "",
      "",
      "",
      "Ishares Core S&P 500 Etf",
      "325",
      "0.26",
      "",
      "",
      "109.36",
      "5"
    )
      -> TransactionRecord.Buy(
        LocalDate.of(2020, 11, 4),
        "해외증권장내매매",
        Money.usd(325),
        Holding(Stock("IVV", Nation.USA), Money.usd(325), 1),
        "현금매수",
        Money.usd(0.26)
      ),
    List(
      "2020.12.22",
      "입금",
      "USD",
      "1.61",
      "",
      "",
      "IVV",
      "",
      "0",
      "",
      "",
      "",
      "",
      "2",
      "배당금",
      "",
      "",
      "",
      "",
      "아이셰어즈 Core S&P 500 ETF",
      "",
      "",
      "0.24",
      "",
      "383.34",
      "5"
    ) -> TransactionRecord.Dividend(
      LocalDate.of(2020, 12, 22),
      "입금",
      Money.usd(1.61),
      Stock("IVV", Nation.USA),
      "배당금",
      Money.usd(0.24)
    )
  )
}
