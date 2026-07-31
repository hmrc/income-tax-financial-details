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

package connectors.hip.httpParsers

import constants.RepaymentHistoryTestConstants
import uk.gov.hmrc.http.HttpResponse
import utils.TestSupport
import connectors.hip.httpParsers.RepaymentsHistoryDetailsHttpParser.RepaymentsHistoryDetailsReads
import models.hip.ErrorResponse
import models.hip.repayments.SuccessfulRepaymentResponse
import play.api.http.Status.NOT_FOUND
import play.api.http.Status.UNPROCESSABLE_ENTITY

class RepaymentsHistoryDetailsHttpParserSpec extends TestSupport {

  "The repayments history parser" should {
    "return a 404 error response" when {
      "provided with a response with a 422 status and a No Data error code from downstream" in {
        val testResponse: HttpResponse = HttpResponse(
          status = UNPROCESSABLE_ENTITY,
          body = RepaymentHistoryTestConstants.repaymentsHistoryFailureWithError().toString
        )
        val result: RepaymentsHistoryDetailsHttpParser.HttpGetResult[SuccessfulRepaymentResponse] = RepaymentsHistoryDetailsReads.read("", "", testResponse)

        result shouldBe Left(ErrorResponse.GenericError(NOT_FOUND, RepaymentHistoryTestConstants.repaymentsHistoryFailureWithError()))
      }
    }

    "return a 422 error response" when {
      "provided with a response with a 422 status without a No Data error code from downstream" in {
        val testResponse: HttpResponse = HttpResponse(
          status = UNPROCESSABLE_ENTITY,
          body = RepaymentHistoryTestConstants.repaymentsHistoryFailureWithError(errorCode = "005").toString
        )
        val result: RepaymentsHistoryDetailsHttpParser.HttpGetResult[SuccessfulRepaymentResponse] = RepaymentsHistoryDetailsReads.read("", "", testResponse)

        result shouldBe Left(ErrorResponse.UnprocessableData(RepaymentHistoryTestConstants.repaymentsHistoryFailureWithError(errorCode = "005").toString))
      }

      "provided with a response with a 422 status with a non-etmp response body" in {
        val testResponse: HttpResponse = HttpResponse(
          status = UNPROCESSABLE_ENTITY,
          body = "{}"
        )
        val result: RepaymentsHistoryDetailsHttpParser.HttpGetResult[SuccessfulRepaymentResponse] = RepaymentsHistoryDetailsReads.read("", "", testResponse)

        result shouldBe Left(ErrorResponse.UnprocessableData("{}"))
      }
    }
  }
}
