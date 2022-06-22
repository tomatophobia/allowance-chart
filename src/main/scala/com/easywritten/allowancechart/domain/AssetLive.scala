package com.easywritten.allowancechart.domain

import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.account.{Account, AccountError, AccountEvent, AccountName, AccountState}
import zio._
import zio.entity.core.Entity
import zio.logging._

// TODO 도메인 계층에서 이벤트 소싱 구현에 너무 강하게 의존하고 있지 않나? => 근데 이벤트 소싱에서 바꿀 생각이 없는 걸 수도 있잖아...
// TODO Asset의 accounts를 숨겨야 한다
// TODO ZIO Module Pattern 2.0 구현처럼 보이지만 틀렸음. 다른 컴포넌트들이 구현 클래스에 직접적으로 의존하고 있음. Service trait 분리하기
// TODO Service trait 분리하면서 TestAsset도 다시 만들어보기
trait Asset {
  def initialize(name: AccountName, company: SecuritiesCompany): IO[ServiceError, Unit]
}

object Asset {
  def initialize(name: AccountName, company: SecuritiesCompany): ZIO[Has[Asset], ServiceError, Unit] =
    ZIO.serviceWith[Asset](_.initialize(name, company))
}

final case class AssetLive(
    accounts: Entity[AccountName, Account, AccountState, AccountEvent, AccountError],
    logger: Logger[String]
) extends Asset {

  override def initialize(name: AccountName, company: SecuritiesCompany): IO[ServiceError, Unit] =
    accounts(name)
      .initialize(company)
      .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
      .tapError(e => logger.error(e.message))
}

object AssetLive {
  val layer: URLayer[Has[Entity[AccountName, Account, AccountState, AccountEvent, AccountError]] with Logging, Has[
    Asset
  ]] = (AssetLive(_, _)).toLayer
}
