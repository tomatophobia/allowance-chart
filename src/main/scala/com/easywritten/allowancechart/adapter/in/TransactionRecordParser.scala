package com.easywritten.allowancechart.adapter.in

import cats.implicits._
import com.easywritten.allowancechart.application.port.in.TransactionRecord
import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.{
  Currency,
  Holding,
  Money,
  MoneyBag,
  Nation,
  SecuritiesCompany,
  Stock,
  Ticker
}
import com.github.tototoshi.csv.CSVReader
import zio._
import zio.stream.ZStream

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TransactionRecordParser {

  /** 증권사별 거래내역이 들어있는 csv 파일을 읽어서 TransactionRecord로 변환
    *  파일은 UTF-8 형식으로 저장된 것만 사용
    */
  def fromFile(file: java.io.File, company: SecuritiesCompany): IO[ServiceError, List[TransactionRecord]] =
    company match {
      case SecuritiesCompany.Daishin =>
        val stream = ZStream
          .fromIterator(CSVReader.open(file).iterator)
          .grouped(2)
          .map(x => x.foldLeft(Seq[String]())(_ ++ _))

        // TODO 에러를 ServiceError로 다 퉁치는 건 어찌 오케이인데 로깅으로 어떤 에러 메시지가 떴는지 나와야 한다...
        (
          for {
            head <- stream.take(1)
            body <- stream.drop(1)
            record <- ZStream.fromEffect(parseDaishin(head, body))
          } yield record
        ).runCollect.mapBoth[ServiceError, List[TransactionRecord]](
          e => ServiceError.InternalServerError("거래내역 파일 파싱 실패", Some(e)),
          _.toList
        )

      case SecuritiesCompany.Nonghyup => ???
    }

  // TODO 적요명을 따로 타입으로 만들까?
  object DaishinBriefName {
    val deposit = "개별상품대체입금"
    val fxBuy = "외화매수환전"
    val buy = "현금매수"
    val sell = "현금매도"
    val dividend = "배당금"
    val depositInterest = "예탁금이용료"
  }

  def parseDaishin(schema: Seq[String], data: Seq[String]): IO[ServiceError, TransactionRecord] = {
    val map = schema.zip(data).toMap
    val formatter = DateTimeFormatter.ofPattern("yyyy.M.d")
    // TODO 에러를 그냥 internal server error로 다 퉁쳤음
    // TODO 적요명에 따라 겹치는 파싱이 많음 거래일, 거래구분, 거래금액 등...
    ZIO
      .foreach(map.get("적요명")) {
        case briefName @ (DaishinBriefName.deposit | DaishinBriefName.depositInterest) =>
          for {
            dateString <- ZIO.succeed(map.get("거래일")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            date <- ZIO
              .effect(LocalDate.parse(dateString, formatter))
              .mapError(e => ServiceError.InternalServerError("거래일 파싱 실패", Some(e)))

            transactionClass <- ZIO
              .succeed(map.get("거래구분"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))

            amountString <- ZIO.succeed(map.get("거래금액")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            amount <- ZIO
              .effect(BigDecimal(amountString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("거래금액 파싱 실패", Some(e)))
          } yield TransactionRecord.Deposit(date, transactionClass, Money.krw(amount), briefName)
        case DaishinBriefName.fxBuy =>
          for {
            dateString <- ZIO.succeed(map.get("거래일")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            date <- ZIO
              .effect(LocalDate.parse(dateString, formatter))
              .mapError(e => ServiceError.InternalServerError("거래일 파싱 실패", Some(e)))

            transactionClass <- ZIO
              .succeed(map.get("거래구분"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))

            krwString <- ZIO.succeed(map.get("거래금액")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            krw <- ZIO
              .effect(BigDecimal(krwString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("거래금액 파싱 실패", Some(e)))

            usdString <- ZIO.succeed(map.get("환전금액")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            usd <- ZIO
              .effect(BigDecimal(usdString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("환전금액 파싱 실패", Some(e)))

            rateString <- ZIO.succeed(map.get("입금환율")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            exRate <- ZIO
              .effect(BigDecimal(rateString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("환율 파싱 실패", Some(e)))
          } yield TransactionRecord.ForeignExchangeBuy(
            date,
            transactionClass,
            MoneyBag.fromMoneys(Money.krw(-krw), Money.usd(usd)),
            exRate,
            DaishinBriefName.fxBuy
          )
        case DaishinBriefName.buy =>
          for {
            dateString <- ZIO.succeed(map.get("거래일")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            date <- ZIO
              .effect(LocalDate.parse(dateString, formatter))
              .mapError(e => ServiceError.InternalServerError("거래일 파싱 실패", Some(e)))

            transactionClass <- ZIO
              .succeed(map.get("거래구분"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))

            totalPriceString <- ZIO
              .succeed(map.get("거래금액"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            totalPrice <- ZIO
              .effect(BigDecimal(totalPriceString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("거래금액 파싱 실패", Some(e)))

            ticker <- ZIO.succeed(map.get("종목코드")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            unitPriceString <- ZIO
              .succeed(map.get("단가"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            unitPrice <- ZIO
              .effect(BigDecimal(unitPriceString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("단가 파싱 실패", Some(e)))
            quantity <- ZIO
              .succeed(map.get("수량").map(_.toInt))
              .get
              .orElseFail(ServiceError.InternalServerError("수량 파싱 실패"))

            feeString <- ZIO.succeed(map.get("수수료")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            fee <- ZIO
              .effect(BigDecimal(feeString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("수수료 파싱 실패", Some(e)))
          } yield TransactionRecord.Buy(
            date,
            transactionClass,
            Money.usd(totalPrice),
            Holding(Stock(ticker, Nation.USA), Money.usd(unitPrice), quantity),
            DaishinBriefName.buy,
            Money.usd(fee)
          )
        case DaishinBriefName.sell =>
          for {
            dateString <- ZIO.succeed(map.get("거래일")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            date <- ZIO
              .effect(LocalDate.parse(dateString, formatter))
              .mapError(e => ServiceError.InternalServerError("거래일 파싱 실패", Some(e)))

            transactionClass <- ZIO
              .succeed(map.get("거래구분"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))

            totalPriceString <- ZIO
              .succeed(map.get("거래금액"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            totalPrice <- ZIO
              .effect(BigDecimal(totalPriceString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("거래금액 파싱 실패", Some(e)))

            ticker <- ZIO.succeed(map.get("종목코드")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            unitPriceString <- ZIO
              .succeed(map.get("단가"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            unitPrice <- ZIO
              .effect(BigDecimal(unitPriceString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("단가 파싱 실패", Some(e)))
            quantity <- ZIO
              .succeed(map.get("수량").map(_.toInt))
              .get
              .orElseFail(ServiceError.InternalServerError("수량 파싱 실패"))

            feeString <- ZIO.succeed(map.get("수수료")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            fee <- ZIO
              .effect(BigDecimal(feeString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("수수료 파싱 실패", Some(e)))

            localTaxString <- ZIO
              .succeed(map.get("현지세"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            localTax <- ZIO
              .effect(BigDecimal(localTaxString.replace(",", "")))
              .mapError(e => ServiceError.InternalServerError("현지세 파싱 실패", Some(e)))
          } yield TransactionRecord.Sell(
            date,
            transactionClass,
            Money.usd(totalPrice),
            Holding(Stock(ticker, Nation.USA), Money.usd(unitPrice), quantity),
            DaishinBriefName.sell,
            Money.usd(fee),
            Money.usd(localTax)
          )
        case DaishinBriefName.dividend =>
          // TODO 국내 배당, 해외 배당 모두 하나로 처리 중 그냥 나눠...?
          // TODO 미국 주식만 한다고 가정하는데 일본 주식도 있어서 현지세 부분 수정 필요
          for {
            dateString <- ZIO.succeed(map.get("거래일")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            date <- ZIO
              .effect(LocalDate.parse(dateString, formatter))
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))

            transactionClass <- ZIO
              .succeed(map.get("거래구분"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))

            currencyString <- ZIO.succeed(map.get("통화")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            currency <- ZIO
              .effect(Currency.withNameInsensitive(currencyString))
              .mapError(e => ServiceError.InternalServerError("통화 파싱 실패", Some(e)))

            amountString <- ZIO.succeed(map.get("거래금액")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            amount <- ZIO
              .effect(Money(currency, BigDecimal(amountString.replace(",", ""))))
              .mapError(e => ServiceError.InternalServerError("거래금액 파싱 실패", Some(e)))

            ticker <- ZIO.succeed(map.get("종목코드")).get.orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))

            domesticTaxString <- ZIO
              .succeed(map.get("국내세"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            localTaxString <- ZIO
              .succeed(map.get("현지세"))
              .get
              .orElseFail(ServiceError.InternalServerError("적요명 존재하지 않음"))
            tax <- ZIO
              .effect(
                if (domesticTaxString.nonEmpty)
                  Money(
                    Currency.KRW,
                    BigDecimal(domesticTaxString.replace(",", ""))
                  )
                else
                  Money(
                    Currency.USD,
                    BigDecimal(localTaxString.replace(",", ""))
                  )
              )
              .mapError(e => ServiceError.InternalServerError("세금 파싱 실패", Some(e)))
          } yield TransactionRecord.Dividend(
            date,
            transactionClass,
            amount,
            Stock(ticker, if (currency === Currency.KRW) Nation.KOR else Nation.USA),
            DaishinBriefName.dividend,
            tax
          )
      }
      .flatMap(ZIO.fromOption(_).orElseFail(ServiceError.InternalServerError("지원하지 않는 적요명")))
  }

  // TODO 더 좋은 이름
  // TODO ZIO.succeed().get.orElseFail => ZIO.fromOption().orElseFail로 변경
  def daishinParseStringToEntry(raw: Map[String, String]): IO[ServiceError, DaishinEntry] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy.M.d")
    for {
      dateString <- ZIO.succeed(raw.get("거래일")).get.orElseFail(ServiceError.InternalServerError("`거래일` 존재하지 않음"))
      date <- ZIO
        .effect(LocalDate.parse(dateString, formatter))
        .mapError(e => ServiceError.InternalServerError(s"거래일 파싱 실패: $dateString", Some(e)))

      transactionClass <- ZIO
        .succeed(raw.get("거래구분"))
        .get
        .orElseFail(ServiceError.InternalServerError("`거래구분` 존재하지 않음"))

      currencyString <- ZIO.succeed(raw.get("통화")).get.orElseFail(ServiceError.InternalServerError("`통화` 존재하지 않음"))
      currency <-
        if (currencyString === "") ZIO.none
        else
          ZIO
            .effect(Currency.withNameInsensitive(currencyString))
            .mapBoth(e => ServiceError.InternalServerError(s"통화 파싱 실패: $currencyString", Some(e)), Some(_))

      transactionAmountString <- ZIO
        .succeed(raw.get("거래금액"))
        .get
        .orElseFail(ServiceError.InternalServerError("`거래금액` 존재하지 않음"))
      transactionAmount <-
        if (transactionAmountString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(transactionAmountString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"거래금액 파싱 실패: $transactionAmountString", Some(e)), Some(_))

      depositExRateString <- ZIO
        .succeed(raw.get("입금환율"))
        .get
        .orElseFail(ServiceError.InternalServerError("`입금환율` 존재하지 않음"))
      depositExRate <-
        if (depositExRateString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(depositExRateString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"입금환율 파싱 실패: $depositExRateString", Some(e)), Some(_))

      tickerString <- ZIO
        .succeed(raw.get("종목코드"))
        .get
        .orElseFail(ServiceError.InternalServerError("`종목코드` 존재하지 않음"))
      ticker = if (tickerString === "") None else Some(tickerString)

      quantityString <- ZIO.succeed(raw.get("수량")).get.orElseFail(ServiceError.InternalServerError("`수량` 존재하지 않음"))
      quantity <-
        if (quantityString === "") ZIO.none
        else
          ZIO
            .effect(quantityString.toInt)
            .mapBoth(e => ServiceError.InternalServerError(s"수량 파싱 실패: $quantityString", Some(e)), Some(_))

      domesticTaxString <- ZIO
        .succeed(raw.get("국내세"))
        .get
        .orElseFail(ServiceError.InternalServerError("`국내세` 존재하지 않음"))
      domesticTax <-
        if (domesticTaxString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(domesticTaxString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"국내세 파싱 실패: $domesticTaxString", Some(e)), Some(_))

      briefName <- ZIO.succeed(raw.get("적요명")).get.orElseFail(ServiceError.InternalServerError("`적요명` 존재하지 않음"))

      fxCurrencyString <- ZIO.succeed(raw.get("환전")).get.orElseFail(ServiceError.InternalServerError("`환전` 존재하지 않음"))
      fxCurrency <-
        if (fxCurrencyString === "") ZIO.none
        else
          ZIO
            .effect(Currency.withNameInsensitive(fxCurrencyString))
            .mapBoth(e => ServiceError.InternalServerError(s"환전 파싱 실패: $fxCurrencyString", Some(e)), Some(_))

      fxAmountString <- ZIO.succeed(raw.get("환전금액")).get.orElseFail(ServiceError.InternalServerError("`환전금액` 존재하지 않음"))
      fxAmount <-
        if (fxAmountString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(fxAmountString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"환전금액 파싱 실패: $fxAmountString", Some(e)), Some(_))

      withdrawalExRateString <- ZIO
        .succeed(raw.get("출금환율"))
        .get
        .orElseFail(ServiceError.InternalServerError("`출금환율` 존재하지 않음"))
      withdrawalExRate <-
        if (withdrawalExRateString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(withdrawalExRateString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"출금환율 파싱 실패: $withdrawalExRateString", Some(e)), Some(_))

      unitPriceString <- ZIO
        .succeed(raw.get("단가"))
        .get
        .orElseFail(ServiceError.InternalServerError("`단가` 존재하지 않음"))
      unitPrice <-
        if (unitPriceString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(unitPriceString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"단가 파싱 실패: $unitPriceString", Some(e)), Some(_))

      feeString <- ZIO.succeed(raw.get("수수료")).get.orElseFail(ServiceError.InternalServerError("`수수료` 존재하지 않음"))
      fee <-
        if (feeString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(feeString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"수수료 파싱 실패: $feeString", Some(e)), Some(_))

      localTaxString <- ZIO
        .succeed(raw.get("현지세"))
        .get
        .orElseFail(ServiceError.InternalServerError("`현지세` 존재하지 않음"))
      localTax <-
        if (localTaxString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(localTaxString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"현지세 파싱 실패: $localTaxString", Some(e)), Some(_))

    } yield DaishinEntry(
      date,
      transactionClass,
      currency,
      transactionAmount,
      depositExRate,
      ticker,
      quantity,
      domesticTax,
      briefName,
      fxCurrency,
      fxAmount,
      withdrawalExRate,
      unitPrice,
      fee,
      localTax
    )
  }

  def daishinMergePartialBuyOrSell(entries: List[DaishinEntry]): IO[ServiceError, DaishinEntry] = {
    for {
      head <- ZIO.succeed(entries.headOption).get.orElseFail(ServiceError.InternalServerError("합칠 주문이 존재하지 않음"))
      briefName = head.briefName
      ticker = head.ticker
      merged <- ZIO.foldLeft(entries.drop(1))(head) { (acc, entry) =>
        if (acc.briefName === briefName && acc.ticker === ticker) {
          val q1 = acc.quantity.getOrElse(0)
          val q2 = entry.quantity.getOrElse(0)
          val p1 = acc.unitPrice.getOrElse(BigDecimal(0))
          val p2 = entry.unitPrice.getOrElse(BigDecimal(0))
          val ta1 = acc.transactionAmount.getOrElse(BigDecimal(0))
          val ta2 = entry.transactionAmount.getOrElse(BigDecimal(0))
          val f1 = acc.fee.getOrElse(BigDecimal(0))
          val f2 = entry.fee.getOrElse(BigDecimal(0))
          ZIO.succeed(
            acc.copy(
              quantity = Some(q1 + q2),
              transactionAmount = Some(ta1 + ta2),
              unitPrice = Some((q1 * p1 + q2 * p2) / (q1 + q2)), // 반올림 등은 여기서 하지 않고 나중에 Money로 바뀔 때 한다
              fee = Some(f1 + f2)
            )
          )
        } else
          ZIO.fail(ServiceError.InternalServerError("합칠 주문의 적요명이 일치하지 않음"))
      }
    } yield merged
  }

  def daishinParseEntryToRecord(entry: DaishinEntry): IO[ServiceError, TransactionRecord] =
    entry.briefName match {
      case DaishinBriefName.deposit | DaishinBriefName.depositInterest =>
        for {
          amount <- ZIO.fromOption(entry.transactionAmount).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
        } yield TransactionRecord.Deposit(entry.date, entry.transactionClass, Money.krw(amount), entry.briefName)
      case DaishinBriefName.fxBuy =>
        for {
          currency <- ZIO.fromOption(entry.currency).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          fxCurrency <- ZIO.fromOption(entry.fxCurrency).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 환전 존재하지 않음"))
          in <- ZIO.fromOption(entry.transactionAmount).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          out <- ZIO.fromOption(entry.fxAmount).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 환전금액 존재하지 않음"))
          exRate <- ZIO.fromOption(entry.depositExRate).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 입금환율 존재하지 않음"))
        } yield TransactionRecord.ForeignExchangeBuy(
          entry.date,
          entry.transactionClass,
          MoneyBag.fromMoneys(Money(currency, -in), Money(fxCurrency, out)),
          exRate,
          entry.briefName
        )
      case DaishinBriefName.buy =>
        for {
          currency <- ZIO.fromOption(entry.currency).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          totalPrice <- ZIO.fromOption(entry.transactionAmount).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          ticker <- ZIO.fromOption(entry.ticker).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 종목코드 존재하지 않음"))
          unitPrice <- ZIO.fromOption(entry.unitPrice).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 단가 존재하지 않음"))
          quantity <- ZIO.fromOption(entry.quantity).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수량 존재하지 않음"))
          fee <- ZIO.fromOption(entry.fee).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수수료 존재하지 않음"))
        } yield TransactionRecord.Buy(
          entry.date,
          entry.transactionClass,
          Money(currency, totalPrice),
          Holding(Stock(ticker, Nation.fromCurrency(currency)), Money(currency, unitPrice), quantity),
          entry.briefName,
          Money(currency, fee)
        )
      case DaishinBriefName.sell =>
        for {
          currency <- ZIO.fromOption(entry.currency).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          totalPrice <- ZIO.fromOption(entry.transactionAmount).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          ticker <- ZIO.fromOption(entry.ticker).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 종목코드 존재하지 않음"))
          unitPrice <- ZIO.fromOption(entry.unitPrice).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 단가 존재하지 않음"))
          quantity <- ZIO.fromOption(entry.quantity).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수량 존재하지 않음"))
          fee <- ZIO.fromOption(entry.fee).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수수료 존재하지 않음"))
          localTax <- ZIO.fromOption(entry.localTax).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 현지세 존재하지 않음"))
        } yield TransactionRecord.Sell(
          entry.date,
          entry.transactionClass,
          Money(currency, totalPrice),
          Holding(Stock(ticker, Nation.fromCurrency(currency)), Money.usd(unitPrice), quantity),
          entry.briefName,
          Money(currency, fee),
          Money(currency, localTax)
        )
      case DaishinBriefName.dividend =>
        for {
          currency <- ZIO.fromOption(entry.currency).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          amount <- ZIO.fromOption(entry.transactionAmount).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          ticker <- ZIO.fromOption(entry.ticker).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 종목코드 존재하지 않음"))
          // TODO 해외주식거래내역밖에 없어서 현지세 데이터만 있어서 국내세가 있는 배당 처리는 보류
          localTax <- ZIO.fromOption(entry.localTax).orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 현지세 존재하지 않음"))
        } yield TransactionRecord.Dividend(
          entry.date,
          entry.transactionClass,
          Money(currency, amount),
          Stock(ticker, Nation.fromCurrency(currency)),
          entry.briefName,
          Money(currency, localTax)
        )
      case _ => ZIO.fail(ServiceError.InternalServerError(s"지원하지 않는 적요명: ${entry.briefName}"))
    }
}

/** @param date 거래일
  * @param transactionClass 거래구분
  * @param currency 통화
  * @param transactionAmount 거래금액
  * @param depositExRate 입금환율
  * @param ticker 종목코드
  * @param quantity 수량
  * @param domesticTax 현지세
  * @param briefName 적요명
  * @param fxCurrency 환전
  * @param fxAmount 환전금액
  * @param withdrawalExRate 출금환율
  * @param unitPrice 단가
  * @param fee 수수료
  * @param localTax 현지세
  */
final case class DaishinEntry(
    date: LocalDate,
    transactionClass: String,
    currency: Option[Currency],
    transactionAmount: Option[BigDecimal],
    depositExRate: Option[BigDecimal],
    ticker: Option[Ticker],
    quantity: Option[Int],
    domesticTax: Option[BigDecimal],
    briefName: String,
    fxCurrency: Option[Currency],
    fxAmount: Option[BigDecimal],
    withdrawalExRate: Option[BigDecimal],
    unitPrice: Option[BigDecimal],
    fee: Option[BigDecimal],
    localTax: Option[BigDecimal]
)
