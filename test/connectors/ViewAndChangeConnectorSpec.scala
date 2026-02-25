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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

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
      .withStatus(200)
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

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/only-open-items"))
          .willReturn(jsonErrorResponse(400, "bad request"))
      )

      val result = connector.getOnlyOpenItems(nino).futureValue
      result mustBe Left(UnexpectedChargeResponse(400, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/only-open-items"))
          .willReturn(jsonErrorResponse(500, "server error"))
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

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(400, "bad request"))
      )

      val result = connector.getChargeDetails(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeResponse(400, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(500, "server error"))
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

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/payments/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(400, "bad request"))
      )

      val result = connector.getPayments(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeResponse(400, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/payments/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(500, "server error"))
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

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/documentId/$documentId"))
          .willReturn(jsonErrorResponse(400, "bad request"))
      )

      val result = connector.getChargeDetailsByDocumentId(nino, documentId).futureValue
      result mustBe Left(UnexpectedChargeResponse(400, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/charges/documentId/$documentId"))
          .willReturn(jsonErrorResponse(500, "server error"))
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

    "return Left(UnexpectedChargeResponse) when ViewAndChange returns 400" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/credits/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(400, "bad request"))
      )

      val result = connector.getCredits(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeResponse(400, "bad request"))
    }

    "return Left(UnexpectedChargeErrorResponse) when ViewAndChange returns 500" in {
      stubFor(
        get(urlEqualTo(s"/income-tax-view-change/$nino/financial-details/credits/from/$fromDate/to/$toDate"))
          .willReturn(jsonErrorResponse(500, "server error"))
      )

      val result = connector.getCredits(nino, fromDate, toDate).futureValue
      result mustBe Left(UnexpectedChargeErrorResponse)
    }
  }
}
