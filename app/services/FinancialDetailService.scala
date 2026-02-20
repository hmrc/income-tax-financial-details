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

import config.MicroserviceAppConfig
import connectors.hip.FinancialDetailsHipConnector
import connectors.httpParsers.ChargeHttpParser.{ChargeResponseError, UnexpectedChargeResponse}
import models.credits.CreditsModel
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FinancialDetailService @Inject()(
                                        val hipConnector: FinancialDetailsHipConnector,
                                        val viewAndChangeConnector: connectors.ViewAndChangeConnector,
                                        val appConfig: MicroserviceAppConfig
                                      )(implicit ec: ExecutionContext) extends Logging {

  type ChargeAsJsonResponse = Either[ChargeResponseError, JsValue]
  type PaymentsAsJsonResponse = Either[ChargeResponseError, JsValue]

  def getChargeDetails(nino: String, fromDate: String, toDate: String)
                      (implicit hc: HeaderCarrier): Future[ChargeAsJsonResponse] = {
    hipConnector.getChargeDetails(nino, fromDate, toDate)
      .flatMap {
        case Right(charges) =>
          Future.successful(Right(Json.toJson(charges)))
        case Left(err: UnexpectedChargeResponse) =>
          Future.successful(Left(err))
        case Left(err) =>
          logger.warn(s"HiP getChargeDetails failed, falling back to ViewAndChange. Error: $err")
          viewAndChangeConnector.getChargeDetails(nino, fromDate, toDate)

      }
  }

  def getPayments(nino: String, fromDate: String, toDate: String)
                 (implicit hc: HeaderCarrier): Future[PaymentsAsJsonResponse] = {
    logger.debug(s"Call::getPayments")
    hipConnector.getChargeDetails(nino, fromDate, toDate)
      .flatMap {
        case Right(charges) =>
          Future.successful(Right(Json.toJson(charges.payments)))
        case Left(err: UnexpectedChargeResponse) =>
          Future.successful(Left(err))
        case Left(err) =>
          logger.warn(s"HiP getPayments failed, falling back to ViewAndChange. Error: $err")
          viewAndChangeConnector.getPayments(nino, fromDate, toDate)
      }
  }

  def getPaymentAllocationDetails(nino: String, documentId: String)
                                 (implicit hc: HeaderCarrier): Future[ChargeAsJsonResponse] = {
    logger.info(s"Call::getPaymentAllocationDetails")
    hipConnector.getPaymentAllocationDetails(nino, documentId)
      .flatMap {
        case Right(charges) =>
          logger.info(s"Call::getPaymentAllocationDetails -> $charges")
          Future.successful(Right(Json.toJson(charges)))
        case Left(err: UnexpectedChargeResponse) =>
          Future.successful(Left(err))
        case Left(err) =>
          logger.warn(s"HiP getPaymentAllocationDetails failed, falling back to ViewAndChange. Error: $err")
          viewAndChangeConnector.getChargeDetailsByDocumentId(nino, documentId)
      }
  }

  def getOnlyOpenItems(nino: String)(implicit hc: HeaderCarrier): Future[ChargeAsJsonResponse] = {
    logger.info(s"Call::getOnlyOpenItems")

    hipConnector.getOnlyOpenItems(nino).flatMap {
      case Right(charges) =>
        Future.successful(Right(Json.toJson(charges)))
      case Left(err: UnexpectedChargeResponse) =>
        Future.successful(Left(err))
      case Left(err) =>
        logger.warn(s"HiP getOnlyOpenItems failed, falling back to ViewAndChange. Error: $err")
        viewAndChangeConnector.getOnlyOpenItems(nino)
    }
  }

  def getCredits(nino: String, fromDate: String, toDate: String)
                (implicit hc: HeaderCarrier): Future[ChargeAsJsonResponse] = {
    logger.info(s"Call::getCreditsModel")
    hipConnector
      .getChargeDetails(nino, fromDate, toDate)
      .flatMap {
        case Right(charges) =>
          val creditsModel: CreditsModel = CreditsModel.fromHipChargesResponse(charges)
          Future.successful(Right(Json.toJson(creditsModel)))
        case Left(err: UnexpectedChargeResponse) =>
          Future.successful(Left(err))
        case Left(err) =>
          logger.warn(s"HiP getCredits failed, falling back to ViewAndChange. Error: $err")
          viewAndChangeConnector.getCredits(nino, fromDate, toDate)
      }
  }
}