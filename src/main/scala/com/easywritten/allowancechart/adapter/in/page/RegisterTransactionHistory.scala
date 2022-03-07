package com.easywritten.allowancechart.adapter.in.page

import scalatags.Text._
import scalatags.Text.all._

object RegisterTransactionHistory extends Base {
  def html: String = frag.toString

  val frag: TypedTag[String] = layout("거래내역 등록", Menu.ManageTransactionHistory)(
    div(
      form(action := "/transaction-history", method := "POST", enctype := "multipart/form-data")(
        div(cls := "mb-3")(
          label(`for` := "account-name", cls := "form-label")("계좌 이름"),
          input(
            tpe := "text",
            cls := "form-control",
            id := "account-name",
            aria.describedby := "account-name-help",
            name := "name"
          ),
          div(id := "account-name-help", cls := "form-text")("중복된 계좌 이름은 사용할 수 없습니다.")
        ),
        div(cls := "mb-3")(
          // TODO 파일 확장자가 csv인지 확인
          label(`for` := "transaction-history-file", cls := "form-label")("거래 내역 파일을 등록해주세요. (csv 확장자만 지원)"),
          input(tpe := "file", cls := "form-control", id := "transaction-history-file", name := "transactionHistory"),
          div(id := "transaction-history-file-help", cls := "form-text")("현재 지원하는 증권사: NH투자증권, 대신증권")
        ),
        button(tpe := "submit", cls := "btn btn-primary")("Submit")
      )
    )
  )
}
