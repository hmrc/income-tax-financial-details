/*
 * Copyright 2023 HM Revenue & Customs
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

import models.repaymentHistory.RepaymentHistorySuccessResponse
import play.api.http.Status.{BAD_GATEWAY, OK, SERVICE_UNAVAILABLE}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object RepaymentHistoryHttpParser extends ResponseHttpParsers {

  sealed trait RepaymentHistoryError

  case class UnexpectedRepaymentHistoryResponse(code: Int, response: String) extends RepaymentHistoryError
  case object RepaymentHistoryErrorResponse extends RepaymentHistoryError

  type RepaymentHistoryResponse = Either[RepaymentHistoryError, RepaymentHistorySuccessResponse]

  implicit object RepaymentHistoryReads extends HttpReads[RepaymentHistoryResponse] {
    val CLIENT_CLOSED_REQUEST = 499

    override def read(method: String, url: String, response: HttpResponse): RepaymentHistoryResponse = {
      response.status match {
        case OK =>
          logger.info("[RepaymentHistoryHttpParser][read] successfully parsed response to List[RepaymentHistory]")
          Right(response.json.as[RepaymentHistorySuccessResponse])
        case status if status == CLIENT_CLOSED_REQUEST || status == BAD_GATEWAY || status == SERVICE_UNAVAILABLE =>
          logger.warn(s"[RepaymentHistoryHttpParser][read] Unexpected Response with status: $status")
          Left(RepaymentHistoryErrorResponse)
        case status if status >= 400 && status < 500 =>
          logger.error(s"[RepaymentHistoryHttpParser][read] $status returned from DES with body: ${response.body}")
          Left(UnexpectedRepaymentHistoryResponse(status, response.body))
        case status =>
          logger.error(s"[RepaymentHistoryHttpParser][read] Unexpected Response with status: $status")
          Left(RepaymentHistoryErrorResponse)
      }
    }
  }
}
