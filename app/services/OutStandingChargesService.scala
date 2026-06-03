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

import connectors.httpParsers.OutStandingChargesHttpParser.{OutStandingChargeResponse, UnexpectedOutStandingChargeResponse}
import connectors.{OutStandingChargesConnector, ViewAndChangeConnector}
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OutStandingChargesService @Inject()(outstandingChargesConnector: OutStandingChargesConnector,
                                          viewAndChangeConnector: ViewAndChangeConnector){

  def listOutStandingCharges(idType: String, idNumber: String, taxYearEndDate: String)
                            (implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[OutStandingChargeResponse] = {
    outstandingChargesConnector.listOutStandingCharges(idType, idNumber, taxYearEndDate).flatMap{
      case Right(success) =>
        Future.successful(Right(success))
      case Left(error: UnexpectedOutStandingChargeResponse) if error.code == NOT_FOUND =>
        Future.successful(Left(error))
      case _ =>
        viewAndChangeConnector.listOutStandingCharges(idType, idNumber, taxYearEndDate)
    }
  }

}
