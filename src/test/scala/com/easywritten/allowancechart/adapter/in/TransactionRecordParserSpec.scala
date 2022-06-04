package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.port.in.TransactionRecord
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
        testM("preparse Daishin") {
          ZIO.foldLeft(DaishinParserFixture.stringToEntry)(assertCompletes) { case (acc, (data, expected)) =>
            val entry = daishinPreParsing(DaishinParserFixture.schema.zip(data).toMap)
            assertM(entry)(equalTo(expected)).map(_ && acc)
          }
        },
        testM("Daishin") {
          ZIO.foldLeft(DaishinParserFixture.stringToRecord)(assertCompletes) { case (acc, (data, expected)) =>
            val record = parseDaishin(DaishinParserFixture.schema, data)
            assertM(record)(equalTo(expected)).map(_ && acc)
          }
        }
      ),
      suite("parse transaction record file")(
        testM("Daishin") {
          for {
            file <- ZIO.effect(Paths.get(getClass.getResource("/transaction-files/creon.csv").toURI).toFile)
            transactionRecords <- fromFile(file, SecuritiesCompany.Daishin)
          } yield assert(transactionRecords)(equalTo(DaishinParserFixture.stringToRecord.values.toList))
        }
      ), // @@ TestAspect.ignore // TODO
      suite("merge TransactionRecord")(
        testM("merge buy record") {
          val records = List(
            TransactionRecord.Buy(
              LocalDate.of(2021, 1, 7),
              "해외증권장내매매",
              Money.usd(0),
              Holding(Stock("DKNG", Nation.USA), Money.usd(46), 2),
              "현금매수",
              Money.usd(0)
            ),
            TransactionRecord.Buy(
              LocalDate.of(2021, 1, 7),
              "해외증권장내매매",
              Money.usd(184.62),
              Holding(Stock("DKNG", Nation.USA), Money.usd(46.31), 2),
              "현금매수",
              Money.usd(0.15)
            )
          )
          assertCompletesM
        }
      )
    )
  }

}
