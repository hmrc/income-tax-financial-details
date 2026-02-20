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

import connectors.{PaymentAllocationsConnector, ViewAndChangeConnector}
import connectors.httpParsers.PaymentAllocationsHttpParser.PaymentAllocationsResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class PaymentAllocationsService @Inject()(paymentAllocationsConnector: PaymentAllocationsConnector,
                                               viewAndChangeConnector: ViewAndChangeConnector) {

  def getPaymentAllocations(nino: String, paymentLot: String, paymentLotItem: String)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[PaymentAllocationsResponse] = {

    paymentAllocationsConnector.getPaymentAllocations(nino, paymentLot, paymentLotItem).flatMap {
      case response @ Right(_) =>
        Future.successful(response)
      case Left(_) =>
        viewAndChangeConnector.getPaymentAllocations(nino, paymentLot, paymentLotItem)
    }
  }
}