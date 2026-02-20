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

import constants.FinancialDetailIntegrationTestConstants.chargeJson
import connectors.httpParsers.ChargeHttpParser.{UnexpectedChargeErrorResponse, UnexpectedChargeResponse}
import helpers.{ComponentSpecBase, WiremockHelper}
import models.credits.CreditsModel
import models.financialDetails.Payment
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsValue, Json}
import utils.AChargesResponse

import java.time.LocalDate

class ViewAndChangeConnectorISpec extends ComponentSpecBase {

  lazy val connector: ViewAndChangeConnector = app.injector.instanceOf[ViewAndChangeConnector]

  private val nino = "BB123456A"
  private val from = "from"
  private val to = "to"
  private val documentId = "123456789"

  private def base(nino: String): String =
    s"/income-tax-view-change/$nino/financial-details"

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWiremock()
  }

  "ViewAndChangeConnector" when {

    ".getChargeDetails() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val url = s"${base(nino)}/charges/from/$from/to/$to"
        val responseBody = chargeJson.toString()

        WiremockHelper.stubGet(url, OK, responseBody)

        val result = connector.getChargeDetails(nino, from, to).futureValue
        result shouldBe Right(chargeJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
        val url = s"${base(nino)}/charges/from/$from/to/$to"
        val body = "bad request"

        WiremockHelper.stubGet(url, BAD_REQUEST, body)

        val result = connector.getChargeDetails(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeResponse(BAD_REQUEST, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        val url = s"${base(nino)}/charges/from/$from/to/$to"

        WiremockHelper.stubGet(url, INTERNAL_SERVER_ERROR, "error")

        val result = connector.getChargeDetails(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getChargeDetailsByDocumentId() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val url = s"${base(nino)}/charges/documentId/$documentId"
        val responseBody = chargeJson.toString()

        WiremockHelper.stubGet(url, OK, responseBody)

        val result = connector.getChargeDetailsByDocumentId(nino, documentId).futureValue
        result shouldBe Right(chargeJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 404" in {
        val url = s"${base(nino)}/charges/documentId/$documentId"
        val body = Json.obj("code" -> "NO_DATA_FOUND", "reason" -> "No data found").toString()

        WiremockHelper.stubGet(url, NOT_FOUND, body)

        val result = connector.getChargeDetailsByDocumentId(nino, documentId).futureValue
        result shouldBe Left(UnexpectedChargeResponse(NOT_FOUND, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        val url = s"${base(nino)}/charges/documentId/$documentId"

        WiremockHelper.stubGet(url, INTERNAL_SERVER_ERROR, "error")

        val result = connector.getChargeDetailsByDocumentId(nino, documentId).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getPayments() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val url = s"${base(nino)}/payments/from/$from/to/$to"

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
        WiremockHelper.stubGet(url, OK, expectedJson.toString())

        val result = connector.getPayments(nino, from, to).futureValue
        result shouldBe Right(expectedJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
        val url = s"${base(nino)}/payments/from/$from/to/$to"
        val body = "bad request"

        WiremockHelper.stubGet(url, BAD_REQUEST, body)

        val result = connector.getPayments(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeResponse(BAD_REQUEST, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        val url = s"${base(nino)}/payments/from/$from/to/$to"

        WiremockHelper.stubGet(url, INTERNAL_SERVER_ERROR, "error")

        val result = connector.getPayments(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getCredits() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val url = s"${base(nino)}/credits/from/$from/to/$to"

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
        WiremockHelper.stubGet(url, OK, expectedJson.toString())

        val result = connector.getCredits(nino, from, to).futureValue
        result shouldBe Right(expectedJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
        val url = s"${base(nino)}/credits/from/$from/to/$to"
        val body = "bad request"

        WiremockHelper.stubGet(url, BAD_REQUEST, body)

        val result = connector.getCredits(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeResponse(BAD_REQUEST, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        val url = s"${base(nino)}/credits/from/$from/to/$to"

        WiremockHelper.stubGet(url, INTERNAL_SERVER_ERROR, "error")

        val result = connector.getCredits(nino, from, to).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }

    ".getOnlyOpenItems() is called" should {

      "return Right(json) when ViewAndChange returns 200" in {
        val url = s"${base(nino)}/only-open-items"
        val responseBody = chargeJson.toString()

        WiremockHelper.stubGet(url, OK, responseBody)

        val result = connector.getOnlyOpenItems(nino).futureValue
        result shouldBe Right(chargeJson)
      }

      "return Left(UnexpectedChargeResponse) when ViewAndChange returns 404" in {
        val url = s"${base(nino)}/only-open-items"
        val body = Json.obj("code" -> "NO_DATA_FOUND", "reason" -> "No data found").toString()

        WiremockHelper.stubGet(url, NOT_FOUND, body)

        val result = connector.getOnlyOpenItems(nino).futureValue
        result shouldBe Left(UnexpectedChargeResponse(NOT_FOUND, body))
      }

      "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
        val url = s"${base(nino)}/only-open-items"

        WiremockHelper.stubGet(url, INTERNAL_SERVER_ERROR, "error")

        val result = connector.getOnlyOpenItems(nino).futureValue
        result shouldBe Left(UnexpectedChargeErrorResponse)
      }
    }
  }
}
