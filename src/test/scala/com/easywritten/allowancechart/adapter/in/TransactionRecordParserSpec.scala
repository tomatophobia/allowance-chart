package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.domain.SecuritiesCompany
import zio._
import zio.test._
import zio.test.Assertion._

import java.io.File

object TransactionRecordParserSpec extends DefaultRunnableSpec {
  import TransactionRecordParser._

  override def spec: ZSpec[Environment, Failure] =
    suite("parse transaction record file")(
    testM("Nonghyup")(
      for {
        file <- ZIO.effect(new File("resources/namuh.csv"))
        transactionRecords = fromFile(file, SecuritiesCompany.Nonghyup)
      } yield assertCompletes
    ),
    testM("Daishin")(
      for {
        file <- ZIO.effect(new File("resources/creon.csv"))
        transactionRecords = fromFile(file, SecuritiesCompany.Daishin)
      } yield assertCompletes
    )
  )
}
