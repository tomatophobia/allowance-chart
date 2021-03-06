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

// TODO kantan 이용해서 타입클래스 기반 csv 인코딩...?
object TransactionRecordParser {

  /** 증권사별 거래내역이 들어있는 csv 파일을 읽어서 TransactionRecord로 변환
    *  파일은 UTF-8 형식으로 저장된 것만 사용
    */
  def fromFile(file: java.io.File, company: SecuritiesCompany): IO[ServiceError, List[TransactionRecord]] =
    company match {
      case SecuritiesCompany.Daishin =>
        val rawStream = ZStream
          .fromIterator(CSVReader.open(file).iterator)
          .grouped(2)
          .map(x => x.foldLeft(Seq[String]())(_ ++ _))
        parseDaishin(rawStream)
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

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps")) // To use `head`
  def parseDaishin(raw: ZStream[Any, Throwable, Seq[String]]): IO[ServiceError, List[TransactionRecord]] = {
    val entries =
      for {
        head <- raw.take(1)
        body <- raw.drop(1)
        entry <- ZStream.fromEffect(daishinParseStringToEntry(head, body))
      } yield entry

    (
      for {
        (swappedRev, buffer) <- entries.foldM((List[DaishinEntry](), List[DaishinEntry]())) {
          case ((acc, buffer), entry) =>
            if (buffer.isEmpty) {
              if (entry.briefName == DaishinBriefName.sell) {
                ZIO.succeed(acc, entry :: buffer)
              } else {
                ZIO.succeed(entry :: acc, buffer)
              }
            } else {
              if (
                entry.ticker == buffer.head.ticker && entry.date == buffer.head.date && (entry.briefName == DaishinBriefName.buy || entry.briefName == DaishinBriefName.sell)
              ) {
                ZIO.succeed(acc, entry :: buffer)
              } else {
                for {
                  sb <- daishinSwapSellBeforeBuy(buffer)
                  next =
                    if (entry.briefName == DaishinBriefName.sell) (sb ++ acc, entry :: Nil)
                    else (entry :: sb ++ acc, Nil)
                } yield next
              }
            }
        }
        swapped <-
          if (buffer.isEmpty) ZIO.succeed(swappedRev.reverse)
          else
            for {
              sb <- daishinSwapSellBeforeBuy(buffer)
            } yield (sb ++ swappedRev).reverse

        (mergedRev, buffer) <- ZIO.foldLeft(swapped)((List[DaishinEntry](), List[DaishinEntry]())) {
          case ((acc, buffer), entry) =>
            if (
              (entry.briefName == DaishinBriefName.buy || entry.briefName == DaishinBriefName.sell) && entry.transactionAmount.isEmpty
            ) {
              ZIO.succeed(acc, entry :: buffer)
            } else {
              for {
                merged <- daishinMergePartialBuyOrSell(entry :: buffer).mapError(e =>
                  new Throwable("병합 중 실패", e)
                ) // 이렇게 안하면 ZIO#fold가 안됨
              } yield (merged :: acc, List[DaishinEntry]())
            }
        }
        merged <-
          if (buffer.isEmpty) ZIO.succeed(mergedRev.reverse)
          else
            for {
              m <- daishinMergePartialBuyOrSell(buffer)
            } yield (m :: mergedRev).reverse

        records <- ZIO.foreach(merged)(daishinParseEntryToRecord)
      } yield records
    ).mapError[ServiceError](e => ServiceError.InternalServerError("거래내역 파싱 실패", Some(e)))
  }

  def daishinParseStringToEntry(schema: Seq[String], body: Seq[String]): IO[ServiceError, DaishinEntry] = {
    val raw = schema.zip(body).toMap
    val formatter = DateTimeFormatter.ofPattern("yyyy.M.d")
    for {
      dateString <- ZIO.fromOption(raw.get("거래일")).orElseFail(ServiceError.InternalServerError("`거래일` 존재하지 않음"))
      date <- ZIO
        .effect(LocalDate.parse(dateString, formatter))
        .mapError(e => ServiceError.InternalServerError(s"거래일 파싱 실패: $dateString", Some(e)))

      transactionClass <- ZIO
        .fromOption(raw.get("거래구분"))
        .orElseFail(ServiceError.InternalServerError("`거래구분` 존재하지 않음"))

      currencyString <- ZIO.fromOption(raw.get("통화")).orElseFail(ServiceError.InternalServerError("`통화` 존재하지 않음"))
      currency <-
        if (currencyString === "") ZIO.none
        else
          ZIO
            .effect(Currency.withNameInsensitive(currencyString))
            .mapBoth(e => ServiceError.InternalServerError(s"통화 파싱 실패: $currencyString", Some(e)), Some(_))

      transactionAmountString <- ZIO
        .fromOption(raw.get("거래금액"))
        .orElseFail(ServiceError.InternalServerError("`거래금액` 존재하지 않음"))
      transactionAmount <-
        if (transactionAmountString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(transactionAmountString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"거래금액 파싱 실패: $transactionAmountString", Some(e)), Some(_))

      depositExRateString <- ZIO
        .fromOption(raw.get("입금환율"))
        .orElseFail(ServiceError.InternalServerError("`입금환율` 존재하지 않음"))
      depositExRate <-
        if (depositExRateString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(depositExRateString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"입금환율 파싱 실패: $depositExRateString", Some(e)), Some(_))

      tickerString <- ZIO
        .fromOption(raw.get("종목코드"))
        .orElseFail(ServiceError.InternalServerError("`종목코드` 존재하지 않음"))
      ticker = if (tickerString === "") None else Some(tickerString)

      quantityString <- ZIO.fromOption(raw.get("수량")).orElseFail(ServiceError.InternalServerError("`수량` 존재하지 않음"))
      quantity <-
        if (quantityString === "") ZIO.none
        else
          ZIO
            .effect(quantityString.toInt)
            .mapBoth(e => ServiceError.InternalServerError(s"수량 파싱 실패: $quantityString", Some(e)), Some(_))

      domesticTaxString <- ZIO
        .fromOption(raw.get("국내세"))
        .orElseFail(ServiceError.InternalServerError("`국내세` 존재하지 않음"))
      domesticTax <-
        if (domesticTaxString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(domesticTaxString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"국내세 파싱 실패: $domesticTaxString", Some(e)), Some(_))

      briefName <- ZIO.fromOption(raw.get("적요명")).orElseFail(ServiceError.InternalServerError("`적요명` 존재하지 않음"))

      fxCurrencyString <- ZIO.fromOption(raw.get("환전")).orElseFail(ServiceError.InternalServerError("`환전` 존재하지 않음"))
      fxCurrency <-
        if (fxCurrencyString === "") ZIO.none
        else
          ZIO
            .effect(Currency.withNameInsensitive(fxCurrencyString))
            .mapBoth(e => ServiceError.InternalServerError(s"환전 파싱 실패: $fxCurrencyString", Some(e)), Some(_))

      fxAmountString <- ZIO.fromOption(raw.get("환전금액")).orElseFail(ServiceError.InternalServerError("`환전금액` 존재하지 않음"))
      fxAmount <-
        if (fxAmountString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(fxAmountString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"환전금액 파싱 실패: $fxAmountString", Some(e)), Some(_))

      withdrawalExRateString <- ZIO
        .fromOption(raw.get("출금환율"))
        .orElseFail(ServiceError.InternalServerError("`출금환율` 존재하지 않음"))
      withdrawalExRate <-
        if (withdrawalExRateString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(withdrawalExRateString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"출금환율 파싱 실패: $withdrawalExRateString", Some(e)), Some(_))

      unitPriceString <- ZIO
        .fromOption(raw.get("단가"))
        .orElseFail(ServiceError.InternalServerError("`단가` 존재하지 않음"))
      unitPrice <-
        if (unitPriceString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(unitPriceString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"단가 파싱 실패: $unitPriceString", Some(e)), Some(_))

      feeString <- ZIO.fromOption(raw.get("수수료")).orElseFail(ServiceError.InternalServerError("`수수료` 존재하지 않음"))
      fee <-
        if (feeString === "") ZIO.none
        else
          ZIO
            .effect(BigDecimal(feeString.replace(",", "")))
            .mapBoth(e => ServiceError.InternalServerError(s"수수료 파싱 실패: $feeString", Some(e)), Some(_))

      localTaxString <- ZIO
        .fromOption(raw.get("현지세"))
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

  /** 모든 매도 주문을 매수 주문 앞쪽으로 넘긴다. 역순이므로 뒤가 아닌 앞으로 넘긴다.
    * @param reversedEntries 역순으로 되어있는 거래내역
    */
  def daishinSwapSellBeforeBuy(reversedEntries: List[DaishinEntry]): IO[ServiceError, List[DaishinEntry]] = {
    for {
      head <- ZIO.fromOption(reversedEntries.headOption).orElseFail(ServiceError.InternalServerError("주문 내역이 비어있음"))
      _ <- ZIO
        .fail(ServiceError.InternalServerError("스왑할 거래 내역의 티커가 일치하지 않음"))
        .when(reversedEntries.exists(_.ticker =!= head.ticker))
      (sell, buy) = reversedEntries.partition(_.briefName == DaishinBriefName.sell)
    } yield sell ++ buy
  }

  /** 거래금액이 비어있는 매수 또는 매도 주문을 합친다.
    */
  def daishinMergePartialBuyOrSell(entries: List[DaishinEntry]): IO[ServiceError, DaishinEntry] =
    for {
      head <- ZIO.fromOption(entries.headOption).orElseFail(ServiceError.InternalServerError("합칠 주문이 존재하지 않음"))
      briefName = head.briefName
      ticker = head.ticker
      merged <- ZIO.foldLeft(entries.drop(1))(head) { (acc, entry) =>
        if (entry.briefName === briefName && entry.ticker === ticker) {
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
          ZIO.fail(ServiceError.InternalServerError("합칠 거래 내역의 적요명이 일치하지 않음"))
      }
    } yield merged

  // TODO buy, sell의 경우 merge 과정에서 평단가의 값이 소수점 아래로 길어질 수 있으므로 `halfEven`을 실행한다. 추후에는 모든 Money에 대해 실행해야 할지도?
  def daishinParseEntryToRecord(entry: DaishinEntry): IO[ServiceError, TransactionRecord] =
    entry.briefName match {
      case DaishinBriefName.deposit | DaishinBriefName.depositInterest =>
        for {
          amount <- ZIO
            .fromOption(entry.transactionAmount)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
        } yield TransactionRecord.Deposit(entry.date, entry.transactionClass, Money.krw(amount), entry.briefName)
      case DaishinBriefName.fxBuy =>
        for {
          currency <- ZIO
            .fromOption(entry.currency)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          fxCurrency <- ZIO
            .fromOption(entry.fxCurrency)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 환전 존재하지 않음"))
          in <- ZIO
            .fromOption(entry.transactionAmount)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          out <- ZIO
            .fromOption(entry.fxAmount)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 환전금액 존재하지 않음"))
          exRate <- ZIO
            .fromOption(entry.depositExRate)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 입금환율 존재하지 않음"))
        } yield TransactionRecord.ForeignExchangeBuy(
          entry.date,
          entry.transactionClass,
          MoneyBag.fromMoneys(Money(currency, -in), Money(fxCurrency, out)),
          exRate,
          entry.briefName
        )
      case DaishinBriefName.buy =>
        for {
          currency <- ZIO
            .fromOption(entry.currency)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          totalPrice <- ZIO
            .fromOption(entry.transactionAmount)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          ticker <- ZIO
            .fromOption(entry.ticker)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 종목코드 존재하지 않음"))
          unitPrice <- ZIO
            .fromOption(entry.unitPrice)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 단가 존재하지 않음"))
          quantity <- ZIO
            .fromOption(entry.quantity)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수량 존재하지 않음"))
          fee <- ZIO
            .fromOption(entry.fee)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수수료 존재하지 않음"))
        } yield TransactionRecord.Buy(
          entry.date,
          entry.transactionClass,
          Money(currency, totalPrice),
          Holding(Stock(ticker, Nation.fromCurrency(currency)), Money(currency, unitPrice).halfEven, quantity),
          entry.briefName,
          Money(currency, fee)
        )
      case DaishinBriefName.sell =>
        for {
          currency <- ZIO
            .fromOption(entry.currency)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          totalPrice <- ZIO
            .fromOption(entry.transactionAmount)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          ticker <- ZIO
            .fromOption(entry.ticker)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 종목코드 존재하지 않음"))
          unitPrice <- ZIO
            .fromOption(entry.unitPrice)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 단가 존재하지 않음"))
          quantity <- ZIO
            .fromOption(entry.quantity)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수량 존재하지 않음"))
          fee <- ZIO
            .fromOption(entry.fee)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 수수료 존재하지 않음"))
          localTax <- ZIO
            .fromOption(entry.localTax)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 현지세 존재하지 않음"))
        } yield TransactionRecord.Sell(
          entry.date,
          entry.transactionClass,
          Money(currency, totalPrice),
          Holding(Stock(ticker, Nation.fromCurrency(currency)), Money.usd(unitPrice).halfEven, quantity),
          entry.briefName,
          Money(currency, fee),
          Money(currency, localTax)
        )
      case DaishinBriefName.dividend =>
        for {
          currency <- ZIO
            .fromOption(entry.currency)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 통화 존재하지 않음"))
          amount <- ZIO
            .fromOption(entry.transactionAmount)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 거래금액 존재하지 않음"))
          ticker <- ZIO
            .fromOption(entry.ticker)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 종목코드 존재하지 않음"))
          // TODO 해외주식거래내역밖에 없어서 현지세 데이터만 있어서 국내세가 있는 배당 처리는 보류
          localTax <- ZIO
            .fromOption(entry.localTax)
            .orElseFail(ServiceError.InternalServerError(s"${entry.briefName} 처리 중, 현지세 존재하지 않음"))
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
    date: LocalDate, // TODO Instant로 변경
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
