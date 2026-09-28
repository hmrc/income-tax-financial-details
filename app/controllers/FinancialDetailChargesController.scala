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

package controllers

import connectors.httpParsers.ChargeHttpParser.UnexpectedChargeResponse
import controllers.predicates.AuthenticationPredicate
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.*
import services.FinancialDetailService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FinancialDetailChargesController @Inject()(authentication: AuthenticationPredicate,
                                                 cc: ControllerComponents,
                                                 financialDetailChargesService : FinancialDetailService)
                                                (implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  private def isDownstreamTimeout(status: Int): Boolean =
    status == 499 || status == 502 || status == 503

  def getChargeDetails(nino: String, fromDate: String, toDate: String): Action[AnyContent] = {
    authentication.async { implicit request =>
      financialDetailChargesService.getChargeDetails(
        nino,
        fromDate,
        toDate
      ) map {
        case Right(chargeDetailsAsJson) =>
          logger.info("[FinancialDetailChargesController][getChargeDetails] Successful Response: " + chargeDetailsAsJson)
          Ok(chargeDetailsAsJson)
        case Left(error: UnexpectedChargeResponse) if error.code == NOT_FOUND =>
          logger.info("[FinancialDetailChargesController][getChargeDetails] 404: " + error)
          Status(error.code)(error.response)
        case Left(error: UnexpectedChargeResponse) if isDownstreamTimeout(error.code) =>
          logger.warn("[FinancialDetailChargesController][getChargeDetails] Downstream Timeout Error Response: " + error)
          Status(error.code)(error.response)
        case Left(error: UnexpectedChargeResponse) if error.code >= BAD_REQUEST && error.code < INTERNAL_SERVER_ERROR =>
          logger.error("[FinancialDetailChargesController][getChargeDetails] error: " + error)
          Status(error.code)(error.response)
        case Left(otherError) =>
          logger.error("[FinancialDetailChargesController][getChargeDetails] other error: " + otherError)
          InternalServerError("Failed to retrieve charge details")
      }
    }
  }

  def getPaymentAllocationDetails(nino: String, documentId: String): Action[AnyContent] = {
    authentication.async { implicit request =>
      financialDetailChargesService.getPaymentAllocationDetails(
        nino = nino,
        documentId = documentId
      ) map {
        case Right(chargeDetails) =>
          logger.debug("[FinancialDetailChargesController][getChargeDetails] Successful Response: " + chargeDetails)
          Ok(Json.toJson(chargeDetails))
        case Left(error: UnexpectedChargeResponse) if isDownstreamTimeout(error.code) =>
          logger.warn("[FinancialDetailChargesController][getChargeDetails] Downstream Timeout Error Response: " + error)
          Status(error.code)(error.response)
        case Left(error: UnexpectedChargeResponse) if error.code >= 400 && error.code < 500 =>
          logger.error("[FinancialDetailChargesController][getChargeDetails] error: " + error)
          Status(error.code)(error.response)
        case Left(otherError) =>
          logger.error("[FinancialDetailChargesController][getChargeDetails] other error: " + otherError)
          InternalServerError("Failed to retrieve payment allocation details")
      }
    }
  }
}
