/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import constants.ViewAndChangeConnectorIntegrationTestConstants.{invalidResponseBody, request, responseBody, validResponseBody}
import helpers.{ComponentSpecBase, WiremockHelper}
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.ClaimToAdjustPoaResponse
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR}

class ViewAndChangeConnectorISpec extends ComponentSpecBase {

  val connector: ViewAndChangeConnector = app.injector.instanceOf[ViewAndChangeConnector]
  val postClaimToAdjustPoaUrl = "/income-tax/calculations/POA/ClaimToAdjust"

  ".postClaimToAdjustPoa() is called" when {

    "the response is a 200 - OK" should {

      "return a valid model when successfully retrieved" in {

        WiremockHelper.stubPost(
          url = postClaimToAdjustPoaUrl,
          status = CREATED,
          responseBody = responseBody.toString()
        )

        val result =
          connector.postClaimToAdjustPoa(request).futureValue

        result shouldBe validResponseBody
      }

      "the response cannot be parsed" should {

        "return INTERNAL_SERVER_ERROR with ErrorResponse" in {

          WiremockHelper.stubPost(
            url = postClaimToAdjustPoaUrl,
            status = CREATED,
            responseBody = invalidResponseBody.toString()
          )

          val result =
            connector.postClaimToAdjustPoa(request).futureValue

          result.status shouldBe INTERNAL_SERVER_ERROR
        }
      }

    }
  }
}
