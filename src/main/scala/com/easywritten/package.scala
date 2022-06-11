package com

import com.easywritten.allowancechart.adapter.in.TransactionRecordEndpoints
import com.easywritten.allowancechart.application.port.in.RegisterTransactionRecordPort
import com.easywritten.allowancechart.application.service.RegisterTransactionRecordService
import com.easywritten.allowancechart.domain.Asset
import com.easywritten.allowancechart.domain.account.EventSourcedAccount
import zio._
import zio.clock.Clock

package object easywritten {
  // TODO EndpointEnv가 적절한 이름이 아닌 것 같기도
  type EndpointEnv = TransactionRecordEndpoints.Env
  type AppEnv = Clock with EndpointEnv

  // TODO 컴포넌트 많아지면 type alias들 추가하기
  val domainLayers: RLayer[ZEnv, Has[Asset]] = (EventSourcedAccount.accounts to Asset.layer)

  val applicationServiceLayers: URLayer[Has[Asset], Has[RegisterTransactionRecordPort]] = RegisterTransactionRecordService.layer

  val appLayers: RLayer[ZEnv, Has[RegisterTransactionRecordPort]] = domainLayers to applicationServiceLayers

}
