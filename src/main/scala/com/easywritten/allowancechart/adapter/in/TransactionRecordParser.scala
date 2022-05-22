package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.port.in.TransactionRecord
import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.{Money, MoneyBag, SecuritiesCompany}
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

        (
          for {
            head <- stream.take(1)
            body <- stream.drop(1)
            record <- ZStream.fromEffect(parseDaishin(head, body))
          } yield record
        ).runCollect.mapBoth[ServiceError, List[TransactionRecord]](_ => ServiceError.InternalServerError, _.toList)

      case SecuritiesCompany.Nonghyup => ???
    }

  // TODO 적요명을 따로 타입으로 만들까?
  object DaishinBriefName {
    val deposit = "개별상품대체입금"
    val fxBuy = "외화매수환전"
    val buy = "현금매수"
  }

  def parseDaishin(schema: Seq[String], data: Seq[String]): IO[ServiceError, TransactionRecord] = {
    val map = schema.zip(data).toMap
    val formatter = DateTimeFormatter.ofPattern("yyyy.M.d")
    // TODO 에러를 그냥 internal server error로 다 퉁쳤음
    ZIO
      .foreach(map.get("적요명")) {
        case DaishinBriefName.deposit =>
          for {
            dateString <- ZIO.succeed(map.get("거래일")).get.orElseFail(ServiceError.InternalServerError)
            date <- ZIO.effect(LocalDate.parse(dateString, formatter)).orElseFail(ServiceError.InternalServerError)

            transactionClass <- ZIO.succeed(map.get("거래구분")).get.orElseFail(ServiceError.InternalServerError)

            amountString <- ZIO.succeed(map.get("거래금액")).get.orElseFail(ServiceError.InternalServerError)
            amount <- ZIO.effect(BigDecimal(amountString.replace(",", ""))).orElseFail(ServiceError.InternalServerError)
          } yield TransactionRecord.Deposit(date, transactionClass, Money.krw(amount), DaishinBriefName.deposit)
        case DaishinBriefName.fxBuy =>
          for {
            dateString <- ZIO.succeed(map.get("거래일")).get.orElseFail(ServiceError.InternalServerError)
            date <- ZIO.effect(LocalDate.parse(dateString, formatter)).orElseFail(ServiceError.InternalServerError)

            transactionClass <- ZIO.succeed(map.get("거래구분")).get.orElseFail(ServiceError.InternalServerError)

            krwString <- ZIO.succeed(map.get("거래금액")).get.orElseFail(ServiceError.InternalServerError)
            krw <- ZIO.effect(BigDecimal(krwString.replace(",", ""))).orElseFail(ServiceError.InternalServerError)

            usdString <- ZIO.succeed(map.get("환전금액")).get.orElseFail(ServiceError.InternalServerError)
            usd <- ZIO.effect(BigDecimal(usdString.replace(",", ""))).orElseFail(ServiceError.InternalServerError)

            rateString <- ZIO.succeed(map.get("입금환율")).get.orElseFail(ServiceError.InternalServerError)
            exRate <- ZIO.effect(BigDecimal(rateString.replace(",", ""))).orElseFail(ServiceError.InternalServerError)
          } yield TransactionRecord.ForeignExchangeBuy(
            date,
            transactionClass,
            MoneyBag.fromMoneys(Money.krw(-krw), Money.usd(usd)),
            exRate,
            DaishinBriefName.fxBuy
          )
      }
      .flatMap(ZIO.fromOption(_).orElseFail(ServiceError.InternalServerError))
  }
}
