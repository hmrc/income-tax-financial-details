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

import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.httpParsers.ChargeHttpParser.{UnexpectedChargeErrorResponse, UnexpectedChargeResponse}
import connectors.httpParsers.PaymentAllocationsHttpParser.{NotFoundResponse, UnexpectedResponse}
import models.paymentAllocations.{AllocationDetail, PaymentAllocations}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

import java.time.LocalDate

class ViewAndChangeConnectorSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with GuiceOneAppPerSuite
    with WireMockSupport {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.income-tax-view-change.host" -> "localhost",
        "microservice.services.income-tax-view-change.port" -> wireMockPort
      )
      .build()

  private lazy val connector: ViewAndChangeConnector = app.injector.instanceOf[ViewAndChangeConnector]

  private val nino = "AA123456A"
  private val fromDate = "2020-01-01"
  private val toDate = "2020-02-01"
  private val documentId = "1234567890"

  private def okJsonResponse(body: JsObject) =
    aResponse()
      .withStatus(OK)
      .withHeader("Content-Type", "application/json")
      .withBody(Json.stringify(body))

  private def jsonErrorResponse(status: Int, body: String) =
    aResponse()
      .withStatus(status)
      .withHeader("Content-Type", "application/json")
      .withBody(body)

  "getOnlyOpenItems" should {

    "return Right(json) when ViewAndChange returns 200" in {
      val body = Json.obj("hello" -> "world")

      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/only-open-items"))
          .willReturn(okJsonResponse(body))
      )

      val result = connector.getOnlyOpenItems(nino).futureValue
      result mustBe Right(body)
    }

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns BAD_REQUEST" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/only-open-items"))
          .willReturn(jsonErrorResponse(BAD_REQUEST, "bad request"))
      )

      val result = connector.getOnlyOpenItems(nino).futureValue
      result mustBe Left(UnexpectedChargeResponse(BAD_REQUEST, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns INTERNAL_SERVER_ERROR" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/only-open-items"))
          .willReturn(jsonErrorResponse(INTERNAL_SERVER_ERROR, "server error"))
      )

      val result = connector.getOnlyOpenItems(nino).futureValue
      result mustBe Left(UnexpectedChargeErrorResponse)
    }
  }

  "getChargeDetails" should {

    "return Right(json) when ViewAndChange returns 200" in {
      val body = Json.obj("charges" -> Json.arr(Json.obj("id" -> 1)))

      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/from/$fromDate/to/$toDate"))
          .willReturn(okJsonResponse(body))
      )

      val result = connector.getChargeDetails(nino, fromDate, toDate).futureValue
      result mustBe Right(body)
    }

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns BAD_REQUEST" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(BAD_REQUEST, "bad request"))
      )

      val result = connector.getChargeDetails(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeResponse(BAD_REQUEST, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns INTERNAL_SERVER_ERROR" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(INTERNAL_SERVER_ERROR, "server error"))
      )

      val result = connector.getChargeDetails(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeErrorResponse)
    }
  }

  "getPayments" should {

    "return Right(json) when ViewAndChange returns 200" in {
      val body = Json.obj("payments" -> Json.arr(Json.obj("amount" -> 123.45)))

      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/payments/from/$fromDate/to/$toDate"))
          .willReturn(okJsonResponse(body))
      )

      val result = connector.getPayments(nino, fromDate, toDate).futureValue
      result mustBe Right(body)
    }

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns BAD_REQUEST" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/payments/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(BAD_REQUEST, "bad request"))
      )

      val result = connector.getPayments(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeResponse(BAD_REQUEST, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns INTERNAL_SERVER_ERROR" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/payments/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(INTERNAL_SERVER_ERROR, "server error"))
      )

      val result = connector.getPayments(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeErrorResponse)
    }
  }

  "getChargeDetailsByDocumentId" should {

    "return Right(json) when ViewAndChange returns 200" in {
      val body = Json.obj("documentId" -> documentId, "allocation" -> Json.arr())

      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/documentId/$documentId"))
          .willReturn(okJsonResponse(body))
      )

      val result = connector.getChargeDetailsByDocumentId(nino, documentId).futureValue
      result mustBe Right(body)
    }

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns BAD_REQUEST" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/documentId/$documentId"))
          .willReturn(jsonErrorResponse(BAD_REQUEST, "bad request"))
      )

      val result = connector.getChargeDetailsByDocumentId(nino, documentId).futureValue
      result mustBe Left(UnexpectedChargeResponse(BAD_REQUEST, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns INTERNAL_SERVER_ERROR" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/documentId/$documentId"))
          .willReturn(jsonErrorResponse(INTERNAL_SERVER_ERROR, "server error"))
      )

      val result = connector.getChargeDetailsByDocumentId(nino, documentId).futureValue
      result mustBe Left(UnexpectedChargeErrorResponse)
    }
  }

  "getCredits" should {

    "return Right(json) when ViewAndChange returns 200" in {
      val body = Json.obj("credits" -> Json.arr(Json.obj("amount" -> 50)))

      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/credits/from/$fromDate/to/$toDate"))
          .willReturn(okJsonResponse(body))
      )

      val result = connector.getCredits(nino, fromDate, toDate).futureValue
      result mustBe Right(body)
    }

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns BAD_REQUEST" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/credits/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(BAD_REQUEST, "bad request"))
      )

      val result = connector.getCredits(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeResponse(BAD_REQUEST, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns INTERNAL_SERVER_ERROR" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/credits/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(INTERNAL_SERVER_ERROR, "server error"))
      )

      val result = connector.getCredits(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeErrorResponse)
    }
  }

  "getPaymentAllocations" should {

    val paymentLot = "12345"
    val paymentLotItem = "67890"
    val url = s"/$nino/payment-allocations/$paymentLot/$paymentLotItem"

    "return Right(PaymentAllocations) when the backend returns 200" in {
      val paymentAllocations = PaymentAllocations(
        amount = Some(100.0),
        method = Some("method"),
        reference = Some("reference"),
        transactionDate = Some(LocalDate.parse("2023-12-25")),
        allocations = Seq(
          AllocationDetail(
            transactionId = Some("transactionId"),
            from = Some(LocalDate.parse("2023-01-01")),
            to = Some(LocalDate.parse("2023-12-31")),
            chargeType = Some("type"),
            mainType = Some("mainType"),
            amount = Some(BigDecimal(100.0)),
            clearedAmount = Some(BigDecimal(100.0)),
            chargeReference = Some("chargeReference")
          )
        )
      )

      val body = Json.obj(
        "paymentDetails" -> Json.arr(
          Json.obj(
            "paymentAmount"    -> 100.0,
            "paymentMethod"    -> "method",
            "paymentReference" -> "reference",
            "valueDate"        -> "2023-12-25",
            "sapClearingDocsDetails" -> Json.arr(
              Json.obj(
                "sapDocNumber"       -> "transactionId",
                "taxPeriodStartDate" -> "2023-01-01",
                "taxPeriodEndDate"   -> "2023-12-31",
                "chargeType"         -> "type",
                "mainType"           -> "mainType",
                "amount"             -> 100.0,
                "clearedAmount"      -> 100.0,
                "chargeReference"    -> "chargeReference"
              )
            )
          )
        )
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(okJsonResponse(body))
      )

      val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue
      result mustBe Right(paymentAllocations)
    }

    "return Left(NotFoundResponse) when the backend returns 404" in {
      stubFor(
        get(urlEqualTo(url))
          .willReturn(jsonErrorResponse(NOT_FOUND, "not found"))
      )

      val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue
      result mustBe Left(NotFoundResponse)
    }

    "return Left(UnexpectedResponse) when the backend returns an error status" in {
      stubFor(
        get(urlEqualTo(url))
          .willReturn(jsonErrorResponse(INTERNAL_SERVER_ERROR, "server error"))
      )

      val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue
      result mustBe Left(UnexpectedResponse)
    }
  }
  
}
