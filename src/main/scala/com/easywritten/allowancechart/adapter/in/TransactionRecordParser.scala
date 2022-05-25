package com.easywritten.allowancechart.adapter.in

import cats.implicits._
import com.easywritten.allowancechart.application.port.in.TransactionRecord
import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.{Currency, Holding, Money, MoneyBag, Nation, SecuritiesCompany, Stock}
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
    val dividend = "배당금"
  }

  def parseDaishin(schema: Seq[String], data: Seq[String]): IO[ServiceError, TransactionRecord] = {
    val map = schema.zip(data).toMap
    val formatter = DateTimeFormatter.ofPattern("yyyy.M.d")
    // TODO 에러를 그냥 internal server error로 다 퉁쳤음
    // TODO 적요명에 따라 겹치는 파싱이 많음 거래일, 거래구분, 거래금액 등...
    ZIO
      .foreach(map.get("적요명")) {
        case DaishinBriefName.deposit =>
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
          } yield TransactionRecord.Deposit(date, transactionClass, Money.krw(amount), DaishinBriefName.deposit)
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
            Stock(ticker, if (currency == Currency.KRW) Nation.KOR else Nation.USA),
            DaishinBriefName.dividend,
            tax
          )
      }
      .flatMap(ZIO.fromOption(_).orElseFail(ServiceError.InternalServerError("지원하지 않는 적요명")))
  }
}
