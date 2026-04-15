/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import connectors.{RepaymentHistoryDetailsConnector, ViewAndChangeConnector}
import connectors.hip.HipRepaymentHistoryDetailsConnector
import connectors.hip.httpParsers.ChargeHipHttpParser.HttpGetResult
import connectors.httpParsers.RepaymentHistoryHttpParser.RepaymentHistoryResponse
import models.hip.repayments.SuccessfulRepaymentResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepaymentHistoryDetailsService @Inject()(hipRepaymentHistoryDetailsConnector: HipRepaymentHistoryDetailsConnector,
                                               repaymentHistoryDetailsConnector: RepaymentHistoryDetailsConnector,
                                               viewAndChangeConnector: ViewAndChangeConnector) {

  def getRepaymentHistoryDetailsList(idValue: String)
                                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SuccessfulRepaymentResponse]] = {
    hipRepaymentHistoryDetailsConnector.getRepaymentHistoryDetailsList(idValue).flatMap {
      case right@Right(_) =>
        Future.successful(right)
      case Left(_) =>
        viewAndChangeConnector.getRepaymentHistoryDetailsList(idValue)
    }
  }

  //ToDo remove when migration to HIP is completed
  def getIFRepaymentHistoryDetailsList(idValue: String)
                                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RepaymentHistoryResponse] = {
    repaymentHistoryDetailsConnector.getAllRepaymentHistoryDetails(idValue).flatMap {
      case right@Right(_) =>
        Future.successful(right)
      case Left(_) =>
        viewAndChangeConnector.getIFRepaymentHistoryDetailsList(idValue)
    }
  }

  def getRepaymentHistoryDetails(idValue: String, repaymentRequestNumber: String)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpGetResult[SuccessfulRepaymentResponse]] = {
    hipRepaymentHistoryDetailsConnector.getRepaymentHistoryDetails(idValue, repaymentRequestNumber).flatMap {
      case right@Right(_) =>
        Future.successful(right)
      case Left(_) =>
        viewAndChangeConnector.getRepaymentHistoryDetails(idValue, repaymentRequestNumber)
    }
  }

  //ToDo remove when migration to HIP is completed
  def getIFRepaymentHistoryDetails(idValue: String, repaymentRequestNumber: String)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RepaymentHistoryResponse] = {
    repaymentHistoryDetailsConnector.getRepaymentHistoryDetailsById(idValue, repaymentRequestNumber).flatMap {
      case right@Right(_) =>
        Future.successful(right)
      case Left(_) =>
        viewAndChangeConnector.getIFRepaymentHistoryDetails(idValue, repaymentRequestNumber)
    }
  }
}
