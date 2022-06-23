package com.easywritten.allowancechart.domain

import com.easywritten.allowancechart.application.service.ServiceError
import com.easywritten.allowancechart.domain.account.{Account, AccountError, AccountEvent, AccountName, AccountState}
import zio._
import zio.entity.core.Entity
import zio.logging._

import java.time.Instant

// TODO 도메인 계층에서 이벤트 소싱 구현에 너무 강하게 의존하고 있지 않나? => 근데 이벤트 소싱에서 바꿀 생각이 없는 걸 수도 있잖아...
// TODO Asset의 accounts를 숨겨야 한다
// TODO ZIO Module Pattern 2.0 구현처럼 보이지만 틀렸음. 다른 컴포넌트들이 구현 클래스에 직접적으로 의존하고 있음. Service trait 분리하기
// TODO Service trait 분리하면서 TestAsset도 다시 만들어보기

// TODO Account의 메소드가 늘어날 때마다 Asset도 똑같이 추가해줘야 한다... 이게 맞음?
// TODO Asset의 존재 의의는 여러 계좌에 대한 작업 자산 수익률 등을 구하기 위함인데 그 외에 계좌 하나에 대한 일들은 생각해보면 그냥 account를 직접 쓰면 되는 거 아닐까? 굳이 account를 Asset이 둘러싸게 할 필요가 있을까?
object Asset {
  // 원래 Asset trait, object, AssetLive case class, object 4가지를 분리하여 한 파일에 구현했는데 번거로워서 Asset object 하나 안에 Service, Live로 구성함
  trait Service {
    def initialize(name: AccountName, company: SecuritiesCompany): IO[ServiceError, Unit]

    def balance(name: AccountName): IO[ServiceError, MoneyBag]

    def holdings(name: AccountName): IO[ServiceError, Set[Holding]]

    def netValue(name: AccountName): IO[ServiceError, MoneyBag]

    def deposit(name: AccountName, money: Money, at: Instant): IO[ServiceError, Unit]

    def withdraw(name: AccountName, money: Money, at: Instant): IO[ServiceError, Unit]

    def buy(
        name: AccountName,
        stock: Stock,
        unitPrice: Money,
        quantity: Int,
        at: Instant
    ): IO[ServiceError, Unit]

    def sell(
        name: AccountName,
        stock: Stock,
        contractPrice: Money,
        quantity: Int,
        at: Instant
    ): IO[ServiceError, Unit]

    def dividendPaid(
        name: AccountName,
        stock: Stock,
        amount: Money,
        tax: Money,
        at: Instant
    ): IO[ServiceError, Unit]

    def foreignExchangeBuy(
        name: AccountName,
        exchange: MoneyBag,
        exchangeRate: BigDecimal,
        at: Instant
    ): IO[ServiceError, Unit]
  }

  val accountNameLogAnnotation: LogAnnotation[Option[AccountName]] = LogAnnotation.optional("account-name", _.name)

  final case class Live(
      accounts: Entity[AccountName, Account, AccountState, AccountEvent, AccountError],
      logger: Logger[String]
  ) extends Service {
    override def initialize(name: AccountName, company: SecuritiesCompany): IO[ServiceError, Unit] =
      accounts(name)
        .initialize(company)
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def balance(name: AccountName): IO[ServiceError, MoneyBag] =
      accounts(name).balance
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def holdings(name: AccountName): IO[ServiceError, Set[Holding]] =
      accounts(name).holdings
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def netValue(name: AccountName): IO[ServiceError, MoneyBag] =
      accounts(name).netValue
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def deposit(name: AccountName, money: Money, at: Instant): IO[ServiceError, Unit] =
      accounts(name)
        .deposit(money, at)
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def withdraw(name: AccountName, money: Money, at: Instant): IO[ServiceError, Unit] =
      accounts(name)
        .withdraw(money, at)
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def buy(
        name: AccountName,
        stock: Stock,
        unitPrice: Money,
        quantity: Int,
        at: Instant
    ): IO[ServiceError, Unit] =
      accounts(name)
        .buy(stock, unitPrice, quantity, at)
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def sell(
        name: AccountName,
        stock: Stock,
        contractPrice: Money,
        quantity: Int,
        at: Instant
    ): IO[ServiceError, Unit] =
      accounts(name)
        .sell(stock, contractPrice, quantity, at)
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def dividendPaid(
        name: AccountName,
        stock: Stock,
        amount: Money,
        tax: Money,
        at: Instant
    ): IO[ServiceError, Unit] =
      accounts(name)
        .dividendPaid(stock, amount, tax, at)
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))

    override def foreignExchangeBuy(
        name: AccountName,
        exchange: MoneyBag,
        exchangeRate: BigDecimal,
        at: Instant
    ): IO[ServiceError, Unit] =
      accounts(name)
        .foreignExchangeBuy(exchange, exchangeRate, at)
        .mapError(e => ServiceError.InternalServerError(e.getMessage, Some(e)))
        .tapError(e => logger.locally(accountNameLogAnnotation(Some(name)))(logger.error(e.message)))
  }

  val layer: URLayer[Has[Entity[AccountName, Account, AccountState, AccountEvent, AccountError]] with Logging, Has[
    Asset.Service
  ]] = (Live(_, _)).toLayer

  def initialize(name: AccountName, company: SecuritiesCompany): ZIO[Has[Asset.Service], ServiceError, Unit] =
    ZIO.serviceWith[Asset.Service](_.initialize(name, company))

  def balance(name: AccountName): ZIO[Has[Asset.Service], ServiceError, MoneyBag] =
    ZIO.serviceWith[Asset.Service](_.balance(name))

  def holdings(name: AccountName): ZIO[Has[Asset.Service], ServiceError, Set[Holding]] =
    ZIO.serviceWith[Asset.Service](_.holdings(name))

  def netValue(name: AccountName): ZIO[Has[Asset.Service], ServiceError, MoneyBag] =
    ZIO.serviceWith[Asset.Service](_.netValue(name))

  def deposit(name: AccountName, money: Money, at: Instant): ZIO[Has[Asset.Service], ServiceError, Unit] =
    ZIO.serviceWith[Asset.Service](_.deposit(name, money, at))

  def withdraw(name: AccountName, money: Money, at: Instant): ZIO[Has[Asset.Service], ServiceError, Unit] =
    ZIO.serviceWith[Asset.Service](_.withdraw(name, money, at))

  def buy(
      name: AccountName,
      stock: Stock,
      unitPrice: Money,
      quantity: Int,
      at: Instant
  ): ZIO[Has[Asset.Service], ServiceError, Unit] =
    ZIO.serviceWith[Asset.Service](_.buy(name, stock, unitPrice, quantity, at))

  def sell(
      name: AccountName,
      stock: Stock,
      contractPrice: Money,
      quantity: Int,
      at: Instant
  ): ZIO[Has[Asset.Service], ServiceError, Unit] =
    ZIO.serviceWith[Asset.Service](_.sell(name, stock, contractPrice, quantity, at))

  def dividendPaid(
      name: AccountName,
      stock: Stock,
      amount: Money,
      tax: Money,
      at: Instant
  ): ZIO[Has[Asset.Service], ServiceError, Unit] =
    ZIO.serviceWith[Asset.Service](_.dividendPaid(name, stock, amount, tax, at))

  def foreignExchangeBuy(
      name: AccountName,
      exchange: MoneyBag,
      exchangeRate: BigDecimal,
      at: Instant
  ): ZIO[Has[Asset.Service], ServiceError, Unit] =
    ZIO.serviceWith[Asset.Service](_.foreignExchangeBuy(name, exchange, exchangeRate, at))
}
