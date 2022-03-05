package com.easywritten.allowancechart.adapter.in.page

import scalatags.Text._
import scalatags.Text.all._

object RegisterTransactionHistory extends Base {
  def html: String = frag.toString

  val frag: TypedTag[String] = layout("거래내역 등록", Menu.ManageTransactionHistory)(
    div(
      form(
        div(cls := "mb-3")(
          label(`for` := "account-name", cls := "form-label")("계좌 이름"),
          input(tpe := "text", cls := "form-control", id := "account-name", aria.describedby := "account-name-help"),
          div(id := "account-name-help", cls := "form-text")("중복된 계좌 이름은 사용할 수 없습니다.")
        ),
        div(cls := "mb-3")(
          label(`for` := "transaction-history-file", cls := "form-label")("거래 내역 파일을 등록해주세요. (여러 개 등록 가능)"),
          input(tpe := "file", cls := "form-control", id := "transaction-history-file", multiple),
          div(id := "transaction-history-file-help", cls := "form-text")("현재 지원하는 증권사: NH투자증권, 대신증권")
        ),
        button(tpe := "submit", cls := "btn btn-primary")("Submit")
      )
    )
  )
}
