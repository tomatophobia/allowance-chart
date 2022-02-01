package com.easywritten.allowancechart.domain

final case class Account() {

  // TODO 여러 계좌들이 하나의 윈도우를 공유한다면 Equity로 올라가야 하나?
  // TODO var로 바꾸지 않고 이벤트를 추가하는 방법 없을까? => aecor의 Folded..? cats의 chain?, 아니면 State monad 이용?
  // TODO 어쩌면 이 고민도 ES 라이브러리 도입하면 해결 될 수도 그 라이브러리에서는 어떻게 했는지 확인
  private var activityWindow: List[Activity] = List()

  // TODO 여기서도 결국 이벤트 소싱으로 가게 된다면 Account에서 잔고를 구하는게 아니고 AccountState가 따로 생겨야 할 것 같음
  // TODO diamond에서는 read를 통해 저널을 읽고 AccountState 생성한 뒤 그걸 사용
  def balance: MoneyBag = activityWindow.foldRight(MoneyBag.empty) {
    (act, remainder) =>
      act match {
        case Deposit(money) => remainder + money
      }
  }

  // 여기서 이벤트 추가한 것이 어떻게 DB에 저장되지..?
  // 도메인에서는 DB에 대해 몰라야 한다면... Service에서 변경된 도메인을 저장하나?
  // 맞는 듯 Service에서 도메인을 Repository에 넣으면 거기서 알아서 저장함
  // 일단 바깥쪽은 신경쓰지 말고 도메인 로직을 짜보자!
  def deposit(money: Money): Unit = {
    activityWindow = Deposit(money) :: activityWindow
  }

}
