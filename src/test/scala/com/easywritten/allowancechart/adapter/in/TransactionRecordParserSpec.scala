package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.domain.SecuritiesCompany
import zio._
import zio.stream.ZStream
import zio.test._
import zio.test.Assertion._

import java.nio.file.Paths

object TransactionRecordParserSpec extends DefaultRunnableSpec {
  import TransactionRecordParser._
  override def spec: ZSpec[Environment, Failure] = {
    suite("TransactionRecordParserSpec")(
      suite("parse Daishin")(
        testM("phase 1. String to DaishinEntry") {
          ZIO.foldLeft(DaishinParserFixture.stringToEntry)(assertCompletes) { case (acc, (raw, expected)) =>
            val entry = daishinParseStringToEntry(DaishinParserFixture.schema, raw)
            assertM(entry)(equalTo(expected)).map(_ && acc)
          }
        },
        testM("phase 2. merge partial buy or sell entry") {
          // TODO 실패하는 테스트 케이스도 추가하면 좋긴 할 듯
          import DaishinParserFixture.mergeEntryTest._
          for {
            mergedBuy <- daishinMergePartialBuyOrSell(dividedBuy)
            mergedSell <- daishinMergePartialBuyOrSell(dividedSell)
          } yield assert(mergedBuy)(equalTo(expectedBuy)) && assert(mergedSell)(equalTo(expectedSell))
        },
        testM("phase 3. DaishinEntry to TransactionRecord") {
          ZIO.foldLeft(DaishinParserFixture.entryToRecord)(assertCompletes) { case (acc, (entry, expected)) =>
            val record = daishinParseEntryToRecord(entry)
            assertM(record)(equalTo(expected)).map(_ && acc)
          }
        },
        testM("whole process") {
          import DaishinParserFixture.wholeProcessTest._
          assertM(parseDaishin(ZStream.fromIterable(DaishinParserFixture.schema :: rawString)))(equalTo(records))
        },
      ),
      suite("parse transaction record file")(
        testM("Daishin") {
          for {
            file <- ZIO.effect(Paths.get(getClass.getResource("/transaction-files/creon.csv").toURI).toFile)
            transactionRecords <- fromFile(file, SecuritiesCompany.Daishin)
          } yield assert(transactionRecords)(equalTo(List()))
        }
      ) @@ TestAspect.ignore // TODO 너무 결과가 길어서 그냥 로컬에서 에러 발생하지 않고 실행되는지만 확인하고 있음
    )
  }

}
