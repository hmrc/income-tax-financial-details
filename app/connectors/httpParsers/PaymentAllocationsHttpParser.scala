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

import models.paymentAllocations.{PaymentAllocations, PaymentDetails}
import play.api.Logging
import play.api.http.Status.{BAD_GATEWAY, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object PaymentAllocationsHttpParser extends Logging {

  val CLIENT_CLOSED_REQUEST = 499

  sealed trait PaymentAllocationsError

  case object UnexpectedResponse extends PaymentAllocationsError

  case object NotFoundResponse extends PaymentAllocationsError

  type PaymentAllocationsResponse = Either[PaymentAllocationsError, PaymentAllocations]

  implicit object PaymentAllocationsReads extends HttpReads[PaymentAllocationsResponse] {
    override def read(method: String, url: String, response: HttpResponse): PaymentAllocationsResponse = {
      response.status match {
        case OK =>
          logger.debug("[PaymentAllocationsHttpParser][read] got OK PaymentAllocations response") // TODO - MIPR-2637: Inform V&C team about no longer logging the response body
          response.json.validate[PaymentDetails] match {
            case JsSuccess(result, _) => result.paymentDetails.headOption match {
              case Some(paymentAllocations) =>
                logger.info("[PaymentAllocationsHttpParser][read] successfully parsed response to PaymentAllocations") // TODO - MIPR-2637: Inform V&C team about no longer logging the response body
                Right(paymentAllocations)
              case None =>
                logger.error("[PaymentAllocationsHttpParser][read] could not parse response")
                Left(UnexpectedResponse)
            }
            case JsError(errors) =>
              logger.error(s"[PaymentAllocationsHttpParser][read] Json validation error. Reasons: ${errors}")
              Left(UnexpectedResponse)
          }
        case NOT_FOUND =>
          logger.info("[PaymentAllocationsHttpParser][read] no allocations found for payment")
          Left(NotFoundResponse)
        case CLIENT_CLOSED_REQUEST | BAD_GATEWAY | SERVICE_UNAVAILABLE =>
          logger.warn(s"[PaymentAllocationsHttpParser][read] Downstream Timeout Error Response status: ${response.status}, body: ${response.body}")
          Left(UnexpectedResponse)
        case status if status >= 400 && status < 500 =>
          logger.error(s"[PaymentAllocationsHttpParser][read] Unexpected Response with status: $status")
          Left(UnexpectedResponse)
        case status =>
          logger.error(s"[PaymentAllocationsHttpParser][read] $status returned from DES with body: ${response.body}")
          Left(UnexpectedResponse)
      }
    }
  }

}
