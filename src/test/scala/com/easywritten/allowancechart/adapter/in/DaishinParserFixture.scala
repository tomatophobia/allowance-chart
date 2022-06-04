package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.port.in.TransactionRecord
import com.easywritten.allowancechart.domain.{Currency, Holding, Money, MoneyBag, Nation, Stock, ZeroAmount}

import java.time.LocalDate

object DaishinParserFixture {

  val schema: List[String] = List(
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

  val stringToRecord: Map[Seq[String], TransactionRecord] = Map(
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
    ),
    List(
      "2021.4.11",
      "입금",
      "",
      "5",
      "",
      "",
      "",
      "",
      "0",
      "",
      "",
      "",
      "",
      "1",
      "예탁금이용료",
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
      "15"
    ) -> TransactionRecord.Deposit(
      LocalDate.of(2021, 4, 11),
      "입금",
      Money.krw(5),
      "예탁금이용료"
    ),
    List(
      "2021.5.25",
      "해외증권장내매매",
      "USD",
      "79.07",
      "",
      "",
      "PBW",
      "1",
      "0",
      "",
      "",
      "",
      "",
      "1",
      "현금매도",
      "",
      "",
      "",
      "",
      "Powershares Wilderh Clean En",
      "79.07",
      "0.06",
      "0.01",
      "",
      "2,423.51",
      "500,015"
    ) -> TransactionRecord.Sell(
      LocalDate.of(2021, 5, 25),
      "해외증권장내매매",
      Money.usd(79.07),
      Holding(Stock("PBW", Nation.USA), Money.usd(79.07), 1),
      "현금매도",
      Money.usd(0.06),
      Money.usd(0.01)
    )
    //    List(
    //      "2020.12.17",
    //      "해외증권장내매매",
    //      "USD",
    //      "",
    //      "",
    //      "",
    //      "DKNG",
    //      "2",
    //      "2",
    //      "",
    //      "",
    //      "",
    //      "",
    //      "2",
    //      "현금매수",
    //      "",
    //      "",
    //      "",
    //      "",
    //      "Draftkings Inc",
    //      "50.19",
    //      "",
    //      "",
    //      "",
    //      "808.34",
    //      "5"
    //    ) -> TransactionRecord.PartialBuy(
    //      LocalDate.of(2020, 12, 17),
    //      "해외증권장내매매",
    //      Holding(Stock("DKNG", Nation.USA), Money.usd(50.19), 2),
    //      "현금매수"
    //    )
  )

  val stringToEntry: Map[Seq[String], DaishinEntry] = Map(
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
    ) -> DaishinEntry(
      LocalDate.of(2020, 10, 12),
      "입금",
      None,
      Some(BigDecimal(500000)),
      None,
      None,
      None,
      None,
      "개별상품대체입금",
      None,
      None,
      None,
      None,
      None,
      None
    ),
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
      -> DaishinEntry(
        LocalDate.of(2020, 10, 12),
        "입금",
        Some(Currency.KRW),
        Some(BigDecimal(499995)),
        Some(BigDecimal(1150.42)),
        None,
        None,
        None,
        "외화매수환전",
        Some(Currency.USD),
        Some(BigDecimal(434.62)),
        None,
        None,
        None,
        None
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
      -> DaishinEntry(
        LocalDate.of(2020, 11, 4),
        "해외증권장내매매",
        Some(Currency.USD),
        Some(BigDecimal(325)),
        None,
        Some("IVV"),
        Some(1),
        None,
        "현금매수",
        None,
        None,
        None,
        Some(BigDecimal(325)),
        Some(BigDecimal(0.26)),
        None
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
    ) -> DaishinEntry(
      LocalDate.of(2020, 12, 22),
      "입금",
      Some(Currency.USD),
      Some(1.61),
      None,
      Some("IVV"),
      None,
      None,
      "배당금",
      None,
      None,
      None,
      None,
      None,
      Some(0.24)
    ),
    List(
      "2021.4.11",
      "입금",
      "",
      "5",
      "",
      "",
      "",
      "",
      "0",
      "",
      "",
      "",
      "",
      "1",
      "예탁금이용료",
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
      "15"
    ) -> DaishinEntry(
      LocalDate.of(2021, 4, 11),
      "입금",
      None,
      Some(5),
      None,
      None,
      None,
      None,
      "예탁금이용료",
      None,
      None,
      None,
      None,
      None,
      None
    ),
    List(
      "2021.5.25",
      "해외증권장내매매",
      "USD",
      "79.07",
      "",
      "",
      "PBW",
      "1",
      "0",
      "",
      "",
      "",
      "",
      "1",
      "현금매도",
      "",
      "",
      "",
      "",
      "Powershares Wilderh Clean En",
      "79.07",
      "0.06",
      "0.01",
      "",
      "2,423.51",
      "500,015"
    ) -> DaishinEntry(
      LocalDate.of(2021, 5, 25),
      "해외증권장내매매",
      Some(Currency.USD),
      Some(79.07),
      None,
      Some("PBW"),
      Some(1),
      None,
      "현금매도",
      None,
      None,
      None,
      Some(79.07),
      Some(0.06),
      Some(0.01)
    ),
    List(
      "2020.12.17",
      "해외증권장내매매",
      "USD",
      "",
      "",
      "",
      "DKNG",
      "2",
      "2",
      "",
      "",
      "",
      "",
      "2",
      "현금매수",
      "",
      "",
      "",
      "",
      "Draftkings Inc",
      "50.19",
      "",
      "",
      "",
      "808.34",
      "5"
    ) -> DaishinEntry(
      LocalDate.of(2020, 12, 17),
      "해외증권장내매매",
      Some(Currency.USD),
      None,
      None,
      Some("DKNG"),
      Some(2),
      None,
      "현금매수",
      None,
      None,
      None,
      Some(50.19),
      None,
      None
    ),
    List(
      "2021.3.15",
      "해외증권장내매매",
      "USD",
      "",
      "",
      "",
      "DDOG",
      "2",
      "10",
      "",
      "",
      "",
      "",
      "1",
      "현금매도",
      "",
      "",
      "",
      "",
      "Datadog Inc",
      "82.92",
      "",
      "",
      "",
      "710.27",
      "12"
    ) -> DaishinEntry(
      LocalDate.of(2021, 3, 15),
      "해외증권장내매매",
      Some(Currency.USD),
      None,
      None,
      Some("DDOG"),
      Some(2),
      None,
      "현금매도",
      None,
      None,
      None,
      Some(82.92),
      None,
      None
    )
  )
}
