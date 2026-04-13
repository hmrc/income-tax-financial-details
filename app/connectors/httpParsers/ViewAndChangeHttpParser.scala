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

package connectors.httpParsers

import connectors.hip.httpParsers.errorResponses.ErrorResponseHttpParsers
import connectors.httpParsers.ChargeHttpParser.{ChargeResponseError, UnexpectedChargeErrorResponse, UnexpectedChargeResponse}
import models.hip.ErrorResponse.GenericError
import models.hip.repayments.SuccessfulRepaymentResponse
import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.util.Try

object ViewAndChangeHttpParser extends ErrorResponseHttpParsers with Logging {

  type ViewAndChangeJsonResponse = Either[ChargeResponseError, JsValue]

  given RepaymentsHistoryDetailsReads: HttpReads[HttpGetResult[SuccessfulRepaymentResponse]] with {

    override def read(method: String, url: String, response: HttpResponse): HttpGetResult[SuccessfulRepaymentResponse] =
      response.status match {
        case OK =>
          logger.info("successfully parsed response to List[RepaymentHistory]")
          Right(response.json.as[SuccessfulRepaymentResponse])
        case status =>
          logger.error(s"Call to RepaymentsHistory failed with status: $status and response body: ${response.body}")
          val jsonBody = Try(response.json).getOrElse(Json.obj("error" -> response.body))
          Left(GenericError(status, jsonBody))
      }
  }
  
  implicit object ViewAndChangeReads extends HttpReads[ViewAndChangeJsonResponse] {
    override def read(method: String, url: String, response: HttpResponse): ViewAndChangeJsonResponse = {
      response.status match {
        case OK =>
          response.json.validate[JsValue] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors) =>
              logger.error(s"ViewAndChange returned invalid JSON: $errors")
              Left(UnexpectedChargeErrorResponse)
          }

        case status if status >= BAD_REQUEST && status < INTERNAL_SERVER_ERROR =>
          logger.error(s"ViewAndChange returned $status with body: ${response.body}")
          Left(UnexpectedChargeResponse(status, response.body))

        case status =>
          logger.error(s"Unexpected response from ViewAndChange with status: $status and body: ${response.body}")
          Left(UnexpectedChargeErrorResponse)
      }
    }
  }
}
