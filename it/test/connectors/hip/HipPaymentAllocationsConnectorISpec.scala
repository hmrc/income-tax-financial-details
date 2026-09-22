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

package connectors.hip

import helpers.WiremockHelper.*
import helpers.{ComponentSpecBase, WiremockHelper}
import models.hip.{HipResponseError, HipResponseErrorsObject}
import models.hip.paymentAllocations.{AllocationDetail, PaymentAllocations, PaymentAllocationsError, PaymentAllocationsNotFound, PaymentAllocationsResponseModel}
import play.api.http.Status.*
import play.api.libs.json.{JsObject, Json}

import java.time.LocalDate

class HipPaymentAllocationsConnectorISpec extends ComponentSpecBase {

  val connector: HipPaymentAllocationsConnector = app.injector.instanceOf[HipPaymentAllocationsConnector]

  val nino: String = "AA123456A"
  val paymentLot: String = "1234567890"
  val paymentLotItem: String = "0001"

  val url: String = s"/etmp/RESTAdapter/payment-allocation/NINO/$nino/ITSA?paymentLot=$paymentLot&paymentLotItem=$paymentLotItem"

  val paymentAllocationsSingle: PaymentAllocations = PaymentAllocations(
    amount = Some(12345.45),
    method = Some("B"),
    reference = Some("2222"),
    transactionDate = Some(LocalDate.of(2024, 5, 12)),
    allocations = Seq(AllocationDetail(
      transactionId = Some("1234567890"),
      from = Some(LocalDate.of(2024, 5, 10)),
      to = Some(LocalDate.of(2025, 5, 30)),
      chargeType = Some("1481"),
      mainType = Some("4915"),
      amount = Some(3892.90),
      clearedAmount = Some(1511.13),
      chargeReference = Some("1234567890")
    ))
  )

  val paymentAllocationsResponseModel: PaymentAllocationsResponseModel = PaymentAllocationsResponseModel(
    paymentDetails = Seq(paymentAllocationsSingle)
  )

  val paymentAllocationsResponseModelApi: JsObject = Json.obj(
    "success" -> Json.obj(
      "paymentDetails" -> Json.arr(
        Json.obj(
          "paymentAmount" -> 12345.45,
          "paymentMethod" -> "B",
          "paymentReference" -> "2222",
          "valueDate" -> "2024-05-12",
          "sapClearingDocsDetails" -> Json.arr(
            Json.obj(
              "sapDocNumber" -> "1234567890",
              "taxPeriodStartDate" -> "2024-05-10",
              "taxPeriodEndDate" -> "2025-05-30",
              "chargeType" -> "1481",
              "mainType" -> "4915",
              "amount" -> 3892.90,
              "clearedAmount" -> 1511.13,
              "chargeReference" -> "1234567890"
            )
          )
        )
      )
    )
  )

  "HipPaymentAllocationsConnector" when {
    "getPaymentAllocations() is called" when {

      "the response is a 200 - Ok" should {
        "return a PaymentAllocationsResponseModel when successfully parsed" in {
          WiremockHelper.stubGet(url, OK, paymentAllocationsResponseModelApi.toString)
          val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue

          result shouldBe Right(paymentAllocationsSingle)
        }

        "return a PaymentAllocationsError when the response cannot be parsed" in {
          val invalidJson = Json.obj("invalid" -> "json")
          WiremockHelper.stubGet(url, OK, invalidJson.toString)
          val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue

          result shouldBe Left(PaymentAllocationsError)
        }
      }

      "the response is a 404 - NotFound" should {
        "return a PaymentAllocationsNotFound" in {
          WiremockHelper.stubGet(url, NOT_FOUND, "{}")
          val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue

          result shouldBe Left(PaymentAllocationsNotFound)
        }
      }

      "the response is a 422 - UnprocessableEntity" should {
        "return a PaymentAllocationsNotFound when the error code indicates data not found" in {
          val jsonError = Json.toJson(HipResponseErrorsObject(HipResponseError("003", "Request could not be processed")))
          WiremockHelper.stubGet(url, UNPROCESSABLE_ENTITY, jsonError.toString)
          val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue

          result shouldBe Left(PaymentAllocationsNotFound)
        }
      }

      "the response is a 500 - InternalServerError" should {
        "return a PaymentAllocationsError" in {
          WiremockHelper.stubGet(url, INTERNAL_SERVER_ERROR, "{}")
          val result = connector.getPaymentAllocations(nino, paymentLot, paymentLotItem).futureValue

          result shouldBe Left(PaymentAllocationsError)
        }
      }
    }
  }
}
