package com.easywritten.allowancechart.adapter.in

import com.easywritten.allowancechart.application.port.in.TransactionRecord
import com.easywritten.allowancechart.domain.SecuritiesCompany

object TransactionRecordParser {
  /** 증권사별 거래내역이 들어있는 csv 파일을 읽어서 TransactionRecord로 변환
   *  csv 관련 라이브러리 (kantan, scala-csv) 같은 것을 쓸까 했는데 NH같은 경우 csv가 너무 불규칙적이어서 일단 좀 수동 느낌으로 가기로 함.
   *  파일은 UTF-8 형식으로 저장된 것만 사용
   */
  def fromFile(file: java.io.File, company: SecuritiesCompany): List[TransactionRecord] = ???
}
