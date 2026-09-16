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

import connectors.PaymentAllocationsConnector
import connectors.hip.HipPaymentAllocationsConnector
import connectors.httpParsers.PaymentAllocationsHttpParser.PaymentAllocationsResponse
import models.hip.paymentAllocations.HipPaymentAllocationsResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class PaymentAllocationsService @Inject()(paymentAllocationsConnector: PaymentAllocationsConnector,
                                               hipPaymentAllocationsConnector: HipPaymentAllocationsConnector) {

  def getPaymentAllocations(nino: String, paymentLot: String, paymentLotItem: String)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PaymentAllocationsResponse] = {
    paymentAllocationsConnector.getPaymentAllocations(nino, paymentLot, paymentLotItem)
  }
  
  def getPaymentAllocationsHip(nino: String, paymentLot: String, paymentLotItem: String)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HipPaymentAllocationsResponse] = {
    hipPaymentAllocationsConnector.getPaymentAllocations(nino, paymentLot, paymentLotItem)
  }
}
