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

import constants.ViewAndChangeConnectorIntegrationTestConstants.*
import helpers.{ComponentSpecBase, WiremockHelper}
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.ClaimToAdjustPoaResponse
import models.hip.chargeHistory.{ChargeHistoryError, ChargeHistoryNotFound}
import org.scalactic.Prettifier.default
import play.api.http.Status.*

class ViewAndChangeConnectorISpec extends ComponentSpecBase {

  val viewAndChangeConnector: ViewAndChangeConnector = app.injector.instanceOf[ViewAndChangeConnector]
  val postClaimToAdjustPoaUrl = "/income-tax/calculations/POA/ClaimToAdjust"
  val getChargeHistoryUrl = "/etmp/RESTAdapter/ITSA/TaxPayer/GetChargeHistory?idType=NINO&idValue=123&chargeReference=456"

  ".postClaimToAdjustPoa() is called" when {

    "the response is a 200 - OK" should {

      "return a valid model when successfully retrieved" in {

        WiremockHelper.stubPost(
          url = postClaimToAdjustPoaUrl,
          status = CREATED,
          responseBody = postClaimToAdjustPoaResponseBody.toString()
        )

        val result =
          viewAndChangeConnector.postClaimToAdjustPoa(postClaimToAdjustPoaRequest).futureValue

        result shouldBe postClaimToAdjustPoaValidResponseBody
      }
    }

    "the response cannot be parsed" should {

      "return INTERNAL_SERVER_ERROR with ErrorResponse" in {

        WiremockHelper.stubPost(
          url = postClaimToAdjustPoaUrl,
          status = CREATED,
          responseBody = postClaimToAdjustPoaInvalidResponseBody.toString()
        )

        val result =
          viewAndChangeConnector.postClaimToAdjustPoa(postClaimToAdjustPoaRequest).futureValue

        result.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  ".getChargeHistory() is called" when {

    "the response is a 200 - OK" should {

      "return a valid model when successfully retrieved" in {

        WiremockHelper.stubGet(
          url = getChargeHistoryUrl,
          status = OK,
          body = getChargeHistoryResponseBody.toString()
        )

        val result =
          viewAndChangeConnector.getChargeHistory("123", "456").futureValue

        result shouldBe Right(getChargeHistoryExpectedModel)
      }
    }
  }

  ".getChargeHistory() is called" when {

    "the response is a 404 - NOT_FOUND" should {

      "return ChargeHistoryNotFound" in {

        WiremockHelper.stubGet(
          url = getChargeHistoryUrl,
          status = NOT_FOUND,
          body = chargeHistoryNotFoundResponseBody.toString()
        )

        val result =
          viewAndChangeConnector.getChargeHistory("123", "456").futureValue

        result shouldBe Left(
          ChargeHistoryNotFound(
            status = NOT_FOUND,
            reason = chargeHistoryNotFoundResponseBody.toString()
          )
        )
      }
    }
  }

  ".getChargeHistory() is called" when {

    "the response is a 422 - UNPROCESSABLE_ENTITY" should {

      "return ChargeHistoryError if the JSON cannot be parsed" in {

        WiremockHelper.stubGet(
          url = getChargeHistoryUrl,
          status = UNPROCESSABLE_ENTITY,
          body = chargeHistoryErrorResponseBody.toString()
        )

        val result =
          viewAndChangeConnector.getChargeHistory("123", "456").futureValue

        result shouldBe Left(
          ChargeHistoryError(UNPROCESSABLE_ENTITY, chargeHistoryErrorResponseBody.toString())
        )
      }

      "return ChargeHistoryNotFound if the error code matches 005 or 014" in {

        WiremockHelper.stubGet(
          url = getChargeHistoryUrl,
          status = UNPROCESSABLE_ENTITY,
          body = chargeHistoryErrorResponseBody.toString()
        )

        val result =
          viewAndChangeConnector.getChargeHistory("123", "456").futureValue

        result shouldBe Left(
          ChargeHistoryError(UNPROCESSABLE_ENTITY, chargeHistoryErrorResponseBody.toString())
        )
      }

      "return ChargeHistoryError if the JSON parses but code is not 005/014" in {

        WiremockHelper.stubGet(
          url = getChargeHistoryUrl,
          status = UNPROCESSABLE_ENTITY,
          body = chargeHistoryValidationError.toString()
        )

        val result =
          viewAndChangeConnector.getChargeHistory("123", "456").futureValue

        result shouldBe Left(
          ChargeHistoryError(UNPROCESSABLE_ENTITY, chargeHistoryValidationError.toString())
        )
      }
    }
  }

}
