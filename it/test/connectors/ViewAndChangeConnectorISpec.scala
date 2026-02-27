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

package connectors

import constants.BaseIntegrationTestConstants.testNino
import constants.ViewAndChangeConnectorIntegrationTestConstants.*
import helpers.{ComponentSpecBase, WiremockHelper}
import models.claimToAdjustPoa.ClaimToAdjustPoaResponse.ClaimToAdjustPoaResponse
import models.paymentAllocations.PaymentAllocations
import play.api.http.Status.{BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.ws.WSResponse
import connectors.httpParsers.ChargeHttpParser.{UnexpectedChargeErrorResponse, UnexpectedChargeResponse}
import constants.FinancialDetailIntegrationTestConstants.chargeJson
import helpers.servicemocks.ViewAndChangeStub
import models.credits.CreditsModel
import models.financialDetails.Payment
import models.hip.chargeHistory.{ChargeHistoryError, ChargeHistoryNotFound}
import org.scalactic.Prettifier.default
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import utils.AChargesResponse

import java.time.LocalDate

class ViewAndChangeConnectorISpec extends ComponentSpecBase {

  val viewAndChangeConnector: ViewAndChangeConnector = app.injector.instanceOf[ViewAndChangeConnector]
  val postClaimToAdjustPoaUrl = "/income-tax/calculations/POA/ClaimToAdjust"
  val getChargeHistoryUrl = "/etmp/RESTAdapter/ITSA/TaxPayer/GetChargeHistory?idType=NINO&idValue=123&chargeReference=456"
  val getPaymentAllocationsUrl = s"/$testNino/payment-allocations/$paymentLot/$paymentLotItem"


  private val nino = "BB123456A"
  private val from = "from"
  private val to = "to"
  private val documentId = "123456789"

  "ViewAndChangeConnector" when {

    ".getChargeDetails() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val responseBody = chargeJson.toString()

        ViewAndChangeStub.stubGetCharges(nino, from, to)(OK, responseBody)

        val result = viewAndChangeConnector.getChargeDetails(nino, from, to).futureValue
        result shouldBe Right(chargeJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
        val body = "bad request"

        ViewAndChangeStub.stubGetCharges(nino, from, to)(BAD_REQUEST, body)

        val result = viewAndChangeConnector.getChargeDetails(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeResponse(BAD_REQUEST, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        ViewAndChangeStub.stubGetCharges(nino, from, to)(INTERNAL_SERVER_ERROR, "error")

        val result = viewAndChangeConnector.getChargeDetails(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getChargeDetailsByDocumentId() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val responseBody = chargeJson.toString()

        ViewAndChangeStub.stubGetChargeByDocumentId(nino, documentId)(OK, responseBody)

        val result = viewAndChangeConnector.getChargeDetailsByDocumentId(nino, documentId).futureValue
        result shouldBe Right(chargeJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 404" in {
        val body = Json.obj("code" -> "NO_DATA_FOUND", "reason" -> "No data found").toString()

        ViewAndChangeStub.stubGetChargeByDocumentId(nino, documentId)(NOT_FOUND, body)

        val result = viewAndChangeConnector.getChargeDetailsByDocumentId(nino, documentId).futureValue
        result shouldBe Left(UnexpectedChargeResponse(NOT_FOUND, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        ViewAndChangeStub.stubGetChargeByDocumentId(nino, documentId)(INTERNAL_SERVER_ERROR, "error")

        val result = viewAndChangeConnector.getChargeDetailsByDocumentId(nino, documentId).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getPayments() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val payments: List[Payment] = List(
          Payment(
            reference = Some("paymentRef-1"),
            amount = BigDecimal("123.45"),
            outstandingAmount = BigDecimal("0.00"),
            documentDescription = Some("Payment"),
            method = Some("CARD"),
            lot = Some("paymentLot"),
            lotItem = Some("paymentLotItem"),
            dueDate = Some(LocalDate.parse("2024-06-20")),
            documentDate = LocalDate.parse("2024-06-17"),
            transactionId = "PAYMENT01",
            mainType = Some("Payment"),
            mainTransaction = Some("0060")
          ),
          Payment(
            reference = None,
            amount = BigDecimal("50.00"),
            outstandingAmount = BigDecimal("10.00"),
            documentDescription = Some("Payment"),
            method = Some("BACS"),
            lot = Some("paymentLot2"),
            lotItem = Some("paymentLotItem2"),
            dueDate = None,
            documentDate = LocalDate.parse("2024-06-18"),
            transactionId = "PAYMENT02",
            mainType = Some("Payment"),
            mainTransaction = Some("0060")
          )
        )

        val expectedJson: JsValue = Json.toJson(payments)
        ViewAndChangeStub.stubGetPayments(nino, from, to)(OK, expectedJson.toString())

        val result = viewAndChangeConnector.getPayments(nino, from, to).futureValue
        result shouldBe Right(expectedJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
        val body = "bad request"

        ViewAndChangeStub.stubGetPayments(nino, from, to)(BAD_REQUEST, body)

        val result = viewAndChangeConnector.getPayments(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeResponse(BAD_REQUEST, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        ViewAndChangeStub.stubGetPayments(nino, from, to)(INTERNAL_SERVER_ERROR, "error")

        val result = viewAndChangeConnector.getPayments(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getCredits() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val creditsModel: CreditsModel = CreditsModel.fromHipChargesResponse(
          AChargesResponse()
            .withAvailableCredit(200.0)
            .withAllocatedFutureCredit(150.0)
            .withTotalCredit(175.0)
            .withUnallocatedCredit(125.0)
            .withFirstRefundRequest(200.0)
            .withSecondRefundRequest(100.0)
            .withCutoverCredit("CUTOVER01", LocalDate.of(2024, 6, 20), -100.0)
            .withBalancingChargeCredit("BALANCING01", LocalDate.of(2024, 6, 19), -200)
            .withMfaCredit("MFA01", LocalDate.of(2024, 6, 18), -300)
            .withPayment("PAYMENT01", LocalDate.of(2024, 6, 17), LocalDate.of(2024, 6, 16), -400)
            .withRepaymentInterest("INTEREST01", LocalDate.of(2024, 6, 16), -500)
            .get()
        )

        val expectedJson: JsValue = Json.toJson(creditsModel)
        ViewAndChangeStub.stubGetCredits(nino, from, to)(OK, expectedJson.toString())

        val result = viewAndChangeConnector.getCredits(nino, from, to).futureValue
        result shouldBe Right(expectedJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
        val body = "bad request"

        ViewAndChangeStub.stubGetCredits(nino, from, to)(BAD_REQUEST, body)

        val result = viewAndChangeConnector.getCredits(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeResponse(BAD_REQUEST, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        ViewAndChangeStub.stubGetCredits(nino, from, to)(INTERNAL_SERVER_ERROR, "error")

        val result = viewAndChangeConnector.getCredits(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getOnlyOpenItems() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val responseBody = chargeJson.toString()

        ViewAndChangeStub.stubGetOnlyOpenItems(nino)(OK, responseBody)

        val result = viewAndChangeConnector.getOnlyOpenItems(nino).futureValue
        result shouldBe Right(chargeJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 404" in {
        val body = Json.obj("code" -> "NO_DATA_FOUND", "reason" -> "No data found").toString()

        ViewAndChangeStub.stubGetOnlyOpenItems(nino)(NOT_FOUND, body)

        val result = viewAndChangeConnector.getOnlyOpenItems(nino).futureValue
        result shouldBe Left(UnexpectedChargeResponse(NOT_FOUND, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        ViewAndChangeStub.stubGetOnlyOpenItems(nino)(INTERNAL_SERVER_ERROR, "error")

        val result = viewAndChangeConnector.getOnlyOpenItems(nino).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

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
  ".getPaymentAllocations() is called" when {

    s"return $OK" when {
      "payment allocations are successfully retrieved" in {
        Given("the user is authorised")
        isAuthorised(true)

        And("the call to retrieve payment allocations is stubbed")
        WiremockHelper.stubGet(
          url = getPaymentAllocationsUrl,
          status = OK,
          body = paymentAllocationsResponseBody.toString
        )

        When(s"I call GET ${controllers.routes.PaymentAllocationsController.getPaymentAllocations(testNino, paymentLot, paymentLotItem)}")
        val res: WSResponse = IncomeTaxFinancialDetails.getPaymentAllocations(testNino, paymentLot, paymentLotItem)

        Then("a successful response is returned with the payment allocations")
        res should have(
          httpStatus(OK),
          jsonBodyMatching(Json.toJson(paymentAllocations))
        )
      }
    }
    s"return $INTERNAL_SERVER_ERROR" when {
      "an unexpected status was returned when retrieving payment allocations" in {
        Given("the user is authorised")
        isAuthorised(true)

        And("the call to retrieve payment allocations is stubbed")
        WiremockHelper.stubGet(
          url = getPaymentAllocationsUrl,
          status = BAD_REQUEST,
          body= ""
        )

        When(s"I call GET ${controllers.routes.PaymentAllocationsController.getPaymentAllocations(testNino, paymentLot, paymentLotItem)}")
        val res: WSResponse = IncomeTaxFinancialDetails.getPaymentAllocations(testNino, paymentLot, paymentLotItem)

        Then("an internal server error response is returned")
        res should have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}